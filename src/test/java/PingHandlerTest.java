import com.netflix.prana.http.api.PingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.nio.charset.Charset;

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
        String response = client.submit(request).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<String>>() {
            @Override
            public Observable<String> call(HttpClientResponse<ByteBuf> response) {
                return response.getContent().map(new Func1<ByteBuf, String>() {
                    @Override
                    public String call(ByteBuf byteBuf) {
                        return byteBuf.toString(Charset.defaultCharset());
                    }
                });
            }
        }).onErrorFlatMap(new Func1<OnErrorThrowable, Observable<String>>() {
            @Override
            public Observable<String> call(OnErrorThrowable onErrorThrowable) {
                throw onErrorThrowable;
            }
        }).toBlocking().first();

        Assert.assertEquals("pong", response);
    }

}
