package com.netflix.prana.http.api;

import com.google.common.base.Strings;
import com.netflix.config.DynamicProperty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

import java.net.MalformedURLException;
import java.net.URL;

public class HealthCheckHandler implements RequestHandler<ByteBuf, ByteBuf> {

    public Observable<Void> handle(HttpServerRequest<ByteBuf> serverRequest, final HttpServerResponse<ByteBuf> serverResponse) {
        String externalHealthCheckURL = DynamicProperty.getInstance("prana.host.healthcheck.url").getString("http://localhost:7001/healthcheck");
        serverResponse.getHeaders().add("Content-Type", "application/xml");
        if (Strings.isNullOrEmpty(externalHealthCheckURL)) {
            serverResponse.writeBytes("<health>ok</health>".getBytes());
            return serverResponse.close();
        }

        return getResponse(externalHealthCheckURL).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<Void>>() {
            @Override
            public Observable<Void> call(HttpClientResponse<ByteBuf> response) {
                if (response.getStatus().code() == 200) {
                    serverResponse.writeBytes("<health>ok</health>".getBytes());
                    return serverResponse.close();
                }
                serverResponse.writeBytes("<health>fail</health>".getBytes());
                return serverResponse.close();
            }
        }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<Void>>() {
            @Override
            public Observable<Void> call(OnErrorThrowable onErrorThrowable) {
                serverResponse.writeBytes("<health>fail</health>".getBytes());
                return serverResponse.close();
            }
        });
    }

    private Observable<HttpClientResponse<ByteBuf>> getResponse(String externalHealthCheckURL) {
        String host = "localhost";
        int port = 7101;
        String path = "/healthcheck";
        try {
            URL url = new URL(externalHealthCheckURL);
            host = url.getHost();
            port = url.getPort();
            path = url.getPath();
        } catch (MalformedURLException e) {
            //continue
        }
        HttpClient<ByteBuf, ByteBuf> httpClient = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder(host, port)
                .pipelineConfigurator(PipelineConfigurators.httpClientConfigurator())
                .channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .build();
        return httpClient.submit(HttpClientRequest.createGet(path));

    }

}
