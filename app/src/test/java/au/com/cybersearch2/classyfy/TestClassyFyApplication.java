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
import org.robolectric.shadows.ShadowEnvironment;

import android.os.Environment;

/**
 * TestClassyFyApplication
 * @author Andrew Bowley
 * 14/04/2014
 */
public class TestClassyFyApplication extends ClassyFyApplication implements TestLifecycleApplication
{
    public static final String TAG = "TestClassyFyApplication";
    public static final String PU_NAME = "classyfy";
    private static TestClassyFyApplication singleton;
 
    private ClassyFyComponent testClassyFyComponent;
    
    public TestClassyFyApplication()
    {
        singleton = this;
        RuntimeEnvironment.application = singleton;
    }

    /**
     * onCreate
     * @see android.app.Application#onCreate()
     */
    @Override public void onCreate() 
    {
        // Don't call super.onCreate() as this initializes dependency injection
        // The following method is super.super.onCreate()
        onAndroidCreate();
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        System.out.println(ShadowEnvironment.getExternalStorageDirectory());
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

    
    public void setTestClassyFyComponent(ClassyFyComponent testClassyFyComponent)
    {
        this.testClassyFyComponent = testClassyFyComponent;
    }

    public static TestClassyFyApplication getTestInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("TestClassyFyApplication called while not initialized");
        return singleton;
    }
    
    public void startup()
    {
    }

    public ClassyFyComponent getClassyFyComponent()
    {
        if (testClassyFyComponent == null)
            return super.getClassyFyComponent();
        // Set super classyFyComponent variable for direct access
        classyFyComponent = testClassyFyComponent;
        return testClassyFyComponent;
    }

}
