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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.prana.http.Context;
import com.netflix.prana.service.HostService;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;

import static com.netflix.appinfo.InstanceInfo.InstanceStatus;

/**
 * Created by dchoudhury on 10/20/14.
 */
public class HostsHandler extends AbstractRequestHandler {

    private final HostService hostService;

    @Inject
    public HostsHandler(HostService hostService, ObjectMapper objectMapper) {
        super(objectMapper);
        this.hostService = hostService;
    }

    @Override
    public void handle(Context context) {
        String appName = context.getQueryParam("appName");
        String vip = context.getQueryParam("vip");
        if (Strings.isNullOrEmpty(appName)) {
            context.sendError(HttpResponseStatus.BAD_REQUEST, "appName has to be specified");
        } else {
            List<InstanceInfo> instances = hostService.getHosts(appName);
            List<String> hosts = new ArrayList<>();
            for (InstanceInfo instanceInfo : instances) {
                if (vip != null && !instanceInfo.getVIPAddress().contains(vip) && instanceInfo.getStatus().equals(InstanceStatus.UP)) {
                    continue;
                }
                hosts.add(instanceInfo.getHostName());
            }
            context.send(hosts);
        }
    }
}
