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
package com.netflix.prana.config;

import org.kohsuke.args4j.Option;

public class PranaConfig {

    @Option(name = "-p", aliases = "--http-port-api", usage = "Http Port of Prana Server")
    private int httpPort = 8078;

    @Option(name = "-a", aliases = "--app-name", usage = "Parent host application name")
    private String appName = "prana";

    @Option(name = "-c", aliases = "--config", usage = "Prana configuration file")
    private String configFile = "prana.properties";

    public int getHttpPort() {
        return httpPort;
    }

    public String getAppName() {
        return appName;
    }

    public String getConfigFile() {
        return configFile;
    }
}
