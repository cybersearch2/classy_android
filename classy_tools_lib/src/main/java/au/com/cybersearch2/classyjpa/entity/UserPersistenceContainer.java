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

import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;

/**
 * TODO - Create PersistenceWorkModule variant for this class
 * UserPersistenceContainer
 * Executes persistence work in a background thread, invoked from User thread
 * @author Andrew Bowley
 * 15/07/2014
 */
public class UserPersistenceContainer extends PersistenceContainer
{
  
    /**
     * Create UserPersistenceContainer object
     * @param puName Persistence Unit name
     */
    public UserPersistenceContainer(PersistenceContext persistenceContext, String puName)
    {
        super(persistenceContext, puName, true);
    }

    /**
     * Execute persistence work in a background thread, invoked from User thread
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceContainer#executeTask(au.com.cybersearch2.classyjpa.entity.PersistenceWork)
     */
    //@Override
    public Executable executeTask(PersistenceWork persistenceWork)
    {
    //    UserTaskImpl taskImpl = new UserTaskImpl(this, persistenceWork);
    //    taskImpl.getTransactionInfo().setUserTransaction(isUserTransactionMode);
    //    taskImpl.execute();
    //    return taskImpl;
        return null;
    }
    
}
