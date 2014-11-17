package com.netflix.prana.http.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.prana.http.Context;
import com.netflix.prana.internal.DefaultContext;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

public abstract class AbstractRequestHandler implements RequestHandler<ByteBuf, ByteBuf> {

    private final ObjectMapper objectMapper;

    protected AbstractRequestHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    abstract void handle(Context context);

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, final HttpServerResponse<ByteBuf> response) {
        DefaultContext context = new DefaultContext(request, response, objectMapper);
        handle(context);
        return context.getResponseSubject();
    }
}
