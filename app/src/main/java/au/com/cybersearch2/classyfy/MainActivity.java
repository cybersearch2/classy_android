/**
    Copyright (C) 2015  www.cybersearch2.com.au

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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.helper.ViewHelper;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;

/**
 * ClassyFy MainActivity
 * ClassyFy displays node details when ACTION_VIEW intent received. 
 * Fast Text Search is provided by ClassyFyProvider.
 * This activity prompts the user to navigate or search ClassyFy records.
 * @author Andrew Bowley
 * 26 Jun 2015
 */
public class MainActivity extends AppCompatActivity 
{
    public static final String TAG = "MainActivity";
    /** Error message for interrupted node search. Not expected to happen in normal operation */
    public static final String SEARCH_NOT_COMPLETED = "Record search did not complete";
    public static final String START_FAIL_MESSAGE = "ClassyFy failed to start due to unexpected error";
    /** Start state tracks appplication initialization progress */
    volatile protected StartState startState = StartState.precreate;
    
    /** Finds and formats records */
    @Inject
    ClassyfyLogic classyfyLogic;

    /**
     * onCreate
     * @see android.support.v7.app.AppCompatActivity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, "In MainActivity onCreate()");
        final ClassyFyApplication classyFyApplication = 
                ClassyFyApplication.getInstance();
        final MainActivity activity = this;
        setContentView(R.layout.activity_main);
         // Complete initialization in background
		AsyncBackgroundTask starter = new AsyncBackgroundTask(getApplication())
        {
            NodeDetailsBean nodeDetails;
            
            @Override
            public Boolean loadInBackground()
            {
                Log.i(TAG, "Loading in background...");
                startState = StartState.build;
                classyFyApplication.getClassyFyComponent().inject(activity);
                // Invoke ClassyFyProvider using ContentResolver to force initialization
                ContentResolver contentResolver = getContentResolver();
                String type = contentResolver.getType(ClassyFySearchEngine.CONTENT_URI);
                Log.i(TAG, "Search Engine initialized for content type: " + type);
                Log.i(TAG, "Getting top level record...");
                // Get first node, which is root of records tree
                nodeDetails = getNodeDetailsBean(classyFyApplication, 1);
                return nodeDetails != null;
            }

            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                Log.i(TAG, "Loading completed " + success);
                startState = success ? StartState.run : StartState.fail;
                if (success)
                    displayContent(nodeDetails);
                else
                    displayToast(START_FAIL_MESSAGE);
            }
        };
        starter.onStartLoading();
	}

    private NodeDetailsBean getNodeDetailsBean(ClassyFyApplication classyFyApplication, int nodeId)
    {   
        MainActivity activity = this;
        ClassyLogicModule classyLogicModule = 
                new ClassyLogicModule(activity, ClassyFyProvider.PU_NAME, nodeId);
        ClassyLogicComponent classyLogicComponent = 
                classyFyApplication.getClassyFyComponent().plus(classyLogicModule );
        Node data = classyLogicComponent.node();
        if (data == null)
            return null;
        // Get first node, which is root of records tree
        NodeDetailsBean nodeDetails = classyfyLogic.getNodeDetails(data);
        if ((nodeDetails == null) || nodeDetails.getCategoryTitles().isEmpty())
            return null;
        return  nodeDetails;
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
			return true;
        switch (item.getItemId()) 
        {
        case R.id.action_search:
            if (isReady())
            {
                onSearchRequested();
                return true;
            }
            else
                return false;
        default:
        }
		return super.onOptionsItemSelected(item);
	}

	private boolean isReady()
    {
	    if (startState == StartState.fail)
	        displayToast(START_FAIL_MESSAGE);
        return !startState.isStarting();
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
