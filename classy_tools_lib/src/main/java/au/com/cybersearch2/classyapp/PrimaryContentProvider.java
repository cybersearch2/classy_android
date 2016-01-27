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
package au.com.cybersearch2.classyapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

/**
 * PrimaryContentProvider
 * Interface for the primary ContentProvider methods that will be called
 * by applications. Note the ContentProvider onCreate() method needs to 
 * be implemented, but should not be exposed for applications to call.  
 * @author Andrew Bowley
 * 22 Jun 2015
 */
public interface PrimaryContentProvider
{
    /**
     * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
     * Returns the "custom" or "vendor-specific" MIME data type of the URI given as a parameter.
     * MIME types have the format "type/subtype". The type value is always "vnd.android.cursor.dir"
     * for multiple rows, or "vnd.android.cursor.item" for a single row. 
     *
     * @param uri The URI whose MIME type is desired.
     * @return The MIME type of the URI.
     * @throws IllegalArgumentException if the incoming URI pattern is invalid.
     * @see android.net.Uri
     */
     String getType(Uri uri);

     /**
      * Perform query with given SQL search parameters
      * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
      */
     Cursor query(Uri uri, 
             String[] projection, 
             String selection,
             String[] selectionArgs, 
             String sortOrder);

     /**
      * Perform query with given SQL search parameters
      * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, android.os.CancellationSignal)
      */
     Cursor query(Uri uri, 
             String[] projection, 
             String selection,
             String[] selectionArgs, 
             String sortOrder,
             CancellationSignal cancellationSignal);
     
     /**
      * Insert content
      * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
      */
     Uri insert(Uri uri, ContentValues values);

     /**
      * Delete content
      * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
      */
     int delete(Uri uri, String selection, String[] selectionArgs);

     /**
      * Update content
      * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
      */
     int update(Uri uri, 
                ContentValues values, 
                String selection,
                String[] selectionArgs);
}
