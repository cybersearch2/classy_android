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
package au.com.cybersearch2.classyfy.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.xmlpull.v1.XmlPullParserException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;

import android.util.Log;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.ClassyFyComponent;
import au.com.cybersearch2.classyfy.DaggerClassyFyComponent;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.module.ClassyFyApplicationModule;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;

/**
 * ClassyFyService
 * Initiializes database and processes PersistenceWork requests
 * @author Andrew Bowley
 * 10 Feb 2016
 */
public class ClassyFyService
{
    public interface ClassyfyServiceCallback
    {
        void onServiceStart(ClassyFyComponent classyFyComponent, ClassyFySearchEngine classyFySearchEngine);
        void onServiceFail(String cause);
    }
    
    public static int MAX_QUEUE_LENGTH = 16;
    public static final String TAG = "ClassyFyService";
    /** Name of query to get Category record by id */
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordCategory.ordinal();
    /** Name of query to get Folder record by id */
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordFolder.ordinal();

    private ClassyFySearchEngine classyFySearchEngine;
    private ClassyFyComponent classyFyComponent;
    private ClassyfyServiceCallback classyfyServiceCallback;
    private String errorMessage;
    
    private BlockingQueue<PersistenceWork> workQueue;
    private Thread consumeThread;
    protected PersistenceContext persistenceContext;
    protected String persistenceUnit;
    protected ConcurrentHashMap<PersistenceWork, Executable> workMap;

    /**
     * 
     */
    public ClassyFyService(ClassyfyServiceCallback classyfyServiceCallback)
    {
        this.classyfyServiceCallback = classyfyServiceCallback;
        persistenceUnit = ClassyFyProvider.PU_NAME;
        // Default error message
        errorMessage = TAG + " failed to start";
        workMap = new ConcurrentHashMap<PersistenceWork, Executable>();
        workQueue = new LinkedBlockingQueue<PersistenceWork>(MAX_QUEUE_LENGTH);
        runConsumer();
    }

    /**
     * Inserts the specified element into the service queue, waiting if necessary
     * for space to become available.
     *
     * @param persistenceWork the element to add
     * @throws InterruptedException if interrupted while waiting
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    public Executable put(PersistenceWork persistenceWork) throws InterruptedException
    {
        workQueue.put(persistenceWork);
        synchronized(persistenceWork)
        {
            persistenceWork.wait();
        }
        Executable exe = workMap.remove(persistenceWork);
        notifyTaskCompleted(exe);
        return exe;
    }

    public void shutdown()
    {
        consumeThread.interrupt();
        Iterator<PersistenceWork> iterator = workQueue.iterator();
        while (iterator.hasNext())
        {
            Executable exe = new Executable()
            {

                @Override
                public WorkStatus getStatus()
                {
                    return WorkStatus.FAILED;
                }
        
            };
            PersistenceWork persistenceWork = iterator.next();
            workMap.put(persistenceWork, exe);
            synchronized(persistenceWork)
            {
                persistenceWork.notifyAll();
            }
        }
        // Ensure Ormlite cleanup
        clearOrmlite();
    }
    
    private void runConsumer() 
    {
        Runnable comsumeTask = new Runnable()
        {
            @Override
            public void run() 
            {
                startService();
                while (true)
                {
                    try 
                    {
                        onWorkReceived(workQueue.take());
                    } 
                    catch (InterruptedException e) 
                    {
                        break;
                    }
                }
            }
        };
        consumeThread = new Thread(comsumeTask, "ClassyFyService");
        consumeThread.start();
    }

    protected void onWorkReceived(PersistenceWork persistenceWork)
    {
        PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(ClassyFyProvider.PU_NAME, false, persistenceWork);
        Executable exe = classyFyComponent.plus(persistenceWorkModule).executable();
        workMap.put(persistenceWork, exe);
        synchronized(persistenceWork)
        {
            persistenceWork.notifyAll();
        }
    }

    /**
     * Notify waiting threads after post-execute completed
     */
    protected void notifyTaskCompleted(Executable executable) 
    {
        if (executable != null)
        synchronized(executable)
        {
            executable.notifyAll();
        }
    }


    protected boolean startService()
    {
        boolean success = false;
        if (Log.isLoggable(TAG, Log.INFO))
            Log.i(TAG, "Starting Classyfy Service...");
        clearOrmlite();
        // Get perisistence context to trigger database initialization
        // Build Dagger2 configuration
        final ClassyFyApplication application = ClassyFyApplication.getInstance();
        try
        {
            classyFyComponent = 
                    DaggerClassyFyComponent.builder()
                    .classyFyApplicationModule(new ClassyFyApplicationModule(application))
                    .build();
            ResourceEnvironment resourceEnvironment = classyFyComponent.resourceEnvironment();
            Map<String, PersistenceUnitInfo> puMap = PersistenceFactory.getPersistenceUnitInfo(resourceEnvironment);
            List<String> managedClassNames = puMap.get(ClassyFyProvider.PU_NAME).getManagedClassNames();
            Log.i(TAG, managedClassNames.toString());
            PersistenceContext persistenceContext = classyFyComponent.persistenceContext();
            startApplicationSetup(persistenceContext);
            success = true;
        }
        catch (PersistenceException e)
        {
            errorMessage = e.getMessage();
            Log.e(TAG, "Database error on initialization", e);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Database error on initialization", e);
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, "Database error on initialization", e);
        }
        if (success)
        {
            classyFySearchEngine = classyFyComponent.classyFySearchEngine();
            FtsEngine ftsEngine = classyFyComponent.ftsEngine();
            classyFySearchEngine.setFtsQuery(ftsEngine);
            classyfyServiceCallback.onServiceStart(classyFyComponent, classyFySearchEngine);
        }
        else
            classyfyServiceCallback.onServiceFail(errorMessage);
        Log.i(TAG, "Start service completed " + success);
        return success;
    }

    protected void startApplicationSetup(PersistenceContext persistenceContext)
    {
        // Persistence system configured by persistence.xml contains one or more Persistence Unitst
        // Set up named queries to find Category and Folder by Node ID
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(ClassyFyProvider.PU_NAME);
        EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
        persistenceAdmin.addNamedQuery(RecordCategory.class, CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
        persistenceAdmin.addNamedQuery(RecordFolder.class, FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
    }

    private void clearOrmlite()
    {
        // From OrmLite OpenHelperManager
        /*
         * Filipe Leandro and I worked on this bug for like 10 hours straight. It's a doosey.
         *
         * Each ForeignCollection has internal DAO objects that are holding a ConnectionSource. Each Android
         * ConnectionSource is tied to a particular database connection. What Filipe was seeing was that when all of
         * his views we closed (onDestroy), but his application WAS NOT FULLY KILLED, the first View.onCreate()
         * method would open a new connection to the database. Fine. But because he application was still in memory,
         * the static BaseDaoImpl default cache had not been cleared and was containing cached objects with
         * ForeignCollections. The ForeignCollections still had references to the DAOs that had been opened with old
         * ConnectionSource objects and therefore the old database connection. Using those cached collections would
         * cause exceptions saying that you were trying to work with a database that had already been close.
         *
         * Now, whenever we create a new helper object, we must make sure that the internal object caches have been
         * fully cleared. This is a good lesson for anyone that is holding objects around after they have closed
         * connections to the database or re-created the DAOs on a different connection somehow.
         */
        BaseDaoImpl.clearAllInternalObjectCaches();
        /*
         * Might as well do this also since if the helper changes then the ConnectionSource will change so no one is
         * going to have a cache hit on the old DAOs anyway. All they are doing is holding memory.
         *
         * NOTE: we don't want to clear the config map.
         */
        DaoManager.clearDaoCache();
    }
}
