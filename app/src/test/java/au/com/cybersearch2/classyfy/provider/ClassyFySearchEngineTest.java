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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import au.com.cybersearch2.classyfts.FtsOpenHelper;
import au.com.cybersearch2.classyfts.FtsQuery;
import au.com.cybersearch2.classyfts.FtsQueryBuilder;
import au.com.cybersearch2.classyfy.BuildConfig;
import au.com.cybersearch2.classyfy.TestClassyFyApplication;

/**
 * ClassyFySearchEngineTest
 * @author Andrew Bowley
 * 11/07/2014
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ClassyFySearchEngineTest
{
    class TestFtsQueryBuilder extends FtsQueryBuilder
    {
        String whereClause;
        String tables;
        Map<String, String> projectionMap;
        
        public TestFtsQueryBuilder(FtsQueryBuilder ftsQueryBuilder)
        {
            super(ftsQueryBuilder.getQueryType(), ftsQueryBuilder.getUri(), ftsQueryBuilder.getProjection(), ftsQueryBuilder.getSelection(),
                    ftsQueryBuilder.getSelectionArgs(), ftsQueryBuilder.getSortOrder());
            setTables(ftsQueryBuilder.getTables());
        }
        
        @Override
        protected Cursor doQuery(SQLiteDatabase sqLiteDatabase)
        {
            return cursor;
        }
        
        @Override
        public void appendWhere(CharSequence inWhere)
        {
            this.whereClause = inWhere.toString();
            super.appendWhere(inWhere);
        }
  
        @Override
        public void setTables(String inTables) 
        {
            this.tables = inTables;
            super.setTables(inTables);
        }
        
        @Override
        public void setProjectionMap(Map<String, String> columnMap) 
        {
            this.projectionMap = columnMap;
            super.setProjectionMap(columnMap);
        }

        public String getWhereClause()
        {
            return whereClause;
        }

        @Override
        public String getTables()
        {
            return tables;
        }
        
        public Map<String, String> getProjectionMap()
        {
            return projectionMap;
        }
    }
 
    class TestClassyFySearchEngine extends ClassyFySearchEngine
    {
        public List<Uri> notifyChangeList;
        public TestFtsQueryBuilder qb;
             
        public TestClassyFySearchEngine(SQLiteOpenHelper sqLiteOpenHelper, Context context, Locale  locale)
        {
            super(sqliteOpenHelper, context, locale);
            notifyChangeList = new ArrayList<Uri>();
        }
        
        @Override
        protected void notifyChange(Uri uri)
        {
            notifyChangeList.add(uri);
        }

        @Override
        protected Cursor query(Uri uri, FtsQueryBuilder qb)
        {
            this.qb = new TestFtsQueryBuilder(qb);
            return super.query(uri, this.qb);
        }
    }
    
    public static final String PROVIDER_AUTHORITY = "au.com.cybersearch2.classyfy.ClassyFyProvider";
    // The scheme part for this provider's URI
    private static final String SCHEME = "content://";
    private static final String PATH_ALL_NODES = "/all_nodes";
    private static final String PATH_SEGMENT_LEX = "/lex/";
    private static final String ALL_NODES_SUB_TYPE = "vnd.classyfy.node";
    private static final String ALL_NODES_VIEW = "all_nodes";
    private static Context context;
    protected FtsOpenHelper ftsOpenHelper;
    protected Cursor cursor;
    protected String searchSuggestPath;
    protected boolean ftsAvailable;
    protected FtsQuery testFtsQuery;
    protected SQLiteQueryBuilder sqLiteQueryBuilder;
    protected SQLiteOpenHelper sqliteOpenHelper;
    protected Locale locale;


    @Before
    public void setup() throws Exception 
    {
        context = TestClassyFyApplication.getTestInstance();
        sqliteOpenHelper = mock(SQLiteOpenHelper.class);
        // SQLiteOpenHelper only has to handle getWritableDatabase() call
        SQLiteDatabase sQLiteDatabase = mock(SQLiteDatabase.class);
        when(sqliteOpenHelper.getWritableDatabase()).thenReturn(sQLiteDatabase);
        ftsOpenHelper = mock(FtsOpenHelper.class);
        cursor = mock(Cursor.class);
        searchSuggestPath = "lex";
        ftsAvailable = true;
        testFtsQuery = mock(FtsQuery.class);
        locale = new Locale("en", "AU");
    }

    @Test
    public void test_constructor() throws Exception
    {   
        ClassyFySearchEngine classyFySearchEngine = 
                new ClassyFySearchEngine(sqliteOpenHelper, context, locale);
        assertThat(classyFySearchEngine.sqLiteOpenHelper).isNotNull();
        UriMatcher uriMatcher = classyFySearchEngine.getUriMatcher();
        Uri searchQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES);
        assertThat(uriMatcher.match(searchQueryUri)).isEqualTo(5);
        searchQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES + "/1");
        assertThat(uriMatcher.match(searchQueryUri)).isEqualTo(6);
        assertThat(classyFySearchEngine.getProjectionMap().size()).isEqualTo(4);
     }
    
    @Test
    public void testGetType() throws Exception
    {
        ClassyFySearchEngine classyFySearchEngine = new ClassyFySearchEngine(sqliteOpenHelper, context, locale);
        Uri allNodesUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES);
        assertThat(classyFySearchEngine.getType(allNodesUri)).isEqualTo(ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ALL_NODES_SUB_TYPE);
        Uri nodeItemUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES + "/1");
        assertThat(classyFySearchEngine.getType(nodeItemUri)).isEqualTo(ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ALL_NODES_SUB_TYPE);
        Uri searchQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_QUERY);
        assertThat(classyFySearchEngine.getType(searchQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*");
        assertThat(classyFySearchEngine.getType(searchQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri searchShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        assertThat(classyFySearchEngine.getType(searchShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*");
        assertThat(classyFySearchEngine.getType(searchShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri searchLexQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_QUERY);
        assertThat(classyFySearchEngine.getType(searchLexQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchLexQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX + SearchManager.SUGGEST_URI_PATH_QUERY + "/*");
        assertThat(classyFySearchEngine.getType(searchLexQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri searchLexShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        assertThat(classyFySearchEngine.getType(searchLexShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchLexShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*");
        assertThat(classyFySearchEngine.getType(searchLexShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri invalidUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/nodes");
        try
        {
            classyFySearchEngine.getType(invalidUri);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage().contains(invalidUri.toString())).isTrue();
        }
    }
             
    @Test
    public void testQuery() throws Exception
    {
        TestClassyFySearchEngine classyFySearchEngine = new TestClassyFySearchEngine(sqliteOpenHelper, context, locale);
        // Defines a projection of column names to return for a query
        final String[] TEST_PROJECTION = {
            ClassyFySearchEngine.KEY_TITLE,
            ClassyFySearchEngine.KEY_NAME,
            ClassyFySearchEngine.KEY_MODEL
        };
        // Defines a selection column for the query. When the selection columns are passed
        // to the query, the selection arguments replace the placeholders.
        final String TITLE_SELECTION = "title = " + "?";

        // Defines the selection columns for a query.
        final String SELECTION_COLUMNS =
            TITLE_SELECTION;

         // Defines the arguments for the selection columns. Put in sort order for sort check below
        final String[] SELECTION_ARGS = { "Policy & Procedures"  };

         // Defines a query sort order
        final String SORT_ORDER = "title ASC";
        Uri allNodesUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES);

        // A query that uses selection criteria should return only those rows that match the
        // criteria. Use a projection so that it's easy to get the data in a particular column.
        Cursor projectionCursor = classyFySearchEngine.query(
            allNodesUri,               // the Uri for the main data table
            TEST_PROJECTION,           // get the title, and model columns
            SELECTION_COLUMNS,         // select on the title column
            SELECTION_ARGS,            // select titles "Note0", "Note1", or "Note5"
            SORT_ORDER                 // sort ascending on the title column
        );
        assertThat(projectionCursor).isEqualTo(cursor);
        assertThat(classyFySearchEngine.qb.getProjection()).isEqualTo(TEST_PROJECTION);
        assertThat(classyFySearchEngine.qb.getSearchTerm()).isEqualTo("");
        assertThat(classyFySearchEngine.qb.getSelection()).isEqualTo(SELECTION_COLUMNS);
        assertThat(classyFySearchEngine.qb.getSelectionArgs()).isEqualTo(SELECTION_ARGS);
        assertThat(classyFySearchEngine.qb.getSortOrder()).isEqualTo(SORT_ORDER);
        assertThat(classyFySearchEngine.qb.getWhereClause()).isNull();
        assertThat(classyFySearchEngine.qb.getTables()).isEqualTo(ALL_NODES_VIEW);
        assertThat(classyFySearchEngine.qb.getProjectionMap()).isNull();
        verify(cursor).setNotificationUri(isA(ContentResolver.class), eq(allNodesUri));
    }
    
    @Test
    public void testSuggestionLexicalQuery() throws Exception
    {
        TestClassyFySearchEngine classyFySearchEngine = new TestClassyFySearchEngine(sqliteOpenHelper, context, locale);
        FtsQuery ftsQuery = mock(FtsQuery.class);
        String selection = SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?";
        String[] columns = new String[] 
        {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
        when(ftsQuery.query(selection, new String[] {"information*"}, columns, 0)).thenReturn(cursor);
        final String SELECTION = "word MATCH ?";
        final String[] SELECTION_ARGS = { "information" };
        // Create the new Cursor Loader
        Uri suggestUri = ClassyFySearchEngine.LEX_CONTENT_URI;
        when(cursor.moveToFirst()).thenReturn(true);
        // A query that uses selection criteria should return only those rows that match the
        // criteria. Use a projection so that it's easy to get the data in a particular column.
        Cursor projectionCursor = classyFySearchEngine.query(
            suggestUri,                // the suggestion Uri
            null,                     // projection always null
            SELECTION,                 // null only when searchSuggestSelection not specified
            SELECTION_ARGS,            // ditto
            null                      // sort always null
        );
        assertThat(projectionCursor).isEqualTo(cursor);
        assertThat(classyFySearchEngine.qb.getSearchTerm()).isEqualTo("information");
    }
    
    @Test
    public void testSuggestionInUri() throws Exception
    {
        // Defines a query sort order
        final String SORT_ORDER = "name ASC";
        TestClassyFySearchEngine classyFySearchEngine = new TestClassyFySearchEngine(sqliteOpenHelper, context, locale);
        Uri suggestUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY  + "/information?limit=50");
 
        // A query that uses selection criteria should return only those rows that match the
        // criteria. Use a projection so that it's easy to get the data in a particular column.
        Cursor projectionCursor = classyFySearchEngine.query(
            suggestUri,                // the suggestion Uri
            null,                     // projection always null
            null,                     // null only when searchSuggestSelection not specified
            null,                     // ditto
            null                      // sort always null
        );
        assertThat(projectionCursor).isEqualTo(cursor);
        assertThat(classyFySearchEngine.qb.getProjection()).isNull();
        assertThat(classyFySearchEngine.qb.getSearchTerm()).isEqualTo("information");
        assertThat(classyFySearchEngine.qb.getSelection()).isNull();
        assertThat(classyFySearchEngine.qb.getSelectionArgs()).isNull();
        assertThat(classyFySearchEngine.qb.getSortOrder()).isEqualTo(SORT_ORDER);
        assertThat(classyFySearchEngine.qb.getWhereClause()).isEqualTo("title like \"%information%\"");
        assertThat(classyFySearchEngine.qb.getTables()).isEqualTo(ALL_NODES_VIEW);
        assertThat(classyFySearchEngine.qb.getProjectionMap().size()).isEqualTo(4);
        verify(cursor).setNotificationUri(isA(ContentResolver.class), eq(suggestUri));
    }
       
    @Test
    public void testSuggestionInSelectionArgQuery() throws Exception
    {
        ftsAvailable = false;
        // Defines a query sort order
        final String SORT_ORDER = "name ASC";
        final String SELECTION = "match WORD ?";
        final String[] SELECTION_ARGS = { "information" };
        TestClassyFySearchEngine classyFySearchEngine = new TestClassyFySearchEngine(sqliteOpenHelper, context, locale);
        // Uri is expected to have searchSuggestPath = "lex"
        Uri suggestUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX + SearchManager.SUGGEST_URI_PATH_QUERY  + "?limit=50");
        // A query that uses selection criteria should return only those rows that match the
        // criteria. Use a projection so that it's easy to get the data in a particular column.
        Cursor projectionCursor = classyFySearchEngine.query(
            suggestUri,                // the suggestion Uri
            null,                     // projection always null
            SELECTION,                // set when searchSuggestSelection is specified
            SELECTION_ARGS,           // ditto
            null                      // sort always null
        );
        assertThat(projectionCursor).isEqualTo(cursor);
        assertThat(classyFySearchEngine.qb.getProjection()).isNull();
        assertThat(classyFySearchEngine.qb.getSearchTerm()).isEqualTo("information");
        assertThat(classyFySearchEngine.qb.getSelection()).isNull();
        assertThat(classyFySearchEngine.qb.getSelectionArgs()).isNull();
        assertThat(classyFySearchEngine.qb.getSortOrder()).isEqualTo(SORT_ORDER);
        assertThat(classyFySearchEngine.qb.getWhereClause()).isEqualTo("title like \"%information%\"");
        assertThat(classyFySearchEngine.qb.getTables()).isEqualTo(ALL_NODES_VIEW);
        assertThat(classyFySearchEngine.qb.getProjectionMap().size()).isEqualTo(4);
        verify(cursor).setNotificationUri(isA(ContentResolver.class), eq(suggestUri));
    }
 
}
