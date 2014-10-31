package com.netflix.prana.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.prana.service.HostService;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netflix.appinfo.InstanceInfo.InstanceStatus;

/**
 * Created by dchoudhury on 10/20/14.
 */
public class HostsHandler implements RequestHandler<ByteBuf, ByteBuf> {

    private final ObjectMapper objectMapper;

    private final HostService hostService;

    @Inject
    public HostsHandler(HostService hostService) {
        this.hostService = hostService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> serverRequest, HttpServerResponse<ByteBuf> serverResponse) {
        Map<String, List<String>> queryParameters = serverRequest.getQueryParameters();
        String appName = Utils.forQueryParam(queryParameters, "appName");
        String vip = Utils.forQueryParam(queryParameters, "vip");
        if (Strings.isNullOrEmpty(appName)) {
            serverResponse.setStatus(HttpResponseStatus.BAD_REQUEST);
            serverResponse.writeString("appName has to be specified");
            return serverResponse.close();
        }
        List<InstanceInfo> instances = hostService.getHosts(appName);
        List<String> hosts = new ArrayList<>();
        for (InstanceInfo instanceInfo : instances) {
            if (vip != null && !instanceInfo.getVIPAddress().contains(vip) && instanceInfo.getStatus().equals(InstanceStatus.UP)) {
                continue;
            }
            hosts.add(instanceInfo.getHostName());
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(hosts);
            serverResponse.getHeaders().set("Content-Type", "application/json");
            serverResponse.writeBytes(bytes);
            return serverResponse.close();
        } catch (JsonProcessingException e) {
            serverResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            serverResponse.writeString(e.getMessage());
            return serverResponse.close();
        }
    }
}
