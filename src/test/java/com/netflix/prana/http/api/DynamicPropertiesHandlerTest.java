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

import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DynamicPropertiesHandlerTest {

    private HttpServer<ByteBuf, ByteBuf> server;

    private HttpClient<ByteBuf, ByteBuf> client;

    private final int port = 23455;

    @Before
    public void setUp() {
        server = RxNetty.newHttpServerBuilder(port, new DynamicPropertiesHandler())
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        server.start();
        client = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder("localhost", port)
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpClientConfigurator())
                .channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .build();

    }

    @After
    public void tearDown() throws InterruptedException {
        server.shutdown();
    }

    @Test
    public void shouldReturnListOfProperties() {
        ConfigurationManager.getConfigInstance().setProperty("foo", "bar");
        ConfigurationManager.getConfigInstance().setProperty("lol", 10);
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/dynamicproperties?id=foo&id=lol");
        String response = TestUtils.getResponse(request, client);
        assertTrue(response.contains("\"lol\":\"10\""));
        assertTrue(response.contains("\"foo\":\"bar\""));
    }


    @Test
    public void shouldReturnNullForUnknownProperties() {
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/dynamicproperties?id=bar");
        assertEquals("{\"bar\":null}", TestUtils.getResponse(request, client));

    }

}
