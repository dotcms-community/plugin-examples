package com.dotmarketing.osgi.ruleengine.conditionlet;

import org.osgi.framework.BundleContext;
import com.dotmarketing.osgi.GenericBundleActivator;

/**
 * This Activator class register and unregister the new VisitorIPCondintionlet
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

        //Register the Conditionlet
        registerRuleConditionlet( bundleContext, new VisitorIPConditionlet() );
    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
    	//Unregister the Conditionlet
        unregisterConditionlets();
    }

}