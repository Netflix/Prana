package com.netflix.prana;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.KaryonBootstrapSuite;
import com.netflix.karyon.KaryonServer;
import com.netflix.karyon.archaius.ArchaiusSuite;
import com.netflix.karyon.eureka.KaryonEurekaModule;
import com.netflix.prana.config.PranaConfig;
import com.netflix.prana.http.api.SimpleRouter;

public class MainModule extends AbstractModule {

    private PranaConfig config;

    public MainModule(PranaConfig config) {
        this.config = config;
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Inject
    public KaryonServer providesKaryonSever(SimpleRouter simpleRouter) {
        return Karyon.forRequestHandler(config.getHttpPort(),
                simpleRouter,
                new KaryonBootstrapSuite(),
                KaryonEurekaModule.asSuite(),
                new ArchaiusSuite("prana")
        );
    }
}
