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
package au.com.cybersearch2.classydb;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.PersistenceException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * AndroidConnectionSourceFactory
 * Creates OpenHelperConnectionSource objects which extend AndroidConnectionSource by
 * attaching an SQLiteOpenHelper implemented as an OpenEventHandler object.
 * @author Andrew Bowley
 * 10/07/2014
 */
public class AndroidConnectionSourceFactory implements ConnectionSourceFactory
{
    //private static final String TAG = "AndroidConnectionSourceFactory";
    //static Log log = JavaLogger.getLogger(TAG);
    /** The owner of this factory **/
    protected Map<String, OpenEventHandler> openEventHandlerMap;

    /**
     * Construct an AndroidConnectionSourceFactory instance
     */
    public AndroidConnectionSourceFactory(OpenEventHandler... openEventHandlerArray)
    {
        openEventHandlerMap = new HashMap<String, OpenEventHandler>();
        for (OpenEventHandler openEventHandler: openEventHandlerArray)
            openEventHandlerMap.put(openEventHandler.getDatabaseName(), openEventHandler);
    }

    /**
     * Returns an OpenHelperConnectionSource object
     * @param databaseName The name passed in the SQLiteOpenHelper constructor
     * @param properties Properties defined in persistence unit
     */
    @Override
    public OpenHelperConnectionSource getConnectionSource(final String databaseName, Properties properties)
    {
        OpenEventHandler openEventHandler = openEventHandlerMap.get(databaseName);
        if (openEventHandler == null)
            throw new PersistenceException("Database name \"" + databaseName + "\" not configured");
        // The SQLiteOpenHelper onCreate and onUpgrade overrides are delegated to the OpenHelperCallbacks implementation 
        // The OpenHelperConnectionSource object is constructed with an SQLiteDatabase object so it can
        // implement get/set dataabase version methods.
        OpenHelperConnectionSource openHelperConnectionSource = 
        		new OpenHelperConnectionSource(getSQLiteDatabase(openEventHandler), openEventHandler);
        return openHelperConnectionSource;
    }

    /**
     * Returns writeable SQLiteDatabase object
     * @param sqLiteOpenHelper SQLiteOpenHelper
     * @return SQLiteDatabase
     * @throws PersistenceException
     */
    protected SQLiteDatabase getSQLiteDatabase(SQLiteOpenHelper sqLiteOpenHelper)
    {
        SQLiteDatabase db = null;
        try 
        {
            db = sqLiteOpenHelper.getWritableDatabase();
        } 
        catch (android.database.SQLException e) 
        {
            throw new PersistenceException("Error getting a writable database from helper", e);
        }
        return db;
    }
}
