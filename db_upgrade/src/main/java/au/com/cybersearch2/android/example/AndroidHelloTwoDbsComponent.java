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
package au.com.cybersearch2.android.example;

import javax.inject.Singleton;

import com.example.hellotwodbs.v1.HelloTwoDbs;

import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import dagger.Component;

/**
 * AndroidHelloTwoDbsComponent
 * @author Andrew Bowley
 * 15 Jan 2016
 */
@Singleton
@Component(modules = AndroidHelloTwoDbsModule.class)
public interface AndroidHelloTwoDbsComponent
{
    void inject(HelloTwoDbs helloTwoDbs);
    PersistenceContext persistenceContext();
    ConnectionType connectionType();
    PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
}
