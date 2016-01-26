/**
    Copyright (C) 2015  www.cybersearch2.com.au

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
package au.com.cybersearch2.android.example.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.example.hellotwodbs.v2.HelloTwoDbsApplication;

import android.content.Context;
import au.com.cybersearch2.classyapp.ResourceEnvironment;

/**
 * AppResourceEnvironment
 * @author Andrew Bowley
 * 16 Jan 2016
 */
public class AppResourceEnvironment implements ResourceEnvironment
{
    private Context context;
    private Locale locale = new Locale("en", "AU");

    public AppResourceEnvironment(Context context)
    {
        this.context = context;
    }
    
    /**
     * @see au.com.cybersearch2.classyapp.ResourceEnvironment#openResource(java.lang.String)
     */
    @Override
    public InputStream openResource(String resourceName) throws IOException
    {
        return context.getAssets().open("v2/" + resourceName);
    }

    /**
     * @see au.com.cybersearch2.classyapp.ResourceEnvironment#getLocale()
     */
    @Override
    public Locale getLocale()
    {
        return locale;
    }

}
