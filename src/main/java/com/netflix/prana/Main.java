/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.prana;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.KaryonServer;
import com.netflix.prana.config.PranaConfig;
import com.netflix.prana.http.api.HandlersModule;
import com.netflix.prana.service.ServiceModule;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        PranaConfig pranaConfig = new PranaConfig();
        CmdLineParser cmdLineParser = new CmdLineParser(pranaConfig);
        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmdLineParser.printUsage(System.err);
            System.exit(1);
        }


        try {
            MainModule sm = new MainModule(pranaConfig);
            try {
                FileInputStream fileInputStream = new FileInputStream(pranaConfig.getConfigFile());
                Properties properties = new Properties();
                properties.load(fileInputStream);
                ConfigurationManager.loadProperties(properties);
            } catch (FileNotFoundException fox) {
                logger.error(String.format("Config file %s is not present, loading default properties present in classpath",
                        pranaConfig.getConfigFile()));
            }
            DeploymentContext deploymentContext = ConfigurationManager.getDeploymentContext();
            deploymentContext.setApplicationId(pranaConfig.getAppName());
            Injector injector = LifecycleInjector.builder().withModules(sm, new ServiceModule(), new HandlersModule()).build().createInjector();
            LifecycleManager manager = injector.getInstance(LifecycleManager.class);
            manager.start();

            KaryonServer karyonServer = injector.getInstance(KaryonServer.class);
            karyonServer.startAndWaitTillShutdown();
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
    }

}
