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
package au.com.cybersearch2.classyfts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * FtsOpenHelper
 * Open helper for the fast text search engine
 * @author Andrew Bowley
 * 04/07/2014
 */
public class FtsOpenHelper
{
    /** Open helper for the database being searched */
    protected SQLiteOpenHelper databaseOpenHelper;
    /** Open helper for the dictionary database */
    protected DictionaryOpenHelper dictionaryOpenHelper;
    

    /**
     * Create an FtsOpenHelper object
     * @param databaseOpenHelper Open helper for the database being searched
     */
    public FtsOpenHelper(Context context, SQLiteOpenHelper databaseOpenHelper)
    {
        this.databaseOpenHelper = databaseOpenHelper;
        dictionaryOpenHelper = new DictionaryOpenHelper(context);
    }


    /**
     * Create and/or open dictionary database that will be used for reading and writing.
     * The first time this is called, the database will be opened and
     * {@link SQLiteOpenHelper#onCreate}, {@link SQLiteOpenHelper#onUpgrade} and/or {@link SQLiteOpenHelper#onOpen} will be
     * called.
     *
     * <p>Once opened successfully, the database is cached, so you can
     * call this method every time you need to write to the database.
     * Errors such as bad permissions or a full disk may cause this method
     * to fail, but future attempts may succeed if the problem is fixed.</p>
     *
     * @return A read/write database object valid until {@link SQLiteDatabase#close} is called
     * @throws SQLiteException if the database cannot be opened for writing
     */
    public SQLiteDatabase getWritableDictionaryDatabase() 
    {
        return dictionaryOpenHelper.getWritableDatabase();
    }

    /**
     * Create and/or open a dictionary database.
     * @return a database object valid until {@link SQLiteOpenHelper#getWritableDatabase}
     *     or {@link SQLiteDatabase#close} is called.
     * @throws SQLiteException if the database cannot be opened
     */
    public SQLiteDatabase getReadableDictionaryDatabase() 
    {
        return dictionaryOpenHelper.getReadableDatabase();
    }

    /**
     * Get database to be searched - readable only for load operation
     * @return SQLiteDatabase
     */
    public SQLiteDatabase getReadableDatabase()
    {
        return databaseOpenHelper.getReadableDatabase();
    }

}
