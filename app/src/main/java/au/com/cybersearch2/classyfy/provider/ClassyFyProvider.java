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

import javax.persistence.PersistenceException;

import com.j256.ormlite.dao.DaoManager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.v4.content.Loader;
import android.util.Log;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.ClassyFyComponent;
import au.com.cybersearch2.classyfy.DaggerClassyFyComponent;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.module.ClassyFyApplicationModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;


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
    /** Name of query to get Category record by id */
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordCategory.ordinal();
    /** Name of query to get Folder record by id */
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordFolder.ordinal();
    /** Limit maximum number of search results */
    public static final int SEARCH_RESULTS_LIMIT = 50; // Same as Android

    public static final String TAG = "ContentProvider";
    
	/** The actual ContentProvider implementation - 
	 * lazily loaded because it is available only when Application startup is completed */
    ClassyFySearchEngine classyFySearchEngine;
    /** Dagger2 Application Component - ClassyFy will not run unless this variable is set */
    protected ClassyFyComponent classyFyComponent;

	/**
	 * onCreate() called before Application onCreate(), so can do nothing as DI not initialized.
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
        final ClassyFyApplication application = ClassyFyApplication.getInstance();
        AsyncBackgroundTask starter = new AsyncBackgroundTask(application)
        {
            @Override
            public Boolean loadInBackground()
            {
                Log.i(TAG, "Loading in background...");
                // Get perisistence context to trigger database initialization
                // Build Dagger2 configuration
                if (Log.isLoggable(TAG, Log.INFO))
                    Log.i(TAG, "ClassyFy application Dagger build");
                DaoManager.clearCache();
                try
                {
                    classyFyComponent = 
                            DaggerClassyFyComponent.builder()
                            .classyFyApplicationModule(new ClassyFyApplicationModule(application))
                            .build();
                    startApplicationSetup(classyFyComponent.persistenceContext());
                }
                catch (PersistenceException e)
                {
                    Log.e(TAG, "Database error on initialization", e);
                    return Boolean.FALSE;
                }
                classyFySearchEngine = classyFyComponent.classyFySearchEngine();
                application.setComponent(classyFyComponent);
                return Boolean.TRUE;
            }

            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                Log.i(TAG, "Loading completed " + success);
            }
        };
        starter.startLoading();
        return true;
	}

	/**
	 * @see android.content.ContentProvider#shutdown()
	 */
	@Override
    public void shutdown() 
	{
	    classyFyComponent.persistenceContext().getDatabaseSupport().close();
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

    protected static void startApplicationSetup(PersistenceContext persistenceContext)
    {
        try
        {
            // Persistence system configured by persistence.xml contains one or more Persistence Unitst
            // Set up named queries to find Category and Folder by Node ID
            PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(PU_NAME);
            EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
            persistenceAdmin.addNamedQuery(RecordCategory.class, CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
            persistenceAdmin.addNamedQuery(RecordFolder.class, FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
        }
        catch (PersistenceException e)
        {   // All SQLExceptions are rethrown as PersistenceExceptions
            Log.e(TAG, "Database initialisation failed", e);
        }
    }

}
