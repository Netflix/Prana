package com.netflix.prana.http.api;

import com.google.common.base.Strings;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.config.IClientConfigKey;
import com.netflix.config.DynamicProperty;
import com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList;
import com.netflix.ribbon.transport.netty.RibbonTransport;
import com.netflix.ribbon.transport.netty.http.LoadBalancingHttpClient;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.client.HttpClientPipelineConfigurator;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyHandler implements RequestHandler<ByteBuf, ByteBuf> {

    private static final ConcurrentHashMap<String, LoadBalancingHttpClient<ByteBuf, ByteBuf>> httpClients = new ConcurrentHashMap<>();

    private final String PROXY_REQ_ACCEPT_ENCODING = DynamicProperty.getInstance("zuul.proxy.req.acceptencoding").getString("deflate, gzip");

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final String ERROR_RESPONSE = "<status><status_code>500</status_code><message>Error forwarding request to origin</message></status>";


    @Override
    public Observable<Void> handle(final HttpServerRequest<ByteBuf> serverRequest, final HttpServerResponse<ByteBuf> serverResponse) {
        String vip = Utils.forQueryParam(serverRequest.getQueryParameters(), "vip");
        String path = Utils.forQueryParam(serverRequest.getQueryParameters(), "path");
        if(vip.equalsIgnoreCase("")) {
            serverResponse.getHeaders().set("Content-Type", "application/xml");
            serverResponse.writeString(ERROR_RESPONSE);
            logger.error("VIP is empty");
            return serverResponse.close();

        }

        final LoadBalancingHttpClient<ByteBuf, ByteBuf> client = getClient(vip);
        final HttpClientRequest<ByteBuf> req = HttpClientRequest.create(serverRequest.getHttpMethod(), path);
        populateRequestHeaders(serverRequest, req);

        final UnicastDisposableCachingSubject<ByteBuf> cachedContent = UnicastDisposableCachingSubject.create();
        /**
         * Why do we retain here?
         * After the onNext on the content returns, RxNetty releases the sent ByteBuf. This ByteBuf is kept out of
         * the scope of the onNext for consumption of the client in the route. The client when eventually writes
         * this ByteBuf over the wire expects the ByteBuf to be usable (i.e. ref count => 1). If this retain() call
         * is removed, the ref count will be 0 after the onNext on the content returns and hence it will be unusable
         * by the client in the route.
         */
        serverRequest.getContent().map(new Func1<ByteBuf, ByteBuf>() {
            @Override
            public ByteBuf call(ByteBuf byteBuf) {
                return byteBuf.retain();
            }
        }).subscribe(cachedContent); // Caches data if arrived before client writes it out, else passes through

        req.withContentSource(cachedContent);

        return client.submit(req).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<Void>>() {
            @Override
            public Observable<Void> call(final HttpClientResponse<ByteBuf> response) {
                serverResponse.setStatus(response.getStatus());
                List<Map.Entry<String, String>> headers = response.getHeaders().entries();
                for (Map.Entry<String, String> header : headers) {
                    serverResponse.getHeaders().add(header.getKey(), header.getValue());
                }
                return response.getContent().map(new Func1<ByteBuf, ByteBuf>() {
                    @Override
                    public ByteBuf call(ByteBuf byteBuf) {
                        return byteBuf.retain();
                    }
                }).map(new Func1<ByteBuf, Void>() {
                    @Override
                    public Void call(ByteBuf byteBuf) {
                        serverResponse.write(byteBuf);
                        return null;
                    }
                });
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<Void>>() {
            @Override
            public Observable<Void> call(Throwable throwable) {
                serverResponse.getHeaders().set("Content-Type", "application/xml");
                serverResponse.writeString(ERROR_RESPONSE);
                return Observable.just(null);
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                serverResponse.close();
                cachedContent.dispose(new Action1<ByteBuf>() {
                    @Override
                    public void call(ByteBuf byteBuf) {
                        /**
                         * Why do we release here?
                         *
                         * All ByteBuf which were never consumed are disposed and sent here. This means that the
                         * client in the route never consumed this ByteBuf. Before sending this ByteBuf to the
                         * content subject, we do a retain (see above for reason) expecting the client in the route
                         * to release it when written over the wire. In this case, though, the client never consumed
                         * it and hence never released corresponding to the retain done by us.
                         */
                        if (byteBuf.refCnt() > 1) { // 1 refCount will be from the subject putting into the cache.
                            byteBuf.release();
                        }
                    }
                });
            }
        });
    }

    private void populateRequestHeaders(HttpServerRequest<ByteBuf> serverRequest, HttpClientRequest<ByteBuf> request) {
        Set<String> headerNames = serverRequest.getHeaders().names();
        for (String name : headerNames) {
            if (name.contains("content-length")) {
                continue;
            }
            request.getHeaders().add(name, serverRequest.getHeaders().getHeader(name));
        }
        // Normally always request gzipped from the server. But can be overridden with a Dynamic Property.
        if (PROXY_REQ_ACCEPT_ENCODING != null && PROXY_REQ_ACCEPT_ENCODING.length() > 0) {
            request.getHeaders().addHeader("accept-encoding", PROXY_REQ_ACCEPT_ENCODING);
        }
        //TODO Write X-Forwarded-Host, X-Forwarded-Port, X-Forwarded-Proto, X-Forwarded-For in the headers
    }

    private LoadBalancingHttpClient<ByteBuf, ByteBuf> getClient(String vip) {
        LoadBalancingHttpClient<ByteBuf, ByteBuf> client = httpClients.get(vip);
        if (client == null) {
            IClientConfig config = IClientConfig.Builder.newBuilder("prana_backend").
                    withDefaultValues().
                    withDeploymentContextBasedVipAddresses(vip).
                    build().
                    set(IClientConfigKey.Keys.MaxTotalConnections, 2000).
                    set(IClientConfigKey.Keys.MaxConnectionsPerHost, 2000).
                    set(IClientConfigKey.Keys.OkToRetryOnAllOperations, false).
                    set(IClientConfigKey.Keys.NIWSServerListClassName, DiscoveryEnabledNIWSServerList.class.getName());

            client = RibbonTransport.newHttpClient(new HttpClientPipelineConfigurator<ByteBuf, ByteBuf>(), config);
            httpClients.putIfAbsent(vip, client);

        }
        return client;
    }

}
