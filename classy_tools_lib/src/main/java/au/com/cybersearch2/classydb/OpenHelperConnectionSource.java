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
// Adapted from com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
// Original copyright license:
/*
Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
granted, provided that this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
USE OR PERFORMANCE OF THIS SOFTWARE.

The author may be contacted via http://ormlite.com/ 
*/
package au.com.cybersearch2.classydb;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.android.AndroidConnectionSource;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * OpenHelperConnectionSource
 * Extends AndroidConnectionSource by attaching an 
 * SQLiteOpenHelper implemented as an OpenEventHandler object 
 * @author kevingalligan, graywatson
 * @author Andrew Bowley
 * 22/06/2014
 */
public class OpenHelperConnectionSource extends AndroidConnectionSource
{
    /** Support for optional android.os.CancellationSignal beginning with Jelly Bean onwards */
    protected boolean cancelQueriesEnabled;
    /** Flag to remember close() called */
    private volatile boolean isOpen = true;
    /** The open helper object internal to super class and otherwise inaccessible */
    protected SQLiteOpenHelper sqLiteOpenHelper;
    /** The SQLiteDatabase db in the onOpen() callback */
    protected SQLiteDatabase sqLiteDatabase;

    /**
     * Create OpenHelperConnectionSource object
     * @param sqLiteDatabase Open SQLiteDatabase object (superclass hides it's db as a private field)
     * @param sqLiteOpenHelper The open helper object made accessible for sharing with collaborators -
     * eg. to create Fast Test Search database for content provider.
     */
    public OpenHelperConnectionSource(SQLiteDatabase sqLiteDatabase, SQLiteOpenHelper sqLiteOpenHelper)
    {
        super(sqLiteDatabase);
        this.sqLiteDatabase = sqLiteDatabase;
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    /**
     * Returns database version
     * @see au.com.cybersearch2.classydb.DatabaseSupport#getVersion(com.j256.ormlite.support.ConnectionSource)
     */
	public int getVersion() 
	{
 		if (sqLiteDatabase.isOpen())
 		{
			return sqLiteDatabase.getVersion();
 		}
		DatabaseConnection connection = null;
		int version = 0;
		try 
		{
			connection = getReadWriteConnection();
			version = sqLiteDatabase.getVersion();
		} 
		catch (SQLException e) 
		{
			throw new PersistenceException(e);
		} 
		finally 
		{
			releaseConnection(connection);
		}
		return version;
	}
	
	/**
	 * Sets database version
	 * @see au.com.cybersearch2.classydb.DatabaseSupport#setVersion(int, com.j256.ormlite.support.ConnectionSource)
	 */
	public void setVersion(int version) 
	{
		if (sqLiteDatabase.isOpen())
		{
			sqLiteDatabase.setVersion(version);
			return;
		}
		DatabaseConnection connection = null;
		try 
		{
			connection = getReadWriteConnection();
			sqLiteDatabase.setVersion(version);
		} 
		catch (SQLException e) 
		{
			throw new PersistenceException(e);
		} 
		finally 
		{
			releaseConnection(connection);
		}
	}

    /**
     * Returns the SQLiteDatabase db in the onOpen callback
     * @return SQLiteDatabase
     * @throws IllegalStateException if connection is closed
     */
    protected SQLiteDatabase getDatabase()
    {
    	if (!isOpen)
    		throw new IllegalStateException("getDatabase() called when connectionSource is not open");
    	return sqLiteDatabase;
    }

    /**
     * Return true if the helper is still open. Once {@link #close()} is called then this will return false.
     */
    public boolean isOpen() 
    {
        return isOpen;
    }

    /**
     * Returns open helper
     * @return SQLiteOpenHelper
     */
    public SQLiteOpenHelper getSQLiteOpenHelper() 
    {
        return sqLiteOpenHelper;
    }

    /**
     * Close connection source
     */
    @Override
    public void close() 
    {
        super.close();
        isOpen = false;
    }

}
