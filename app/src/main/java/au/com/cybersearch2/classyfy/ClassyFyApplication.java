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
package au.com.cybersearch2.classyfy;

import android.app.Application;
import android.util.Log;
import au.com.cybersearch2.classyapp.PrimaryContentProvider;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.interfaces.ClassyFyLauncher;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * ClassyFyApplication
 * Launches ClassyFy persistence
 * @author Andrew Bowley
 * 19 Jun 2015
 */
public class ClassyFyApplication extends Application implements ClassyFyLauncher
{
    public static final String TAG = "ClassyFyApplication";
    /** Persistence unit name refers to peristence.xml in assets */
    public static final String PU_NAME = "classyfy";
    /** Name of query to get Category record by id */
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordCategory.ordinal();
    /** Name of query to get Folder record by id */
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordFolder.ordinal();
    /** Limit maximum number of search results */
    public static final int SEARCH_RESULTS_LIMIT = 50; // Same as Android
    
    /** Object to initialize ClassyFy persistence, on which the appliication depends */
    protected ClassyFyStartup startup;
    /** The actual ContentProvider implementation */
    protected ClassyFySearchEngine classyFySearchEngine;
    /** Singleton */
    static ClassyFyApplication singleton;
    

    /**
     * Construct ClassyFyApplication object
     */
    public ClassyFyApplication()
    {
        singleton = this;
        startup = new ClassyFyStartup();
    }
 
    /**
     * onCreate
     * @see android.app.Application#onCreate()
     */
    @Override public void onCreate() 
    {
        super.onCreate();
        startApplicationSetup();
    }

    /**
     * Wait for application setup
     * @see au.com.cybersearch2.classyfy.interfaces.ClassyFyLauncher#waitForApplicationSetup()
     */
    @Override
    public WorkStatus waitForApplicationSetup()
    {
        return startup.waitForApplicationSetup();
    }
 
    public PrimaryContentProvider getContentProvider()
    {
        return classyFySearchEngine;
    }
    
    protected void startApplicationSetup()
    {
        // Initialize ClassyFy persistence
        if (Log.isLoggable(TAG, Log.INFO))
            Log.i(TAG, "Start ClassyFy application");
        ClassyFyStartup.Callback callback = new ClassyFyStartup.Callback(){

            @Override
            public void onStartupFinished()
            {   // Start SearchEngine
                classyFySearchEngine = new ClassyFySearchEngine();
                FtsEngine ftsEngine = classyFySearchEngine.createFtsEngine();
                ftsEngine.initialize();
                classyFySearchEngine.setFtsQuery(ftsEngine);
               //     ContentResolver contentResolver  = context.getContentResolver();
               //     contentResolver.getType(ClassyFySearchEngine.CONTENT_URI);
            }
        };
        startup.start(this, callback);
    }

    public static ClassyFyApplication getInstance()
    {
        return singleton;
    }
}
