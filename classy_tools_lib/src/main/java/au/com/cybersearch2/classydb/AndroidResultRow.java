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

import android.database.CharArrayBuffer;
import android.database.Cursor;
import au.com.cybersearch2.classyjpa.query.ResultRow;

/**
 * AndroidResultRow
 * @author Andrew Bowley
 * 02/09/2014
 */
public class AndroidResultRow implements ResultRow
{
    final protected Cursor cursor;
    
    public AndroidResultRow(Cursor cursor)
    {
        this.cursor = cursor;
    }
    
    /**
     * Returns the current position of the cursor in the row set.
     * The value is zero-based. When the row set is first returned the cursor
     * will be at positon -1, which is before the first row. After the
     * last row is returned another call to next() will leave the cursor past
     * the last entry, at a position of count().
     *
     * @return the current cursor position.
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getPosition()
     */
    @Override
    public int getPosition() {
        return cursor.getPosition();
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getColumnIndex(java.lang.String)
     */
    @Override
    public int getColumnIndex(String columnName) {
        return cursor.getColumnIndex(columnName);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        return cursor.getColumnName(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getColumnNames()
     */
    @Override
    public String[] getColumnNames() {
        return cursor.getColumnNames();
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return cursor.getColumnCount();
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getBlob(int)
     */
    @Override
    public byte[] getBlob(int columnIndex) {
        return cursor.getBlob(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getString(int)
     */
    @Override
    public String getString(int columnIndex) {
        return cursor.getString(columnIndex);
    }

    /**
     * Retrieves the requested column text and stores it in the buffer provided.
     * If the buffer size is not sufficient, a new char buffer will be allocated 
     * and assigned to CharArrayBuffer.data
     * @param columnIndex the zero-based index of the target column.
     *        if the target column is null, return buffer
     * @param buffer the buffer to copy the text into. 
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#copyStringToBuffer(int, java.lang.StringBuffer)
     */
    @Override
    public void copyStringToBuffer(int columnIndex, StringBuffer buffer) {
        CharArrayBuffer charArrayBuffer = new CharArrayBuffer(buffer.capacity());
        cursor.copyStringToBuffer(columnIndex, charArrayBuffer);
        buffer.append(charArrayBuffer.data);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getShort(int)
     */
    @Override
    public short getShort(int columnIndex) {
        return cursor.getShort(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getInt(int)
     */
    @Override
    public int getInt(int columnIndex) {
        return cursor.getInt(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getLong(int)
     */
    @Override
    public long getLong(int columnIndex) {
        return cursor.getLong(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getFloat(int)
     */
    @Override
    public float getFloat(int columnIndex) {
        return cursor.getFloat(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#getDouble(int)
     */
    @Override
    public double getDouble(int columnIndex) {
        return cursor.getDouble(columnIndex);
    }

    /**
     * @see au.com.cybersearch2.classyjpa.query.ResultRow#isNull(int)
     */
    @Override
    public boolean isNull(int columnIndex) {
        return cursor.isNull(columnIndex);
    }

}
