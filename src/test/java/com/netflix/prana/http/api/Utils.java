package com.netflix.prana.http.api;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

import java.nio.charset.Charset;

public class Utils {

    public static String getResponse(HttpClientRequest<ByteBuf> request, HttpClient<ByteBuf, ByteBuf> client) {
        return client.submit(request).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<String>>() {
            @Override
            public Observable<String> call(HttpClientResponse<ByteBuf> response) {
                return response.getContent().map(new Func1<ByteBuf, String>() {
                    @Override
                    public String call(ByteBuf byteBuf) {
                        return byteBuf.toString(Charset.defaultCharset());
                    }
                });
            }
        }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<String>>() {
            @Override
            public Observable<String> call(OnErrorThrowable onErrorThrowable) {
                throw onErrorThrowable;
            }
        }).toBlocking().first();
    }
}