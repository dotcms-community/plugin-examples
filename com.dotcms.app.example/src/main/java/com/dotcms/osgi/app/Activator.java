package com.dotcms.osgi.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import com.dotcms.filters.interceptor.FilterWebInterceptorProvider;
import com.dotcms.filters.interceptor.WebInterceptorDelegate;
import com.dotcms.security.apps.AppSecretSavedEvent;
import com.dotcms.system.event.local.business.LocalSystemEventsAPI;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.filters.InterceptorFilter;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.ConfigUtils;
import com.dotmarketing.util.Logger;

public final class Activator extends GenericBundleActivator {


    private final AppWebInterceptor appWebInterceptor = new AppWebInterceptor();

    final LocalSystemEventsAPI localSystemEventsAPI = APILocator.getLocalSystemEventsAPI();


    @Override
    public void start(BundleContext context) throws Exception {

        Logger.info(this.getClass().getName(), "Activating Example App");
        
        // copy the yaml
        copyAppYml();

        // flush the app cache
        CacheLocator.getAppsCache().clearCache();

        // add an event that subscribes to changes in App configuration
        subscribeToAppSaveEvent();

        final FilterWebInterceptorProvider filterWebInterceptorProvider =
                        FilterWebInterceptorProvider.getInstance(Config.CONTEXT);
        final WebInterceptorDelegate delegate = filterWebInterceptorProvider.getDelegate(InterceptorFilter.class);
        delegate.addFirst(appWebInterceptor);
        
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        unsubscribeToAppSaveEvent();
        deleteYml();
        
        final FilterWebInterceptorProvider filterWebInterceptorProvider =
                        FilterWebInterceptorProvider.getInstance(Config.CONTEXT);
        final WebInterceptorDelegate delegate = filterWebInterceptorProvider.getDelegate(InterceptorFilter.class);
        delegate.remove(appWebInterceptor.getName(), true);


    }


    private final File installedAppYaml = new File(ConfigUtils.getAbsoluteAssetsRootPath() + File.separator + "server"
                    + File.separator + "apps" + File.separator + AppKeys.APP_YAML_NAME);


    /**
     * copies the App yaml to the apps directory and refreshes the apps
     * 
     * @throws IOException
     */
    private void copyAppYml() throws IOException {


        Logger.info(this.getClass().getName(), "copying YAML File:" + installedAppYaml);
        installedAppYaml.getParentFile().mkdirs();
        try (final InputStream in = this.getClass().getResourceAsStream("/" + AppKeys.APP_YAML_NAME)) {
            IOUtils.copy(in, Files.newOutputStream(installedAppYaml.toPath()));
        }
        CacheLocator.getAppsCache().clearCache();


    }

    /**
     * Deletes the App yaml to the apps directory and refreshes the apps
     * 
     * @throws IOException
     */
    private void deleteYml() throws IOException {


        Logger.info(this.getClass().getName(), "deleting the YAML File:" + installedAppYaml);

        installedAppYaml.delete();
        CacheLocator.getAppsCache().clearCache();


    }


    /**
     * 
     */
    private void subscribeToAppSaveEvent() {
        Logger.info(this.getClass().getName(), "Subscribing to App Save Event");
        localSystemEventsAPI.subscribe(AppSecretSavedEvent.class, new AppSecretEventSubscriber());


    }


    private void unsubscribeToAppSaveEvent() {
        Logger.info(this.getClass().getName(), "Unsubscribing to App Save Event");
        localSystemEventsAPI.unsubscribe(AppSecretEventSubscriber.class);


    }


}
