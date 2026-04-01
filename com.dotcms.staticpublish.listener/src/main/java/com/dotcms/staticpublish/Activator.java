package com.dotcms.staticpublish;

import com.dotcms.staticpublish.listener.SuccessEndpointsSubscriber;
import com.dotcms.system.event.local.business.LocalSystemEventsAPI;
import com.dotcms.system.event.local.type.staticpublish.SingleStaticPublishEndpointSuccessEvent;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.osgi.GenericBundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi bundle activator — subscribes to static-publish endpoint success events.
 * Extend {@link SuccessEndpointsSubscriber#notify} to add your own publish logic.
 */
public class Activator extends GenericBundleActivator {

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        initializeServices(bundleContext);

        final LocalSystemEventsAPI localSystemEventsAPI = APILocator.getLocalSystemEventsAPI();
        localSystemEventsAPI.subscribe(
                SingleStaticPublishEndpointSuccessEvent.class,
                new SuccessEndpointsSubscriber());
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        final LocalSystemEventsAPI localSystemEventsAPI = APILocator.getLocalSystemEventsAPI();
        localSystemEventsAPI.unsubscribe(
                SingleStaticPublishEndpointSuccessEvent.class,
                SuccessEndpointsSubscriber.class.getName());

        unregisterServices(bundleContext);
    }
}
