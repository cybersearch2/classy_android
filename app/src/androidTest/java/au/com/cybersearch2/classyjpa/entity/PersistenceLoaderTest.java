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
package au.com.cybersearch2.classyjpa.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.persistence.EntityExistsException;
import android.support.test.InstrumentationRegistry;
import android.test.InstrumentationTestCase;
import android.support.test.runner.AndroidJUnit4;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.ClassyFyComponent;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * LoaderPersistenceContainerTest
 * @author Andrew Bowley
 * 16/07/2014
 */
@RunWith(AndroidJUnit4.class)
public class PersistenceLoaderTest extends InstrumentationTestCase
{
    protected PersistenceLoader testUserTransLoaderTask;
    protected ClassyFyComponent component;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        component = ClassyFyApplication.getInstance().getClassyFyComponent();
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }


    private Executable doWork(PersistenceWork persistenceWork)
    {
        PersistenceLoader testLoaderTask =
                new PersistenceLoader(getInstrumentation().getContext(), component.persistenceContext());
        return testLoaderTask.execute(ClassyFyProvider.PU_NAME, persistenceWork);
    }

    // Cannot test NPE
    // Test failed to run to completion. Reason: 'Instrumentation run failed due to 'java.lang.NullPointerException''. Check device logcat for details
    @Test
    public void test_background_called() throws Throwable
    {
    	Transcript transcript = new Transcript();
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                 exeHolder[0] = doWork(persistenceWork);
            }});
        WorkStatus status = exeHolder[0].waitForTask();
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        assertThat(status).isEqualTo(WorkStatus.FINISHED);
    }

    @Test
    public void test_rollback_only() throws Throwable
    {
    	Transcript transcript = new Transcript();
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        // Return false to cause transaction setRollbackOnly() to be called
                        // In container-managed transactions this does not cause a failure
                        return false;
                    }});
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                exeHolder[0] = doWork(persistenceWork);
            }});
        WorkStatus status = exeHolder[0].waitForTask();
        transcript.assertEventsSoFar("background task", "onPostExecute false");
        assertThat(status).isEqualTo(WorkStatus.FAILED);
    }

    @Test
    public void test_exception_thrown() throws Throwable
    {   
        final EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
    	Transcript transcript = new Transcript();
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        throw persistException;
                    }});
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                exeHolder[0] = doWork(persistenceWork);
            }});
        WorkStatus status = exeHolder[0].waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        assertThat(status).isEqualTo(WorkStatus.FAILED);
    }

}
