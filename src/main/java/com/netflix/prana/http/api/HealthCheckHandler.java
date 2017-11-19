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
import com.netflix.prana.service.healthcheck.HealthCheckService;
import com.netflix.prana.service.healthcheck.HealthStatus;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

import javax.inject.Inject;

public class HealthCheckHandler extends AbstractRequestHandler {
    private static final String DEFAULT_CONTENT_TYPE = "application/xml";
    private static final Observable<Void> DEFAULT_NOOP_RESPONSE = Observable.just(null);
    private static final HealthCheckService HEALTH_CHECK_SERVICE = HealthCheckService.getInstance();

    public static final String DEFAULT_OK_HEALTH = "<health>ok</health>";
    public static final String DEFAULT_FAIL_HEALTH = "<health>fail</health>";

    @Inject
    public HealthCheckHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    Observable<Void> handle(final Context context) {
        context.setHeader("Content-type", DEFAULT_CONTENT_TYPE);

        return HEALTH_CHECK_SERVICE.getHealthStatus().flatMap(new Func1<HealthStatus, Observable<Void>>() {
            @Override
            public Observable<Void> call(HealthStatus healthStatus) {
                if (healthStatus == HealthStatus.HEALTHY) {
                    context.sendSimple(DEFAULT_OK_HEALTH);
                } else {
                    context.sendError(HttpResponseStatus.SERVICE_UNAVAILABLE, DEFAULT_FAIL_HEALTH);
                }
                return DEFAULT_NOOP_RESPONSE;
            }
        }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<? extends Void>>() {
            @Override
            public Observable<? extends Void> call(OnErrorThrowable onErrorThrowable) {
                context.sendError(HttpResponseStatus.SERVICE_UNAVAILABLE, DEFAULT_FAIL_HEALTH);
                return DEFAULT_NOOP_RESPONSE;
            }
        });
    }
}
