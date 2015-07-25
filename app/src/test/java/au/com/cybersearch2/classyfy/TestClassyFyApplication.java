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
package au.com.cybersearch2.classyfy;

import java.lang.reflect.Method;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestLifecycleApplication;

import android.util.Log;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * TestClassyFyApplication
 * @author Andrew Bowley
 * 14/04/2014
 */
public class TestClassyFyApplication extends ClassyFyApplication implements TestLifecycleApplication
{
    private static ClassyFyApplicationModule classyFyApplicationModule;
    public static final String TAG = "TestClassyFyApplication";
    public static final String PU_NAME = "classyfy";
    private static TestClassyFyApplication singleton;
 
    public TestClassyFyApplication()
    {
        singleton = this;
        RuntimeEnvironment.application = singleton;
    }

    @Override
    public void beforeTest(Method method) 
    {
    }

    @Override
    public void prepareTest(Object test) 
    {
    }

    @Override
    public void afterTest(Method method) 
    {
    }

    public static TestClassyFyApplication getTestInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("TestClassyFyApplication called while not initialized");
        return singleton;
    }
    
    public static ClassyFyApplicationModule getTestApplicationModule()
    {
        return classyFyApplicationModule;
    }

    public void init(Object... extraModules)
    {
        classyFyApplicationModule = new ClassyFyApplicationModule();
        ContextModule contextModule = new ContextModule(this);
        Object[] initModules = new Object[extraModules.length + 1];
        initModules[0] = contextModule;
        if (extraModules.length > 0)
            for (int i = 0; i < extraModules.length; i++)
                initModules[i + 1] = extraModules[i];
        new DI(classyFyApplicationModule, initModules);
    }
    
    public void startup()
    {
        super.startApplicationSetup();
    }

    /**
     * Wait for application setup
     * @see au.com.cybersearch2.classyfy.interfaces.ClassyFyLauncher#waitForApplicationSetup()
     */
    @Override
    public WorkStatus waitForApplicationSetup()
    {
        return startup.waitForApplicationSetup();
    }

    /**
     * Override to allow startup to be optional
     * @see au.com.cybersearch2.classyfy.ClassyFyApplication#startApplicationSetup()
     */
    @Override
    protected void startApplicationSetup()
    {
    }

}
