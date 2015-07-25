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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityTransaction;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import android.net.Uri;
import au.com.cybersearch2.classydb.SqlParser;
import au.com.cybersearch2.classydb.SqlParser.StatementCallback;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.data.DataLoader;
import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;
import au.com.cybersearch2.classyfy.helper.FileUtils;

/**
 * AlfrescoFilePlanLoader
 * @author Andrew Bowley
 * 14/04/2014
 */
public class AlfrescoFilePlanLoader implements DataLoader
{
    protected ByteArrayInputStream instream;
    protected PersistenceAdmin persistenceAdmin;
    @Inject @Named("AlfrescoFilePlan") DataStreamParser dataStreamParser;
    @Inject SqlFromNodeGenerator sqlFromNodeGenerator;
    
    public AlfrescoFilePlanLoader()
    {
        DI.inject(new AlfrescoFilePlanLoaderModule(), this);
        persistenceAdmin = new PersistenceContext().getPersistenceAdmin(ClassyFyApplication.PU_NAME);
    }
    
    @Override
    public Executable loadData(final Uri uri) throws IOException 
    {
        FileUtils.validateUri(uri, ".*\\.xml");
        File dataFile = new File(uri.getPath());
        InputStream is = new FileInputStream(dataFile);
        Node rootNode = dataStreamParser.parseDataStream(is);
        is.close();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(baos));
        sqlFromNodeGenerator.generateSql(rootNode, writer); 
        writer.flush();
        instream = new ByteArrayInputStream(baos.toByteArray());
    	// Database work is executed in a transaction
        final TransactionCallable processFilesCallable = new TransactionCallable()
        {

			@Override
			public Boolean call(final DatabaseConnection databaseConnection)
					throws Exception 
			{
		        StatementCallback callback = new StatementCallback(){
		            
		            @Override
		            public void onStatement(String statement) throws SQLException {
		                databaseConnection.executeStatement(statement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
		            }};
		            SqlParser sqlParser = new SqlParser();
		            sqlParser.parseStream(instream, callback);
		            //if (log.isLoggable(TAG, Level.FINE))
		            //    log.debug(TAG, "Executed " + sqlParser.getCount() + " statements from " + uri.toString());
		        return true;
			}
        };
        return executeTask(processFilesCallable);
    }
    
    
    protected Executable executeTask(final TransactionCallable processFilesCallable) 
    {
        final WorkTracker workTracker = new WorkTracker();
		Runnable runnable = new Runnable()
        {

			@Override
			public void run() 
			{
				try
				{
					workTracker.setStatus(WorkStatus.RUNNING);
			        // Execute task on transaction commit using Callable
			        ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
			     	EntityTransaction transaction = new EntityTransactionImpl(connectionSource, processFilesCallable);
			        transaction.begin();
			        transaction.commit();
					workTracker.setStatus(WorkStatus.FINISHED);
				}
				finally
				{
					if (workTracker.getStatus() != WorkStatus.FINISHED)
						workTracker.setStatus(WorkStatus.FAILED);
				}
				synchronized(workTracker)
				{
					workTracker.notifyAll();
				}
			}
        	
        };
        new Thread(runnable).start();
        return workTracker;
    }

}
