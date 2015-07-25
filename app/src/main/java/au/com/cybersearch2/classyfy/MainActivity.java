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

import javax.inject.Inject;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import au.com.cybersearch2.classyfy.helper.ViewHelper;
import au.com.cybersearch2.classyfy.interfaces.ClassyFyLauncher;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classytask.BackgroundTask;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;

/**
 * ClassyFy MainActivity
 * ClassyFy displays node details when ACTION_VIEW intent received. 
 * Fast Text Search is provided by ClassyFyProvider.
 * This activity prompts the user to navigate or search ClassyFy records.
 * @author Andrew Bowley
 * 26 Jun 2015
 */
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity 
{
    public static final String TAG = "MainActivity";
    /** Monitor to track startup status. Used only for integraion testing */
    protected Executable startMonitor;
    /** Monitor status */
    protected WorkStatus status;
    
    @Inject /** Persistence queries to obtain record details */
    ClassyfyLogic classyfyLogic;

    /**
     * onCreate
     * @see android.support.v7.app.AppCompatActivity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final ClassyFyLauncher classyfyLauncher = (ClassyFyLauncher)getApplication();
        // Complete initialization in background
        BackgroundTask starter = new BackgroundTask(this)
        {
            NodeDetailsBean nodeDetails;
            
            /**
             * The background task
             * @see au.com.cybersearch2.classytask.BackgroundTask#loadInBackground()
             */
            @Override
            public Boolean loadInBackground()
            {
                // Wait for database initialzation started by Application object
                status = classyfyLauncher.waitForApplicationSetup();
                if (status != WorkStatus.FINISHED)
                    return Boolean.FALSE;
                // Reset status for database query
                status = WorkStatus.RUNNING;
                // Dependency injection delayed until database initialization complete
                try
                {
                    DI.inject(MainActivity.this);
                }
                catch (IllegalArgumentException e)
                {   // DI may throw this exception
                    notifyStatus(WorkStatus.FAILED);
                    Log.e(TAG, "Fatal initialization error", e);
                    return Boolean.FALSE;
                }
                // Get first node, which is root of records tree
                nodeDetails = classyfyLogic.getNodeDetails(1);
                if ((nodeDetails == null) || nodeDetails.getCategoryTitles().isEmpty())
                    return Boolean.FALSE;
                return  Boolean.TRUE;
            }

            /**
             * onLoadComplete
             * @see au.com.cybersearch2.classytask.BackgroundTask#onLoadComplete(android.support.v4.content.Loader, java.lang.Boolean)
             */
            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                if (!success)
                {
                    notifyStatus(WorkStatus.FAILED);
                    displayToast("ClassyFy failed to start due to unexpected error");
                }
                else
                {
                    // Call ClassyFyProvider to wait for SearchEngine start
                    ContentResolver contentResolver = getContentResolver();
                    contentResolver.getType(ClassyFySearchEngine.CONTENT_URI);
                    displayContent(nodeDetails);
                    notifyStatus(WorkStatus.FINISHED);
                }
            }
            protected void notifyStatus(WorkStatus finalStatus) 
            {
                status = finalStatus;
                synchronized(startMonitor)
                {
                    startMonitor.notifyAll();
                }
            }
        };
        startMonitor = new Executable(){

            @Override
            public WorkStatus getStatus()
            {
                return status;
            }};
        starter.onStartLoading();
		
	}

	/**
	 * onCreateOptionsMenu
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// Get the action view of the menu item whose id is action_search
        createSearchView(menu);
        return super.onCreateOptionsMenu(menu);
	}

	/**
	 * onOptionsItemSelected
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
        switch (item.getItemId()) 
        {
        case R.id.action_search:
            onSearchRequested();
            return true;
        default:
        }
		return super.onOptionsItemSelected(item);
	}

	/**
	 * onNewIntent
	 * @see android.support.v4.app.FragmentActivity#onNewIntent(android.content.Intent)
	 */
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);      
        setIntent(intent);
        parseIntent(intent);
    }

    /**
     * Display content - Heading and list of top categories
     * @param nodeDetails Bean containing details to be displayed
     */
    protected void displayContent(NodeDetailsBean nodeDetails)
    {
        setContentView(R.layout.activity_main);
        View categoryDetails = ViewHelper.createRecordView(
                this, 
                nodeDetails.getHeading(), 
                nodeDetails.getCategoryTitles(), 
                true, 
                new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                Intent viewIntent = new Intent(MainActivity.this, TitleSearchResultsActivity.class);
                viewIntent.setData(Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, String.valueOf(id)));
                viewIntent.setAction(Intent.ACTION_VIEW);
                startActivity(viewIntent);
                finish();
            }
        });
        LinearLayout categoryLayout = (LinearLayout) findViewById(R.id.top_category);
        categoryLayout.addView(categoryDetails);
    }

    /**
     * Parse intent - placeholder only
     * @param intent Intent object
     */
    protected void parseIntent(Intent intent)
    {
    }

    /**
     * Create search view SearchableInfo (xml/searchable.xml) and IconifiedByDefault (false)
     * @param menu Menu object
     */
    protected void createSearchView(Menu menu)
    {
        /** Get the action view of the menu item whose id is action_search */

        // Associate searchable configuration (in res/xml/searchable.xml with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        if (searchMenuItem == null)
            throw new IllegalStateException("Search menu item not found in main menu");
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        if (searchView == null)
            throw new IllegalStateException("SearchView not found in main menu");
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    }

    /**
     * Display toast
     * @param text Message
     */
    protected void displayToast(String text)
    {
        Log.e(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

}
