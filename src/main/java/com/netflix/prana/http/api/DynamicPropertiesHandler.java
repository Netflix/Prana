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
import com.netflix.config.DynamicProperty;
import com.netflix.prana.http.Context;
import rx.Observable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dchoudhury on 10/20/14.
 */
public class DynamicPropertiesHandler extends AbstractRequestHandler {

    private static final String ID_QUERY_PARAMETER = "id";

    @Inject
    public DynamicPropertiesHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    Observable<Void> handle(Context context) {
        Map<String, String> properties = new HashMap<>();
        List<String> ids = context.getQueryParams(ID_QUERY_PARAMETER);
        for (String id : ids) {
            String property = DynamicProperty.getInstance(id).getString(null);
            properties.put(id, property);
        }
        return context.send(properties);
    }
}
