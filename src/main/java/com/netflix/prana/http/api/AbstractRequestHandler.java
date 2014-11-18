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

    abstract Observable<Void> handle(Context context);

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, final HttpServerResponse<ByteBuf> response) {
        DefaultContext context = new DefaultContext(request, response, objectMapper);
        return handle(context);
    }
}
