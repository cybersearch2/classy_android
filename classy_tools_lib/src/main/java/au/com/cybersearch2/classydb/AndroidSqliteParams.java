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
package au.com.cybersearch2.classydb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

/**
 * AndroidSqliteParams
 * Contains parameters to pass to SQLiteOpenHelper constructor
 * @author Andrew Bowley
 * 12 Jan 2016
 */
public class AndroidSqliteParams
{
    /** Context to use to open or create the database */
    protected Context context;
    /** Name of the database file, or null for an in-memory database */
    protected String name;
    /** Factory  to use for creating cursor objects, or null for the default */
    protected SQLiteDatabase.CursorFactory factory;
    /** Version number of the database (starting at 1); */
    protected int version;
    protected OpenHelperCallbacks openHelperCallbacks;
    protected PersistenceAdmin persistenceAdmin;
    
    public AndroidSqliteParams(Context context, String puName, PersistenceFactory persistenceFactory)
    {
        this.context = context;
        Persistence persistenceUnit = persistenceFactory.getPersistenceUnit(puName);
        DatabaseAdmin databaseAdmin = persistenceUnit.getDatabaseAdmin();
        if (databaseAdmin != null)
            openHelperCallbacks = databaseAdmin.getCustomOpenHelperCallbacks();
        if (openHelperCallbacks == null)
        {
            openHelperCallbacks = new OpenHelperCallbacksImpl(puName);
            openHelperCallbacks.setDatabaseAdmin(databaseAdmin);
            openHelperCallbacks.setPersistenceAdmin(persistenceAdmin);
        }
        persistenceAdmin = persistenceUnit.getPersistenceAdmin();
        name = persistenceAdmin.getDatabaseName();
        version = persistenceAdmin.getDatabaseVersion();
    }

    public Context getContext()
    {
        return context;
    }

    public String getName()
    {
        return name;
    }

    public SQLiteDatabase.CursorFactory getFactory()
    {
        return factory;
    }

    public int getVersion()
    {
        return version;
    }

    public OpenHelperCallbacks getOpenHelperCallbacks()
    {
        return openHelperCallbacks;
    }

    public PersistenceAdmin getPersistenceAdmin()
    {
        return persistenceAdmin;
    }

}
