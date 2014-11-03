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

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.ribbon.transport.netty.http.LoadBalancingHttpClient;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;

public class NIWSCommand extends HystrixObservableCommand<HttpClientResponse<ByteBuf>> {

    private final LoadBalancingHttpClient<ByteBuf, ByteBuf> httpClient;

    private final HttpClientRequest<ByteBuf> req;

    protected NIWSCommand(LoadBalancingHttpClient<ByteBuf, ByteBuf> httpClient, HttpClientRequest<ByteBuf> req,
                          HystrixCommandKey key) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("prana")).andCommandKey(key).
                andCommandPropertiesDefaults(HystrixCommandProperties.Setter().
                        withExecutionIsolationThreadTimeoutInMilliseconds(10000).
                        withRequestCacheEnabled(false).
                        withExecutionIsolationSemaphoreMaxConcurrentRequests(1000).
                        withCircuitBreakerEnabled(false)));

        this.httpClient = httpClient;
        this.req = req;
    }

    @Override
    protected Observable<HttpClientResponse<ByteBuf>> run() {
        return httpClient.submit(req);
    }
}
