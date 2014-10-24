package com.netflix.prana.http.api;

import com.google.inject.Inject;
import com.netflix.karyon.transport.http.SimpleUriRouter;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

/**
 * Created by dchoudhury on 10/20/14.
 */
public class SimpleRouter implements RequestHandler<ByteBuf, ByteBuf> {

    private final SimpleUriRouter<ByteBuf, ByteBuf> delegate;

    @Inject
    public SimpleRouter(ProxyHandler proxyHandler, HealthCheckHandler healthCheckHandler, HostsHandler hostsHandler,
                        PingHandler pingHandler, DynamicPropertiesHandler dynamicPropertiesHandler) {
        delegate = new SimpleUriRouter<>();
        delegate.addUri("/healthcheck",healthCheckHandler)
                .addUri("/dynamicproperties", dynamicPropertiesHandler)
                .addUriRegex("^/proxy.*", proxyHandler)
                .addUri("/eureka/hosts", hostsHandler)
                .addUri("/ping", pingHandler);
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        return delegate.handle(request, response);
    }
}
