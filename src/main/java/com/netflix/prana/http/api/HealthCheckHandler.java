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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.netflix.config.DynamicProperty;
import com.netflix.prana.http.Context;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

public class HealthCheckHandler extends AbstractRequestHandler {
    private static final String DEFAULT_CONTENT_TYPE = "application/xml";
    private static final Observable<Void> DEFAULT_NOOP_RESPONSE = Observable.just(null);

    public static final int DEFAULT_APPLICATION_PORT = 7101;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
    public static final String DEFAULT_HEALTHCHECK_ENDPOINT = "http://localhost:7001/healthcheck";
    public static final String DEFAULT_OK_HEALTH = "<health>ok</health>";
    public static final String DEFAULT_FAIL_HEALTH = "<health>fail</health>";


    @Inject
    public HealthCheckHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    void handle(final Context context) {
        String externalHealthCheckURL = DynamicProperty.getInstance("prana.host.healthcheck.url")
                .getString(DEFAULT_HEALTHCHECK_ENDPOINT);
        context.setHeader("Content-type", DEFAULT_CONTENT_TYPE);
        if (Strings.isNullOrEmpty(externalHealthCheckURL)) {
            context.sendSimple(DEFAULT_OK_HEALTH);
        } else {
            getResponse(externalHealthCheckURL).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<Void>>() {
                @Override
                public Observable<Void> call(HttpClientResponse<ByteBuf> response) {
                    if (response.getStatus().code() == HttpResponseStatus.OK.code()) {
                        context.sendSimple(DEFAULT_OK_HEALTH);
                    } else {
                        context.sendError(HttpResponseStatus.SERVICE_UNAVAILABLE, DEFAULT_FAIL_HEALTH);
                    }
                    return DEFAULT_NOOP_RESPONSE;
                }
            }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<Void>>() {
                @Override
                public Observable<Void> call(OnErrorThrowable onErrorThrowable) {
                    context.sendError(HttpResponseStatus.SERVICE_UNAVAILABLE, DEFAULT_FAIL_HEALTH);
                    return DEFAULT_NOOP_RESPONSE;
                }
            }).subscribe();
        }
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
