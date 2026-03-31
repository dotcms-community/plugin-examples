package com.dotmarketing.osgi.job;

import com.dotmarketing.util.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by Jonathan Gamba
 * Date: 1/28/13
 */
public class CustomJob implements Job {

    @Override
    public void execute ( JobExecutionContext context ) throws JobExecutionException {

        Logger.info( this.getClass().getName(), "------------------------------------------" );
        Logger.info( this.getClass().getName(), "Start custom job" );
        Logger.info( this.getClass().getName(), "" );

        Logger.info( this.getClass().getName(), "param1: " + context.getJobDetail().getJobDataMap().get( "param1" ) );
        Logger.info( this.getClass().getName(), "param2: " + context.getJobDetail().getJobDataMap().get( "param2" ) );

        Logger.info( this.getClass().getName(), "" );

        TestClass testClass = new TestClass();
        testClass.printA();
        testClass.printB();

        Logger.info( this.getClass().getName(), "" );
        Logger.info( this.getClass().getName(), "Finish custom job (osgi version)." );
        Logger.info( this.getClass().getName(), "------------------------------------------" );
    }

}