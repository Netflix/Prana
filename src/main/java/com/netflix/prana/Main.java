package com.netflix.prana;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.KaryonServer;
import com.netflix.prana.config.PranaConfig;
import com.netflix.prana.http.api.HandlersModule;
import com.netflix.prana.service.ServiceModule;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Main {

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
            @SuppressWarnings("deprecation")
            Injector injector = LifecycleInjector.builder().withModules(sm, new ServiceModule(), new HandlersModule()).createInjector();
            LifecycleManager manager = injector.getInstance(LifecycleManager.class);
            manager.start();
            KaryonServer karyonServer = injector.getInstance(KaryonServer.class);
            karyonServer.startAndWaitTillShutdown();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

}
