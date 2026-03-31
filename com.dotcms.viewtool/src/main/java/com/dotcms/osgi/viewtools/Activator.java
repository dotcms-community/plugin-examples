package com.dotmarketing.osgi.viewtools;

import org.osgi.framework.BundleContext;
import com.dotmarketing.osgi.GenericBundleActivator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Activator extends GenericBundleActivator {

    private Logger logger = LogManager.getLogger(Activator.class);


    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing services...
        initializeServices( bundleContext );
        logger.info("Registering the ViewTool service");
        //Registering the ViewTool service
        registerViewToolService( bundleContext, new MyToolInfo() );
    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
        unregisterViewToolServices();
    }

}