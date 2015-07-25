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
package au.com.cybersearch2.example.v2;

/**
 * HellowoDbsEnvironmentModule
 * @author Andrew Bowley
 * 22 Nov 2014
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.WorkerRunnable;
import au.com.cybersearch2.example.AppThreadHelper;

/**
 * AndroidHelloTwoDbsModule
 * Dependency injection data object. @see AndroidHelloTwoDbs.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module(injects = { 
        WorkerRunnable.class,
        PersistenceFactory.class,
        NativeScriptDatabaseWork.class,
        PersistenceContext.class,
        DatabaseAdminImpl.class
        })
public class HelloTwoDbsEnvironmentModule 
{
	ConnectionType CONNECTION_TYPE = ConnectionType.file;
	
    @Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new AppThreadHelper();
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new ResourceEnvironment(){

            @Override
            public InputStream openResource(String resourceName)
                    throws IOException
            {
                ApplicationContext applicationContext = new ApplicationContext();
                return applicationContext.getContext().getAssets().open("v2/" + resourceName);
            }

            @Override
            public Locale getLocale()
            {
                return new Locale("en", "AU");
            }};
    }

    @Provides @Singleton PersistenceFactory providePersistenceFactory()
    {
        return new PersistenceFactory(new AndroidDatabaseSupport());
    }


}
