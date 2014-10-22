package com.netflix.prana.http.api;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Created by dchoudhury on 10/17/14.
 */
public class HandlersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SimpleRouter.class).in(Scopes.SINGLETON);
        bind(ProxyHandler.class).in(Scopes.SINGLETON);
        bind(HealthCheckHandler.class).in(Scopes.SINGLETON);
    }
}
