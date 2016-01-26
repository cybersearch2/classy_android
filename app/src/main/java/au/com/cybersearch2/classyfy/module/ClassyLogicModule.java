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

import dagger.Module;
import dagger.Provides;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.data.NodeFinder;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
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
    
    private String puName;
    private NodeFinder nodeFinder;
    private Context context;
    
    public ClassyLogicModule(Context context, String puName, int nodeId)
    {
        this.puName = puName;
        nodeFinder = new NodeFinder(nodeId){

            @Override
            public void onRollback(Throwable rollbackException)
            {   
                displayToast("Record not available due to unexpected error");
                Log.e(TAG, "Fetch node id " + nodeId + ": failed", rollbackException);
            }
        };
    }

    @Provides Node provideNode(PersistenceContext persistenceContext)
    {
        Node node = null;
        PersistenceContainer persistenceContainer = 
                new PersistenceContainer(persistenceContext, puName, false);
        JavaPersistenceContext jpaContext = 
                persistenceContainer.getPersistenceTask(nodeFinder);
        Executable exe = jpaContext.executeInProcess();
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
