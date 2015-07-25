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

import java.util.concurrent.Callable;

import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.SimpleFuture;

import dagger.Module;
import dagger.Provides;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import au.com.cybersearch2.classyfy.data.NodeEntity;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * IntegrateMainActivityTest
 * @author Andrew Bowley
 * 26/05/2014
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class IntegrateMainActivityTest
{
    @Implements(value = SystemClock.class, callThroughByDefault = true)
    public static class MyShadowSystemClock {
        public static long elapsedRealtime() {
            return 0;
        }
    }

    @Implements(AsyncTaskLoader.class)
    public static class MyShadowAsyncTaskLoader<D> 
    {
          @RealObject private AsyncTaskLoader<D> realLoader;
          private SimpleFuture<D> future;

          public void __constructor__(Context context) {
            BackgroundWorker worker = new BackgroundWorker();
            future = new SimpleFuture<D>(worker) {
              @Override protected void done() {
                try {
                  final D result = get();
                  ShadowLooper.getUiThreadScheduler().post(new Runnable() {
                    @Override public void run() {
                      realLoader.deliverResult(result);
                    }
                  });
                } catch (InterruptedException e) {
                  // Ignore
                }
              }
            };
          }

          @Implementation
          public void onForceLoad() {
            ShadowApplication.getInstance().getBackgroundScheduler().post(new Runnable() {
              @Override
              public void run() {
                future.run();
              }
            });
          }

          private final class BackgroundWorker implements Callable<D> {
            @Override public D call() throws Exception {
              return realLoader.loadInBackground();
            }
          }
    }

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

    @Module(injects=TestMainActivity.class)
    public static class TestModule implements ApplicationModule
    {   // Note there will be two  ClassyfyLogic "singletons" if both 
        // MainActivy and TitleSearchResultsActivity are included in the same test.
        @Provides @Singleton ClassyfyLogic proviceClassyfyLogic()
        {
            return new ClassyfyLogic();
        }
    }
    

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
    
    @Before
    public void setUp() throws Exception 
    {
        TestClassyFyApplication classyfyLauncher = TestClassyFyApplication.getTestInstance();
        classyfyLauncher.startup();
        // Add TestModule to graph to cover TestMainActivity
        DI.add(new TestModule());
        classyfyLauncher.waitForApplicationSetup();
    }
    
    @After
    public void tearDown() 
    {
    }

    private Intent getNewIntent()
    {
        return new Intent(RuntimeEnvironment.application, MainActivity.class);
    }

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class }, constants = BuildConfig.class, emulateSdk = 21)
    @Test
    public void test_mainActivity_onCreate() throws InterruptedException
    {
        // Create MainActivity
        MainActivity mainActivity = Robolectric.buildActivity(TestMainActivity.class)
        .create()
        .start()
        .visible()
        .get();
        mainActivity.startMonitor.waitForTask();
        assertThat(mainActivity.startMonitor.getStatus()).isEqualTo(WorkStatus.FINISHED);
        ShadowActivity activity = Shadows.shadowOf(mainActivity);
        LinearLayout categoryLayout = (LinearLayout) activity.findViewById(R.id.top_category);
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
    }
    
    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class }, constants = BuildConfig.class, emulateSdk = 21)
    @Test
    public void test_parseIntent_action_view() throws InterruptedException
    {
        // Check that ContentProvider is available for search operations
        ContentResolver contentResolver  = TestClassyFyApplication.getTestInstance().getContentResolver();
        assertThat(contentResolver.getType(ClassyFySearchEngine.CONTENT_URI)).isEqualTo("vnd.android.cursor.dir/vnd.classyfy.node");
        // Send view intent to TitleSearchResultsActivity
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "3");
        intent.setData(actionUri);
        TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class)
        .create()
        .start()
        .visible()
        .get();
        ShadowLooper.getUiThreadScheduler().pause();
        ShadowApplication.getInstance().getBackgroundScheduler().pause();
        titleSearchResultsActivity.parseIntent(intent);
        ShadowApplication.getInstance().getBackgroundScheduler().runOneTask();
        Robolectric.flushForegroundScheduler();
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
