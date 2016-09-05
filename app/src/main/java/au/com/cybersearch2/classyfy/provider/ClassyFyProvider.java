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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.Log;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.ClassyFyComponent;
import au.com.cybersearch2.classyfy.service.ClassyFyService;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;


/**
 * ClassyFyProvider
 * Implements android.content.ContentProvider and binds to persistence implementation through Dagger Dependency Injection.
 * This class is only a shell delegating to a PrimaryContentProvider implementation managed by ClassyFyApplication.
 * 
 * @author Andrew Bowley
 * 12/07/2014
 * @see au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine
 * @see au.com.cybersearch2.classyfy.ClassyFyApplciation
 */
public class ClassyFyProvider extends ContentProvider
{
    /** Persistence unit name refers to peristence.xml in assets */
    public static final String PU_NAME = "classyfy";
    /** Limit maximum number of search results */
    public static final int SEARCH_RESULTS_LIMIT = 50; // Same as Android

    public static final String TAG = "ContentProvider";
    
	/** The actual ContentProvider implementation - 
	 * lazily loaded because it is available only when Application startup is completed */
    protected ClassyFySearchEngine classyFySearchEngine;
    /** Dagger2 Application Component - ClassyFy will not run unless this variable is set */
    protected ClassyFyComponent classyFyComponent;
    protected ClassyFyService classyFyService;

	/**
	 * onCreate() called before Application onCreate(), so can do nothing as DI not initialized.
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
        final ClassyFyApplication application = ClassyFyApplication.getInstance();
        classyFyService = new ClassyFyService(new ClassyFyService.ClassyfyServiceCallback()
        {
            
            @Override
            public void onServiceStart(ClassyFyComponent classyFyComponent,
                    ClassyFySearchEngine classyFySearchEngine)
            {
                ClassyFyProvider.this.classyFyComponent = classyFyComponent;
                ClassyFyProvider.this.classyFySearchEngine = classyFySearchEngine;
                application.setComponent(classyFyComponent, classyFyService);
            }
            
            @Override
            public void onServiceFail(String cause)
            {
                Log.e(TAG, cause);
                application.closeApplication();
                
            }
        });
        return true;
	}

	/**
	 * @see android.content.ContentProvider#shutdown()
	 */
	@Override
    public void shutdown() 
	{
	    classyFyService.shutdown();
        //PersistenceContext persistenceContext =
        //        classyFyComponent == null ? null : classyFyComponent.persistenceContext();
        //if (persistenceContext != null)
        //    persistenceContext.getDatabaseSupport().close();
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
        return classyFySearchEngine.getType(uri);
    }

	/**
	 * Perform query with given SQL search parameters
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	        String[] selectionArgs, String sortOrder)
	{
	    return classyFySearchEngine.query(uri, projection, selection, selectionArgs, sortOrder);
	}

    /**
     * Perform query with given SQL search parameters
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, android.os.CancellationSignal)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder,
            CancellationSignal cancellationSignal)
    {
        return classyFySearchEngine.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }
    
    /**
     * Insert content
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
	    return classyFySearchEngine.insert(uri, values);
	}

	/**
	 * Delete content
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
	    return classyFySearchEngine.delete(uri, selection, selectionArgs);
	}

	/**
	 * Update content
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
	        String[] selectionArgs)
	{
	    return classyFySearchEngine.update(uri, values, selection, selectionArgs);
	}


}
