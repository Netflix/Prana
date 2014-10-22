package com.netflix.prana.http.api;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

public class PingHandler implements RequestHandler<ByteBuf, ByteBuf> {

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        response.getHeaders().set("Cache-Control", "must-revalidate,no-cache,no-store");
        response.writeString("pong");
        return response.close();
    }
}
