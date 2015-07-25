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

import java.io.InputStream;

import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classynode.Node;

/**
 * TestDataStreamLoader
 * @author Andrew Bowley
 * 14/04/2014
 */
public class TestDataStreamParser implements DataStreamParser
{
    private Node rootNode = Node.rootNodeNewInstance();
    private DataStreamParser dataStreamParser;
    
    @Override
    public Node parseDataStream(InputStream stream) 
    {
        if (dataStreamParser != null)
            rootNode = dataStreamParser.parseDataStream(stream);
        return rootNode;
    }

    public Node getRootNode()
    {
        return rootNode;
    }
    
    public void setRootNode (Node node)
    {
        rootNode = node;
    }
    
    public void setDataStreamParser(DataStreamParser dataStreamParser)
    {
        this.dataStreamParser = dataStreamParser;
    }
}
