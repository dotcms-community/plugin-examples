package com.dotmarketing.osgi.job;


import com.dotmarketing.business.APILocator;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Logger;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.util.CronExpression;
import org.osgi.framework.BundleContext;


public class Activator extends GenericBundleActivator {


    private final Runnable runner = new MyCustomRunnable();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public final static String CRON_EXPRESSION = "0/10 * * * * ?";// Every 10 seconds

    @Override
    public void start(final BundleContext context) throws Exception {

        CronExpression cron = new CronExpression(CRON_EXPRESSION);

        Instant now = Instant.now();

        Instant previousRun = cron.getPrevFireTime(Date.from(now)).toInstant();
        Instant nextRun = cron.getNextValidTimeAfter(Date.from(previousRun)).toInstant();

        Duration delay = Duration.between(previousRun, now);
        Duration runEvery = Duration.between(previousRun, nextRun);


        scheduler.scheduleAtFixedRate(new MyCustomRunnable(), delay.getSeconds(), runEvery.getSeconds(), TimeUnit.SECONDS);

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
