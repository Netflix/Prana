package com.netflix.prana.service;

import com.netflix.appinfo.InstanceInfo;

import java.util.List;

public interface HostService {

    List<InstanceInfo> getHosts(String appName);
}
