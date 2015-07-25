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
package au.com.cybersearch2.classyfy.test;

import static org.robolectric.util.Util.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

/**
 * AndroidEnvironmentFile
 * @author Andrew Bowley
 * 15/04/2014
 */
public class AndroidEnvironmentFile
{
    private File tempFile;
    private String filename;

    public AndroidEnvironmentFile(String projectPath, String filename) throws IOException
    {
        this.filename = filename;
        File testFile = new File(projectPath, filename);
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        tempFile = new File(path, testFile.getName());
        copyFile(testFile, tempFile);
    }

    public File getTestFile()
    {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, filename);
    }
    
    private void copyFile(File source, File dest) throws IOException
    {
        InputStream is = new FileInputStream(source);
        OutputStream os = new FileOutputStream(dest);
        copy(is, os);
        is.close();
        os.close();
    }
}
