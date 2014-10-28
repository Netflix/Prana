import com.netflix.prana.http.api.PingHandler;
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

import static org.junit.Assert.assertEquals;

public class PingHandlerTest {

    private HttpServer<ByteBuf, ByteBuf> server;

    private HttpClient<ByteBuf, ByteBuf> client;

    private final int port = 23455;

    @Before
    public void setUp() {
        server = RxNetty.newHttpServerBuilder(port, new PingHandler())
                .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        server.start();
        client = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder("localhost", port)
                .pipelineConfigurator(PipelineConfigurators.httpClientConfigurator())
                .channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .build();

    }

    @After
    public void tearDown() throws InterruptedException {
        server.shutdown();
    }

    @Test
    public void shouldRespondWithPong() {
        HttpClientRequest<ByteBuf> request = HttpClientRequest.<ByteBuf>createGet("/ping");
        assertEquals("pong", Utils.getResponse(request, client));
    }

}
