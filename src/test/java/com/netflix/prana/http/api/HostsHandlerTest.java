package com.netflix.prana.http.api;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.Application;
import com.netflix.prana.service.HostService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.junit.*;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dchoudhury on 10/28/14.
 */
public class HostsHandlerTest {

    private HttpServer<ByteBuf, ByteBuf> server;

    private HttpClient<ByteBuf, ByteBuf> client;

    private final int port = 23455;

    private HostService hostService;

    @Before
    public void setUp() {
        hostService = mock(HostService.class);
        ArrayList<InstanceInfo> instanceInfos = new ArrayList<>();
        instanceInfos.add(InstanceInfo.Builder.newBuilder().setAppName("foo").setVIPAddress("bar").setHostName("host1").build());
        instanceInfos.add(InstanceInfo.Builder.newBuilder().setAppName("foo").setVIPAddress("bar").setHostName("host2").build());
        when(hostService.getHosts("foo")).thenReturn(instanceInfos);
        server = RxNetty.newHttpServerBuilder(port, new HostsHandler(hostService))
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
    public void shouldReturnAListOfHostsWhenBothVipAndAppIsSpecified() {
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/hosts?appName=foo&vip=bar");
        String response = Utils.getResponse(request, client);
        Assert.assertEquals("[\"host1\",\"host2\"]", response);
    }

}
