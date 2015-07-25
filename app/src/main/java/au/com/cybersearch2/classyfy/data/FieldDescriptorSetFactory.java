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

import java.util.Set;
import java.util.TreeSet;

/**
 * FieldDescriptorSet
 * @author Andrew Bowley
 * 29 Jun 2015
 */
public class FieldDescriptorSetFactory  
{
    public static Set<FieldDescriptor> instance(Node data)
    {
        FieldDescriptor descriptionField = new FieldDescriptor();
        descriptionField.setOrder(1);
        descriptionField.setName("description");
        descriptionField.setTitle("Description");
        FieldDescriptor createdField = new FieldDescriptor();
        createdField.setOrder(2);
        createdField.setName("created");
        createdField.setTitle("Created");
        FieldDescriptor creatorField = new FieldDescriptor();
        creatorField.setOrder(3);
        creatorField.setName("creator");
        creatorField.setTitle("Creator");
        FieldDescriptor modifiedField = new FieldDescriptor();
        modifiedField.setOrder(4);
        modifiedField.setName("modified");
        modifiedField.setTitle("Modified");
        FieldDescriptor modifier = new FieldDescriptor();
        modifier.setOrder(5);
        modifier.setName("modifier");
        modifier.setTitle("Modifier");
        FieldDescriptor identifierField = new FieldDescriptor();
        identifierField.setOrder(6);
        identifierField.setName("identifier");
        identifierField.setTitle("Identifier");
        Set<FieldDescriptor> fieldSet = new TreeSet<FieldDescriptor>();
        fieldSet.add(descriptionField);
        fieldSet.add(createdField);
        fieldSet.add(creatorField);
        fieldSet.add(modifiedField);
        fieldSet.add(modifier);
        fieldSet.add(identifierField);
        return fieldSet;
    }
}
