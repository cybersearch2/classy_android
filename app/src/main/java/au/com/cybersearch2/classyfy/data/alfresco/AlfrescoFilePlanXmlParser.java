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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classyfy.data.RecordField;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classyfy.data.RecordModel;

/**
 * AlfrescoFilePlanXmlParser
 * @author Andrew Bowley
 * 14/04/2014
 */
public class AlfrescoFilePlanXmlParser implements DataStreamParser
{
    private XmlPullParser xpp;
 
    
    private String[] SKIP_LIST = 
    {
            "dispositionSchedule",
            "dispositionAction",
            "aspects",
            "acl"
    };

    public AlfrescoFilePlanXmlParser()
    {
        XmlPullParserFactory factory;
        try
        {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        
    }

    @Override
    public Node parseDataStream(InputStream stream) 
    {
        Reader r = new BufferedReader(new InputStreamReader(stream));
        return parseDocument(r);
    }

    public Node parseDocument(Reader reader)
    {
        Node root = Node.rootNodeNewInstance();
        try
        {
            xpp.setInput(reader);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) 
            {
                if (eventType == XmlPullParser.START_DOCUMENT) 
                {
                    System.out.println("Start document");
                } 
                else if (eventType == XmlPullParser.START_TAG) 
                {
                    if ("recordCategory".equals(xpp.getName()))
                        if (!addNode(new Node(RecordModel.recordCategory.ordinal(), root)))
                            break;
                    else if ("recordFolder".equals(xpp.getName()))
                        if (!addNode(new Node(RecordModel.recordFolder.ordinal(), root)))
                            break;
                } 
                /*
                else if (eventType == XmlPullParser.END_TAG) 
                {
                    System.out.println("End tag "+xpp.getName());
                } 
                else if (eventType == XmlPullParser.TEXT) 
                {
                    System.out.println("Text "+xpp.getText());
                }
                */
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return root;
    }
    
    private boolean addNode(Node node) 
    {
        try
        {
            int eventType = xpp.next();
            while (eventType != XmlPullParser.END_DOCUMENT) 
            {
                if (eventType == XmlPullParser.START_TAG) 
                {
                    if ("recordCategory".equals(xpp.getName()))
                        addNode(new Node(RecordModel.recordCategory.ordinal(), node));
                    else if ("recordFolder".equals(xpp.getName()))
                        addNode(new Node(RecordModel.recordFolder.ordinal(), node));
                    else if (!skipList(xpp.getName(), SKIP_LIST))
                        break;
                    else if ("properties".equals(xpp.getName()) && !addProperties(node))
                        break;
                } 
                else if (eventType == XmlPullParser.END_TAG) 
                {
                    if ((node.getModel() == RecordModel.recordCategory.ordinal()) && "recordCategory".equals(xpp.getName()))
                        return true;
                    if ((node.getModel() == RecordModel.recordFolder.ordinal()) && "recordFolder".equals(xpp.getName()))
                        return true;
                } 
                else if (eventType == XmlPullParser.TEXT) 
                {
                }
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false; // Should exit from loop if no error
    }
  
    boolean skipList(final String currentElement, final String[] elementNames) throws XmlPullParserException, IOException
    {
        for (String skipElement: elementNames)
        {
            if (currentElement.equals(skipElement))
            {
                return skipElements(skipElement);
            }
        }
        return true;
    }
    
    boolean skipElements(String enclosingTagName) throws XmlPullParserException, IOException
    {
        int nestCount = 0;
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT) 
        {
            if (eventType == XmlPullParser.END_TAG) 
            {
                if (enclosingTagName.equals(xpp.getName()) && (nestCount-- == 0))
                     return true;
            }
            if (eventType == XmlPullParser.START_TAG) 
            {
                if (enclosingTagName.equals(xpp.getName()))
                    ++nestCount;
            }
            eventType = xpp.next();
        }
        return false;
   }
    
    private boolean addProperties(Node node) throws XmlPullParserException, IOException 
    {
        boolean inMlvalue = false;
        RecordField currentField = RecordField.OTHER;
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT) 
        {
            if (eventType == XmlPullParser.START_TAG) 
            {
                if (!hasAttribute("isNull", "true"))
                {
                    RecordField recordField = RecordField.getRecordField(xpp.getName());
                    if (recordField != null)
                        currentField = recordField;
                    else if ("mlvalue".equals(xpp.getName()))
                        inMlvalue = true;
                }
            }
            else if (eventType == XmlPullParser.END_TAG) 
            {
                if ("properties".equals(xpp.getName()))
                    return true;
                RecordField recordField = RecordField.getRecordField(xpp.getName());
                if (currentField == recordField)
                    currentField = RecordField.OTHER;
                else if (inMlvalue && "mlvalue".equals(xpp.getName()))
                    inMlvalue = false;
            } 
            else if (eventType == XmlPullParser.TEXT) 
            {
                String value = xpp.getText();
                value.trim();
                if (currentField == RecordField.name)
                {
                    node.setName(value);
                }
                else if (currentField == RecordField.title)
                {    
                    if (inMlvalue)
                    {
                        node.setTitle(value);
                    //(currentField == RecordField.description)
                        inMlvalue = false;
                    }
                }
                else if (currentField != RecordField.OTHER)
                {
                    node.getProperties().put(currentField.toString(), value);
                    currentField = RecordField.OTHER;
                }
            }
            eventType = xpp.next();
       }
       return false;
    }
    
    boolean hasAttribute(String name, String value)
    {
        for (int i = 0; i < xpp.getAttributeCount(); i++)
            if (xpp.getAttributeName(i).equals(name) && xpp.getAttributeValue(i).equals(value))
                return true;
        return false;
    }
}
