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
package au.com.cybersearch2.classyfy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
//import android.app.SearchableInfo;

/**
 * MainActivityTest
 * @author Andrew Bowley
 * 14/05/2014
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainActivityTest
{
    public static class TestMainActivity extends MainActivity
    {
        /**
         * Bypass search action view creation
         * which Robolectric does not support because of Shadow MenuItemCompat.getActionView() bug
         * @return 
         */
        @Override
        protected void createSearchView(Menu menu)
        {
        }
        
        public void doCreateSearchView(Menu menu)
        {
            super.createSearchView(menu);
        }
    }
    
    public static final long ID = 1L;
    public static final String NAME = "name";
    public static final String TITLE = "Corporate Management";
    public static final String MODEL = "recordCategory";

    @Before
    public void setUp() 
    {
    }

    @After
    public void tearDown() 
    {
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Test
    public void test_createSearchView()
    {
        Menu menu = mock(Menu.class);
        MenuItem searchMenuItem = mock(MenuItem.class);
        SearchView searchView = mock(SearchView.class);
        MainActivity mainActivity = Robolectric.buildActivity(TestMainActivity.class).create().get();
        when(menu.findItem(R.id.action_search)).thenReturn(searchMenuItem);
        when(searchMenuItem.getActionView()).thenReturn(searchView);
        ((TestMainActivity)mainActivity).doCreateSearchView(menu);
        // Shadow SearchManager returns null for SearchablInfo
        //verify(searchView).setSearchableInfo(isA(SearchableInfo.class));
        verify(searchView).setIconifiedByDefault(false);
        
    }
    
}
