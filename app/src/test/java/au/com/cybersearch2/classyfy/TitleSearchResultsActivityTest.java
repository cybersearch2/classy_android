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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classybean.BeanMap;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfy.data.FieldDescriptor;
import au.com.cybersearch2.classyfy.data.FieldDescriptorSetFactory;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.data.NodeEntity;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanSubcomponent;
import au.com.cybersearch2.classyfy.helper.TicketManager;
import au.com.cybersearch2.classyfy.module.AlfrescoFilePlanModule;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classynode.NodeType;
import au.com.cybersearch2.classywidget.ListItem;


/**
 * TitleSearchResultsActivityTest
 * @author Andrew Bowley
 * 29/04/2014
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class TitleSearchResultsActivityTest
{
    class TestTicketManager extends TicketManager
    {
        Intent currentIntent;
        
        @Override
        public int addIntent(Intent intent)
        {
            currentIntent = intent;
            return super.addIntent(intent);
        }

        public Intent getCurrentIntent()
        {
            return currentIntent;
        }
        
    }
    
    class TestClassyFyComponent implements ClassyFyComponent
    {
        ClassyfyLogic classyfyLogic = mock(ClassyfyLogic.class);
        TestTicketManager ticketManager = new TestTicketManager();

        public ClassyfyLogic getClassyfyLogic()
        {
            return classyfyLogic;
        }

        public TestTicketManager getTicketManager()
        {
            return ticketManager;
        }

        @Override
        public PersistenceContext persistenceContext()
        {
            return null;
        }

        @Override
        public ClassyFySearchEngine classyFySearchEngine()
        {
            ClassyFySearchEngine classyFySearchEngine = mock(ClassyFySearchEngine.class);
            return classyFySearchEngine;
        }

        @Override
        public void inject(TitleSearchResultsActivity titleSearchResultsActivity)
        {
            titleSearchResultsActivity.ticketManager = ticketManager;
            titleSearchResultsActivity.classyfyLogic = classyfyLogic;
            nodeDetails = getNodeDetails(testNode);
            when(titleSearchResultsActivity.classyfyLogic.getNodeDetails(testNode)).thenReturn(nodeDetails);
       }

        @Override
        public ClassyLogicComponent plus(ClassyLogicModule classyLogicModule)
        {
            return new ClassyLogicComponent(){

                @Override
                public Node node()
                {
                    try
                    {
                        return testNode;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return null;
                }};
        }

        @Override
        public void inject(MainActivity mainActivity)
        {
        }

        @Override
        public AlfrescoFilePlanSubcomponent plus(AlfrescoFilePlanModule alfrescoFilePlanModule)
        {
            return null;
        }

        @Override
        public FtsEngine ftsEngine()
        {
            return null;
        }

        @Override
        public PersistenceWorkSubcontext plus(
                PersistenceWorkModule persistenceWorkModule)
        {
            return null;
        }

        @Override
        public ResourceEnvironment resourceEnvironment()
        {
            return null;
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
    
    private static final String[][] RECORD_DETAILS_ARRAY =
    {
        { "description", "" },
        { "created", "2014-02-12 10:58:00.000000" },
        { "creator", "admin" },
        { "modified", "2014-02-12 11:28:35.000000" },
        { "modifier", "admin" },
        { "identifier", "2014-1392163053802" }
    };


    public static final String TITLE = "Corporate Management";
    public static final String MODEL = "Category";
    private final static String SEARCH_TEXT = "Information";
    private static final String RECORD_NAME = "Cybersearch2-Records";
    private static final String RECORD_VALUE = "Cybersearch2 Records";
    private static final long NODE_ID = 34L;
    private Node testNode;
    private NodeDetailsBean nodeDetails;
    private TestClassyFyComponent testClassyFyComponent;

    @Before
    public void setUp() throws Exception 
    {
        testNode = getTestNode();
        TestClassyFyApplication testClassyFyApplication = TestClassyFyApplication.getTestInstance();
        testClassyFyComponent = new TestClassyFyComponent();
        testClassyFyApplication.setTestClassyFyComponent(testClassyFyComponent);
        // ShadowContentResolver.registerProvider() Deprecated version 3.2
        // Register the ContentProvider
        //ContentProvider provider = mock(ContentProvider.class);
        //when(provider.getType(ClassyFySearchEngine.CONTENT_URI)).thenReturn(ClassyFySearchEngine.CONTENT_URI.toString());
        //ShadowContentResolver.registerProvider(ClassyFySearchEngine.PROVIDER_AUTHORITY, provider);
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = ClassyFySearchEngine.PROVIDER_AUTHORITY;
        Robolectric.buildContentProvider(ClassyFyProvider.class).create(providerInfo).get();
}

    @After
    public void tearDown() 
    {
    }
    
    private Intent getNewIntent()
    {
        return new Intent(RuntimeEnvironment.application, TitleSearchResultsActivity.class);
    }
    
    @Test 
    public void test_onCreate() throws Exception
    {
        final Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_DEFAULT);
        final TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class, intent).setup().get();
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();

        // Check activity fields initialization
        assertThat(titleSearchResultsActivity.progressFragment).isNotNull();
        assertThat(titleSearchResultsActivity.REFINE_SEARCH_MESSAGE).isEqualTo("Only first 50 hits displayed. Please refine search.");

        // Test search suggesion initiated by intent up to display
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, SEARCH_TEXT);
        Robolectric.getBackgroundThreadScheduler().pause();
        ShadowActivity activity = Shadows.shadowOf(titleSearchResultsActivity);
        activity.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                titleSearchResultsActivity.parseIntent(intent);
            }});
        // Navigate doSearchQuery() and onLoadComplete()
        ArrayList<ListItem> singletonList = new ArrayList<ListItem>();
        ListItem listItem = new ListItem(RECORD_NAME, RECORD_VALUE, NODE_ID);
        singletonList.add(listItem);
        ClassyfyLogic classyfyLogic = titleSearchResultsActivity.classyfyLogic;
        when(classyfyLogic.doSearchQuery(SEARCH_TEXT)).thenReturn(singletonList);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        verify(classyfyLogic).doSearchQuery(SEARCH_TEXT);
        TextView tv1 = (TextView)activity.findViewById(R.id.node_detail_title);
        assertThat(tv1.getText()).isEqualTo("Search: " + SEARCH_TEXT);
        // Next test covers scenario search success and select item 
     }

    @Test
    public void test_search() throws Exception
    {
        // Navigate doSearchQuery() and onLoadComplete()
        ArrayList<ListItem> singletonList = new ArrayList<ListItem>();
        ListItem listItem = new ListItem(RECORD_NAME, RECORD_VALUE, NODE_ID);
        singletonList.add(listItem);
        ClassyfyLogic classyfyLogic = testClassyFyComponent.getClassyfyLogic();
        when(classyfyLogic.doSearchQuery(SEARCH_TEXT)).thenReturn(singletonList);
        final Intent intent = getNewIntent();
        // Test search suggesion initiated by intent
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, SEARCH_TEXT);
        final TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class, intent).setup().get();
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
        ShadowActivity activity = Shadows.shadowOf(titleSearchResultsActivity);
        TextView tv1 = (TextView)activity.findViewById(R.id.node_detail_title);
        assertThat(tv1.getText()).isEqualTo("Search: " + SEARCH_TEXT);
        // Scenario: search success and select item
        LinearLayout propertiesLayout = (LinearLayout)activity.findViewById(R.id.node_properties);
        LinearLayout dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(0);
        LinearLayout titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        TextView titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Titles");
        ListView itemList = (ListView)dynamicLayout.getChildAt(1);
        ListAdapter adapter = itemList.getAdapter();
        assertThat(adapter.getCount()).isEqualTo(1);
        assertThat((ListItem)adapter.getItem(0)).isEqualTo(listItem);
        final OnItemClickListener onItemClickListener = itemList.getOnItemClickListener();

        // Test item selection by activating the onItemClickListener
        final ProgressBar spinner = titleSearchResultsActivity.progressFragment.getSpinner();
        activity.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                onItemClickListener.onItemClick(null, null, 0, NODE_ID);
                assertThat(spinner.getVisibility()).isEqualTo(View.VISIBLE);
            }});
        // Wait up to 10 seconds for TicketManager to release intent
        final Intent viewIntent = testClassyFyComponent.getTicketManager().getCurrentIntent();
        synchronized(viewIntent)
        {
            viewIntent.wait(10000);
        }
        verify(classyfyLogic).getNodeDetails(testNode);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
        assertThat(spinner.getVisibility()).isEqualTo(View.GONE);
        tv1 = (TextView)activity.findViewById(R.id.node_detail_title);
        assertThat(tv1.getText()).isEqualTo(nodeDetails.getHeading());
        propertiesLayout = (LinearLayout)activity.findViewById(R.id.node_properties);
        dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(0);
        titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Hierarchy");
        itemList = (ListView)dynamicLayout.getChildAt(1);
        adapter = itemList.getAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);
        listItem = (ListItem)adapter.getItem(0);
        assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[0].id);
        assertThat(listItem.getValue()).isEqualTo(NODE_FIELDS[0].title);
        listItem = (ListItem)adapter.getItem(1);
        assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[1].id);
        assertThat(listItem.getValue()).isEqualTo(NODE_FIELDS[1].title);
        dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(1);
        titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Categories");
        itemList = (ListView)dynamicLayout.getChildAt(1);
        adapter = itemList.getAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);
        listItem = (ListItem)adapter.getItem(0);
        assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[3].id + 2);
        assertThat(listItem.getValue()).isEqualTo("Category " + NODE_FIELDS[3].title);
        listItem = (ListItem)adapter.getItem(1);
        assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[4].id + 2);
        assertThat(listItem.getValue()).isEqualTo("Category " + NODE_FIELDS[4].title);
        dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(2);
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
        dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(3);
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

    @Test
    public void test_parseIntent_action_view_invalid_uri()
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = ClassyFySearchEngine.CONTENT_URI;
        intent.setData(actionUri);
        Robolectric.buildActivity(TitleSearchResultsActivity.class).withIntent(intent)
        .create()
        .start()
        .visible()
        .get();
        ShadowToast.showedToast("Invalid resource address: \"" + ClassyFySearchEngine.CONTENT_URI.toString() + "\"");
    }
    
    @Test
    public void test_parseIntent_action_view_invalid_node_id()
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "x4");
        intent.setData(actionUri);
        Robolectric.buildActivity(TitleSearchResultsActivity.class).withIntent(intent)
        .create()
        .start()
        .visible()
        .get();
        ShadowToast.showedToast("Resource address has invalid ID: \"" + actionUri.toString() + "\"");
    }

   /*
    @Test
    public void pausesAndResumesActivity() 
    {
        createWithIntent(Intent.ACTION_SEARCH);
        controller.pause().resume();
        // Assertions go here
    }

    @Test
    public void recreatesActivity() 
    {
        createWithIntent(Intent.ACTION_SEARCH);
        titleSearchResultsActivity.recreate();
        // Assertions go here
    }
    */

    protected NodeDetailsBean getNodeDetails(Node data)
    {
        NodeDetailsBean nodeDetailsBean = new NodeDetailsBean();
        // Collect children, distinguishing between folders and categories
        for (Node child: data.getChildren())
        {
            String title = child.getTitle();
            long id = (long)child.getId();
            ListItem item = new ListItem("Title", title, id);
            if (RecordModel.getModel(child.getModel()) == RecordModel.recordFolder)
                nodeDetailsBean.getFolderTitles().add(item);
            else
                nodeDetailsBean.getCategoryTitles().add(item);
        }
        // Collect node hierarchy up to root node
        Node node = data.getParent();
        Deque<Node> nodeDeque = new ArrayDeque<Node>();
        // Walk up to top node
        while (node.getModel() != NodeType.ROOT)// Top of tree
        {
            nodeDeque.add(node);
            node = node.getParent();
        }
        Iterator<Node> nodeIterator = nodeDeque.descendingIterator();
        while (nodeIterator.hasNext())
        {
            node = nodeIterator.next();
            String title = node.getTitle();
            long id = (long)node.getId();
            ListItem item = new ListItem("Title", title, id);
            nodeDetailsBean.getHierarchy().add(item);
        }
        // Build heading from Title and record type
        StringBuilder builder = new StringBuilder();
        builder.append(RecordModel.getNameByNode(data)).append(": ");
        builder.append(data.getTitle());
        nodeDetailsBean.setHeading(builder.toString());
        // Collect details in FieldDescripter order
        Map<String,Object> valueMap = data.getProperties();
        Set<FieldDescriptor> fieldSet = FieldDescriptorSetFactory.instance(data);
        for (FieldDescriptor descriptor: fieldSet)
        {
            Object value = valueMap.get(descriptor.getName());
            if (value == null)
                continue;
            nodeDetailsBean.getFieldList().add(new ListItem(descriptor.getTitle(), value.toString()));
        }
        return nodeDetailsBean;
    }

    public Node getTestNode() throws Exception
    {
        NodeEntity nodeEntity0 = NODE_FIELDS[0].getNodeEntity(null);
        List<NodeEntity> children0 = new ArrayList<NodeEntity>();
        NodeEntity nodeEntity1 = NODE_FIELDS[1].getNodeEntity(nodeEntity0);
        children0.add(nodeEntity1);
        nodeEntity0.set_children(children0);
        
        NodeEntity nodeEntity2 = NODE_FIELDS[2].getNodeEntity(nodeEntity1);
        List<NodeEntity> children1 = new ArrayList<NodeEntity>();
        children1.add(nodeEntity2);
        nodeEntity1.set_children(children1);
        
        NodeEntity nodeEntity3 = NODE_FIELDS[3].getNodeEntity(nodeEntity2);
        NodeEntity nodeEntity4 = NODE_FIELDS[4].getNodeEntity(nodeEntity2);
        List<NodeEntity> children2 = new ArrayList<NodeEntity>();
        children2.add(nodeEntity3);
        children2.add(nodeEntity4);
        nodeEntity2.set_children(children2);
      
        Node node = new Node(nodeEntity0, null);
        node = node.getChildren().get(0).getChildren().get(0);
        node.setProperties(getNodeProperties());
        // Add 2 Category children to data to test setting CategoryTitles 
        NodeEntity nodeEntity5 = NODE_FIELDS[3].getNodeEntity(null);
        nodeEntity5.set_id(6);
        nodeEntity5.setTitle("Category " + NODE_FIELDS[3].title);
        nodeEntity5.setModel(1);
        new Node(nodeEntity5, node);
        NodeEntity nodeEntity6 = NODE_FIELDS[4].getNodeEntity(null);
        nodeEntity6.set_id(7);
        nodeEntity6.setTitle("Category " + NODE_FIELDS[4].title);
        nodeEntity6.setModel(1);
        new Node(nodeEntity6, node);
        return node;
    }


    private Map<String, Object> getNodeProperties() throws Exception
    {
        return new BeanMap(getRecordCategory());
    }

    private RecordCategory getRecordCategory() throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", new Locale("en", "AU"));
        RecordCategory recordCategory = new RecordCategory();
        Date created = sdf.parse("2014-02-05 18:45:46.000000");
        Date modified = sdf.parse("2014-02-12 11:55:23.000000");
        recordCategory.setCreated(created);
        recordCategory.setModified(modified);
        recordCategory.setCreator("admin");
        recordCategory.setModifier("prole");
        recordCategory.setDescription("Information Technology");
        recordCategory.setIdentifier("2014-1391586274589");
        return recordCategory;
    }
    
}
