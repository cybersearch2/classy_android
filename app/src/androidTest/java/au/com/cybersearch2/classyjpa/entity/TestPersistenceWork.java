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

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * TestPersistenceWork
 * @author Andrew Bowley
 * 28/06/2014
 */
public class TestPersistenceWork implements PersistenceWork
{
    public interface Callable
    {
        Boolean call(EntityManagerLite entityManager) throws Exception;
    }

    Transcript transcript;
    protected Callable doInBackgroundCallback;
    
    public TestPersistenceWork(Transcript transcript)
    {
        this.transcript = transcript;
    }
    
    public TestPersistenceWork(Transcript transcript, Callable doInBackgroundCallback)
    {
        this.transcript = transcript;
        this.doInBackgroundCallback = doInBackgroundCallback;
    }
    
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
        transcript.add("background task");
        if (doInBackgroundCallback != null)
            try
            {
                if (!doInBackgroundCallback.call(entityManager))
                    entityManager.getTransaction().setRollbackOnly();
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new PersistenceException("Exception thrown in doInBackground", e);
            }
    }

    @Override
    public void onPostExecute(boolean success) 
    {
        transcript.add("onPostExecute " + success);
        transcript.signal();
    }

    @Override
    public void onRollback(Throwable rollbackException) 
    {
        transcript.add("onRollback " + rollbackException.toString());
    }

    public void setCallable(Callable doInBackgroundCallback) 
    {
        this.doInBackgroundCallback = doInBackgroundCallback;
    }
}
