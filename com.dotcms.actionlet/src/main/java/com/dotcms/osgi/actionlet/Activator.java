package com.dotcms.osgi.actionlet;

import com.dotmarketing.osgi.GenericBundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends GenericBundleActivator {

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing services...
        initializeServices( bundleContext );

        //Registering the test Actionlet
        registerActionlet( bundleContext, new MyActionlet() );
    }

    public void stop(BundleContext context) throws Exception {

        //Unregister all the bundle services
        unregisterServices(context);
    }

}