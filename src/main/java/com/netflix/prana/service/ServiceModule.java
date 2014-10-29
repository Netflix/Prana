package com.netflix.prana.service;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HostService.class).to(EurekaHostService.class).in(Scopes.SINGLETON);
    }
}
