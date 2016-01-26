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
package au.com.cybersearch2.android.example.v2;

import javax.inject.Named;
import javax.inject.Singleton;

import android.content.Context;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.AndroidConnectionSourceFactory;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
import au.com.cybersearch2.classydb.AndroidSqliteParams;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.OpenEventHandler;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.example.v2.AppThreadHelper;
import au.com.cybersearch2.example.v2.ComplexOpenHelperCallbacks;
import au.com.cybersearch2.example.v2.HelloTwoDbsMain;
import au.com.cybersearch2.example.v2.SimpleOpenHelperCallbacks;
import dagger.Module;
import dagger.Provides;

/**
 * AndroidHelloTwoDbsModule
 * @author Andrew Bowley
 * 24 Apr 2015
 */
@Module
public class AndroidHelloTwoDbsModule 
{
	ConnectionType CONNECTION_TYPE = ConnectionType.file;
    private Context context;
    private AndroidHelloTwoDbs androidHelloTwoDbs;

	public AndroidHelloTwoDbsModule(
	        Context context,
	        AndroidHelloTwoDbs androidHelloTwoDbs)
    {
        this.context = context;
        this.androidHelloTwoDbs = androidHelloTwoDbs;
    }

    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new AppResourceEnvironment(context);
    }
    
    @Provides @Singleton AndroidHelloTwoDbs provideAndroidHelloTwoDbs()
    {
        return androidHelloTwoDbs;
    }

    @Provides @Singleton ConnectionType provideConnectionType()
    {
        return ConnectionType.file;
    }
    
	@Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new AppThreadHelper();
    }
    
    @Provides @Singleton TaskManager provideTaskManager()
    {
        return new TaskManager();
    }

    @Provides @Singleton AndroidDatabaseSupport provideDatabaseSupport()
    {
        AndroidDatabaseSupport androidDatabaseSupport = new AndroidDatabaseSupport();
        androidDatabaseSupport.registerOpenHelperCallbacks(new SimpleOpenHelperCallbacks());
        androidDatabaseSupport.registerOpenHelperCallbacks(new ComplexOpenHelperCallbacks());
        return androidDatabaseSupport;
    }
    
    @Provides @Singleton PersistenceFactory providePersistenceFactory(
            AndroidDatabaseSupport databaseSupport, 
            ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment);
    }

    @Provides @Singleton @Named(HelloTwoDbsMain.PU_NAME1) 
    OpenEventHandler provideOpenEventHandler1(Context context, PersistenceFactory persistenceFactory)
    {
        // NOTE: This class extends Android SQLiteHelper 
        return new OpenEventHandler(new AndroidSqliteParams(context, HelloTwoDbsMain.PU_NAME1, persistenceFactory));
    }
  
    @Provides @Singleton @Named(HelloTwoDbsMain.PU_NAME2) 
    OpenEventHandler provideOpenEventHandler2(Context context, PersistenceFactory persistenceFactory)
    {
        // NOTE: This class extends Android SQLiteHelper 
        return new OpenEventHandler(new AndroidSqliteParams(context, HelloTwoDbsMain.PU_NAME2, persistenceFactory));
    }
  
    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory(
            @Named(HelloTwoDbsMain.PU_NAME1) OpenEventHandler openEventHandler1,
            @Named(HelloTwoDbsMain.PU_NAME2) OpenEventHandler openEventHandler2)
    {
        return new AndroidConnectionSourceFactory(openEventHandler1, openEventHandler2);
    }
    
    @Provides @Singleton PersistenceContext providePersistenceContext(
            PersistenceFactory persistenceFactory, 
            ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }

    /**
     * Returns Android Application Context
     * @return Context
     */
    @Provides @Singleton Context provideContext()
    {
        return context;
    }
}
