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
package au.com.cybersearch2.classyfy.provider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.BaseColumns;
import android.text.TextUtils;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfts.FtsOpenHelper;
import au.com.cybersearch2.classyfts.FtsQueryBuilder;
import au.com.cybersearch2.classyfts.SearchEngineBase;
import au.com.cybersearch2.classyfts.WordFilter;

/**
 * ClassyFySearchEngine
 * The ClassyFy ContentProvider implementation with Fast Text Search.
 * The ClassyFyContentProvider object accesses this object by ClassyFyApllication instance
 * @author Andrew Bowley
 * 11/07/2014
 */
public class ClassyFySearchEngine extends SearchEngineBase
{
    
    public static final String PROVIDER_AUTHORITY = 
            "au.com.cybersearch2.classyfy.ClassyFyProvider";
    public static final Uri CONTENT_URI = 
            Uri.parse("content://au.com.cybersearch2.classyfy.ClassyFyProvider/all_nodes");
    public static final Uri LEX_CONTENT_URI = 
            Uri.parse("content://" + PROVIDER_AUTHORITY + "/" + LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);
    public static final String ALL_NODES_VIEW = "all_nodes";
    
    // Column names
    // Android expects RowIDColumn to be "_id", so do not use any other value.
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MODEL = "model";

    // Create the constants used to differntiate between the different URI requests
    // Note values 1 - 4 are reserved for 
    // SEARCH_SUGGEST, REFRESH_SHORTCUT, LEXICAL_SEARCH_SUGGEST and LEXICAL_REFRESH_SHORTCUT
    protected static final int ALL_NODES_TYPES = PROVIDER_TYPE;
    protected static final int ALL_NODES_TYPE_ID = ALL_NODES_TYPES + 1;

    /** Search suggestions support. A Cursor must be returned with a set of pre-defined columns */
    protected final Map<String, String> ALL_NODES_TYPE_SEARCH_PROJECTION_MAP;
    /** SQLite database helper dependency accesses application persistence implementation */
    protected SQLiteOpenHelper sqLiteOpenHelper;

    /**
     * Construct ClassyFySearchEngine object
     */
    public ClassyFySearchEngine(SQLiteOpenHelper sqLiteOpenHelper, Context context, Locale  locale)
    {
        super(PROVIDER_AUTHORITY, context, locale);
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        // Add node searches to UriMatcher
        uriMatcher.addURI(PROVIDER_AUTHORITY, "all_nodes", ALL_NODES_TYPES);
        uriMatcher.addURI(PROVIDER_AUTHORITY, "all_nodes/#", ALL_NODES_TYPE_ID);
         // Projection map decouples external names from database column names
        ALL_NODES_TYPE_SEARCH_PROJECTION_MAP = createProjectionMap();
    }

    /**
     * Returns Uri matcher
     * @return UriMatcher object
     */
    protected UriMatcher getUriMatcher()
    {
        return uriMatcher;
    }

    /**
     * Returns projection map
     * @return Container mapping column name to column alias
     */
    protected Map<String, String> getProjectionMap()
    {
        return ALL_NODES_TYPE_SEARCH_PROJECTION_MAP;
    }
    
    /**
     * Returns FtsEngine customised for use with this class.
     * Once initialized by background thread, return it with call to setFtsQuery().
     * @return
     */
    public FtsEngine createFtsEngine()
    {
        WordFilter text2Filter = new WordFilter(){
            /**
             * Search result word filter 
             * @param key Database column name 
             * @param word Word from column identified by the key
             * @return Same value as "word" parameter or a replacement value
             */
            @Override
            public String filter(String key, String word) {
                if ("model".equals(key))
                    return word.replace("record", "");
                else
                    return word;
            }
        };
        Map<String,String> COLUMN_MAP = new HashMap<String,String>();
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "title");
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "model");
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "_id");
        FtsOpenHelper ftsOpenHelper = new FtsOpenHelper(context, sqLiteOpenHelper);
        FtsEngine ftsEngine = new FtsEngine(ftsOpenHelper, "all_nodes", COLUMN_MAP);
        ftsEngine.setOrderbyText2(true);
        ftsEngine.setText2Filter(text2Filter);
        ftsEngine.initialize();
        setFtsQuery(ftsEngine);
        return ftsEngine;
    }
    
    /**
     * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
     * Returns the "custom" or "vendor-specific" MIME data type of the URI given as a parameter.
     * MIME types have the format "type/subtype". The type value is always "vnd.android.cursor.dir"
     * for multiple rows, or "vnd.android.cursor.item" for a single row. 
     *
     * @param uri The URI whose MIME type is desired.
     * @return The MIME type of the URI.
     * @throws IllegalArgumentException if the incoming URI pattern is invalid.
     */
    @Override
    public String getType(Uri uri)
    {
        int queryType = uriMatcher.match(uri);
        switch (queryType)
        {
        case ALL_NODES_TYPES: 
            return "vnd.android.cursor.dir/vnd.classyfy.node";
        case ALL_NODES_TYPE_ID: 
            return "vnd.android.cursor.item/vnd.classyfy.node";
        default: 
            return super.getType(queryType, uri);
        }
    }

    /**
     * query
     * @see au.com.cybersearch2.classyapp.PrimaryContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, final String[] projection, final String selection,
            final String[] selectionArgs, final String sortOrder)
    {
        return query(uri, projection, selection, selectionArgs, sortOrder, null);
    }
    
    /**
     * Perform query with given SQL search parameters and CancellationSignal
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, android.os.CancellationSignal)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder,
            CancellationSignal cancellationSignal)
    {
        final int queryType = uriMatcher.match(uri);
        FtsQueryBuilder qb = new FtsQueryBuilder(
                queryType, 
                uri, 
                projection, 
                selection,
                selectionArgs, 
                sortOrder);
        if (cancellationSignal != null)
            qb.setCancellationSignal(cancellationSignal);
        return query(uri, qb);
    }

    /**
     * Perform query, using FTS if appropriate
     * @param uri Query Uri
     * @param qb Query builder containing parameters depending on type of query
     * @return Cursor object
     */
    protected Cursor query(Uri uri, FtsQueryBuilder qb)
    {
        qb.setTables(ALL_NODES_VIEW);
        // If this is a row query then limit the result set to the passed in row
        switch (qb.getQueryType())
        {
        case ALL_NODES_TYPE_ID: 
            if (uri.getPathSegments().size() < 2)
                throw new IllegalArgumentException("Invalid quiery Uri: " + uri.toString());
            qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1)); 
            break;
        case ALL_NODES_TYPES:
            break; 
        case LEXICAL_SEARCH_SUGGEST: // Fall back if Fts not available
        case SEARCH_SUGGEST:         // Search Suggestions support query appended with: where title like "%<search-term>%" Note: uri can have /?limit=50
        {
            qb.appendWhere(KEY_TITLE + " like \"%" + qb.getSearchTerm() + "%\"");
            qb.setProjectionMap(ALL_NODES_TYPE_SEARCH_PROJECTION_MAP);
            break;
        }          
        case REFRESH_SHORTCUT:
        case LEXICAL_REFRESH_SHORTCUT:
           // TODO
            break;
        default: break;
        }
        // If no sort order is specified, sort by title
        if (TextUtils.isEmpty(qb.getSortOrder()))
            qb.setSortOrder(KEY_NAME + " ASC");
        // Apply the query to the underlying database
        return query(qb, sqLiteOpenHelper);
    }

    /**
     * insert
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    public Uri insert(Uri uri, ContentValues values)
    {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        // Insert the new row. The call to database.insert will return the row number if it is successful
        long rowId = database.insert(ALL_NODES_VIEW, null, values);
        // Return a URI to the newly inserted row on success
        if (rowId > 0)
        {
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            notifyChange(resultUri);
            return resultUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * delete
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri))
        {
        case ALL_NODES_TYPES: 
            count = database.delete(ALL_NODES_VIEW, selection, selectionArgs);
            break;
        case ALL_NODES_TYPE_ID:
            String segment = uri.getPathSegments().get(1);
            String rowSelection = (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            count = database.delete(ALL_NODES_VIEW, KEY_ID + "=" + segment + rowSelection, selectionArgs);
            break;
        default: throw new IllegalArgumentException("Unsupported URI: " + uri);
            
        }
        notifyChange(uri);
        return count;
    }

    /**
     * update
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs)
    {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri))
        {
        case ALL_NODES_TYPES: 
            count = database.update(ALL_NODES_VIEW, values, selection, selectionArgs);
            break;
        case ALL_NODES_TYPE_ID:
            String segment = uri.getPathSegments().get(1);
            String rowSelection = (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            count = database.update(ALL_NODES_VIEW, values,
                    KEY_ID + "=" + segment + rowSelection, selectionArgs);
            break;
        default: throw new IllegalArgumentException("Unknown URI " + uri);
            
        }
        notifyChange(uri);
        return count;
    }

    /**
     * Returns container which maps names to database columns
     * @return
     */
    protected static Map<String, String> createProjectionMap() 
    {
        Map<String, String> newProjectionMap = new HashMap<String, String>();
        newProjectionMap.put(BaseColumns._ID, 
                KEY_ID + " as " + BaseColumns._ID);
        newProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, 
                KEY_TITLE + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        newProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, 
                KEY_MODEL + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2);
        newProjectionMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, 
                KEY_ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        return newProjectionMap;
    }

}
