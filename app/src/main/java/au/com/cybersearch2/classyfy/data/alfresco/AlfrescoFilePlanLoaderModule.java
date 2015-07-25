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
package au.com.cybersearch2.classyfy.data.alfresco;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyinject.DependencyProvider;



/**
 * AlfrescoFilePlanLoaderModule
 * @author Andrew Bowley
 * 14/04/2014
 */
@Module(injects = AlfrescoFilePlanLoader.class, complete = false)
public class AlfrescoFilePlanLoaderModule implements DependencyProvider<AlfrescoFilePlanLoader>
{

    @Provides @Named("AlfrescoFilePlan") DataStreamParser provideDataStreamParser() {
        return new AlfrescoFilePlanXmlParser();
    }

    @Provides SqlFromNodeGenerator provideSqlFromNodeGenerator() {
        return new SqlFromNodeGenerator();
    }
}
