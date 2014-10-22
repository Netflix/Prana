package com.netflix.prana.config;

import org.kohsuke.args4j.Option;

public class PranaConfig {

    @Option(name = "-p", aliases = "--http-port-api", usage = "Http Port of Prana Server")
    private int httpPort = 8078;
    
    @Option(name = "-b", aliases = "--http-bind-ip", usage = "Hostname or IP address to bind")
    private String bindHost = "0.0.0.0";
    
    @Option(name = "-w", aliases = "--http-worker-max", usage = "Max http worker count")
    private int workerMax = 10;
    
    @Option(name = "-a", aliases = "--app-name", usage = "Parent host application name")
    private String appName = "prana";
    
    @Option(name = "-h", aliases = "--base-dir", usage = "Prana install dir")
    private String pranaHome = System.getProperty("user.home");
    
    public int getWorkerMax() {
        return workerMax;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getBindHost() {
        return bindHost;
    }
    
    public String getPranaHome() {
        return pranaHome;
    }

    public String getAppName() {
        return appName;
    }

}
