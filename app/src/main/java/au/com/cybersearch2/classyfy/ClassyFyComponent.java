/**
    Copyright (C) 2016  www.cybersearch2.com.au

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

import javax.inject.Singleton;

import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanSubcomponent;
import au.com.cybersearch2.classyfy.module.AlfrescoFilePlanModule;
import au.com.cybersearch2.classyfy.module.ClassyFyApplicationModule;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import dagger.Component;

/**
 * ClassyFyComponent
 * @author Andrew Bowley
 * 13 Jan 2016
 */
@Singleton
@Component(modules = ClassyFyApplicationModule.class)
public interface ClassyFyComponent
{
    PersistenceContext persistenceContext();
    ClassyFySearchEngine classyFySearchEngine();
    void inject(MainActivity mainActivity);
    void inject(TitleSearchResultsActivity titleSearchResultsActivity);
    ClassyLogicComponent plus(ClassyLogicModule classyLogicModule);
    AlfrescoFilePlanSubcomponent plus(AlfrescoFilePlanModule alfrescoFilePlanModule);
}
