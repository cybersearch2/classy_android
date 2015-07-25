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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * ManagedRecord
 * @author Andrew Bowley
 * 01/05/2014
 */
public abstract class ManagedRecord implements Serializable
{
    private static final long serialVersionUID = 8577297186634353771L;

    @Id @GeneratedValue
    protected int _id;
   
    @Column
    protected int node_id;
    
    @Column
    protected String description;
    
    @Column
    protected Date created;

    @Column
    protected String creator;
    
    @Column(nullable = true)
    protected Date modified;

    @Column(nullable = true)
    protected String modifier;
 
    @Column(nullable = true)
    protected String identifier;

    public abstract void set_id(int _id);

    public abstract void set_nodeId(int node_id);

    public abstract void setDescription(String description);

    public abstract void setCreated(Date created);

    public abstract void setCreator(String creator);

    public abstract void setModified(Date modified);

    public abstract void setModifier(String modifier);

    public abstract void setIdentifier(String identifier);

    public int get_id() 
    {
        return _id;
    }

    public int get_node_id() 
    {
        return node_id;
    }
    
}
