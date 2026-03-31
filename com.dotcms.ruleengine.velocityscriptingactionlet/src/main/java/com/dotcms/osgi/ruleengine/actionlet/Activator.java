package com.dotmarketing.osgi.ruleengine.actionlet;

import org.osgi.framework.BundleContext;
import com.dotmarketing.osgi.GenericBundleActivator;

/**
 * This Activator class register and unregister the new VelocityScriptingActionlet
 * on dotCMS 
 * 
 * @author Oswaldo Gallango
 * @version 1.0
 * @since 10/17/2016 
 */
public class Activator extends GenericBundleActivator {

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing services...
        initializeServices( bundleContext );

        //Register the Actionlet
        registerRuleActionlet( bundleContext, new VelocityScriptingActionlet() );
    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
    	//Unregister the Actionlet
        unregisterServices(bundleContext);
    }

}