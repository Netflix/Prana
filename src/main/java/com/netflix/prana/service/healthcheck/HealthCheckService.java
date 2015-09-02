/*
 * Copyright 2015 Nike, Inc.
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
package com.netflix.prana.service.healthcheck;

import com.netflix.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * A service to retrieve the {@link HealthCheck} service provider implementations.
 */
public final class HealthCheckService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);
    private static final String HC_PLUGIN_DIR_PROPERTY = "healthcheck.plugins.directory";
    private static final String DEFAULT_HC_PLUGIN_DIR = "/var/prana/plugins/healthcheck";

    private static final HealthCheckService INSTANCE = new HealthCheckService();

    private ServiceLoader<HealthCheck> loader;

    private volatile HealthCheck healthCheck = null;

    private HealthCheckService() {
        final String pluginDir = ConfigurationManager.getConfigInstance().getString(HC_PLUGIN_DIR_PROPERTY, DEFAULT_HC_PLUGIN_DIR);
        LOGGER.info("Loading healthcheck plugins from " + pluginDir);

        final File dir = new File(pluginDir);

        if (!dir.exists()) {
            LOGGER.warn("The healthcheck plugin directory (" + pluginDir + ") doesn't exist.");
            return;
        }
        if (!dir.isDirectory()) {
            LOGGER.warn("The healthcheck plugin directory (" + pluginDir + ") isn't a directory.");
            return;
        }

        File[] jarList = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        // listFiles will return null if there was an IO error
        if (jarList == null) {
            LOGGER.error("There was an error reading from the specified healthcheck plugin directory (" + pluginDir + ").");
            jarList = new File[0];
        }

        final URL[] urls = new URL[jarList.length];

        try {
            for (int i = 0; i < jarList.length; i++) {
                urls[i] = jarList[i].toURI().toURL();
            }
        } catch (MalformedURLException mue) {
            LOGGER.error("Error parsing plugin directory.", mue);
            return;
        }

        final URLClassLoader classLoader = new URLClassLoader(urls);
        loader = ServiceLoader.load(HealthCheck.class, classLoader);
    }

    public static HealthCheckService getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the current health status of the associated application. This method will also determine which HealthCheck
     * implementation to use if one hasn't already been set. It will first attempt to use the ServiceLoader to find
     * HealthCheck implementations. If it doesn't find any, then it will revert to the default HTTP-based health check.
     * If it finds multiple implementations, then it will throw an IllegalStateException, as we have no way to
     * determine which implementation should be used.
     * @return An observable wrapping the HealthStatus of the associated application.
     */
    public Observable<HealthStatus> getHealthStatus() {
        // If we don't have an implementation of the healthcheck service, then go look for one
        if (healthCheck == null) {
            synchronized (HealthCheck.class) {
                if (healthCheck == null) {
                    try {
                        // If the service loader couldn't be setup, then revert to the default
                        if (loader == null) {
                            LOGGER.info("Using the default HealthCheck implementation.");
                            healthCheck = new DefaultHealthCheck();
                        } else {
                            for (HealthCheck hc : loader) {
                                LOGGER.info("Found HealthCheck service implementation: " + hc.toString());
                                if (healthCheck == null) {
                                    healthCheck = hc;
                                } else {
                                    // Multiple implementations constitutes illegal state
                                    throw new IllegalStateException("Found multiple implementations of the HealthCheck service. Please ensure that only one implementation is contained in your healthcheck plugin directory.");
                                }
                            }
                            if (healthCheck == null) {
                                // Didn't find any health check implementations, revert to default
                                LOGGER.info("Didn't find implementations of the HealthCheck service in your healthcheck plugin directory. Reverting to default.");
                                healthCheck = new DefaultHealthCheck();
                            }
                        }
                    } catch (ServiceConfigurationError serviceError) {
                        LOGGER.error("Error using HealthCheck service.", serviceError);
                        throw new IllegalStateException("There was an error configuring the ServiceLoader.", serviceError);
                    }
                }
            }
        }
        return healthCheck.getHealthStatus();
    }
}
