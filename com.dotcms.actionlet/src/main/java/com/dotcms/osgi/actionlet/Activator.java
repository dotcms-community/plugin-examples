package com.dotmarketing.osgi.actionlet;

import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;

public class Activator extends GenericBundleActivator {

    private LoggerContext pluginLoggerContext;

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager
                .getContext(this.getClass().getClassLoader(),
                        false,
                        dotcmsLoggerContext,
                        dotcmsLoggerContext.getConfigLocation());

        //Initializing services...
        initializeServices( bundleContext );

        //Registering the test Actionlet
        registerActionlet( bundleContext, new MyActionlet() );
    }

    public void stop(BundleContext context) throws Exception {

        //Unregister all the bundle services
        unregisterServices(context);

        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}