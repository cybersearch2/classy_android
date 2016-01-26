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
package au.com.cybersearch2.classyfy.module;

import java.util.Locale;

import javax.inject.Singleton;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
import au.com.cybersearch2.classyfy.ClassyFyResourceEnvironment;
import au.com.cybersearch2.classyfy.ClassyFyThreadHelper;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.InternalHandler;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.ThreadHelper;
import dagger.Module;
import dagger.Provides;


/**
 * ClassyFyApplicationModule
 * @author Andrew Bowley
 * 18/04/2014
 */
@Module
public class ClassyFyEnvironmentModule
{
    @Provides @Singleton ThreadHelper provideThreadHelper()
    {
        return new ClassyFyThreadHelper();
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new ClassyFyResourceEnvironment();
    }

    @Provides @Singleton InternalHandler provideInternalHandler()
    {
        return new InternalHandler();
    }

    @Provides @Singleton TaskManager provideTaskManager()
    {
        return new TaskManager();
    }

    @Provides @Singleton AndroidDatabaseSupport provideDatabaseSupport()
    {
        return new AndroidDatabaseSupport();
    }
    
    @Provides @Singleton PersistenceFactory providePersistenceFactory(
            AndroidDatabaseSupport databaseSupport, 
            ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment);
    }

    @Provides @Singleton Locale provideLocale(ResourceEnvironment resourceEnvironment)
    {
        return resourceEnvironment.getLocale();
    }
}
