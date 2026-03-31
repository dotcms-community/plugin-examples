package com.dotmarketing.osgi.webinterceptors;

import org.osgi.framework.BundleContext;
import com.dotcms.filters.interceptor.FilterWebInterceptorProvider;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotcms.filters.interceptor.WebInterceptorDelegate;
import com.dotmarketing.filters.InterceptorFilter;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Config;

public class Activator extends GenericBundleActivator {



    private WebInterceptor[] interceptors = new WebInterceptor[] {
            new PreWebInterceptor(),
            new WrappingWebInterceptor()};

    
    
    public void start(BundleContext context) throws Exception {

        final FilterWebInterceptorProvider provider = FilterWebInterceptorProvider.getInstance(Config.CONTEXT);

        final WebInterceptorDelegate delegate = provider.getDelegate(InterceptorFilter.class);


        for (WebInterceptor interceptor : interceptors) {
            delegate.addFirst(interceptor);
        }

    }

    public void stop(BundleContext context) throws Exception {

        final FilterWebInterceptorProvider provider = FilterWebInterceptorProvider.getInstance(Config.CONTEXT);

        final WebInterceptorDelegate delegate = provider.getDelegate(InterceptorFilter.class);

        for (WebInterceptor interceptor : interceptors) {
            delegate.remove(interceptor.getName(), true);
        }


    }

}
