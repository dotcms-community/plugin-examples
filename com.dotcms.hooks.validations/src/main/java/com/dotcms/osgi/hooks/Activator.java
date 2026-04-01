package com.dotcms.osgi.hooks;

import com.dotmarketing.osgi.GenericBundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This Adds a validator for a Content Type as an example
 * You can add your own ones for your content type
 * @author jsanca
 */
public class Activator extends GenericBundleActivator {

    @SuppressWarnings ("unchecked")
    public void start (final BundleContext context ) throws Exception {

        //Initializing services...
        initializeServices ( context );

        //Adding hooks
        final ValidatorPreContentHook preContentHook = (ValidatorPreContentHook) Class.forName(
                ValidatorPreContentHook.class.getName()).newInstance();

        preContentHook.addValidator(new GenericTitleContentletValidatorStrategy(),
                new NumericTitleContentletValidatorStrategy(), new BaseTitleContentletValidatorStrategy());
        addPreHook(preContentHook);
    }

    public void stop (final BundleContext context ) throws Exception {

        unregisterServices( context );
    }

}