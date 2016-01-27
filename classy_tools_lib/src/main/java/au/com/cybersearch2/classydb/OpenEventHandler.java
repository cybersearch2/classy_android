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
package au.com.cybersearch2.classydb;

import java.sql.SQLException;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.support.DatabaseConnection;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * OpenEventHandler - SQLiteOpenHelper for AndroidConnectionSource
 * The onCreate() and onUpgrade() methods are provided by an OpenHelperCallbacks implementation.
 * A custom OpenHelperCallbacks implementation is specified by perisistence.xml property 
 * "open-helper-callbacks-classname". If not specified, the default OpenHelperCallbacksImpl
 * class is used which calls DatabaseAdmin onCreate() and onUpgrade() methods. 
 * @author Andrew Bowley
 * 26 Nov 2014
 * @see android.database.sqlite.SQLiteOpenHelper
 * @see com.j256.ormlite.android.AndroidConnectionSource
 */
public class OpenEventHandler extends SQLiteOpenHelper
{
    /** Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods */
    protected OpenHelperCallbacks openHelperCallbacks;
    /** Support for optional android.os.CancellationSignal beginning with Jelly Bean onwards */
    protected boolean cancelQueriesEnabled;

    /**
     * Create OpenEventHandler object
     * @param openHelperCallbacks The OpenHelperCallbacks delegate for onCreate() and onUpdate() handling
     * @param context Android Context
     * @param databaseName The name passed in the SQLiteOpenHelper constructor
     * @param databaseVersion Schema version number
     */
	public OpenEventHandler(AndroidSqliteParams androidSqliteParams)
	{
		super(androidSqliteParams.getContext(),
		       androidSqliteParams.getName(),
		       androidSqliteParams.getFactory(),
		       androidSqliteParams.getVersion());
        this.openHelperCallbacks = androidSqliteParams.getOpenHelperCallbacks();
	}

    /**
     * Satisfies the {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)} interface method.
     */
	@Override
    public final void onCreate(final SQLiteDatabase db) 
    {
        /*
         * The method is called by Android database helper's get-database calls when Android detects that we need to
         * create or update the database. So we have to use the database argument and save a connection to it on the
         * AndroidConnectionSource, otherwise it will go recursive if the subclass calls getConnectionSource().
         */
    	final AndroidConnectionSource connectionSource = new AndroidConnectionSource(db);
        wrappedDatabaseOperation(db, connectionSource, new Runnable(){

            @Override
            public void run() {
                openHelperCallbacks.onCreate(connectionSource);
            }});
    }

    /**
     * Satisfies the {@link SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)} interface method.
     */
	@Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) 
    {
        /*
         * The method is called by Android database helper's get-database calls when Android detects that we need to
         * create or update the database. So we have to use the database argument and save a connection to it on the
         * AndroidConnectionSource, otherwise it will go recursive if the subclass calls getConnectionSource().
         */
       	final AndroidConnectionSource connectionSource = new AndroidConnectionSource(db);
        wrappedDatabaseOperation(db, connectionSource, new Runnable(){

            @Override
            public void run() {
                openHelperCallbacks.onUpgrade(new AndroidConnectionSource(db), oldVersion, newVersion);
            }});
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * {@link #onUpgrade} method, but is called whenever current version is newer than requested one.
     * Supported to allow database version change during testing. A script is required to support the
     * downgrade, so still under customer control as intended by design.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
	@Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
    	onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Execute a task in the context database create or update. 
     * Saves a database connection to the specified SQLiteDatabase object to avoid recursion on calls to getConnectionSource().
     * @param db SQLiteDatabase undergoing create or update task
     * @param connectionSource AndroidConnectionSource object
     * @param runnable Create or update task
     */
    protected void wrappedDatabaseOperation(SQLiteDatabase db, AndroidConnectionSource connectionSource, Runnable runnable)
    {
        DatabaseConnection conn = connectionSource.getSpecialConnection();
        boolean clearSpecial = false;
        if (conn == null) 
        {
            conn = new AndroidDatabaseConnection(db, true, cancelQueriesEnabled);
            try 
            {   // Note that multi-threading not permitted due to use of ThreadLocal variable:
                //  private ThreadLocal<NestedConnection> specialConnection = new ThreadLocal<NestedConnection>();
            	connectionSource.saveSpecialConnection(conn);
                clearSpecial = true;
            } 
            catch (SQLException e) 
            {
                throw new IllegalStateException("Could not save special connection", e);
            }
        }
        try 
        {
            runnable.run();
        } 
        finally 
        {
            if (clearSpecial) 
            	connectionSource.clearSpecialConnection(conn);
        }
    }

    /**
     * Returns flag for support of optional android.os.CancellationSignal
     * @return boolean
     */
	public boolean isCancelQueriesEnabled() 
	{
		return cancelQueriesEnabled;
	}

    /**
     * Sets flag for support of optional android.os.CancellationSignal
     * @param cancelQueriesEnabled boolean
     */
	public void setCancelQueriesEnabled(boolean cancelQueriesEnabled) 
	{
		this.cancelQueriesEnabled = cancelQueriesEnabled;
	}
    
}
