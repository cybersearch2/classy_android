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

import java.util.Locale;

import android.app.SearchManager;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import au.com.cybersearch2.classyapp.PrimaryContentProvider;
import au.com.cybersearch2.classyfts.FtsQuery;
import au.com.cybersearch2.classyfts.FtsSearch;

/**
 * SearchEngineBase
 * Abstract class implementing a search suggestion dictionary. Sub-class to override onCreate() and make a concrete class.
 * @author Andrew Bowley
 * 12/07/2014
 */
public abstract class SearchEngineBase implements PrimaryContentProvider
{
       
    public static final String TAG = "SearchEngine";
    /** Keyword in search URI indicates use FTS for search suggestions */
    public static final String LEX = "lex";
    
    /** UriMatcher ID for Slow text search - for backup only */
    protected static final int SEARCH_SUGGEST = 1;
    /** UriMatcher ID for when using {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table */
    protected static final int REFRESH_SHORTCUT = 2;
    /** UriMatcher ID for Fast text search */
    protected static final int LEXICAL_SEARCH_SUGGEST = 3;
    /** UriMatcher ID for when using {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our dictionary table */
    protected static final int LEXICAL_REFRESH_SHORTCUT = 4;
    /** UriMatcher ID for content provider query */
    protected static final int PROVIDER_TYPE = 5;

    /** Provider Authority - application-specific */
    protected String providerAuthority;
    /** The fast text search query engine */
    protected FtsQuery ftsQuery;
    /** Flag to indicate FTS is supported and available */
    protected boolean isFtsAvailable;
    /** Android Application Context */
    protected Context context;
    /** Android Application Locale */
    protected Locale  locale;
    /** Map to pair Uri to content type */
    protected final UriMatcher uriMatcher;
    

    /**
     * Create SearchEngineBase object
     */
    public SearchEngineBase(String providerAuthority, Context context, Locale  locale)
    {
        this.providerAuthority = providerAuthority;
        this.context = context;
        this.locale = locale;
        uriMatcher = createUriMatcher();

        // Install FtsQuery place holder until flag isFtsAvailable is set true
        ftsQuery = new FtsQuery(){

            @Override
            public Cursor query(String selection, String[] argselectionArgs, String[] columns, int limit) {
                throw new IllegalStateException("FTS query invoked while FTS Engine not available");
            }};
    }

    /**
     * Set FtsQuery to replace default once database initialization is complete
     * @param ftsQuery
     */
    public void setFtsQuery(FtsQuery ftsQuery)
    {
        this.ftsQuery = ftsQuery;
        isFtsAvailable = true;
    }
    
    /**
     * Returns MIME type for suggestions data  
     * @param queryType UriMatcher value
     * @param uri URI matched by UriMatcher
     * @return "vnd.android.cursor.dir/vnd.android.search.suggest"
     * @throws IllegalArgumentException if UriMatcher value does not match one for a suggestion type URI
     */
    public String getType(int queryType, Uri uri)
    {
        switch (queryType)
        {
        // Search Suggestions support
        case SEARCH_SUGGEST: 
        case LEXICAL_SEARCH_SUGGEST:
            return SearchManager.SUGGEST_MIME_TYPE;
        case REFRESH_SHORTCUT: 
        case LEXICAL_REFRESH_SHORTCUT: 
            return SearchManager.SUGGEST_MIME_TYPE;
        default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
    
    /**
     * Perform a search suggestion query using FTS if supported and available
     * @param qb Populated QueryBuilder object
     * @param sqLiteOpenHelper Open helper for dictionary database
     * @return A cursor over the result set
     */
    protected Cursor query(FtsQueryBuilder qb, SQLiteOpenHelper sqLiteOpenHelper)
    {
        if ((qb.queryType == LEXICAL_SEARCH_SUGGEST) && isFtsAvailable)
        {    
            Cursor cursor = doLexicalSearch(qb.searchTerm, qb.uri);
            // Allow cancellation, if notified
            qb.throwIfCanceled();
            if (cursor != null)
                return cursor;
            // Fall back to slow text search
        }
        if ((qb.queryType == LEXICAL_SEARCH_SUGGEST) || 
            (qb.queryType == SEARCH_SUGGEST))          
        {
            // Set passed selection and selectionArgs parameters to null to match search by Uri
            qb.selection = null;
            qb.selectionArgs = null;
        }
        Cursor cursor = qb.doQuery(sqLiteOpenHelper.getWritableDatabase());
        if (cursor != null)
            cursor.setNotificationUri(context.getContentResolver(), qb.uri);
        return cursor;
    }

    /**
     * Notify registered observers that a row was updated and attempt to sync changes
     * to the network.
     * @param uri The uri of the content that was changed.
     */
    protected void notifyChange(Uri uri)
    {
        context.getContentResolver().notifyChange(uri, null);
    }

    protected Cursor doLexicalSearch(String searchTerm, Uri uri) 
    {
        Cursor cursor = getSuggestions(searchTerm, getLimitFromUri(uri));
        if (cursor != null)
            // Register the context ContentResolver to be notified if the cursor result set changes
            cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns results of query to retrieve suggestions from Fast Text Search database
     * @param searchTerm Term to search on
     * @param limit Maximum number of results
     * @return Cursor object
     */
    protected Cursor getSuggestions(String searchTerm, int limit) 
    {
        searchTerm = searchTerm.toLowerCase(locale);
        String[] columns = new String[] 
        {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
         /* SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                          (only if you want to refresh shortcuts) */
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
        FtsSearch ftsSearch = new FtsSearch(ftsQuery);
        return ftsSearch.getWordMatches(searchTerm, columns, limit);
    }
   
    /**
     * Returns limit value from Uri or 0 if absent or invalid
     * @param uri Uri
     * @return Limit value
     */
    public static int getLimitFromUri(Uri uri)
    {
        String limitText = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
        if (limitText != null)
        {
            try
            {
                return Integer.parseInt(limitText);
            }
            catch (NumberFormatException e)
            {
                Log.e(TAG, "Invalid \"" + SearchManager.SUGGEST_PARAMETER_LIMIT + "\" value of " + limitText);
            }
        }
        return 0;
    }
    
    /**
     * Returns container to map Uri to type
     * @return UriMatcher object
     */
    protected UriMatcher createUriMatcher() 
    {
        // Allocate the UriMatcher object where a URI ending is 'all_nodes' will correspond to a request for all nodes, 
        // and 'all_nodes' with a trailing '/[rowID]' will represent a single all_nodes row

        UriMatcher newUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get suggestions...
        newUriMatcher.addURI(providerAuthority, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        newUriMatcher.addURI(providerAuthority, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        newUriMatcher.addURI(providerAuthority, LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY, LEXICAL_SEARCH_SUGGEST);
        newUriMatcher.addURI(providerAuthority, LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", LEXICAL_SEARCH_SUGGEST);
        /* The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box, in which case, the following Uris would be provided and we
         * would return a cursor with a single item representing the refreshed suggestion data.
         */
        newUriMatcher.addURI(providerAuthority, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        newUriMatcher.addURI(providerAuthority, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        newUriMatcher.addURI(providerAuthority, LEX + "/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT, LEXICAL_REFRESH_SHORTCUT);
        newUriMatcher.addURI(providerAuthority, LEX + "/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", LEXICAL_REFRESH_SHORTCUT);
        return newUriMatcher;
    }

}
