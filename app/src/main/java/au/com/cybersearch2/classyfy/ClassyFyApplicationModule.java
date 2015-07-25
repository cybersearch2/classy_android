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

import javax.inject.Singleton;

import android.database.sqlite.SQLiteOpenHelper;
import au.com.cybersearch2.classyfy.helper.TicketManager;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.AndroidPersistenceEnvironment;
import au.com.cybersearch2.classyjpa.AndroidPersistenceFactory;
import dagger.Module;
import dagger.Provides;

/**
 * ClassyFyApplicationModule
 * @author Andrew Bowley
 * 08/07/2014
 */
@Module(injects = { 
    ClassyFyStartup.class, 
    ClassyFyProvider.class,
    ClassyFySearchEngine.class,
    MainActivity.class,
    TitleSearchResultsActivity.class
    }, includes = ClassyFyEnvironmentModule.class)
public class ClassyFyApplicationModule implements ApplicationModule
{
    @Provides @Singleton SQLiteOpenHelper provideSQLiteOpenHelper()
    {
        AndroidPersistenceFactory androidPersistenceFactory = 
            new AndroidPersistenceFactory();
        AndroidPersistenceEnvironment androidPersistence = 
            androidPersistenceFactory.getAndroidPersistenceEnvironment(ClassyFyApplication.PU_NAME);
        return androidPersistence.getSQLiteOpenHelper();
    }
    
    @Provides @Singleton ClassyfyLogic proviceClassyfyLogic()
    {
        return new ClassyfyLogic();
    }
    
    @Provides @Singleton TicketManager provideTicketManager()
    {
        return new TicketManager();
    }
}
