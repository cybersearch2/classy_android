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
package au.com.cybersearch2.classyfy.module;

import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanLoader;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import dagger.Module;
import dagger.Provides;

/**
 * AlfrescoFilePlanModule
 * @author Andrew Bowley
 * 15 Jan 2016
 */
@Module
public class AlfrescoFilePlanModule
{
    private DataStreamParser dataStreamParser; 
    private SqlFromNodeGenerator sqlFromNodeGenerator;
    
    public AlfrescoFilePlanModule(
            DataStreamParser dataStreamParser, 
            SqlFromNodeGenerator sqlFromNodeGenerator)
    {
        this.dataStreamParser = dataStreamParser;
        this.sqlFromNodeGenerator = sqlFromNodeGenerator;
    }
    
    @Provides DataStreamParser provideDataStreamParser() 
    {
        return dataStreamParser;
    }

    @Provides SqlFromNodeGenerator provideSqlFromNodeGenerator() 
    {
        return sqlFromNodeGenerator;
    }
    
    @Provides AlfrescoFilePlanLoader provideAlfrescoFilePlanLoader(
            PersistenceContext persistenceContext,
            DataStreamParser dataStreamParser,
            SqlFromNodeGenerator sqlFromNodeGenerator)
    {
        return new AlfrescoFilePlanLoader(persistenceContext, dataStreamParser, sqlFromNodeGenerator);
    }
}
