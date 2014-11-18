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

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusHandlerTest extends AbstractIntegrationTest {

    private ApplicationInfoManager applicationInfoManager = mock(ApplicationInfoManager.class);

    @Override
    protected RequestHandler<ByteBuf, ByteBuf> getHandler() {
        return new StatusHandler(objectMapper, applicationInfoManager);
    }

    @Test
    public void shouldReturnDiscoveryStatus() {
        InstanceInfo outOfService = InstanceInfo.Builder.newBuilder()
                .setAppName("foo")
                .setStatus(InstanceInfo.InstanceStatus.OUT_OF_SERVICE).build();
        when(applicationInfoManager.getInfo()).thenReturn(outOfService);
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/status");
        String response = TestUtils.getResponse(request, client);
        assertTrue(response.contains("\"status\":\"OUT_OF_SERVICE\""));
    }
}
