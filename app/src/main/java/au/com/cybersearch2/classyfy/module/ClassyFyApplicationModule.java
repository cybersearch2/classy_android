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

import android.content.Context;
import au.com.cybersearch2.classydb.AndroidConnectionSourceFactory;
import au.com.cybersearch2.classydb.AndroidSqliteParams;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.OpenEventHandler;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfy.ClassyfyLogic;
import au.com.cybersearch2.classyfy.helper.TicketManager;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import dagger.Module;
import dagger.Provides;

/**
 * ClassyFyApplicationModule
 * @author Andrew Bowley
 * 08/07/2014
 */
@Module(includes = ClassyFyEnvironmentModule.class)
public class ClassyFyApplicationModule
{
    private Context context;

    public ClassyFyApplicationModule(Context context)
    {
        this.context = context;
    }
    
    /**
     * Returns Android Application Context
     * @return Context
     */
    @Provides @Singleton Context provideContext()
    {
        return context;
    }

    @Provides @Singleton OpenEventHandler provideOpenEventHandler(Context context, PersistenceFactory persistenceFactory)
    {
        // NOTE: This class extends Android SQLiteHelper. OpenHelperManager not required because it is a singleton. 
        return new OpenEventHandler(new AndroidSqliteParams(context, ClassyFyProvider.PU_NAME, persistenceFactory));
    }
    
    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory(OpenEventHandler openEventHandler)
    {
        return new AndroidConnectionSourceFactory(openEventHandler);
    }
    
    @Provides @Singleton PersistenceContext providePersistenceContext(
            PersistenceFactory persistenceFactory, 
            ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }

    @Provides @Singleton TicketManager provideTicketManager()
    {
        return new TicketManager();
    }
    
    @Provides @Singleton ClassyFySearchEngine provideClassyFySearchEngine(
            PersistenceContext persistenceContext, 
            OpenEventHandler openEventHandler,
            Context context,
            Locale locale)
    {
        return new ClassyFySearchEngine(openEventHandler, context, locale);
    }
    
    @Provides @Singleton FtsEngine provideFtsEngine(ClassyFySearchEngine classyFySearchEngine)
    {
        return classyFySearchEngine.createFtsEngine();
    }
    
    @Provides @Singleton ClassyfyLogic provideClassyfyLogic(Context context)
    {
        return new ClassyfyLogic(context);
    }
 }
