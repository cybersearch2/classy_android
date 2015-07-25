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
package au.com.cybersearch2.classyfy.helper;

import android.net.Uri;

/**
 * FileUtils
 * @author Andrew Bowley
 * 14/04/2014
 */
public class FileUtils
{

    public static void validateUri(Uri uri, String ...nameRegExs ) throws UnsupportedOperationException, IllegalArgumentException
    {
        if (uri == null)
            throw new IllegalArgumentException("Parameter \"uri\" is null");
        if (!"file".equals(uri.getScheme())) // TODO support "content" scheme
            throw new UnsupportedOperationException("Protocol in " + uri.toString() + " not \"file\"");
        String filename = uri.getLastPathSegment();
        int index = filename.lastIndexOf('\\');
        if (index != -1)
            filename = filename.substring(index + 1);
        if (filename == null)
            throw new IllegalArgumentException("Parameter \"uri\" has null filename");
        for (String nameRegEx: nameRegExs)
        {
            if (filename.matches("^" + nameRegEx + "$"))
                return;
        }
        throw new IllegalArgumentException("File " + filename + ": name has invalid format");
    }

}
