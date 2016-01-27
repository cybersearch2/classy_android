package com.example.hellotwodbs.v1;

import javax.inject.Inject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import au.com.cybersearch2.android.example.AndroidHelloTwoDbs;
import au.com.cybersearch2.example.HelloTwoDbsMain;

/**
 * Sample Android UI activity which displays a text window when it is run.
 * 
 */
public class HelloTwoDbs extends Activity 
{

	private final String LOG_TAG = getClass().getSimpleName();
	@Inject
	AndroidHelloTwoDbs androidHelloTwoDbs;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		HelloTwoDbsApplication.getInstance()
		    .getHelloTwoDbsComponent()
		    .inject(this);
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

}