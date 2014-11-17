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
