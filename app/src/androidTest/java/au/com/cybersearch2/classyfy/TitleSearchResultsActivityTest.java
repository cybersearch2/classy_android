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

import android.app.Activity;
import android.app.Instrumentation;
import android.app.SearchManager;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.support.test.runner.AndroidJUnit4;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.cybersearch2.classyfy.data.NodeEntity;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * TitileSearchResultsActivityTest
 * @author Andrew Bowley
 * 24/07/2014
 */
@RunWith(AndroidJUnit4.class)
public class TitleSearchResultsActivityTest extends ActivityInstrumentationTestCase2<TitleSearchResultsActivity>
{
    class NodeField
    {
        public int id;
        public int parentId;
        public String name;
        public String title;
        public int model;
        public int level;

        public NodeField(
                int id,
                int parentId,
                String name,
                String title,
                int model,
                int level)
        {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.title = title;
            this.model = model;
            this.level = level;
        }

        NodeEntity getNodeEntity(NodeEntity parent)
        {
            NodeEntity nodeEntity = new NodeEntity();
            nodeEntity.set_id(id);
            nodeEntity.set_parent_id(parentId);
            nodeEntity.setLevel(level);
            nodeEntity.setModel(model);
            nodeEntity.setName(name);
            nodeEntity.setParent(parent);
            nodeEntity.setTitle(title);
            return nodeEntity;
        }
    }

    NodeField[] NODE_FIELDS = new NodeField[]
            {
                    new NodeField(1,1,"cybersearch2_records","Cybersearch2 Records",1,1),
                    new NodeField(2,1,"administration","Administration",1,2),
                    new NodeField(3,2,"premises","Premises",1,3),
                    new NodeField(4,3,"maintenance","Maintenance",2,4),
                    new NodeField(5,3,"rent","Rent",2,4)
            };
    private static final String[][] RECORD_DETAILS_ARRAY =
    {
        { "description", "" },
        { "created", "2014-02-12 10:58:00.000000" },
        { "creator", "admin" },
        { "modified", "2014-02-12 11:28:35.000000" },
        { "modifier", "admin" },
        { "identifier", "2014-1392163053802" }
    };
 

    /**
     * 
     */
    public TitleSearchResultsActivityTest()
    {
        super(TitleSearchResultsActivity.class);
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        // Block until Dagger application component is available
        ClassyFyApplication.getInstance().getClassyFyComponent();
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    /*
        @Test
        public void test_parseIntent_action_view() throws Throwable
        {
            Intent intent = getNewIntent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "3");
            intent.setData(actionUri);
            setActivityIntent(intent);
            final TitleSearchResultsActivity activity = getActivity();
            synchronized(intent)
            {
                intent.wait(10000);
            }
            ProgressBar spinner = activity.progressFragment.getSpinner();
            assertThat(spinner).isNotNull();
            assertThat(spinner.getVisibility()).isEqualTo(View.GONE);
            TextView tv1 = (TextView)activity.findViewById(R.id.node_detail_title);
            assertThat(tv1.getText()).isEqualTo("Category: " + NODE_FIELDS[2].title);
            LinearLayout propertiesLayout = (LinearLayout)activity.findViewById(R.id.node_properties);
            LinearLayout dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(0);
            LinearLayout titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
            TextView titleView = (TextView) titleLayout.getChildAt(0);
            assertThat(titleView.getText()).isEqualTo("Hierarchy");
            ListView itemList = (ListView)dynamicLayout.getChildAt(1);
            ListAdapter adapter = (ListAdapter)itemList.getAdapter();
            assertThat(adapter.getCount()).isEqualTo(2);
            ListItem listItem = (ListItem)adapter.getItem(0);
            assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[0].id);
            assertThat(listItem.getValue()).isEqualTo(NODE_FIELDS[0].title);
            listItem = (ListItem)adapter.getItem(1);
            assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[1].id);
            assertThat(listItem.getValue()).isEqualTo(NODE_FIELDS[1].title);
            dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(1);
            titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
            titleView = (TextView) titleLayout.getChildAt(0);
            assertThat(titleView.getText()).isEqualTo("Folders");
            itemList = (ListView)dynamicLayout.getChildAt(1);
            adapter = itemList.getAdapter();
            assertThat(adapter.getCount()).isEqualTo(2);
            listItem = (ListItem)adapter.getItem(0);
            assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[3].id);
            assertThat(listItem.getValue()).isEqualTo(NODE_FIELDS[3].title);
            listItem = (ListItem)adapter.getItem(1);
            assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[4].id);
            assertThat(listItem.getValue()).isEqualTo(NODE_FIELDS[4].title);
            dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(2);
            titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
            titleView = (TextView) titleLayout.getChildAt(0);
            assertThat(titleView.getText()).isEqualTo("Details");
            itemList = (ListView)dynamicLayout.getChildAt(1);
            adapter = itemList.getAdapter();
            assertThat(adapter.getCount()).isEqualTo(RECORD_DETAILS_ARRAY.length);
            for (int i = 0; i < RECORD_DETAILS_ARRAY.length; i++)
            {
                ListItem item = (ListItem)adapter.getItem(i);
                assertThat(item.getName().equals(RECORD_DETAILS_ARRAY[i][0]));
                assertThat(item.getValue().equals(RECORD_DETAILS_ARRAY[i][1]));
            }
        }
/* Following two tests fail with exception originating from the Kernal.
   Test search for "~" on application worked correctly = "No record found",
   so is likely an emulation/instrumentation issue.
 */
 /*
    @Test
    public void test_action_view_no_nodeid() throws Throwable
    {
        do_action_view_bad_url(ClassyFySearchEngine.CONTENT_URI);
    }

    @Test
    public void test_action_view_invalid_nodeid() throws Throwable
    {
        Uri uri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "NaN");
        do_action_view_bad_url(uri);
    }

    @Test
    public void test_action_view_out_of_range_nodeid() throws Throwable
    {
        Uri uri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, String.valueOf(Integer.MAX_VALUE));
        do_action_view_bad_url(uri);
    }
*/
    @Test
    public void test_action_search_fail() throws Throwable
    {
        do_action_search_fail("xyz");
    }

    protected void do_action_view_bad_url(Uri uri) throws Throwable
    {
        final Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        //intent.putExtra(SearchManager.QUERY, Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, ).toString());
        setActivityIntent(intent);
        final TitleSearchResultsActivity activity = getActivity();
        runTestOnUiThread(new Runnable() {
            public void run() {
                TextView tv1 = (TextView) activity.findViewById(R.id.node_detail_title);
                // Populate fields to test they are cleared when the error occurs
                tv1.setText("Category: Premises");
                LinearLayout propertiesLayout = (LinearLayout) activity.findViewById(R.id.node_properties);
                propertiesLayout.addView(createDynamicLayout("Hierarchy", activity));
                propertiesLayout.addView(createDynamicLayout("Categories", activity));
            }
        });
        intent.setData(uri);
        runTestOnUiThread(new Runnable() {
            public void run() {
                activity.onNewIntent(intent);
            }});
        synchronized (intent) {
            intent.wait(10000);
        }
        runTestOnUiThread(new Runnable() {
            public void run() {
		        ProgressBar spinner = activity.progressFragment.getSpinner();
		        assertThat(spinner).isNotNull();
		        assertThat(spinner.getVisibility()).isEqualTo(View.GONE);
		        TextView tv1 = (TextView) activity.findViewById(R.id.node_detail_title);
		        assertThat(tv1.getText()).isEqualTo("Record not found");
		        LinearLayout propertiesLayout = (LinearLayout) activity.findViewById(R.id.node_properties);
		        assertThat(propertiesLayout.getChildCount()).isEqualTo(0);
            }});
    }

    protected void do_action_search_fail(String searchQuery) throws Throwable {
        final Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        setActivityIntent(intent);
        final TitleSearchResultsActivity activity = getActivity();
        runTestOnUiThread(new Runnable() {
            public void run() {
                TextView tv1 = (TextView) activity.findViewById(R.id.node_detail_title);
                // Populate fields to test they are cleared when the error occurs
                tv1.setText("Category: Premises");
                LinearLayout propertiesLayout = (LinearLayout) activity.findViewById(R.id.node_properties);
                propertiesLayout.addView(createDynamicLayout("Hierarchy", activity));
                propertiesLayout.addView(createDynamicLayout("Categories", activity));
            }
        });
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchQuery);
        runTestOnUiThread(new Runnable() {
            public void run() {
                activity.onNewIntent(intent);
            }
        });
        synchronized (intent) {
            intent.wait(10000);
        }
        runTestOnUiThread(new Runnable() {
            public void run() {
		        ProgressBar spinner = activity.progressFragment.getSpinner();
		        assertThat(spinner).isNotNull();
		        assertThat(spinner.getVisibility()).isEqualTo(View.GONE);
		        TextView tv1 = (TextView) activity.findViewById(R.id.node_detail_title);
		        assertThat(tv1.getText()).isEqualTo("Record not found");
		        LinearLayout propertiesLayout = (LinearLayout) activity.findViewById(R.id.node_properties);
		        assertThat(propertiesLayout.getChildCount()).isEqualTo(0);
            }
        });
    }

    private View createDynamicLayout(String title, Activity activity) {
        LinearLayout dynamicLayout = new LinearLayout(activity);
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        int layoutHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        int layoutWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        TextView titleView = new TextView(activity);
        titleView.setText(title);
        titleView.setTextColor(Color.BLUE);
        return dynamicLayout;
    }

    private Intent getNewIntent() {
        Intent intent = new Intent();
        return intent;
    }

}
