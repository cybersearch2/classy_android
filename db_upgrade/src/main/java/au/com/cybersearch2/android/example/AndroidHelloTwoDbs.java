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
package au.com.cybersearch2.android.example;

import android.content.Context;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.example.HelloTwoDbsMain;

/**
 * AndroidHelloTwoDbs
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class AndroidHelloTwoDbs extends HelloTwoDbsMain
{
    protected AndroidHelloTwoDbsModule androidHelloTwoDbsModule;
    protected Context context;
    protected AndroidHelloTwoDbsComponent component;
   
    public AndroidHelloTwoDbs(final Context context)
    {
        super();
        this.context = context;
    }

 
    @Override
    protected PersistenceContext createObjectGraph()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        component = 
            DaggerAndroidHelloTwoDbsComponent.builder()
            .androidHelloTwoDbsModule(new AndroidHelloTwoDbsModule(context, this))
            .build(); 
        return component.persistenceContext();
    }

    public Executable getExecutable(String puName, PersistenceWork persistenceWork)
    {
        persistenceWorkModule = new PersistenceWorkModule(puName, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }

    protected ConnectionType getConnectionType()
    {
        return component.connectionType();
    }


    public AndroidHelloTwoDbsComponent getComponent()
    {
        return component;
    }
    
}
