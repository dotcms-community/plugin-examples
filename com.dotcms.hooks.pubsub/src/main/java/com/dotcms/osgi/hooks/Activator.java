package com.dotcms.osgi.hooks;

import com.dotmarketing.osgi.GenericBundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Adds a post hook to the contentlet api and listening a sub/pub msg
 * @author jsanca
 */
public class Activator extends GenericBundleActivator {

    @SuppressWarnings ("unchecked")
    public void start ( BundleContext context ) throws Exception {

        //Initializing services...
        initializeServices ( context );

        //Adding hooks
        addPostHook( Class.forName( PublishContentPostContentHook.class.getName() ).newInstance() );
    }

    public void stop ( BundleContext context ) throws Exception {

        unregisterServices( context );
    }

}
