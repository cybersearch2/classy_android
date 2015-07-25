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

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.classywidget.ListItem;

/**
 * NodeDetailsBean
 * @author Andrew Bowley
 * 6 Jul 2015
 */
public class NodeDetailsBean
{
    protected String heading;
    protected String errorMessage;
    protected ArrayList<ListItem> categoryTitles;
    protected ArrayList<ListItem> folderTitles;
    protected ArrayList<ListItem> hierarchy;
    protected List<ListItem> fieldList;

    public NodeDetailsBean()
    {
        categoryTitles = new ArrayList<ListItem>();
        folderTitles = new ArrayList<ListItem>();
        hierarchy = new ArrayList<ListItem>();
        fieldList = new ArrayList<ListItem>();
    }

    public String getHeading()
    {
        return heading;
    }

    public void setHeading(String heading)
    {
        this.heading = heading;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
    
    public ArrayList<ListItem> getCategoryTitles()
    {
        return categoryTitles;
    }

    public ArrayList<ListItem> getFolderTitles()
    {
        return folderTitles;
    }

    public ArrayList<ListItem> getHierarchy()
    {
        return hierarchy;
    }

    public List<ListItem> getFieldList()
    {
        return fieldList;
    }
}
