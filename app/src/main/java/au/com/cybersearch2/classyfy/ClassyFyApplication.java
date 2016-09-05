/**
    Copyright (C) 2015  www.cybersearch2.com.au

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
package au.com.cybersearch2.classyfy;

import android.app.Application;
import android.util.Log;
import au.com.cybersearch2.classyfy.helper.ConfigureLog4J;
import au.com.cybersearch2.classyfy.service.ClassyFyService;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * ClassyFyApplication
 * Launches ClassyFy persistence
 * @author Andrew Bowley
 * 19 Jun 2015
 */
public class ClassyFyApplication extends Application
{
    public static final String TAG = "ClassyFyApplication";
    
    /** Object to initialize ClassyFy persistence, on which the appliication depends */
    /** Singleton */
    static ClassyFyApplication singleton;
    /** Dagger2 Application Component - ClassyFy will not run unless this variable is set */
    protected ClassyFyComponent classyFyComponent;
    protected ClassyFyService classyFyService;
    protected Object startMonitor;
    /** Record last executable to monitor activity */
    private volatile Executable executable;

    /**
     * Construct ClassyFyApplication object
     */
    public ClassyFyApplication()
    {
        startMonitor = new Object();
        singleton = this;
    }
 
    /**
     * onCreate
     * @see android.app.Application#onCreate()
     */
    @Override public void onCreate() 
    {
        super.onCreate();
        startApplication();
    }
    
    public void startApplication()
    {
        // Configure Log4j used by Ormlite SQLite library
        ConfigureLog4J.configure();
        if (Log.isLoggable(TAG, Log.INFO))
            Log.i(TAG, "Start ClassyFy application");
    }

    public void closeApplication()
    {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    /**
     * Returns Dagger2 application component but blocks if
     * it is not available due to initialization in progress.
     * NOTE
     * MainActivity and other injectees must call inject() from 
     * background thread when responding to OnCreate event.
     * @return
     */
    public ClassyFyComponent getClassyFyComponent()
    {
        if (classyFyComponent == null)
        {
            synchronized(startMonitor)
            {
                if (classyFyComponent == null)
                    try
                    {
                        startMonitor.wait();
                    }
                    catch (InterruptedException e)
                    {
                    }
            }
        }
        return classyFyComponent;
    }

    protected void onAndroidCreate()
    {
        super.onCreate();
    }
   
    public static ClassyFyApplication getInstance()
    {
        return singleton;
    }

    public void setComponent(ClassyFyComponent classyFyComponent, ClassyFyService classyFyService)
    {
        this.classyFyService = classyFyService;
        synchronized(startMonitor)
        {
            this.classyFyComponent = classyFyComponent;
            startMonitor.notifyAll();
        }
    }
 
    public WorkStatus getWorkStatus() throws InterruptedException
    {
        if (executable == null)
            return WorkStatus.PENDING;
        WorkStatus workStatus = executable.waitForTask();
        if (workStatus != WorkStatus.RUNNING)
            executable = null;
        return workStatus;
    }

    public Executable getExecutable(PersistenceWork persistenceWork) throws InterruptedException
    {
        executable = classyFyService.put(persistenceWork);
        return executable;
    }
}
