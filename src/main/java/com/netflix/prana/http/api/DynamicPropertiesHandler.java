package com.netflix.prana.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.config.DynamicProperty;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dchoudhury on 10/20/14.
 */
public class DynamicPropertiesHandler implements RequestHandler<ByteBuf, ByteBuf> {

    private final ObjectMapper objectMapper;

    public DynamicPropertiesHandler() {
        this.objectMapper = new ObjectMapper();
    }
    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        response.getHeaders().add("Content-Type", "application/json");
        Map<String, String> properties = new HashMap<>();
        List<String> ids = forQueryParam(request.getQueryParameters(), "id");
        for (String id : ids) {
            String property = DynamicProperty.getInstance(id).getString(null);
            properties.put(id, property);
        }
        try {
            response.writeBytes(objectMapper.writeValueAsBytes(properties));
            return response.close();
        } catch (JsonProcessingException e) {
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return response.close();
        }
    }

    private List<String> forQueryParam(Map<String, List<String>> queryParams, String paramName) {
        List<String> values = queryParams.get(paramName);
        if (values == null) {
            return new ArrayList<>(1);
        }
        return values;
    }

}
