package com.netflix.prana.service;

import com.google.inject.Inject;
import com.netflix.appinfo.InstanceInfo;

import java.util.List;

public interface HostService {

    public List<InstanceInfo> getHosts(String appName);
}
