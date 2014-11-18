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

import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

public class HealthCheckHandlerTest extends AbstractIntegrationTest {

    private HttpServer<ByteBuf, ByteBuf> externalServer;
    private int externalServerPort;

    @Override
    protected RequestHandler<ByteBuf, ByteBuf> getHandler() {
        return new HealthCheckHandler(objectMapper);
    }

    @Before
    public void setUp() {
        super.setUp();
        externalServer = RxNetty.newHttpServerBuilder(0, new ExternalServerHandler())
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        externalServer.start();
        this.externalServerPort = externalServer.getServerPort();
    }

    @Test
    public void shouldPingExternalHostsForHealthCheck() {
        ConfigurationManager.getConfigInstance().setProperty("prana.host.healthcheck.url", "http://localhost:" + externalServerPort);
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/healthcheck");
        Assert.assertEquals("<health>ok</health>", TestUtils.getResponse(request, client));
    }

    @Test
    public void shouldReturnOkIfHealthCheckURLIsSetToNull() {
        System.setProperty("prana.host.healthcheck.url", "");
        ConfigurationManager.getConfigInstance().setProperty("prana.host.healthcheck.url", "");
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/healthcheck");
        Assert.assertEquals("<health>ok</health>", TestUtils.getResponse(request, client));
    }


    private class ExternalServerHandler implements RequestHandler<ByteBuf, ByteBuf> {

        @Override
        public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
            response.setStatus(HttpResponseStatus.OK);
            return response.close();
        }
    }
}
