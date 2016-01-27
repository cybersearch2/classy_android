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
package au.com.cybersearch2.classywidget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * PropertiesListAdapter
 * Adapts a ListView to display name, value pairs in a standard Android simple list 
 * @author Andrew Bowley
 * 29/05/2014
 */
public class PropertiesListAdapter extends BaseAdapter
{
    /** Resource IDs used to bind an existing view to a value */
    protected static final int[]  uiBindTo = { android.R.id.text1, android.R.id.text2 };
    /** The underlying list implementation */
    protected List<ListItem> propertiesList;
    /** Resource identifier of a layout file that defines the views */
    protected int layout;
    /** Inflater used to create a new view when required */
    protected LayoutInflater inflater;
    /** Flag set if list has only a single line */
    protected boolean singleLine;

    /**
     * Create new, empty PropertiesListAdapter
     * @param context which provides System Service
     */
    public PropertiesListAdapter(Context context)
    {
        this(context, getEmptyValueList());
    }

    /**
     * Create new, populated PropertiesListAdapter
     * @param context which provides System Service
     * @param data Value collection. 
     * The underlying properties list is populated with this data, retaining collection order.
     */
    public PropertiesListAdapter(Context context, Collection<ListItem> data)
    {
        propertiesList = new ArrayList<ListItem>(data);
        layout = android.R.layout.simple_list_item_2;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public boolean isSingleLine()
    {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine)
    {
        this.singleLine = singleLine;
        if (singleLine)
            layout = android.R.layout.simple_list_item_1;
        else
            layout = android.R.layout.simple_list_item_2;
    }

    /**
     * Change the properties list to be viewed
     * 
     * @param data Collection containing values to be viewed
     */
    public void changeData(Collection<ListItem> data) 
    {
        if ((data == null) || (data.size() == 0))
        {
            propertiesList = getEmptyValueList();
            // Notify the observers about the lack of a data set
            notifyDataSetInvalidated();
            return;
        }
        propertiesList = new ArrayList<ListItem>(data);
        // Notify the observers data set changed
        notifyDataSetChanged();
    }

    /**
     * Returns the number of items
     * @return int
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() 
    {
        return propertiesList.size();
    }

    /**
     * Get item at specified position
     * @param position - zero-based list index 
     * @return Item as Object type
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) 
    {
        if ((position < 0) || (position >= propertiesList.size() ))
            return new ListItem("","", 0);
        return propertiesList.get(position);
    }

    /**
     * Returns item identity. Defaults to position +1 if none supplied 
     * @param position - zero-based list index 
     * @return long
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) 
    {
        validatePosition(position);
        long id = propertiesList.get(position).getId();
        return id == -1 ? (long)position + 1 : id; 
    }

    /**
     * Get view of item at specified position
     * 
     * @param position Zero-based list index 
     * @param convertView If not null, view to be reused for efficiency
     * @param parent ViewGroup
     * @return View
     * @throws IllegalStateException if position is invalid
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        validatePosition(position);
        View view = convertView;
        if (view == null) 
            view = newView(parent);
        bindView(view, propertiesList.get(position));
        return view;
    }

    protected void validatePosition(int position)
    {
        if ((position < 0) || (position >= propertiesList.size() ))
            throw new IllegalStateException("Couldn't move to position " + position);
    }

    /**
     * Override default value for hasStableIds
     * @see android.widget.BaseAdapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() 
    {
        return true;
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     * @param parent The parent to which the new view is attached to
     * @return The newly created view.
     */
    protected View newView(ViewGroup parent)
    {
        return inflater.inflate(layout, parent, false);
    }

    /**
     * Bind an existing view to the value
     * @param viewToBind Existing view, returned earlier by newView
     * @param data Value object containing the data.
     */
   protected void bindView(View viewToBind, ListItem data)
   {
       int lineCount = singleLine ? 1 : 2;
       for (int i = 0; i < lineCount; i++) 
       {
           final View itemView = viewToBind.findViewById(uiBindTo[i]);
           if (itemView != null)
           {
               String text = data.value;
               if (!singleLine && (i == 0))
                   text = data.name;
               if (text == null) 
                   text = "";
               if (itemView instanceof TextView) 
                   ((TextView)itemView).setText(text);
               /*
               else if (itemView instanceof ImageView) 
               {
                   setViewImage((ImageView) itemView, text);
               } 
               */
               else 
                    throw new IllegalStateException(itemView.getClass().getName() + " is not a " +
                           " view that can be bound by this PropertiesListAdapter");
           }
        }
    }

   /**
    * Utility method to create an empty list
    * @return Value List
    */
   private static List<ListItem> getEmptyValueList()
   {
       return Collections.emptyList();
   }
   
}
