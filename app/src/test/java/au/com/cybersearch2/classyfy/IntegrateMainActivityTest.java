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
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowSQLiteConnection;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import au.com.cybersearch2.classyfy.data.NodeEntity;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * IntegrateMainActivityTest
 * @author Andrew Bowley
 * 26/05/2014
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class IntegrateMainActivityTest
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

    // '2014-02-06 19:46:38.000000','admin','2014-02-06 19:50:40.000000','admin','2014-1391676387479')
    private static final String[][] RECORD_DETAILS_ARRAY =
    {
        { "description", "" },
        { "created", "2014-02-06 19:46:38.000000" },
        { "creator", "admin" },
        { "modified", "2014-02-06 19:50:40.000000" },
        { "modifier", "admin" },
        { "identifier", "2014-1391676387479" }
    };

    static final String TITLE = "Corporate Management";
    private MainActivity mainActivity;
    
    @Before
    public void setUp() throws Exception 
    {
        // Reset ShadowSQLiteConnection to avoid "Illegal connection pointer" exception 
        ShadowSQLiteConnection.reset();
        TestClassyFyApplication classyfyLauncher = TestClassyFyApplication.getTestInstance();
        classyfyLauncher.startApplication();
        ClassyFyProvider classyFyProvider = new ClassyFyProvider();
        classyFyProvider.onCreate();
        ShadowContentResolver.registerProvider(
                ClassyFySearchEngine.PROVIDER_AUTHORITY, 
                classyFyProvider);
        mainActivity = Robolectric.setupActivity(MainActivity.class);
        while (true) {
            Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
            Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
            WorkStatus startStatus = classyfyLauncher.getWorkStatus();
            if (startStatus == WorkStatus.FINISHED)
                break;
            if (startStatus == WorkStatus.FAILED)
                Assert.fail("Launch of initial node lookup failed");
        }
    }
    
    @After
    public void tearDown() 
    {
    }

    private Intent getNewIntent()
    {
        return new Intent(RuntimeEnvironment.application, MainActivity.class);
    }

    @Test
    public void test_parseIntent_action_view() throws InterruptedException
    {
        assertThat(mainActivity).isNotNull();
        //assertThat(mainActivity.startState).isEqualTo(StartState.run);
        // Check that ContentProvider is available for search operations
        ContentResolver contentResolver  = TestClassyFyApplication.getTestInstance().getContentResolver();
        assertThat(contentResolver.getType(ClassyFySearchEngine.CONTENT_URI)).isEqualTo("vnd.android.cursor.dir/vnd.classyfy.node");
        // Send view intent to TitleSearchResultsActivity
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "3");
        intent.setData(actionUri);
        TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class, intent).setup().get();
        while (true)
        {
            Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
            Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
            WorkStatus startStatus = TestClassyFyApplication.getTestInstance().getWorkStatus();
            if (startStatus == WorkStatus.FINISHED)
                break;
            if (startStatus == WorkStatus.FAILED)
                Assert.fail("Launch of TitleSearchResults failed");
        }
        // Wait up to 10 seconds for TicketManager to release intent
        synchronized(intent)
        {
            intent.wait(10000);
        }
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
        assertThat(titleSearchResultsActivity.progressFragment.getSpinner().getVisibility()).isEqualTo(View.GONE);
        TextView tv1 = (TextView)titleSearchResultsActivity.findViewById(R.id.node_detail_title);
        assertThat(tv1.getText()).isEqualTo("Category: " + NODE_FIELDS[2].title);
        LinearLayout propertiesLayout = (LinearLayout)titleSearchResultsActivity.findViewById(R.id.node_properties);
        LinearLayout dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(0);
        LinearLayout titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        TextView titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Hierarchy");
        ListView itemList = (ListView)dynamicLayout.getChildAt(1);
        ListAdapter adapter = itemList.getAdapter();
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
}
