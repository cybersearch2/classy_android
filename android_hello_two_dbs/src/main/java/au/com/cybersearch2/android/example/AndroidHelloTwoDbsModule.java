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
package au.com.cybersearch2.android.example;

import javax.inject.Named;
import javax.inject.Singleton;

import android.content.Context;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.AndroidConnectionSourceFactory;
import au.com.cybersearch2.classydb.AndroidSqliteParams;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.OpenEventHandler;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.example.HelloTwoDbsMain;
import dagger.Module;
import dagger.Provides;

/**
 * AndroidHelloTwoDbsModule
 * Dependency injection data object. 
 * @see AndroidHelloTwoDbs.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module(includes = HelloTwoDbsEnvironmentModule.class) 
public class AndroidHelloTwoDbsModule
{
    private Context context;
    private AndroidHelloTwoDbs androidHelloTwoDbs;

    public AndroidHelloTwoDbsModule(Context context, AndroidHelloTwoDbs androidHelloTwoDbs)
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
