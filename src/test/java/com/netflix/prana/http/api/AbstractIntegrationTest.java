package com.netflix.prana.http.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractIntegrationTest {
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected HttpServer<ByteBuf, ByteBuf> server;
    protected HttpClient<ByteBuf, ByteBuf> client;

    private final int port = 23455;


    protected abstract RequestHandler<ByteBuf, ByteBuf> getHandler();

    @Before
    public void setUp() {
        server = RxNetty.newHttpServerBuilder(port, getHandler())
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
}
