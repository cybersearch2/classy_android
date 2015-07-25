package com.example.hellotwodbs;

import java.util.Collections;
import java.util.Properties;

import javax.inject.Inject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.example.AndroidHelloTwoDbs;
import au.com.cybersearch2.example.HelloTwoDbsMain;

import com.j256.ormlite.support.ConnectionSource;

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

	private final String LOG_TAG = getClass().getSimpleName();
	/** Properties containing database version set to "2" */
    protected Properties dbV2;
    protected PropertiesListAdapter adapter;
    protected String v1Details;
    protected String v2Details;
    protected String updateDetails;
    protected TextView tv;

	@Inject AndroidHelloTwoDbs androidHelloTwoDbs;


	public HelloTwoDbs()
	{
		DI.inject(this);
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
		Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
		tv = new TextView(this);
        v1Details = doSampleDatabaseV1Stuff("onCreate");
        updateDetails = doDatabaseUpgradeV1ToV2("onCreate");
        v2Details = doSampleDatabaseV2Stuff("onCreate");
		Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
        tv.append(v1Details);
		setContentView(tv);
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
        if (androidHelloTwoDbs != null)
        	androidHelloTwoDbs.shutdown();
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
	protected String doSampleDatabaseV1Stuff(String action) 
	{
		String text = "";
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
        try
        {
        	au.com.cybersearch2.example.SimpleTask simpleTask = new au.com.cybersearch2.example.SimpleTask(action);
        	androidHelloTwoDbs.performPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
			au.com.cybersearch2.example.ComplexTask complexTask = new au.com.cybersearch2.example.ComplexTask(action);
			androidHelloTwoDbs.performPersistenceWork(HelloTwoDbsMain.PU_NAME2, complexTask);
			androidHelloTwoDbs.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully at " + System.currentTimeMillis());
			text = sb
					.append(HelloTwoDbsMain.SEPARATOR_LINE)
					.append(simpleTask.getMessage())
					.append(HelloTwoDbsMain.SEPARATOR_LINE)
					.append(complexTask.getMessage())
					.toString();
        }
        catch (InterruptedException e)
        {
        }
        return text;
	}

	/**
	 * Perform upgrade from version 1 to version 2.
	 */
	protected String doDatabaseUpgradeV1ToV2(String action) 
	{
		final StringBuilder sb = new StringBuilder();
		Runnable upgradeTask = new Runnable()
		{

			@Override
			public void run() 
			{
				PersistenceContext persistenceContext = new PersistenceContext();
				PersistenceAdmin simpleAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME1);
				PersistenceAdmin complexAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME2);
				DatabaseSupport databaseSupport = persistenceContext.getDatabaseSupport();
				ConnectionSource connectionSource1 = simpleAdmin.getConnectionSource();
				int simpleVersion = databaseSupport.getVersion(connectionSource1);
				sb
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append("Simple version = ")
				.append(simpleVersion)
				.append("\n");
				ConnectionSource connectionSource2 = complexAdmin.getConnectionSource();
				int complexVersion = databaseSupport.getVersion(connectionSource2);
				sb
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append("Complex version = ")
				.append(complexVersion)
				.append("\n");
		        // We cannot load a 2nd persistence.xml to get V2 configuration, so will 
		        // update the V1 configuration instead.
		        // We need to add the V2 entity classes and change the database version from 1 to 2.
				if (simpleVersion == 1)
				{
			        persistenceContext.registerClasses(HelloTwoDbsMain.PU_NAME1, Collections.singletonList("au.com.cybersearch2.example.v2.SimpleData"));
			        persistenceContext.putProperties(HelloTwoDbsMain.PU_NAME1, dbV2);
				}
				if (complexVersion == 1)
				{
			        persistenceContext.registerClasses(HelloTwoDbsMain.PU_NAME2, Collections.singletonList("au.com.cybersearch2.example.v2.ComplexData"));
			        persistenceContext.putProperties(HelloTwoDbsMain.PU_NAME2, dbV2);
				}
				if ((simpleVersion == 1) || (complexVersion == 1))
				{
					au.com.cybersearch2.example.v2.HelloTwoDbsMain helloTwoDbsMain_v2 = 
							new au.com.cybersearch2.example.v2.HelloTwoDbsMain();
			        // Use version of set up which does not include Dependency Injection initialization
					try 
					{
						helloTwoDbsMain_v2.setUpNoDI();
					} 
					catch (InterruptedException e) 
					{
					}
					simpleVersion = databaseSupport.getVersion(connectionSource1);
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
				}
				synchronized(this)
				{
					notifyAll();
				}
			}};
		Thread upgradeThread = new Thread(upgradeTask);
		upgradeThread.start();
        try
        {
			synchronized(upgradeTask)
			{
				upgradeTask.wait();
			}
        }
        catch (InterruptedException e)
        {
        }
        
        return sb.toString();
	}

	/**
	 * Do our sample database version 2 stuff as an example.
	 */
	protected String doSampleDatabaseV2Stuff(String action) 
	{
		String text = "";
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
        try
        {
        	au.com.cybersearch2.example.v2.SimpleTask simpleTask = 
        			new au.com.cybersearch2.example.v2.SimpleTask(action);
        	androidHelloTwoDbs.performPersistenceWork(AndroidHelloTwoDbs.PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
			au.com.cybersearch2.example.v2.ComplexTask complexTask = 
					new au.com.cybersearch2.example.v2.ComplexTask(action);
			androidHelloTwoDbs.performPersistenceWork(AndroidHelloTwoDbs.PU_NAME2, complexTask);
			androidHelloTwoDbs.logMessage(AndroidHelloTwoDbs.TAG, "Test completed successfully at " + System.currentTimeMillis());
			text = sb
					.append(AndroidHelloTwoDbs.SEPARATOR_LINE)
					.append(simpleTask.getMessage())
					.append(AndroidHelloTwoDbs.SEPARATOR_LINE)
					.append(complexTask.getMessage())
					.toString();
        }
        catch (InterruptedException e)
        {
        }
		Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
        return text;
	}

}