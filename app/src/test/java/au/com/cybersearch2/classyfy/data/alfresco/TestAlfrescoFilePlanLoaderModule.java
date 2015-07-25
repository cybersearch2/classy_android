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

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;


import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyfy.data.TestDataStreamParser;
import au.com.cybersearch2.classyinject.DependencyProvider;
import static org.mockito.Mockito.*;

/**
 * TestAlfrescoFilePlanLoaderModule
 * @author Andrew Bowley
 * 14/04/2014
 */
@Module(injects = TestAlfrescoFilePlanLoader.class, complete = false)
public class TestAlfrescoFilePlanLoaderModule implements DependencyProvider<AlfrescoFilePlanLoader>
{
    private TestDataStreamParser alfrescoFilePlanXmlParser;
    private SqlFromNodeGenerator sqlFromNodeGenerator;

    public TestAlfrescoFilePlanLoaderModule()
    {
        alfrescoFilePlanXmlParser = new TestDataStreamParser();
        sqlFromNodeGenerator = mock(SqlFromNodeGenerator.class);
    }
    
    
    @Provides @Named("AlfrescoFilePlan") DataStreamParser provideDataStreamParser() {
        return alfrescoFilePlanXmlParser;
    }

    @Provides SqlFromNodeGenerator provideSqlFromNodeGenerator() {
        return sqlFromNodeGenerator;
    }

    public TestDataStreamParser getTestDataStreamLoader()
    {
        return alfrescoFilePlanXmlParser;
    }
    
    public SqlFromNodeGenerator getSqlFromNodeGenerator()
    {
        return sqlFromNodeGenerator;
    }
}
