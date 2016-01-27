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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.Log;

/**
 * QueryBuilder
 * Fast text search query builder
 * @author Andrew Bowley
 * 02/09/2014
 */
public class FtsQueryBuilder extends SQLiteQueryBuilder
{
    private static final String TAG = "FtsQueryBuilder";
    /** UriMatcher type */
    int queryType;
    /** Query URI received by Content Resolver */
    Uri uri;
    /** A list of which columns to return */
    String[] projection;
    /**  A filter declaring which rows to return */
    String selection;
    /** Selection arguments for "?" components in the selection */
    String[] selectionArgs;
    /** How to order the rows */
    String sortOrder;
    /** What to search for */
    String searchTerm;
    /** Optional maximum number of results to return, or unlimited if zero */
    int limit;
    /** CancellationSignal */
    CancellationSignal cancellationSignal;

    /**
     * Create an FtsQueryBuilder object 
     * @param queryType UriMatcher type
     * @param uri Query URI received by Content Resolver
     * @param projection A list of which columns to return
     * @param selection A filter declaring which rows to return
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param sortOrder How to order the rows
     */
    public FtsQueryBuilder(
            int queryType, 
            Uri uri, 
            String[] projection, 
            String selection,
            String[] selectionArgs, 
            String sortOrder)
    {
        this.queryType = queryType;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
        limit = SearchEngineBase.getLimitFromUri(uri);
        if (Log.isLoggable(TAG, Log.INFO))
        {
            StringBuilder message = new StringBuilder("Uri  = ").append(uri.toString());
            if (selection != null)
                message.append(", Selection: ").append(selection);
            if ((selectionArgs != null) && (selectionArgs.length) > 0)
                message.append(", SelectionArgs: ").append(selectionArgs[0]);
            Log.i(TAG, message.toString() );
        }
        searchTerm = "";
        if (queryType == SearchEngineBase.LEXICAL_SEARCH_SUGGEST)
        {    
            if ((selectionArgs != null) && (selectionArgs.length > 0) && (selectionArgs[0] != null))
                searchTerm = selectionArgs[0];
        }
        // Fall back if Fts not available
        // Search Suggestions support query appended with: where title like "%<search-term>%" 
        // Note: uri can have /?limit=50
        if ((queryType == SearchEngineBase.LEXICAL_SEARCH_SUGGEST) || 
            (queryType == SearchEngineBase.SEARCH_SUGGEST))          
        {
            if ((searchTerm.length() == 0) && (uri.getPathSegments().size() > 1))
                searchTerm = uri.getPathSegments().get(1);
        }
    }
    
    /**
     * Returns UriMatcher type
     * @return int
     */
    public int getQueryType() {
        return queryType;
    }
    
    /**
     * Returns Query URI received by Content Resolver
     * @return Uri
     */
    public Uri getUri() {
        return uri;
    }
    /**
     * Returns A list of which columns to return
     * @return String[]
     */
    public String[] getProjection() {
        return projection;
    }
    
    /**
     * Returns A filter declaring which rows to return
     * @return String
     */
    public String getSelection() {
        return selection;
    }
    /**
     * Returns Selection arguments for "?" components in the selection
     * @return String[]
     */
    public String[] getSelectionArgs() {
        return selectionArgs;
    }
    /**
     * Returns How to order the rows
     * @return String
     */
    public String getSortOrder() {
        return sortOrder;
    }
    /**
     * Set How to order the rows
     * @param sortOrder String formatted as an SQL
     *   ORDER BY clause (excluding the ORDER BY itself). 
     *   The default is to use the default sort order, which may be unordered.
     */
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
    /**
     * Returns What to search for
     * @return STring
     */
    public String getSearchTerm() {
        return searchTerm;
    }
  
    /**
     * Returns limit
     * @return int
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Set cancelationSignal
     * @param cancellationSignal
     */
    public void setCancellationSignal(CancellationSignal cancellationSignal)
    {
        this.cancellationSignal = cancellationSignal;
    }

    /**
     * Returns cancellation signal
     * @return
     */
    public CancellationSignal getCancellationSignal()
    {
        return cancellationSignal;
    }

    /**
     * Perform a query by combining all current settings and the
     * information passed into this method.
     * @param sqLiteDatabase The database to query on
     * @return A cursor over the result set
     */
    protected Cursor doQuery(SQLiteDatabase sqLiteDatabase)
    {
        if (limit > 0)
            return query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder, Integer.toString(limit), cancellationSignal);
        return query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder, null, cancellationSignal);
    }

    /**
     * Throw exception if cancellation notification receieved
     */
    public void throwIfCanceled()
    {
        if (cancellationSignal != null)
            cancellationSignal.throwIfCanceled();
    }
}
