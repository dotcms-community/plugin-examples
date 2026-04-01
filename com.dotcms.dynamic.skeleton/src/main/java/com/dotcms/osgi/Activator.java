package com.dotcms.osgi;

import com.dotmarketing.osgi.GenericBundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This class will be used by OSGi as the entry point to your code during the bundle
 * activation.
 */
public class Activator extends GenericBundleActivator {

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing services...
        initializeServices( bundleContext );
        
        // You could...
		//registerActionlet(BundleContext, WorkFlowActionlet)
		//registerActionMapping(ActionMapping)
		//registerBundleResourceMessages(BundleContext)
		//registerCacheProvider(BundleContext, String, Class<CacheProvider>)
		//registerPortlets(BundleContext, String[])
		//registerRuleActionlet(BundleContext, RuleActionlet)
		//registerRuleConditionlet(BundleContext, Conditionlet)
		//registerViewToolService(BundleContext, ToolInfo)
    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
		// remember to "unregister" anything you registered at the "start" method
		// this will ensure the working consistency of the system.

        //Unregister all the bundle services
        unregisterServices(bundleContext);
    }
} // EOC Activator