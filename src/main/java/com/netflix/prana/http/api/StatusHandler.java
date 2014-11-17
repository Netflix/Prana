package com.netflix.prana.http.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.prana.http.Context;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * A request handler to return the application's status in Discovery
 */
public class StatusHandler extends AbstractRequestHandler {

    private final ApplicationInfoManager applicationInfoManager;

    @Inject
    public StatusHandler(ObjectMapper objectMapper, ApplicationInfoManager applicationInfoManager) {
        super(objectMapper);
        this.applicationInfoManager = applicationInfoManager;
    }

    @Override
    void handle(Context context) {
        Map<String, String> status = new HashMap<String, String>() {{
            put("status", applicationInfoManager.getInfo().getStatus().name());
        }};
        context.send(status);
    }
}
