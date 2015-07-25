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
package au.com.cybersearch2.classyfy.data;

/**
 * FieldDescriptor
 * @author Andrew Bowley
 * 14/05/2014
 */
public class FieldDescriptor implements Comparable<FieldDescriptor>
{
    protected int _id;
    protected RecordModel model;
    protected int order;
    protected String title;
    protected String name;
    
    public FieldDescriptor()
    {
    }

    @Override
    public int compareTo(FieldDescriptor another) 
    {   // Same if same name, else compare by order
        return name.equals(another.name) ? 0 : order - another.order;
    }

    @Override
    public boolean equals(Object another) 
    {
        if ((another != null) && (another instanceof FieldDescriptor))
            return name.equals(((FieldDescriptor)another).name); 
        return false;
    }

    @Override
    public int hashCode() 
    {
        return name.hashCode();
    }

    public int getOrder() 
    {
        return order;
    }

    public void setOrder(int order) 
    {
        this.order = order;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getName() 
    {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public RecordModel getModel() 
    {
        return model;
    }

    public void setModel(RecordModel model) 
    {
        this.model = model;
    }

    public int get_id() 
    {
        return _id;
    }

    public void set_id(int _id) 
    {
        this._id = _id;
    }

}
