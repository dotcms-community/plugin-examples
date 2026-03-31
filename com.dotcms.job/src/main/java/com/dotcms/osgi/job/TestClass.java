package com.dotmarketing.osgi.job;

import java.io.Serializable;
import com.dotmarketing.util.Logger;

/**
 * Created by Jonathan Gamba
 * Date: 1/29/13
 */
public class TestClass implements Serializable{

    public void printA () {
        Logger.info( this.getClass().getName(), "Printing from TestClass.printA" );
    }

    public void printB () {
        Logger.info( this.getClass().getName(), "Printing from TestClass.printB" );
    }

}