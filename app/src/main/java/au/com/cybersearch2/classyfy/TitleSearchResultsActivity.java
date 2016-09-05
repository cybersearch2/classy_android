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


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.helper.TicketManager;
import au.com.cybersearch2.classyfy.helper.ViewHelper;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * TitleSearchResultsActivity
 * Display record lists requested by search action and record details from search action or record selection
 * @author Andrew Bowley
 * 21/04/2014
 */
public class TitleSearchResultsActivity extends FragmentActivity
{
    public static final String TAG = "TitleSearchResults";
    private static final String RECORD_NOT_FOUND = "Record not found";

    /** Refine search message displayed when too many records are retrieved by a search */
    protected String REFINE_SEARCH_MESSAGE;
    /** Progress spinner fragment */
    protected ProgressFragment progressFragment;
    private ClassyFyComponent classyFyComponent;

    @Inject /* Intent tracker */
    TicketManager ticketManager;
    @Inject
    ClassyfyLogic classyfyLogic;
    /**
     * onCreate
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        ClassyFyApplication classyFyApplication = ClassyFyApplication.getInstance();
        classyFyComponent = classyFyApplication.getClassyFyComponent();
        classyFyComponent.inject(this);
        setContentView(R.layout.results_list);
        progressFragment = getProgressFragment();
        REFINE_SEARCH_MESSAGE = this.getString(R.string.refine_search);
         // Process intent
        parseIntent(getIntent());
    }

    /**
     * onResume
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
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
     * Returns progress fragment
     * @return ProgressFragment object
     */
    protected ProgressFragment getProgressFragment()
    {
        return (ProgressFragment) getSupportFragmentManager().findFragmentById(R.id.activity_progress_fragment);
    }

    /**
     * Parse intent - ACTION_SEARCH or ACTION_VIEW
     * @param intent Intent object
     */
    protected void parseIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            if (searchQuery != null)
                launchSearch(searchQuery, ticketManager.addIntent(intent));
        }
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && (intent.getData() != null)) 
            viewUri(intent.getData(), ticketManager.addIntent(intent));
        
    }

    /**
     * View record by id contained in Uri
     * @param uri Uri appended with record id
     * @param ticket Intent tracker id
     */
    void viewUri(Uri uri, int ticket)
    {
        int nodeId = 0;
        String errorMessage = null;
        // Handles a click on a search suggestion
        if (uri.getPathSegments().size() < 2)
            errorMessage = "Invalid resource address: \"" + uri.toString() + "\"";
        else
        try
        {
            nodeId = Integer.parseInt(uri.getPathSegments().get(1)); 
        }
        catch (NumberFormatException e)
        {
                errorMessage = "Resource address has invalid ID: \"" + uri.toString() + "\"";
            }
        if (errorMessage != null) {
            showTitle(RECORD_NOT_FOUND);
            displayToast(errorMessage);
            ticketManager.removeIntent(ticket);
        }
        else
            displayNodeDetails(nodeId, ticket);
    }

    /**
     * Launch record search query
     * @param searchQuery Search string 
     * @param ticket Intent tracker id
     */
    protected void launchSearch(final String searchQuery, final int ticket)
    {
        final List<ListItem> resultList = new ArrayList<ListItem>();
        final ClassyFyApplication classyFyApplication = ClassyFyApplication.getInstance();
        AsyncBackgroundTask queryTask = new AsyncBackgroundTask(classyFyApplication)
        {
            /**
             * Execute task in  background thread
             * Called on a worker thread to perform the actual load. 
             * @return Boolean object - Boolean.TRUE indicates successful result
             * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
             */
            @Override
            public Boolean loadInBackground()
            {
                resultList.addAll(classyfyLogic.doSearchQuery(searchQuery));
                return Boolean.TRUE;
            }

            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                if (success)
                {
                    success = resultList.size() > 0;
                    if (success)
                    {
                        LinearLayout propertiesLayout = (LinearLayout) findViewById(R.id.node_properties);
                        propertiesLayout.addView(createDynamicLayout("Titles", resultList, false));
                    }
                    if (resultList.size() >= ClassyFyProvider.SEARCH_RESULTS_LIMIT)
                        displayToast(REFINE_SEARCH_MESSAGE);  
                }
                if (!success)
                {
                    showTitle(RECORD_NOT_FOUND);
                    displayToast("Search for \"" + searchQuery + "\" returned no records");
                }
                ticketManager.removeIntent(ticket);
            }
        };
        showTitle("Search: " + searchQuery);
        queryTask.onStartLoading();
    }
    
    protected void showTitle(String title)
    {
        TextView tv1 = (TextView)findViewById(R.id.node_detail_title);
        tv1.setText(title);
        LinearLayout propertiesLayout = (LinearLayout) findViewById(R.id.node_properties);
        propertiesLayout.removeAllViews();
    }
    /**
     * Display Node details in a dialog
     * @param nodeId Node id in path segment 1
     * @param ticket Intent tracker id
     */
    protected void displayNodeDetails(final int nodeId, final int ticket)
    {
        progressFragment.showSpinner();
        AsyncBackgroundTask getDetailsTask = new AsyncBackgroundTask(getApplication())
        {
            NodeDetailsBean nodeDetails;
            
            @Override
            public Boolean loadInBackground()
            {
                ClassyLogicComponent classyLogicComponent = getClassyLogicComponent(nodeId);
                nodeDetails = getNodeDetailsBean(classyLogicComponent.node());
                return nodeDetails != null ? Boolean.TRUE : Boolean.FALSE;
            }
            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                progressFragment.hideSpinner();
                String errorMessage = null;
                if (success)
                {
                    errorMessage = nodeDetails.getErrorMessage();
                    if (errorMessage == null)
                        showRecordDetails(nodeDetails);
                }
                else
                    errorMessage = ClassyfyLogic.RECORD_NOT_FOUND;
                if (errorMessage != null)
                {
                    showTitle(RECORD_NOT_FOUND);
                    displayToast(errorMessage);
                }
                ticketManager.removeIntent(ticket);
            }
        };
        getDetailsTask.onStartLoading();
    }

    protected ClassyLogicComponent getClassyLogicComponent(int nodeId)
    {
        ClassyLogicModule classyLogicModule = 
                new ClassyLogicModule(this, nodeId);
        return classyFyComponent.plus(classyLogicModule );
    }
    
    private NodeDetailsBean getNodeDetailsBean(Node node)
    {   // Use NodeFinder to perform persistence query
        if (node == null)
            return null;
        // Get first node, which is root of records tree
        NodeDetailsBean nodeDetails = classyfyLogic.getNodeDetails(node);
        // TODO - investigate why CategoryTitles is mandatory
        if ((nodeDetails == null)/* || nodeDetails.getCategoryTitles().isEmpty()*/)
            return null;
        return  nodeDetails;
    }

    /**
     * Display record details
     * @param nodeDetails NodeDetailsBean object
     */
    protected void showRecordDetails(NodeDetailsBean nodeDetails) {
        showTitle(nodeDetails.getHeading());
        LinearLayout propertiesLayout = (LinearLayout) findViewById(R.id.node_properties);
        if (nodeDetails.getHierarchy().size() > 0)
            propertiesLayout.addView(createDynamicLayout("Hierarchy", nodeDetails.getHierarchy(), true));
        if (nodeDetails.getCategoryTitles().size() > 0)
            propertiesLayout.addView(createDynamicLayout("Categories", nodeDetails.getCategoryTitles(), true));
        if (nodeDetails.getFolderTitles().size() > 0)
            propertiesLayout.addView(createDynamicLayout("Folders", nodeDetails.getFolderTitles(), true));
        propertiesLayout.addView(createDynamicLayout("Details", nodeDetails.getFieldList(), false));
    }

    /**
     * Returns view containg a title and list of items
     * @param title Title text
     * @param items ListItem list
     * @param isSingleLine flag to indicate whether to show only value or value and name
     * @return View object
     */
    protected View createDynamicLayout(String title, List<ListItem> items, boolean isSingleLine)
    {
        final TitleSearchResultsActivity myActivity = this;
        return ViewHelper.createRecordView(this, title, items, isSingleLine, 
                new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                Intent viewIntent = new Intent(myActivity.getApplicationContext(), myActivity.getClass());
                viewIntent.setAction(Intent.ACTION_VIEW);
                Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, Long.toString(id));
                viewIntent.setData(actionUri);
                displayNodeDetails((int)id, ticketManager.addIntent(viewIntent));
            }
        });
    }

    /**
     * Display toast
     * @param text Message
     */
    protected void displayToast(String text)
    {
        //Log.e(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

}
