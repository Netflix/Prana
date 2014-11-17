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

import javax.inject.Inject;

public class PingHandler extends AbstractRequestHandler {

    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    private static final String CACHE_CONTROL_HEADER_VAL = "must-revalidate,no-cache,no-store";
    private static final String DEFAULT_PONG_RESPONSE = "pong";

    @Inject
    public PingHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    void handle(Context context) {
        context.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_HEADER_VAL);
        context.sendSimple(DEFAULT_PONG_RESPONSE);
    }
}
