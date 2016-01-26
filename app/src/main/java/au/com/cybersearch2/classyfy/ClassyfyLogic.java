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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import au.com.cybersearch2.classycontent.SuggestionCursorParameters;
import au.com.cybersearch2.classyfy.data.FieldDescriptor;
import au.com.cybersearch2.classyfy.data.FieldDescriptorSetFactory;
import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classynode.NodeType;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * ClassyfyLogic
 * Queries to support application operations. Methods must be executed on background thread.
 * @author Andrew Bowley
 * 6 Jul 2015
 */
public class ClassyfyLogic
{
    public static final String TAG = "ClassyfyLogic";
    /** Error message for unsuccessful node search. Not expected to happen in normal operation */
    public static final String RECORD_NOT_FOUND = "Record not found due to database error";
    /** Error message for interrupted node search. Not expected to happen in normal operation */
    public static final String SEARCH_NOT_COMPLETED = "Record search did not complete";
    
    /** Application context provides ContentResolver to access content provider */
    protected Context context;

    /**
     * Create ClassyfyLogic object
     */
    public ClassyfyLogic(Context context)
    {
        this.context = context;
    }
 
    /**
     * Perform content provider query for fast text search, Must be executed on background thread. 
     * @param searchQuery Query string
     * @return
     */
    public List<ListItem> doSearchQuery(String searchQuery)
    {
        // Perform the search, passing in the search query as an argument to the Cursor Loader
        SuggestionCursorParameters params = 
                new SuggestionCursorParameters(searchQuery, 
                                               ClassyFySearchEngine.LEX_CONTENT_URI, 
                                               ClassyFyProvider.SEARCH_RESULTS_LIMIT); 
        
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                params.getUri(), 
                params.getProjection(), 
                params.getSelection(), 
                params.getSelectionArgs(), 
                params.getSortOrder());
         List<ListItem> fieldList = new ArrayList<ListItem>();
         int nameColumnId = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
         int valueColumnId = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_2);
         // Id column name set in android.support.v4.widget.CursorAdaptor
         int idColumnId = cursor.getColumnIndexOrThrow("_id");
         if (cursor.getCount() > 0) 
         {
             cursor.moveToPosition(-1);         
             while (cursor.moveToNext())
             {
                 String name = cursor.getString(nameColumnId);
                 String value = cursor.getString(valueColumnId);
                 long id = cursor.getLong(idColumnId);
                 fieldList.add(new ListItem(name, value, id));
             }
         }
         cursor.close();
         return fieldList;
    }

    public NodeDetailsBean getNodeDetails(Node data)
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
        if ((data.getTitle() != null) && (data.getTitle().length() > 0))
            builder.append(data.getTitle());
        else
            builder.append('?'); // This is an error. Handle gracefully.
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
    
}
