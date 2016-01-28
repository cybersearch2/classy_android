/**
    Copyright (C) 2016  www.cybersearch2.com.au

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
package au.com.cybersearch2.classyfy.module;

import au.com.cybersearch2.classyfy.ClassyFyApplication;
import dagger.Module;
import dagger.Provides;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.data.NodeFinder;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * ClassyLogicModule
 * @author Andrew Bowley
 * 13 Jan 2016
 */
@Module 
public class ClassyLogicModule
{
    public static final String TAG = "ClassyLogicModule";
    
    private NodeFinder nodeFinder;
    private Context context;
    private ClassyFyApplication classyFyApplication;
    
    public ClassyLogicModule(Context context, int nodeId)
    {
        classyFyApplication = ClassyFyApplication.getInstance();
        nodeFinder = new NodeFinder(nodeId){

            @Override
            public void onRollback(Throwable rollbackException)
            {   
                displayToast("Record not available due to unexpected error");
                Log.e(TAG, "Fetch node id " + nodeId + ": failed", rollbackException);
            }
        };
    }

    @Provides Node provideNode()
    {
        Node node = null;
        Executable exe = classyFyApplication.getExecutable(nodeFinder);
        try
        {
            if (exe.waitForTask() == WorkStatus.FINISHED)
                node = nodeFinder.getNode();
        }
        catch (InterruptedException e)
        {
        }
        return node;
    }
    
    /**
     * Display toast
     * @param text Message
     */
    protected void displayToast(String text)
    {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();    
    }
}
