package com.apkscanner.gui.tabpanels;

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

    @Override
    public void setUserObject(Object userObject) {
    	super.setUserObject(userObject);
    	if(userObject instanceof ResourceObject) {
    		((ResourceObject) userObject).setNode(this);
    	}
    }
}
