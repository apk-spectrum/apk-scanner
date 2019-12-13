package com.apkscanner.gui.tabpanels;

import java.io.File;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import com.apkscanner.gui.component.SortedMutableTreeNode;

public class ResourceNode extends SortedMutableTreeNode
{
	private static final long serialVersionUID = -7644917198175319373L;

	private static final Comparator<? super MutableTreeNode> nodeComparator = new Comparator<MutableTreeNode>() {
		@Override
		public int compare(MutableTreeNode o1, MutableTreeNode o2) {
			if(o1 instanceof DefaultMutableTreeNode && o2 instanceof DefaultMutableTreeNode) {
				Object obj1 = ((DefaultMutableTreeNode) o1).getUserObject();
				Object obj2 = ((DefaultMutableTreeNode) o2).getUserObject();
				if(obj1 instanceof ResourceObject && obj2 instanceof ResourceObject) {
					if(((ResourceObject) obj1).isFolder != ((ResourceObject) obj2).isFolder) {
						return ((ResourceObject) obj1).isFolder ? -1 : 1;
					}
				}
			} else if(o1 instanceof DefaultMutableTreeNode) {
				return -1;
			} else if(o2 instanceof DefaultMutableTreeNode) {
				return 1;
			}
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	};

	public ResourceNode() {
    	this(null);
    }

    public ResourceNode(Object userObject) {
    	this(userObject, true);
    }

    public ResourceNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
        setComparator(nodeComparator);
    	if(userObject instanceof ResourceObject) {
    		((ResourceObject) userObject).setNode(this);
    	}
    }

    public void add(File files) {
    	if(files.isDirectory()) {
			for(File c: files.listFiles()) {
				ResourceNode childNode = new ResourceNode(new ResourceObject(c));
				add(childNode);
				if(c.isDirectory()) {
					childNode.add(c);
				}
			}
    	} else {
    		ResourceNode childNode = new ResourceNode(new ResourceObject(files));
    		add(childNode);
    	}
    }

    @Override
    public void add(MutableTreeNode newChild) {
    	super.add(newChild);
    	if(newChild instanceof DefaultMutableTreeNode) {
    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) newChild; 
    		Object uo = node.getUserObject();
	    	if(uo instanceof ResourceObject) {
				if(((ResourceObject) uo).attr == ResourceObject.ATTR_FS_IMG) {
					ResourceObject obj = new ResourceObject("Loading...");
					node.add(new ResourceNode(obj));
					obj.setLoadingState(true);
				}
	    	}
    	}
    }

    @Override
    public void setUserObject(Object userObject) {
    	super.setUserObject(userObject);
    	if(userObject instanceof ResourceObject) {
    		((ResourceObject) userObject).setNode(this);
    	}
    }

    @Override
    public Object clone() {
    	ResourceNode newNode = (ResourceNode) super.clone();
    	if(userObject instanceof ResourceObject) {
    		newNode.setUserObject(((ResourceObject) userObject).clone());
    	}
    	return newNode;
    }
}
