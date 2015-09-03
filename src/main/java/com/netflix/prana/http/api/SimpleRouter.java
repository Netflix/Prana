/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.prana.http.api;

import com.google.inject.Inject;
import netflix.karyon.transport.http.SimpleUriRouter;
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
                        PingHandler pingHandler, DynamicPropertiesHandler dynamicPropertiesHandler, StatusHandler statusHandler) {
        delegate = new SimpleUriRouter<>();
        delegate.addUri("/healthcheck", healthCheckHandler)
                .addUri("/dynamicproperties", dynamicPropertiesHandler)
                .addUri("/proxy", proxyHandler)
                .addUri("/eureka/hosts", hostsHandler)
                .addUri("/ping", pingHandler)
                .addUri("/status", statusHandler);
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        return delegate.handle(request, response);
    }
}
