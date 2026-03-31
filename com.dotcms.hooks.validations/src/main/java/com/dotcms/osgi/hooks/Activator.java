package com.dotmarketing.osgi.hooks;

import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;

/**
 * This Adds a validator for a Content Type as an example
 * You can add your own ones for your content type
 * @author jsanca
 */
public class Activator extends GenericBundleActivator {

    private LoggerContext pluginLoggerContext;

    @SuppressWarnings ("unchecked")
    public void start (final BundleContext context ) throws Exception {

        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(),
                false,
                dotcmsLoggerContext,
                dotcmsLoggerContext.getConfigLocation());

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

        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}