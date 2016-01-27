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
package au.com.cybersearch2.classytask;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;

/**
 * AsyncBackgroundTask
 * Basic AsyncTaskLoader implementation
 * @author Andrew Bowley
 * 16 Jun 2015
 */
public class AsyncBackgroundTask extends AsyncTaskLoader<Boolean> implements OnLoadCompleteListener<Boolean>
{
    protected TaskCallback taskCallback;
    
    public interface TaskCallback
    {
        void onTaskComplete(boolean success);
    }
    
    /**
     * Construct AsyncBackgroundTask object
     * @param context Android context
     */
    public AsyncBackgroundTask(Context context)
    {
        super(context);
        // Register self as onLoadCompleteListener
        registerListener(1, this);
    }

    /**
     * Execute task in  background thread
     * Called on a worker thread to perform the actual load. 
     * @return Boolean object - Boolean.TRUE indicates successful result
     * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
     */
    @Override
    public Boolean loadInBackground()
    {
        return Boolean.TRUE;
    }

    /**
     * Handle load complete on calling thread
     * @param loader the loader that completed the load
     * @param success Boolean object - Boolean.TRUE indicates successful result
     */
    @Override
    public void onLoadComplete(Loader<Boolean> loader, Boolean success)
    {
        if (taskCallback != null)
            taskCallback.onTaskComplete(success);
    }

    /**
     * Starts an asynchronous load of the Loader's data. When the result
     * is ready the callbacks will be called on the process's main thread.
     * If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * The loader will monitor the source of
     * the data set and may deliver future callbacks if the source changes.
     * <p>Must be called from the process's main thread.
     */
    @Override
    public void onStartLoading() 
    {
        forceLoad();
    }
    
    public void start(TaskCallback taskCallback)
    {
        this.taskCallback = taskCallback;
        forceLoad();
    }
}
