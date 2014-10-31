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
        assertEquals("{\"lol\":\"10\",\"foo\":\"bar\"}", TestUtils.getResponse(request, client));
    }


    @Test
    public void shouldReturnNullForUnknownProperties() {
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/dynamicproperties?id=bar");
        assertEquals("[null]", TestUtils.getResponse(request, client));

    }

}
