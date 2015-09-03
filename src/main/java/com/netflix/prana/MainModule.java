/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.prana;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.prana.config.PranaConfig;
import com.netflix.prana.http.api.SimpleRouter;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonBootstrapModule;
import netflix.karyon.KaryonServer;
import netflix.karyon.archaius.ArchaiusBootstrapModule;
import netflix.karyon.eureka.KaryonEurekaModule;


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
                new KaryonBootstrapModule(),
                new ArchaiusBootstrapModule(config.getAppName()),
                KaryonEurekaModule.asBootstrapModule(),
                Karyon.toBootstrapModule(KaryonWebAdminModule.class));
    }
}
