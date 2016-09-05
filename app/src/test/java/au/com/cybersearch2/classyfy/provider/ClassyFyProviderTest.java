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
package au.com.cybersearch2.classyfy.provider;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ContentValues;
import android.net.Uri;
import android.os.CancellationSignal;

import au.com.cybersearch2.classyfy.BuildConfig;
import au.com.cybersearch2.classyfy.ClassyFyApplication;

/**
 * ClassyFyProviderTest
 * @author Andrew Bowley
 * 24 Jun 2015
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ClassyFyProviderTest
{
    public static final String PROVIDER_AUTHORITY = "au.com.cybersearch2.classyfy.ClassyFyProvider";

    @Before
    public void setUp() throws Exception 
    {
    }

    @Test
    public void test_onCreate()
    {
        ClassyFyProvider classyFyProvider = new ClassyFyProvider();
        assertThat(classyFyProvider.onCreate()).isTrue();
        assertThat(ClassyFyApplication.getInstance().getClassyFyComponent()).isNotNull();
        classyFyProvider.shutdown();
    }
    
    @Test
    public void testPrimaryContentProvider()
    {
        ClassyFyProvider classyFyProvider = new ClassyFyProvider();
        ClassyFySearchEngine classyFySearchEngine = mock(ClassyFySearchEngine.class);
        when(classyFySearchEngine.getType(Uri.EMPTY)).thenReturn("vnd.android.cursor.dir/vnd.classyfy.node");
        classyFyProvider.classyFySearchEngine = classyFySearchEngine;
        classyFyProvider.getType(Uri.EMPTY);
        verify(classyFySearchEngine).getType(Uri.EMPTY);
        CancellationSignal cancellationSignal = mock(CancellationSignal.class);
        String[] projection = new String[]{};
        String selection = "";
        String[] selectionArgs = new String[]{};
        String sortOrder = "";
        classyFyProvider.query(Uri.EMPTY, projection , selection , selectionArgs, sortOrder , cancellationSignal);
        verify(classyFySearchEngine).query(Uri.EMPTY, projection, selection, selectionArgs, sortOrder, cancellationSignal);
        classyFyProvider.query(Uri.EMPTY, projection , selection , selectionArgs, sortOrder);
        verify(classyFySearchEngine).query(Uri.EMPTY, projection, selection, selectionArgs, sortOrder);
        ContentValues values = mock(ContentValues.class);
        classyFyProvider.insert(Uri.EMPTY, values );
        verify(classyFySearchEngine).insert(Uri.EMPTY, values );
        classyFyProvider.update(Uri.EMPTY, values, selection, selectionArgs);
        verify(classyFySearchEngine).update(Uri.EMPTY, values, selection, selectionArgs);
        classyFyProvider.delete(Uri.EMPTY, selection, selectionArgs);
        verify(classyFySearchEngine).delete(Uri.EMPTY, selection, selectionArgs);
    }
    
}
