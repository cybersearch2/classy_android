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
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DictionaryOpenHelper
 * Open helper for the dictionary database used to perform fast text search
 * @author Andrew Bowley
 * 03/09/2014
 */
public class DictionaryOpenHelper extends SQLiteOpenHelper
{
    private static final String TAG = "DictionaryOpenHelper";
    
    /**
     * Create DictionaryOpenHelper object
     * @param context Android Application Context
     */
    public DictionaryOpenHelper(Context context)
    {   // Use FTS database parameters for super constructor
        super(context, FtsEngine.DATABASE_NAME, null, FtsEngine.DATABASE_VERSION);
    }
    
    /**
     * Called when the dictionary database is created for the first time, therefore create tables.
      * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase dictionaryDb) 
    {
        dictionaryDb.execSQL(FtsEngine.FTS_TABLE_CREATE);
    }

     /**
     * Called when the dictionary database needs to be upgraded. Drop tables, then call onCreate().
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase dictionaryDb, int oldVersion, int newVersion) 
    {
        Log.w(TAG, "Upgrading dictionary database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        dictionaryDb.execSQL("DROP TABLE IF EXISTS " + FtsEngine.FTS_VIRTUAL_TABLE);
        onCreate(dictionaryDb);
    }
}
