package com.example.hellotwodbs.upgrade;

import java.util.Properties;

import com.example.hellotwodbs.R;
import com.j256.ormlite.support.ConnectionSource;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import au.com.cybersearch2.android.example.v2.AndroidHelloTwoDbs;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;
import au.com.cybersearch2.classytask.InternalHandler;
import au.com.cybersearch2.classytask.ResultMessage;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.example.HelloTwoDbsMain;
import au.com.cybersearch2.example.v2.ComplexTask;
import au.com.cybersearch2.example.v2.SimpleTask;

/**
 * Sample Android UI activity which displays a text window when it is run.
 * 
 * <p>
 * The persistence.xml configuration is for database version 1 only.
 * is sample begins with Hello Two Dbs version 1 activities, then
 * upgrades the databases to version 2 and runs the version 2
 * activities demonstrating the presence of a  new "quote" column
 * in both databases.
 * </p>
 * <p>
 * Once the upgrade is completed, the persistence configuration must
 * be updated at every application started until the persistence.xml
 * file (in assets folder) is updated to version 2.
 * </p>
 */
public class HelloTwoDbs extends Activity 
{
    public static final String START_FAIL_MESSAGE = "Hello Two Dbs failed to start due to unexpected error";

	private final String LOG_TAG = getClass().getSimpleName();
	/** Properties containing database version set to "2" */
    protected Properties dbV2;
    protected PropertiesListAdapter adapter;
    public String v1Details;
    public String v2Details;
    public String updateDetails;
    protected TextView tv;
    protected HelloTwoDbsMain helloTwoDbsMain_v1;

	public HelloTwoDbs()
	{
		dbV2 = new Properties();
	    dbV2.setProperty(DatabaseAdmin.DATABASE_VERSION, "2");
	}
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        v1Details = "";
        updateDetails = "";
        v2Details = "";
		Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
		tv = new TextView(this);
        AsyncBackgroundTask starter = new AsyncBackgroundTask(getApplication())
        {
            @Override
            public Boolean loadInBackground()
            {
                boolean result = false;
                if (version1Task())
                {
                    upgradeTask();
                    version2Task();
                    result = true;;
                }
                signalResultAvailable();
                return result;
            }      
            
            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                if (!success)
                    displayToast(START_FAIL_MESSAGE);
                else
                {
                    Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
                    tv.append(v1Details);
                    setContentView(tv);
                }
           }
        };
         starter.startLoading();
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        String content = null;
        switch (item.getItemId())
        {
            case R.id.menu_v1:
                content = v1Details;
                break;
            case R.id.menu_update:
                content = updateDetails;
                 break;
            case R.id.menu_v2:
                content = v2Details;
                break;
            default:
        }
        if (content != null)
        {
            tv.setText(content);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Do our sample database version 1 stuff as an example.
     */
    private boolean version1Task()
    {
        boolean success = false;
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
        try
        {
            helloTwoDbsMain_v1 = 
                    new au.com.cybersearch2.android.example.AndroidHelloTwoDbs(HelloTwoDbsApplication.getInstance());
            // Set up v1 from start which causes the database tables to be dropped and the version reset to 0
            helloTwoDbsMain_v1.setUp(true);
            au.com.cybersearch2.example.SimpleTask simpleTask = 
                    new au.com.cybersearch2.example.SimpleTask("main");
            helloTwoDbsMain_v1.performPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
            // Our string builder for building the content-view
            StringBuilder sb = new StringBuilder();
            au.com.cybersearch2.example.ComplexTask complexTask = 
                    new au.com.cybersearch2.example.ComplexTask("main");
            helloTwoDbsMain_v1.performPersistenceWork(HelloTwoDbsMain.PU_NAME2, complexTask);
            helloTwoDbsMain_v1.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully at " + System.currentTimeMillis());
            v1Details = sb
                    .append(HelloTwoDbsMain.SEPARATOR_LINE)
                    .append(simpleTask.getMessage())
                    .append(HelloTwoDbsMain.SEPARATOR_LINE)
                    .append(complexTask.getMessage())
                    .toString();
            success = true;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Perform upgrade from version 1 to version 2.
     */
    private void upgradeTask()
    {
        // We cannot load a 2nd persistence.xml to get V2 configuration, so will 
        // update the V1 configuration instead.
        // We need to add the V2 entity classes and change the database version from 1 to 2.
        PersistenceContext persistenceContext = HelloTwoDbsMain.upgradePersistenceContext(helloTwoDbsMain_v1.getPersistenceContext());
        PersistenceAdmin simpleAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME1);
        PersistenceAdmin complexAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME2);
        DatabaseSupport databaseSupport = persistenceContext.getDatabaseSupport();
        ConnectionSource connectionSource1 = simpleAdmin.getConnectionSource();
        int simpleVersion = databaseSupport.getVersion(connectionSource1);
        ConnectionSource connectionSource2 = complexAdmin.getConnectionSource();
        int complexVersion = databaseSupport.getVersion(connectionSource2);
        StringBuilder sb = new StringBuilder();
        sb
        .append(HelloTwoDbsMain.SEPARATOR_LINE)
        .append("Simple version = ")
        .append(simpleVersion)
        .append("\n");
        sb
        .append(HelloTwoDbsMain.SEPARATOR_LINE)
        .append("Complex version = ")
        .append(complexVersion)
        .append("\n");
        // Reinitialize databases using PersistenceContext
        persistenceContext.upgradeAllDatabases();
        simpleVersion = databaseSupport.getVersion(connectionSource1);
        complexVersion = databaseSupport.getVersion(connectionSource2);
        sb
        .append(HelloTwoDbsMain.SEPARATOR_LINE)
        .append("Simple version = ")
        .append(simpleVersion)
        .append("\n");
        complexVersion = databaseSupport.getVersion(connectionSource2);
        sb
        .append(HelloTwoDbsMain.SEPARATOR_LINE)
        .append("Complex version = ")
        .append(complexVersion)
        .append("\n");
        // Need to close connections to clear caches etc and ensure updates are picked up
        databaseSupport.close();
        updateDetails = sb.toString();
    }

    /**
     * Do our sample database version 2 stuff as an example.
     */
    private boolean version2Task()
    {
        boolean success = false;
        // Now it's OK to use V2 code
        AndroidHelloTwoDbs helloTwoDbsMain = null;
        try
        {
            helloTwoDbsMain = new AndroidHelloTwoDbs(HelloTwoDbsApplication.getInstance());
            System.out.println("Calling setUp() in object: " + helloTwoDbsMain);
            helloTwoDbsMain.setUp();
            SimpleTask simpleTask = new SimpleTask("main");
            helloTwoDbsMain.performPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
            // Our string builder for building the content-view
            StringBuilder sb = new StringBuilder();
            ComplexTask complexTask = new ComplexTask("main");
            helloTwoDbsMain.performPersistenceWork(HelloTwoDbsMain.PU_NAME2, complexTask);
            helloTwoDbsMain.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully page at " + System.currentTimeMillis());
            v2Details = sb
                    .append(HelloTwoDbsMain.SEPARATOR_LINE)
                    .append(simpleTask.getMessage())
                    .append(HelloTwoDbsMain.SEPARATOR_LINE)
                    .append(complexTask.getMessage())
                    .toString();
            success = true;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return success;
    }

    private void signalResultAvailable()
    {
        synchronized(this)
        {
            notifyAll();
        }
    }
    /**
     * Display toast
     * @param text Message
     */
    protected void displayToast(String text)
    {
        Log.e(LOG_TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

    public void postOnResult(InternalHandler internalHandler, ResultMessage resultMessage)
    {
    }
}