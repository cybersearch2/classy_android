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
package au.com.cybersearch2.classyfy.data.alfresco;

import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

/**
 * TestAlfrescoFilePlanLoader
 * @author Andrew Bowley
 * 28 Nov 2014
 */
public class TestAlfrescoFilePlanLoader extends AlfrescoFilePlanLoader 
{

	public TestAlfrescoFilePlanLoader(
	        PersistenceContext persistenceContext, 
	        DataStreamParser dataStreamParser,
            SqlFromNodeGenerator sqlFromNodeGenerator)
    {
        super(persistenceContext, dataStreamParser, sqlFromNodeGenerator);
    }

    public TransactionCallable processFilesCallable;
	
    protected Executable executeTask(final TransactionCallable processFilesCallable) 
    {
    	this.processFilesCallable = processFilesCallable;
        final WorkTracker workTracker = new WorkTracker();
        workTracker.setStatus(WorkStatus.FINISHED);
    	return workTracker;
    }
}
