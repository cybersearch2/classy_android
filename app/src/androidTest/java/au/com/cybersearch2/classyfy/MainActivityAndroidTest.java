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

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * MainActivityAndroidTest
 * @author Andrew Bowley
 * 23/07/2014
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityAndroidTest
{
 
    private static final String[][] INF_LIST =
    {
        { "Information & Communications Technology", "Category" },
        { "Information Management", "Folder" },
        { "Information Management Policy", "Folder" }

    };

    String[] TOP_CATS = new String[]
            {
                    "Administration",
                    "Communications",
                    "Corporate Management",
                    "Customer Relations",
                    "Financial Management",
                    "Information & Communications Technology",
                    "Workforce Management"
            };


    protected Context context;
    protected Instrumentation instrumentation;
 
    @Rule
    public ActivityTestRule<MainActivity> activityRule = 
        new ActivityTestRule<MainActivity>(
            MainActivity.class,
            true,  // initialTouchMode
            true); // launchActivity

    @Before
    public void setUp() throws Exception
    {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        instrumentation = InstrumentationRegistry.getInstrumentation();
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void test_search() throws Throwable
    {
        final MainActivity mainActivity = activityRule.getActivity();
        // Block until Dagger application component is available
        ClassyFyApplication.getInstance().getClassyFyComponent();
        // Wait up to 20 seconds for start completion
        for (int i = 0; i < 20; i++)
        {
            if (mainActivity.startState == StartState.run)
                break;
            if (mainActivity.startState == StartState.fail)
                fail("MainActivity failed on start");
            Thread.sleep(1000);
        }
        assertThat(mainActivity.startState == StartState.run);
        // Check that ContentProvider is available for search operations
        ContentResolver contentResolver  = mainActivity.getContentResolver();
        Uri CONTENT_URI = 
                Uri.parse("content://au.com.cybersearch2.classyfy.ClassyFyProvider/all_nodes");
        assertThat(contentResolver.getType(CONTENT_URI)).isEqualTo("vnd.android.cursor.dir/vnd.classyfy.node");
        LinearLayout categoryLayout = (LinearLayout) mainActivity.findViewById(R.id.top_category);
        LinearLayout dynamicLayout = (LinearLayout)categoryLayout.getChildAt(0);
        LinearLayout titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        TextView titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Category: Cybersearch2 Records");
        ListView itemList = (ListView)dynamicLayout.getChildAt(1);
        ListAdapter adapter = itemList.getAdapter();
        assertThat(adapter.getCount()).isEqualTo(TOP_CATS.length);
        for (int i = 0; i < TOP_CATS.length; i++)
        {
            ListItem listItem = (ListItem)adapter.getItem(i);
            assertThat(listItem.getValue()).isEqualTo(TOP_CATS[i]);
            assertThat(listItem.getId()).isGreaterThan(1);
        }
        ActivityMonitor am2 = instrumentation.addMonitor(TitleSearchResultsActivity.class.getName(), null, false);
        onView(withId(au.com.cybersearch2.classyfy.R.id.action_search)).perform(click());
        // Can't find right expression for text view id
        //onView(withId(android.support.v7.appcompat.R.id.search_src_text)).perform(typeText("inf\n"));
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_I);
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_N); 
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_F);
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        TitleSearchResultsActivity titleSearchResultsActivity = (TitleSearchResultsActivity) instrumentation.waitForMonitorWithTimeout(am2, 5000);
        assertThat(titleSearchResultsActivity).isNotNull();
        Intent intent = titleSearchResultsActivity.getIntent();
        synchronized(intent)
        {
           intent.wait(10000);
        }
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_SEARCH);
        assertThat(intent.getStringExtra(SearchManager.QUERY)).isEqualTo("inf");
        TextView tv1 = (TextView)titleSearchResultsActivity.findViewById(R.id.node_detail_title);
        assertThat(tv1.getText()).isEqualTo("Search: inf");
        LinearLayout propertiesLayout = (LinearLayout) titleSearchResultsActivity.findViewById(R.id.node_properties);
        dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(0);
        titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Titles");
        itemList = (ListView)dynamicLayout.getChildAt(1);
        adapter = (PropertiesListAdapter)itemList.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++)
        {
            ListItem listItem = (ListItem)adapter.getItem(i);
            assertThat(INF_LIST[i][0]).isEqualTo(listItem.getName());
            assertThat(INF_LIST[i][1]).isEqualTo(listItem.getValue());
        }
    }
    
}
