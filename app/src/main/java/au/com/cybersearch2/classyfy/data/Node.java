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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.classynode.NodeType;

/**
 * Node
 * Anchor for a component of a graph. 
 * Each node has a model which identifies what the node contains.
 * The top node of a graph has a special model called "root".
 * @author Andrew Bowley
 * 05/09/2014
 */
public class Node implements Serializable 
{
    private static final long serialVersionUID = -611139781193264363L;
    public static final String ROOT = "Root";
    /** Prefix for name of query to fetch node by primary key */
    public static final String NODE_BY_PRIMARY_KEY_QUERY = "NodeByPrimaryKey";
    /** Node properties defined by model */
    Map<String,Object> properties;
    /** Persistence object */
    NodeEntity nodeEntity;
    Node parent;
    /** Child nodes list. When this node is fetched from a database, the list may contain place holders with only primary key set */
    List<Node> children;
    
    /**
     * Create root node. Private default constructor prevents creation of node orphans.
     * To begin constructing a graph, create a top node by calling 
     * static method rootNodeNewInstance(). The graph then grows
     * by using the Node(model, parent) constructor.
     */
    private Node()
    {
    	nodeEntity = new NodeEntity();
        nodeEntity.setModel(NodeType.ROOT);
        nodeEntity.setParent(nodeEntity);
        nodeEntity.setLevel(0);
        nodeEntity.set_id(0);
        nodeEntity.setName(ROOT);
        nodeEntity.setTitle(ROOT);
        parent = this;
    }
 
    /**
     * Returns primary key
     * @return int
     */
    public int getId()
    {
        return nodeEntity.get_id();
    }
    
     /**
     * Returns model ordinal value
     * @return int
     */
    public int getModel() 
    {
        return nodeEntity.getModel();
    }
    
    /**
     * Returns node name
     * @return String
     */
    public String getName() 
    {
        return nodeEntity.getName();
    }
    
    /**
     * Returns node title (human readable)
     * @return String
     */
    public String getTitle() 
    {
        return nodeEntity.getTitle();
    }
    
    /**
     * Returns depth in graph, starting at 1 for the solitary root node
     * @return int
     */
    public int getLevel() 
    {
        return nodeEntity.getLevel();
    }
    
    

    /**
     * Returns parent node or, if root node, self
     * @return NodeEntity
     */
    public Node getParent() 
    {
        return parent;
    }
    
    /**
     * Returns parent primary key
     * @return int
     */
    public int getParentId()
    {
        return nodeEntity.get_parent_id();
    }
    

    /**
     * Construct a Node from its persisted state. 
     * @param nodeEntity The persisted object
     * @param parent The parent on the graph under construction or null if this is the first node of the graph
     */
    public Node(NodeEntity nodeEntity, Node parent)
    {
        if (nodeEntity  == null)
            throw new IllegalArgumentException("Parameter nodeEntity is null");
        this.nodeEntity = nodeEntity;
        if (parent != null)
        {
        	this.parent = parent;
            // Check if a Node with same id already in children list
            // Replace it, if found, as it only a placeholder
            Node existingNode = null;
            for (Node childNode: parent.getChildren())
            {   
                if (childNode.getId() == nodeEntity.get_id())
                {
                    existingNode = childNode;
                    break;
                }
            }
            if (existingNode != null)
                parent.getChildren().remove(existingNode);
            // Now add this node to the parent's children list
            parent.getChildren().add(this);
            // Set level to one more than parent's
            setLevel(parent.getLevel() + 1);
        }
        else
        {   // No parent specified, so create a root node to be parent
            Node rootNode = new Node();
            rootNode.setId(nodeEntity._parent_id);
            rootNode.getChildren().add(this);
            rootNode.setLevel(nodeEntity.level - 1);
            this.parent = rootNode;
        }
        // Transfer nodeEntity's chidren to this Node 
        if ((nodeEntity._children != null) && (nodeEntity._children.size() > 0))
        {
            for (NodeEntity childEntity: nodeEntity._children)
            {
                if (childEntity._id != childEntity._parent_id) // Never add top node as a child
                    new Node(childEntity, this);
            }
        }
    }
    
    /**
     * Create an empty Node object and attach to an existing graph
     * @param model Ordinal of model enum type
     * @param parent Parent node - use Node.rootNodeNewInstance() for first Node in graph
     */
    public Node(int model, Node parent)
    {
        if (parent == null)
            throw new IllegalArgumentException("Parameter parent is null");
    	nodeEntity = new NodeEntity();
        nodeEntity.setModel(model);
        nodeEntity.setParent(parent.getNodeEntity());
        nodeEntity.setLevel(parent.getLevel() + 1);
        //nodeEntity.set_id(0);
        //nodeEntity.setName("");
        //nodeEntity.setTitle("");
        this.parent = parent;
        parent.getChildren().add(this);
    }
    
    /**
     * Set properties
     * @param properties Map&lt;String, Object*gt;
     */
    public void setProperties(Map<String, Object> properties) 
    {
        this.properties = properties;
    }
   
    /** 
     * Returns children list
     * @return List&lt;Node&gt;
     */
    public List<Node> getChildren()
    {
        if (children == null)
            children = new ArrayList<Node>();
        return children;
    }
    
    /**
     * Returns properties
     * @return Map&lt;String,Object&gt;
     */
    public Map<String,Object> getProperties()
    {
        if (properties == null)
            properties = new HashMap<String,Object>();
        return properties;
    }

    protected NodeEntity getNodeEntity()
    {
    	return nodeEntity;
    }

    /**
     * Sets depth in graph, starting at 1 for the solitary root node
     * @param level int
     */
    protected void setLevel(int level) 
    {
        nodeEntity.setLevel(level);
    }

    /**
     * Set primary key
     * @param _id int
     */
    protected void setId(int id)
    {
        nodeEntity.set_id(id);
    }

    /**
     * Returns root Node object
     * @return Node
     */
    public static Node rootNodeNewInstance()
    {
        return new Node();
    }
   
    /**
     * Returns property value as quote-delimited text for logging perposes 
     * @param node Node from which to extract property
     * @param key Property name
     * @param defaultValue Value to return if property not found 
     * @return String
     */
    public static String getProperty(Node node, String key, String defaultValue)
    {
        Object object = (node.properties == null ? null : node.properties.get(key));
        if (object == null)
        {
            if (defaultValue == null)
                return "null";
            else
                return "'" + defaultValue + "'";
        }
        return "'" + object.toString() + "'";
    }
    
    /**
     * Marshall a nodeEntity object into a graph fragment containing all ancestors and immediate children.
     * Deletes children of other Nodes in graph to prevent triggering lazy fetches and thus potentially fetching the entire graph
     * @param nodeEntity The object to marshall
     * @return Root node of graph
     */
    public static Node marshall(NodeEntity nodeEntity)
    {
        Deque<NodeEntity> nodeEntityDeque = new ArrayDeque<NodeEntity>();
        // Walk up to top node
        while (nodeEntity != null)
        {
             nodeEntityDeque.add(nodeEntity);
             if  (nodeEntity.get_id() == nodeEntity.get_parent_id())// Top of tree indicated by self parent
                break;
             nodeEntity = nodeEntity.getParent();
        }
        // Now build graph fragment
        Node node = Node.rootNodeNewInstance();
        Iterator<NodeEntity> nodeEntityIterator = nodeEntityDeque.descendingIterator();
        while (nodeEntityIterator.hasNext())
        {
            nodeEntity = nodeEntityIterator.next();
            node = new Node(nodeEntity, node);
        }
        return node;
    }
}
