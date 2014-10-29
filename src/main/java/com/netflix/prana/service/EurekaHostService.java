package com.netflix.prana.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;

import java.util.List;

public class EurekaHostService implements HostService {

    private DiscoveryClient discoveryClient;

    public EurekaHostService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    @Override
    public List<InstanceInfo> getHosts(String appName) {
        return discoveryClient.getApplication(appName).getInstances();
    }
}
