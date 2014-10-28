package com.netflix.prana.http.api;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

public class HealthCheckHandlerTest {

    private HttpServer<ByteBuf, ByteBuf> server;
    private HttpServer<ByteBuf, ByteBuf> externalServer;

    private HttpClient<ByteBuf, ByteBuf> client;

    private final int port = 23455;

    private final int externalServerPort = 23457;

    @Before
    public void setUp() {
        server = RxNetty.newHttpServerBuilder(port, new HealthCheckHandler())
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        server.start();

        externalServer = RxNetty.newHttpServerBuilder(externalServerPort, new ExternalServerHandler())
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        externalServer.start();

        client = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder("localhost", port)
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpClientConfigurator())
                .channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .build();

    }


    @After
    public void tearDown() throws InterruptedException {
        server.shutdown();
        externalServer.shutdown();
    }


    @Test
    public void shouldPingExternalHostsForHealthCheck() {
        System.setProperty("prana.host.healthcheck.url", "http://localhost:" + externalServerPort);
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/healthcheck");
        Assert.assertEquals("<health>ok</health>", Utils.getResponse(request, client));
    }

    @Test
    public void shouldReturnOkIfHealthCheckURLIsSetToNull() {
        System.setProperty("prana.host.healthcheck.url", "");
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/healthcheck");
        Assert.assertEquals("<health>ok</health>", Utils.getResponse(request, client));
    }


    private class ExternalServerHandler implements RequestHandler<ByteBuf, ByteBuf> {

        @Override
        public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
            response.setStatus(HttpResponseStatus.OK);
            return response.close();
        }
    }
}
