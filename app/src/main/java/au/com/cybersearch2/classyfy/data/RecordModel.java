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

import au.com.cybersearch2.classynode.NodeType;

/**
 * Model
 * @author Andrew Bowley
 * 14/04/2014
 */
public enum RecordModel
{
   root, 
   recordCategory, // Alfresco Records managemement
   recordFolder ;  // Alfresco Records managemement

   public static String[] MODEL_NAMES =
   {
       "Root",
       "Category",
       "Folder"
   };
   
   public static RecordModel getModelByName(String name)
   {
       if (name == null)
           throw new IllegalArgumentException("Parameter \"name\" is null");
       else if (name.equals(NodeType.ROOT_NAME))
           return root;
       return valueOf(RecordModel.class, name);
   }

   public static RecordModel getModel(int id)
   {
       return RecordModel.values()[id];

   }

   public static String getNameByNode(Node node)
   {
       return MODEL_NAMES[node.getModel()];

   }
}
