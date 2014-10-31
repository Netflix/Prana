package com.netflix.prana.config;

import org.kohsuke.args4j.Option;

public class PranaConfig {

    @Option(name = "-p", aliases = "--http-port-api", usage = "Http Port of Prana Server")
    private int httpPort = 8078;
    
    @Option(name = "-a", aliases = "--app-name", usage = "Parent host application name")
    private String appName = "prana";
    
    public int getHttpPort() {
        return httpPort;
    }

    public String getAppName() {
        return appName;
    }

}
