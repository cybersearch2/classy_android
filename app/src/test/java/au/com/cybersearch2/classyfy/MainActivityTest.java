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
import static org.mockito.Matchers.isA;
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
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import android.content.ContentProvider;
import android.net.Uri;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import au.com.cybersearch2.classyfy.module.AlfrescoFilePlanModule;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classynode.NodeType;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * MainActivityTest
 * @author Andrew Bowley
 * 14/05/2014
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class MainActivityTest
{
    // RuntimeEnvironment.application
    // application = TestClassyFyApplication.class,
    class TestClassyFyComponent implements ClassyFyComponent
    {

        private ClassyfyLogic classyfyLogic = mock(ClassyfyLogic.class);

        public ClassyfyLogic getClassyfyLogic()
        {
            return classyfyLogic;
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
            when(classyFySearchEngine.getType(isA(Uri.class))).thenReturn(ClassyFySearchEngine.CONTENT_URI.toString());
            return classyFySearchEngine;
        }

        @Override
        public void inject(TitleSearchResultsActivity titleSearchResultsActivity)
        {
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
            mainActivity.classyfyLogic = classyfyLogic;
            nodeDetails = getNodeDetails(testNode);
            when(mainActivity.classyfyLogic.getNodeDetails(testNode)).thenReturn(nodeDetails);
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
    
    public static final long ID = 1L;
    public static final String NAME = "name";
    public static final String TITLE = "Corporate Management";
    public static final String MODEL = "recordCategory";
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
        //register the ContentProvider
        ContentProvider provider = mock(ContentProvider.class);
        when(provider.getType(ClassyFySearchEngine.CONTENT_URI)).thenReturn(ClassyFySearchEngine.CONTENT_URI.toString());
        ShadowContentResolver.registerProvider(ClassyFySearchEngine.PROVIDER_AUTHORITY, provider);
    }


    @After
    public void tearDown() 
    {
    }
    
 	@Test
    public void test_createSearchView() throws Exception
    {
        MainActivity mainActivity = Robolectric.setupActivity(MainActivity.class);
        verify(testClassyFyComponent.getClassyfyLogic()).getNodeDetails(testNode);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
        LinearLayout propertiesLayout = (LinearLayout)mainActivity.findViewById(R.id.top_category);
        LinearLayout dynamicLayout = (LinearLayout)propertiesLayout.getChildAt(0);
        LinearLayout titleLayout = (LinearLayout)dynamicLayout.getChildAt(0);
        TextView titleView = (TextView) titleLayout.getChildAt(0);
        assertThat(titleView.getText()).isEqualTo("Category: Premises");
        ListView itemList = (ListView)dynamicLayout.getChildAt(1);
        ListAdapter adapter = itemList.getAdapter();
        assertThat(adapter.getCount()).isEqualTo(2);
        ListItem listItem = (ListItem)adapter.getItem(0);
         assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[3].id + 2);
        assertThat(listItem.getValue()).isEqualTo("Category " + NODE_FIELDS[3].title);
        listItem = (ListItem)adapter.getItem(1);
        assertThat(listItem.getId()).isEqualTo(NODE_FIELDS[4].id + 2);
        assertThat(listItem.getValue()).isEqualTo("Category " + NODE_FIELDS[4].title);
     }
    
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
