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
package com.netflix.prana.service.healthcheck;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A default implementation of a HealthCheck that uses an HTTP GET request to determine the health status of a third
 * party application.
 */
public class DefaultHealthCheck implements HealthCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHealthCheck.class);
    public static final int DEFAULT_APPLICATION_PORT = 7101;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
    public static final String DEFAULT_HEALTHCHECK_ENDPOINT = "http://localhost:7001/healthcheck";

    /**
     * Determine the health of the associated app by making an HTTP GET request to a specified URL
     * @return A tuple containing the health status of the app and a message
     */
    @Override
    public Observable<HealthStatus> getHealthStatus() {
        String externalHealthCheckURL = DynamicProperty.getInstance("prana.host.healthcheck.url")
                .getString(DEFAULT_HEALTHCHECK_ENDPOINT);
        if (Strings.isNullOrEmpty(externalHealthCheckURL)) {
            // No URL to check
            LOGGER.info("No external health check URL was provided while using the default HealthCheck implementation");
            return Observable.just(HealthStatus.HEALTHY);
        } else {
            return getResponse(externalHealthCheckURL).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<HealthStatus>>() {
                @Override
                public Observable<HealthStatus> call(HttpClientResponse<ByteBuf> response) {
                    if (response.getStatus().code() == HttpResponseStatus.OK.code()) {
                        return Observable.just(HealthStatus.HEALTHY);
                    } else {
                        return Observable.just(HealthStatus.UNHEALTHY);
                    }
                }
            }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<HealthStatus>>() {
                @Override
                public Observable<HealthStatus> call(OnErrorThrowable onErrorThrowable) {
                    LOGGER.error("Error getting Http Health Check", onErrorThrowable);
                    return Observable.just(HealthStatus.ERROR);
                }
            });
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
