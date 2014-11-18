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

package com.netflix.prana.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.prana.http.Context;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpRequestHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultContext implements Context {
    private static final String ULTIMATE_FAILURE_STRING = "no content";

    private final HttpServerResponse<ByteBuf> response;
    private final HttpRequestHeaders requestHeaders;
    private final Map<String, List<String>> queryParameters;
    private final ObjectMapper objectMapper;

    public DefaultContext(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response, ObjectMapper objectMapper) {
        this.response = response;
        this.requestHeaders = request.getHeaders();
        this.queryParameters = request.getQueryParameters();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getHeader(String name) {
        return requestHeaders.getHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        response.getHeaders().setHeader(name, value);
    }

    @Override
    public String getQueryParam(String key) {
        List<String> param = queryParameters.get(key);
        return (param != null && param.size() > 0) ? param.get(0) : null;
    }

    @Override
    public List<String> getQueryParams(String key) {
        List<String> param = queryParameters.get(key);
        return (param != null && param.size() > 0) ? param : new ArrayList<String>();
    }

    @Override
    public Observable<Void> send(Object object) {
        return sendJson(object);
    }

    @Override
    public Observable<Void> sendSimple(String message) {
        setOk();
        response.writeString(message);
        return response.close();
    }

    @Override
    public Observable<Void> sendError(final HttpResponseStatus status, final String message) {
        Map<String, String> messageObject = new HashMap<String, String>() {{
            put("reason", status.reasonPhrase());
            put("message", message);
        }};

        return sendJson(messageObject);
    }

    private void setOk() {
        response.setStatus(HttpResponseStatus.OK);
    }

    private Observable<Void> sendJson(Object object) {
        byte[] bytes = new byte[0];
        try {
            setOk();
            bytes = objectMapper.writeValueAsBytes(object);
            response.getHeaders().setHeader("Content-type", MediaType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            try {
                bytes = objectMapper.writeValueAsBytes(e);
            } catch (JsonProcessingException e1) {
                // we can't really do much at this point.
            }
        }
        if (bytes.length > 0) {
            response.writeBytes(bytes);
        } else {
            response.writeString(ULTIMATE_FAILURE_STRING);
        }
        return response.close();
    }
}
