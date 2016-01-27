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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ListItem
 * A name, value pair container. Each pair is displayed using a 2-line layout in the displayed list.
 * @author Andrew Bowley
 * 03/07/2014
 */
public class ListItem implements Parcelable
{
    String name;
    String value;
    // For getItemId()
    Long id;

    public ListItem(String name, String value)
    {
        // Id = -1 means default to position
        this(name, value, -1);
    }
    
    public ListItem(String name, String value, long id)
    {
        this.name = name;
        this.value = value;
        this.id = id;
    }

    private ListItem(Parcel in)
    {
        id = in.readLong();
        name = in.readString();
        value = in.readString();
    }

    public String getName() 
    {
        return name;
    }

    public String getValue() 
    {
        return value;
    }
    
    public long getId() 
    {
        return id;
    }

    /**
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(value);
    }

    public static final Parcelable.Creator<ListItem> CREATOR =
        new Parcelable.Creator<ListItem>() { 
        
            @Override
            public ListItem createFromParcel(Parcel in) 
            {
                return new ListItem(in);
            }

            @Override
            public ListItem[] newArray(int size) 
            {
                return new ListItem[size];
            }
        };
}
