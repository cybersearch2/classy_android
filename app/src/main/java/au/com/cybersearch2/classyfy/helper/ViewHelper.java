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
package au.com.cybersearch2.classyfy.helper;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import au.com.cybersearch2.classywidget.ListItem;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;

/**
 * ViewHelper
 * @author Andrew Bowley
 * 9 Jul 2015
 */
public class ViewHelper
{
    /**
     * Returns view containg a title and list of items
     * @param title Title text
     * @param items ListItem list
     * @param isSingleLine flag to indicate whether to show only value or value and name
     * @return View object
     */
    public static View createRecordView(
            Activity activity, 
            String title, 
            List<ListItem> items, 
            boolean isSingleLine,
            OnItemClickListener onItemClickListener)
    {
        LinearLayout dynamicLayout = new LinearLayout(activity);
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        int layoutHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        int layoutWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        TextView titleView = new TextView(activity);
        titleView.setText(title);
        titleView.setTextColor(Color.BLUE);
        LinearLayout titleLayout = new LinearLayout(activity);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams titleLayoutParms = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
        titleLayout.addView(titleView, titleLayoutParms);
        dynamicLayout.addView(titleLayout);
        ListView itemList = new ListView(activity);
        PropertiesListAdapter listAdapter = new PropertiesListAdapter(activity, items);
        listAdapter.setSingleLine(isSingleLine);
        itemList.setAdapter(listAdapter);
        itemList.setOnItemClickListener(onItemClickListener);
        dynamicLayout.addView(itemList);
        return dynamicLayout;
    }


}
