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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * RecordFolder
 * @author Andrew Bowley
 * 12/05/2014
 */
@Entity(name = "folders")
public class RecordFolder extends ManagedRecord
{

    private static final long serialVersionUID = -116543269110722658L;

    protected String location;
    
    @Column(nullable = false)
    protected boolean hasDispositionSchedule;
    
    @Column(nullable = true)
    protected String dispositionInstructions;
    
    @Column(nullable = true)
    protected String dispositionAuthority;
    
    public RecordFolder()
    {
    }

    @Override
    public void set_id(int _id) 
    {
        this._id = _id;
    }

    @Override
    public void setDescription(String description) 
    {
        this.description = description;
    }

    @Override
    public void setCreated(Date created) 
    {
        this.created = created;
    }

    @Override
    public void setCreator(String creator) 
    {
        this.creator = creator;
    }

    @Override
    public void setModified(Date modified) 
    {
        this.modified = modified;
    }

    @Override
    public void setModifier(String modifier) 
    {
        this.modifier = modifier;
    }

    @Override
    public void setIdentifier(String identifier) 
    {
        this.identifier = identifier;
    }

    public String getDescription() 
    {
        return description;
    }

    public Date getCreated() 
    {
        return created;
    }

    public String getCreator() 
    {
        return creator;
    }

    public Date getModified() 
    {
        return modified;
    }

    public String getModifier() 
    {
        return modifier;
    }

    public String getIdentifier() 
    {
        return identifier;
    }

    @Override
    public void set_nodeId(int node_id) 
    {
        this.node_id = node_id;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }

    public boolean isHasDispositionSchedule() 
    {
        return hasDispositionSchedule;
    }

    public void setHasDispositionSchedule(boolean hasDispositionSchedule) 
    {
        this.hasDispositionSchedule = hasDispositionSchedule;
    }

    public String getDispositionInstructions() 
    {
        return dispositionInstructions;
    }

    public void setDispositionInstructions(String dispositionInstructions) 
    {
        this.dispositionInstructions = dispositionInstructions;
    }

    public String getDispositionAuthority() 
    {
        return dispositionAuthority;
    }

    public void setDispositionAuthority(String dispositionAuthority) 
    {
        this.dispositionAuthority = dispositionAuthority;
    }

}
