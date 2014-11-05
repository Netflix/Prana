/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.prana.http.api;

import com.google.common.base.Strings;
import com.netflix.config.DynamicProperty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
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

    private final int DEFAULT_APPLICATION_PORT = 7101;

    private final int DEFAULT_CONNECTION_TIMEOUT = 2000;

    public Observable<Void> handle(HttpServerRequest<ByteBuf> serverRequest, final HttpServerResponse<ByteBuf> serverResponse) {
        String externalHealthCheckURL = DynamicProperty.getInstance("prana.host.healthcheck.url").getString("http://localhost:7001/healthcheck");
        serverResponse.getHeaders().add("Content-Type", "application/xml");
        if (Strings.isNullOrEmpty(externalHealthCheckURL)) {
            serverResponse.setStatus(HttpResponseStatus.OK);
            serverResponse.writeBytes("<health>ok</health>".getBytes());
            return serverResponse.close();
        }

        return getResponse(externalHealthCheckURL).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<Void>>() {
            @Override
            public Observable<Void> call(HttpClientResponse<ByteBuf> response) {
                if (response.getStatus().code() == HttpResponseStatus.OK.code()) {
                    serverResponse.setStatus(HttpResponseStatus.OK);
                    serverResponse.writeBytes("<health>ok</health>".getBytes());
                    return serverResponse.close();
                }
                serverResponse.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
                serverResponse.writeBytes("<health>fail</health>".getBytes());
                return serverResponse.close();
            }
        }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<Void>>() {
            @Override
            public Observable<Void> call(OnErrorThrowable onErrorThrowable) {
                serverResponse.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
                serverResponse.writeBytes("<health>fail</health>".getBytes());
                return serverResponse.close();
            }
        });
    }

    private Observable<HttpClientResponse<ByteBuf>> getResponse(String externalHealthCheckURL) {
        String host = "localhost";
        int port = DEFAULT_APPLICATION_PORT;
        String path = "/healthcheck";
        try {
            URL url = new URL(externalHealthCheckURL);
            host = url.getHost();
            port = url.getPort();
            path = url.getPath();
        } catch (MalformedURLException e) {
            //continue
        }
        Integer timeout = DynamicProperty.getInstance("prana.host.healthcheck.timeout").getInteger(DEFAULT_CONNECTION_TIMEOUT);
        HttpClient<ByteBuf, ByteBuf> httpClient = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder(host, port)
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpClientConfigurator())
                .channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .build();
        return httpClient.submit(HttpClientRequest.createGet(path));

    }

}
