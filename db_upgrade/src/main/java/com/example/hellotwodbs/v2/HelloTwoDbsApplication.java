/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package com.example.hellotwodbs.v2;

import android.app.Application;
import au.com.cybersearch2.android.example.v2.AndroidHelloTwoDbs;
import au.com.cybersearch2.android.example.v2.AndroidHelloTwoDbsComponent;

/**
 * HelloTwoDbsApplication
 * @author Andrew Bowley
 * 20 Nov 2014
 */
public class HelloTwoDbsApplication extends Application 
{
    private static HelloTwoDbsApplication singleton;
    private static AndroidHelloTwoDbsComponent helloTwoDbsComponent;
    private AndroidHelloTwoDbs androidHelloTwoDbsSingleton;
    
    @Override public void onCreate() 
    {
        super.onCreate();
        singleton = this;
        androidHelloTwoDbsSingleton = new AndroidHelloTwoDbs(this);
        try 
        {   // Start from beginning in case database at v2
            androidHelloTwoDbsSingleton.setUp();
            helloTwoDbsComponent = androidHelloTwoDbsSingleton.getComponent();
        } 
        catch (InterruptedException e) 
        {   // This not expected to ever happen
            throw new IllegalStateException("onCreate()  interrupted in AndroidHelloTwoDbs setUp()", e);
        }
   }

   public AndroidHelloTwoDbsComponent getHelloTwoDbsComponent()
   {
       return helloTwoDbsComponent;
   }
    
   public static HelloTwoDbsApplication getInstance()
   {
       if (singleton == null)
           throw new IllegalStateException("HelloTwoDbsApplication called while not initialized");
       return singleton;
   }
}
