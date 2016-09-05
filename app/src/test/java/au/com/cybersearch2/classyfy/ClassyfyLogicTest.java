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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import au.com.cybersearch2.classybean.BeanMap;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.data.NodeEntity;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * ClassyfyLogicTest
 * @author Andrew Bowley
 * 6 Jul 2015
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ClassyfyLogicTest
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

    private final static String SEARCH_TEXT = "Information";
    private static final String RECORD_NAME = "Cybersearch2-Records";
    private static final String RECORD_VALUE = "Cybersearch2 Records";
    private static final long NODE_ID = 34L;
    
    protected Context mockContext;
    protected ContentResolver contentResolver;
    
    @Before
    public void setUp() 
    {
        contentResolver = mock(ContentResolver.class);
        mockContext = mock(Context.class);
        when(mockContext.getContentResolver()).thenReturn(contentResolver);
    }

    @Test
    public void test_doSearchQuery()
    {
        ClassyfyLogic classyfyLogic = new ClassyfyLogic(mockContext);
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1)).thenReturn(1);
        when(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_2)).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("_id")).thenReturn(0);
        when(cursor.getCount()).thenReturn(1);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getString(1)).thenReturn(RECORD_NAME);
        when(cursor.getString(2)).thenReturn(RECORD_VALUE);
        when(cursor.getLong(0)).thenReturn(NODE_ID);
        Uri uri = Uri.parse(ClassyFySearchEngine.LEX_CONTENT_URI + "?" + 
                            SearchManager.SUGGEST_PARAMETER_LIMIT + "=" +
                            String.valueOf(ClassyFyProvider.SEARCH_RESULTS_LIMIT));
        when(contentResolver.query(uri, null, "word MATCH ?", new String[] { SEARCH_TEXT }, null)).thenReturn(cursor);
        List<ListItem> result = classyfyLogic.doSearchQuery(SEARCH_TEXT);
        assertThat(result.size()).isEqualTo(1);
        ListItem listItem = result.get(0);
        assertThat(listItem.getId()).isEqualTo(NODE_ID);
        assertThat(listItem.getName()).isEqualTo(RECORD_NAME);
        assertThat(listItem.getValue()).isEqualTo(RECORD_VALUE);
        verify(cursor).close();
    }
 
    @Test
    public void test_doSearchQueryNotFound()
    {
        ClassyfyLogic classyfyLogic = new ClassyfyLogic(mockContext);
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1)).thenReturn(1);
        when(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_2)).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("_id")).thenReturn(0);
        when(cursor.getCount()).thenReturn(0);
        Uri uri = Uri.parse(ClassyFySearchEngine.LEX_CONTENT_URI + "?" + 
                            SearchManager.SUGGEST_PARAMETER_LIMIT + "=" +
                            String.valueOf(ClassyFyProvider.SEARCH_RESULTS_LIMIT));
        when(contentResolver.query(uri, null, "word MATCH ?", new String[] { SEARCH_TEXT }, null)).thenReturn(cursor);
        List<ListItem> result = classyfyLogic.doSearchQuery(SEARCH_TEXT);
        assertThat(result.size()).isEqualTo(0);
        verify(cursor).close();
    }

    @Test
    public void test_getNodeDetails() throws Exception
    {
        Node data = getTestNode();
        ClassyfyLogic classyfyLogic = new ClassyfyLogic(mockContext);
        NodeDetailsBean result = classyfyLogic.getNodeDetails(data);
        assertThat(result.getHeading()).isEqualTo("Category: " + NODE_FIELDS[2].title);
        assertThat(result.getHierarchy().size()).isEqualTo(2);
        assertThat(result.getHierarchy().get(0).getId()).isEqualTo(NODE_FIELDS[0].id);
        assertThat(result.getHierarchy().get(0).getValue()).isEqualTo(NODE_FIELDS[0].title);
        assertThat(result.getHierarchy().get(1).getId()).isEqualTo(NODE_FIELDS[1].id);
        assertThat(result.getHierarchy().get(1).getValue()).isEqualTo(NODE_FIELDS[1].title);
        assertThat(result.getFolderTitles().size()).isEqualTo(2);
        assertThat(result.getFolderTitles().get(0).getId()).isEqualTo(NODE_FIELDS[3].id);
        assertThat(result.getFolderTitles().get(0).getValue()).isEqualTo(NODE_FIELDS[3].title);
        assertThat(result.getFolderTitles().get(1).getId()).isEqualTo(NODE_FIELDS[4].id);
        assertThat(result.getFolderTitles().get(1).getValue()).isEqualTo(NODE_FIELDS[4].title);
        assertThat(result.getCategoryTitles().size()).isEqualTo(0);
        assertThat(result.getFieldList().size()).isEqualTo(RECORD_DETAILS_ARRAY.length);
        for (int i = 0; i < RECORD_DETAILS_ARRAY.length; i++)
        {
            ListItem item = result.getFieldList().get(i);
            assertThat(item.getName().equals(RECORD_DETAILS_ARRAY[i][0]));
            assertThat(item.getValue().equals(RECORD_DETAILS_ARRAY[i][1]));
        }
        // Add 2 Category children to data to test setting CategoryTitles 
        NodeEntity nodeEntity5 = NODE_FIELDS[3].getNodeEntity(null);
        nodeEntity5.set_id(6);
        nodeEntity5.setTitle("Category " + NODE_FIELDS[3].title);
        nodeEntity5.setModel(1);
        new Node(nodeEntity5, data);
        NodeEntity nodeEntity6 = NODE_FIELDS[4].getNodeEntity(null);
        nodeEntity6.set_id(7);
        nodeEntity6.setTitle("Category " + NODE_FIELDS[4].title);
        nodeEntity6.setModel(1);
        new Node(nodeEntity6, data);
        result = classyfyLogic.getNodeDetails(data);
        assertThat(result.getCategoryTitles().size()).isEqualTo(2);
        assertThat(result.getCategoryTitles().get(0).getId()).isEqualTo(6);
        assertThat(result.getCategoryTitles().get(0).getValue()).isEqualTo("Category " + NODE_FIELDS[3].title);
        assertThat(result.getCategoryTitles().get(1).getId()).isEqualTo(7);
        assertThat(result.getCategoryTitles().get(1).getValue()).isEqualTo("Category " + NODE_FIELDS[4].title);
        assertThat(result.getCategoryTitles().size()).isEqualTo(2);
        assertThat(result.getFolderTitles().get(0).getId()).isEqualTo(NODE_FIELDS[3].id);
        assertThat(result.getFolderTitles().get(0).getValue()).isEqualTo(NODE_FIELDS[3].title);
        assertThat(result.getFolderTitles().get(1).getId()).isEqualTo(NODE_FIELDS[4].id);
        assertThat(result.getFolderTitles().get(1).getValue()).isEqualTo(NODE_FIELDS[4].title);
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
