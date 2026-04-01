package com.dotcms.osgi.job;


import com.dotmarketing.business.APILocator;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;


public class Activator extends GenericBundleActivator {

    // Run every 10 seconds
    private static final long RUN_EVERY_SECONDS = 10;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void start(final BundleContext context) throws Exception {

        scheduler.scheduleAtFixedRate(new MyCustomRunnable(), 0, RUN_EVERY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Allows developers to correctly stop/un-register/remove Services and other utilities when an OSGi Plugin is
     * stopped.
     *
     * @param context The OSGi {@link BundleContext} object.
     * @throws Exception An error occurred during the plugin's stop.
     */
    @Override
    public void stop(final BundleContext context) throws Exception {

        Logger.info(this.getClass(), "Stopping Delete Old Content Versions");
        scheduler.shutdownNow();
    }



    class MyCustomRunnable implements Runnable {

        /**
         * Only executes if this is the oldest server in the cluster - meaning only one node
         * in a cluster will run this at any given time.  If you want this
         * to run on every server, remove the shouldRun check.
         */
        @Override
        public void run() {
            if(!shouldRun()){
                System.out.println("I'm not the oldest server in the cluster, not running.");
                return;
            }
            System.out.println("I'm running every 10 seconds!");
        }
    };


    /**
     * Am I the longest running server in the cluster?
     * @return
     */
    boolean shouldRun() {
        try{
            final String oldestServer = APILocator.getServerAPI().getOldestServer();
            return (oldestServer.equals(APILocator.getServerAPI().readServerId()));
        }catch(Exception e){
            Logger.error(this, "Error checking if I'm the oldest server in the cluster: " + e.getMessage(), e);
            return false;
        }

    }



}
