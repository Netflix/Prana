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
