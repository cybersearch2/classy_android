package com.example.hellotwodbs;

import javax.inject.Inject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.example.v2.AndroidHelloTwoDbs;
import au.com.cybersearch2.example.v2.ComplexTask;
import au.com.cybersearch2.example.v2.SimpleTask;

/**
 * Sample Android UI activity which displays a text window when it is run.
 * Version 2 has new "quote" column in the database.
 */
public class HelloTwoDbs extends Activity 
{

	private final String LOG_TAG = getClass().getSimpleName();
	@Inject AndroidHelloTwoDbs androidHelloTwoDbs;


	public HelloTwoDbs()
	{
		DI.inject(this);
	}
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
		TextView tv = new TextView(this);
		tv.append(doSampleDatabaseStuff("onCreate"));
		Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
		setContentView(tv);
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
        if (androidHelloTwoDbs != null)
        	androidHelloTwoDbs.shutdown();
	}


	/**
	 * Do our sample database stuff as an example.
	 */
	protected String doSampleDatabaseStuff(String action) 
	{
		String text = "";
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
        try
        {
        	androidHelloTwoDbs.setUp();
        	SimpleTask simpleTask = new SimpleTask(action);
        	androidHelloTwoDbs.performPersistenceWork(AndroidHelloTwoDbs.PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
			ComplexTask complexTask = new ComplexTask(action);
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