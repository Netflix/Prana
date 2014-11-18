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
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.prana.http.Context;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * A request handler to return the application's status in Discovery
 */
public class StatusHandler extends AbstractRequestHandler {

    private final ApplicationInfoManager applicationInfoManager;

    @Inject
    public StatusHandler(ObjectMapper objectMapper, ApplicationInfoManager applicationInfoManager) {
        super(objectMapper);
        this.applicationInfoManager = applicationInfoManager;
    }

    @Override
    void handle(Context context) {
        Map<String, String> status = new HashMap<String, String>() {{
            put("status", applicationInfoManager.getInfo().getStatus().name());
        }};
        context.send(status);
    }
}
