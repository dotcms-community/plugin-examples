package com.dotmarketing.osgi.override;

import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.folders.model.Folder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;

/**
 * Created by Jonathan Gamba
 * Date: 6/18/12
 */
public class Activator extends GenericBundleActivator {

    private LoggerContext pluginLoggerContext;

    @SuppressWarnings ("unchecked")
    public void start ( BundleContext context ) throws Exception {

        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager
                .getContext(this.getClass().getClassLoader(),
                        false,
                        dotcmsLoggerContext,
                        dotcmsLoggerContext.getConfigLocation());

        //Initializing services...
        initializeServices( context );

        /*
         * Trying to use our custom implementation of this class, after the last method call should be possible
         * if was added the Override-Classes property inside the MANIFEST.MF.
         */
        Folder folder = new Folder();
        folder.getPath();
    }

    public void stop ( BundleContext context ) throws Exception {

        //Unpublish bundle services
        unpublishBundleServices();

        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}