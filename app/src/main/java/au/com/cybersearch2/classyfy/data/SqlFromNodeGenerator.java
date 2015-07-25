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

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import au.com.cybersearch2.classynode.Node;

/**
 * SqlFromNodeGenerator
 * Takes a Node tree referenced by root Node and writes SQL insert statements to populate "node" and "child_nodes" tables.<br/>
 * Each node name is subject to changes to conform with a program-friendly format. Single quotes in titles are SQL escaped.
 * @author Andrew Bowley
 * 15/04/2014
 */
public class SqlFromNodeGenerator
{
    static String INSERT_NODE = "insert into nodes (_parent_id, name, title, model, level) values ({0},''{1}'',''{2}'',{3},{4});";
    static String INSERT_CHILD_NODES = "insert into child_nodes (_parent, _child) values ({0}, {1});";
    static String INSERT_CATEGORIES = 
        "insert into categories ( node_id, description, created, creator, modified, modifier, identifier) values " +
        "({0},{1},{2},{3},{4},{5},{6});";
    static String INSERT_FOLDERS = 
        "insert into folders ( node_id, description, created, creator, modified, modifier, identifier, " +
        "location, hasDispositionSchedule, dispositionInstructions, dispositionAuthority) values " +
        "({0},{1},{2},{3},{4},{5},{6},{7},{8},{9}";
    static String INSERT_FOLDERS2 = ",{0});";

    
    int nodeIndex;
    
    public void generateSql(Node rootNode, Writer writer) throws IOException
    {
        nodeIndex = 1;
        depthFirst(0, rootNode.getChildren().get(0), writer);
    }

    private void depthFirst(int parentKey, Node node, Writer writer) throws IOException
    {
        FilePlanNodeType filePlanNodeType = new FilePlanNodeType(node);
        String modelId = Integer.toString(node.getModel());
        // Apply name conventions
        String name = node.getName().replace(' ','_');
        name = name.replace(',','_');
        name = name.replace('\'','_');
        name = name.replace("&", "");
        // Escape single quotes in title
        String title = node.getTitle().replace("'", "''");
        String level = Integer.toString(node.getLevel());
        // Top of tree is not persisted, so reference self at level 1
        String parent_id = Integer.toString(parentKey == 0 ? 1 : parentKey); 
        String insertNode = MessageFormat.format(INSERT_NODE, parent_id, name, title, modelId, level);
        writer.write(insertNode + System.getProperty("line.separator"));
        int primaryKey = nodeIndex++;
        String[] values = new String[filePlanNodeType.getModel() == RecordModel.recordCategory ? 8 : 12];
        // Set ManagedRecord fields shared by both models
        // node_id
        values[0] = Integer.toString(primaryKey);
        //Map<String,Object> properties = node.getProperties();
        // description
        values[1] = Node.getProperty(node, RecordField.description.toString(), "").trim();
        // created
        values[2] = Node.getProperty(node, RecordField.created.toString(), null);
        // creator
        values[3] = Node.getProperty(node, RecordField.creator.toString(), "");
        // modified (optional)
        values[4] = Node.getProperty(node, RecordField.modified.toString(), null);
        // modified (optional)
        values[5] = Node.getProperty(node, RecordField.modifier.toString(), null);
        // identifier
        values[6] = Node.getProperty(node, RecordField.identifier.toString(), "");
        if (filePlanNodeType.getModel() == RecordModel.recordFolder)
        {
            // location (optional)
            values[7] = Node.getProperty(node, RecordField.location.toString(), null);
            // hasDispositionSchedule
            String hasDispositionSchedule = Node.getProperty(node, RecordField.recordSearchHasDispositionSchedule.toString(), "false");
            if (hasDispositionSchedule.equals("'true'"))
                hasDispositionSchedule = "1";
            else 
                hasDispositionSchedule = "0";
            values[8] = hasDispositionSchedule;
            // dispositionInstructions (optional)
            values[9] = Node.getProperty(node, RecordField.recordSearchDispositionInstructions.toString(), null);
            // dispositionAuthority (optional)
            values[10] = Node.getProperty(node, RecordField.recordSearchDispositionAuthority.toString(), null);
            StringBuilder insertRecordFolder = new StringBuilder(
                    MessageFormat.format(INSERT_FOLDERS, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9]));
            insertRecordFolder.append(
                    MessageFormat.format(INSERT_FOLDERS2, values[10]));
            writer.write(insertRecordFolder.toString() + System.getProperty("line.separator"));
        }
        else
        {
            String insertRecordCategory = 
                    MessageFormat.format(INSERT_CATEGORIES, values[0], values[1], values[2], values[3], values[4], values[5], values[6]);

            writer.write(insertRecordCategory + System.getProperty("line.separator"));
        }
        if (parentKey != 0)
        {
            String insertChild = MessageFormat.format(INSERT_CHILD_NODES, Integer.toString(parentKey), Integer.toString(primaryKey));
            writer.write(insertChild + System.getProperty("line.separator"));
        }
        for (Node childNode: node.getChildren())
        {
            depthFirst(primaryKey, childNode, writer);
        }
    }

    public SqlFromNodeGenerator()
    {
    }

}
