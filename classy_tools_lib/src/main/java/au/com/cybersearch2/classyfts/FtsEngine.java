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
//// Copyright of origin code from SearchableDictionary example project, which has only been slightly modified, 
//   principally to promote this class as a separate class:////    
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.com.cybersearch2.classyfts;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * FtsEngine
 * Fast text search engine with dictionary.
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 * @author Andrew Bowley
 * 27/04/2014
 */
public class FtsEngine implements FtsQuery
{
    /**
      * FtsStatus - dictionary states initial, loading, loaded and error
      */
    public enum FtsStatus
    {
        Initial, // Should not be visible to outside
        Loading, // ditto
        Loaded,
        Error
    }
    
    private static final String TAG = "FtsEngine";
    /** Dictionary database name */
    protected static final String DATABASE_NAME = "lexicon";
    /** Dictionary database version */
    protected static final int DATABASE_VERSION = 1;
    /** Dictionary database table */
    protected static final String FTS_VIRTUAL_TABLE = "FTSlexicon";
    /** Maps all columns that may be requested to actual columns */
    protected static final Map<String,String> COLUMN_MAP;
    /** Table from which to select words in source database */
    protected final String SOURCE_TABLE;   
    /** Map of Engine column names to source database column names */
    protected final Map<String,String> SOURCE_COLUMN_MAP;

    /** Flag indicating order by second column */
    protected boolean orderbyText2;
    /** Column 1 word filter */
    protected WordFilter text1Filter;
    /** Column 2 word filter */
    protected WordFilter text2Filter;
    /** Engine status */
    volatile FtsStatus status;
    /** SQLiteOpenHelper concrete implementation */
    protected FtsOpenHelper ftsOpenHelper;
 
    /* Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, "_id" can  be an alias for "rowid"
     */
    /** Dictionary table creation SQL statement  */
    protected static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                " USING fts3 (" +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID + "," +
                SearchManager.SUGGEST_COLUMN_TEXT_1 + ", " +
                SearchManager.SUGGEST_COLUMN_TEXT_2 + ");";

    static 
    {
        COLUMN_MAP = createColumnMap();
    }

    /**
     * Construct an FtsEngine
     * @param ftsOpenHelper SQLiteOpenHelper concrete implementation
     * @param targetTable Table from which to select words in source database
     * @param targetColumnMap Map of Engine column names to target database column names
     */
    public FtsEngine(FtsOpenHelper ftsOpenHelper, String targetTable, Map<String,String> targetColumnMap) 
    {
        this.ftsOpenHelper = ftsOpenHelper;
        this.SOURCE_TABLE = targetTable;
        this.SOURCE_COLUMN_MAP = copyMap(targetColumnMap);
        status = FtsStatus.Initial;
    }

    /**
     * Populates FTS dictionary. This is a potentially long-running operation that should run on a background thread.
     * @return FtsStatus Final status "Loaded" or "Error"
     */
    public FtsStatus initialize()
    {
        SQLiteDatabase dictionaryDb = ftsOpenHelper.getWritableDictionaryDatabase();
        if (dictionaryDb.getVersion() != DATABASE_VERSION)
            throw new IllegalStateException("Lexical Database version = " + dictionaryDb.getVersion() + "  incorrect");
        Cursor cursor = null;
        try
        {   // Query to confirm table is empty
            cursor = dictionaryDb.rawQuery("select count(*) from " + FTS_VIRTUAL_TABLE, null);
            if ((cursor != null ) && cursor.moveToFirst())
            {
                if (cursor.getInt(cursor.getColumnIndex("count(*)")) == 0)
                {
                    SQLiteDatabase database = ftsOpenHelper.getReadableDatabase();
                    status = FtsStatus.Loading;
                    status = loadDictionary(database, dictionaryDb);
                }
                else
                    status = FtsStatus.Loaded;
            }
            else
                status = FtsStatus.Error;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
        return status;
    }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @param limit Maximum number of hits to return or unlimited if zero
     * @return A Cursor over all rows matching the query
     */
    @Override
    public Cursor query(String selection, String[] selectionArgs, String[] columns, int limit) 
    {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = getSQLiteQueryBuilder();
        String orderBy = null;
        if (orderbyText2)
            orderBy = SearchManager.SUGGEST_COLUMN_TEXT_2 + " ASC, " + SearchManager.SUGGEST_COLUMN_TEXT_1 + " ASC";
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        Cursor cursor = null;
        if (limit > 0)
            cursor = builder.query(ftsOpenHelper.getReadableDictionaryDatabase(),
                    columns, selection, selectionArgs, null, null, orderBy, Integer.toString(limit));
        else
            cursor = builder.query(ftsOpenHelper.getReadableDictionaryDatabase(),
                    columns, selection, selectionArgs, null, null, orderBy);
        if (cursor == null) 
            return null;
        else if (!cursor.moveToFirst()) 
        {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Returns Engine status
     * @return FtsStatus
     */
    public FtsStatus getStatus() 
    {
        return status;
    }

    /**
     * Set flag for "order by second column" 
     * @param value boolean
     */
    public void setOrderbyText2(boolean value)
    {
        orderbyText2 = value;
    }
    
    /**
     * Set word filter for column 1
     * @param text1Filter WordFilter
     */
    public void setText1Filter(WordFilter text1Filter) 
    {
        this.text1Filter = text1Filter;
    }

    /**
     * Set word filter for column 2
     * @param text2Filter WordFilter
     */
    public void setText2Filter(WordFilter text2Filter) 
    {
        this.text2Filter = text2Filter;
    }

    /**
     * Add a word to the dictionary.
     * @param dictionaryDb Dictionary database
     * @param text1 Column 1 text
     * @param text2 Column 2 text
     * @param intentDataId Primary key of associated data row
     * @return rowId or -1 if failed
     */
    protected long addWord(SQLiteDatabase dictionaryDb, String text1, String text2, int intentDataId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, intentDataId);
        initialValues.put(SearchManager.SUGGEST_COLUMN_TEXT_1, text1);
        initialValues.put(SearchManager.SUGGEST_COLUMN_TEXT_2, text2);
    
        return dictionaryDb.insert(FTS_VIRTUAL_TABLE, null, initialValues);
    }

    /**
     * Load the database table with words. Must execute in a background thread.
     * @param database Source database
     * @param dictionaryDb Dictionary database
     * @return Engine status
     */
    protected FtsStatus loadDictionary(SQLiteDatabase database, SQLiteDatabase dictionaryDb) 
    {
        boolean success = false;

        final String SOURCE_TEXT1_COLUMN = SOURCE_COLUMN_MAP.get(SearchManager.SUGGEST_COLUMN_TEXT_1);
        final String SOURCE_TEXT2_COLUMN = SOURCE_COLUMN_MAP.get(SearchManager.SUGGEST_COLUMN_TEXT_2);
        final String SOURCE_INTENT_DATA_ID_COLUMN = SOURCE_COLUMN_MAP.get(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        
        final Cursor cursor = database.rawQuery("select " + SOURCE_INTENT_DATA_ID_COLUMN + ", " + SOURCE_TEXT1_COLUMN + ", " + SOURCE_TEXT2_COLUMN + " from " + SOURCE_TABLE, null);
        if ((cursor != null ) && cursor.moveToFirst())
        {
            if(Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "Loading words...");

            try 
            {
                do 
                {
                    int dataId = cursor.getInt(cursor.getColumnIndex(SOURCE_INTENT_DATA_ID_COLUMN));
                    String text1 = cursor.getString(cursor.getColumnIndex(SOURCE_TEXT1_COLUMN));
                    if (text1Filter != null)
                        text1 = text1Filter.filter(SOURCE_TEXT1_COLUMN, text1);
                    String text2 = cursor.getString(cursor.getColumnIndex(SOURCE_TEXT2_COLUMN));
                    if (text2Filter != null)
                        text2 = text2Filter.filter(SOURCE_TEXT2_COLUMN, text2);
                    long id = addWord(dictionaryDb, text1.trim(), text2.trim(), dataId);
                    if (id < 0) 
                    {
                        Log.e(TAG, "unable to add text: " + text1);
                        return FtsStatus.Error;
                    }
                } while (cursor.moveToNext());
                success = true;
            } 
            finally 
            {
                cursor.close();
            }
            if (success && Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "DONE loading words.");
        }
        return success ? FtsStatus.Loaded : FtsStatus.Error;
    }

    /**
     * Returns query builder with tables and projection map pre-set
     * @return SQLiteQueryBuilder
     */
    protected SQLiteQueryBuilder getSQLiteQueryBuilder()
    {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(COLUMN_MAP);
        return builder;
    }
    
    /**
     * Returns an immutable copy of source Map
     * @param sourceMap Map&lt;String, String&gt;
     * @return Map&lt;String, String&gt;sd
     */
    static protected Map<String, String> copyMap(Map<String, String> sourceMap) 
    {
        Map<String, String> destMap = new HashMap<String, String>(sourceMap);
        return Collections.unmodifiableMap(destMap);
    }

    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    static protected Map<String,String> createColumnMap()
    {
        Map<String,String> columnMap = new HashMap<String,String>();
        columnMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID); 
        columnMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_1);
        columnMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_TEXT_2);
        columnMap.put(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID + " AS " + BaseColumns._ID);
        columnMap.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID + " AS " +
                       SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return Collections.unmodifiableMap(columnMap);

    }
}
