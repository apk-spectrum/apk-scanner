package com.apkscanner.gui.tabpanels;

import java.io.File;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.apkspectrum.swing.SortedMutableTreeNode;
import com.apkspectrum.util.SystemUtil;

public class ResourceNode extends SortedMutableTreeNode
{
	private static final long serialVersionUID = -7644917198175319373L;

	private static final Comparator<? super TreeNode> nodeComparator = new Comparator<TreeNode>() {
		@Override
		public int compare(TreeNode o1, TreeNode o2) {
			if(o1 instanceof DefaultMutableTreeNode && o2 instanceof DefaultMutableTreeNode) {
				Object obj1 = ((DefaultMutableTreeNode) o1).getUserObject();
				Object obj2 = ((DefaultMutableTreeNode) o2).getUserObject();
				if(obj1 instanceof TreeNodeData && obj2 instanceof TreeNodeData) {
					if(((TreeNodeData) obj1).isFolder() != ((TreeNodeData) obj2).isFolder()) {
						return ((TreeNodeData) obj1).isFolder() ? -1 : 1;
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
    	if(userObject instanceof DefaultNodeData) {
    		((DefaultNodeData) userObject).setNode(this);
    	}
    }

    public void add(File files) {
    	if(files.isDirectory()) {
			for(File c: files.listFiles()) {
				ResourceNode childNode = new ResourceNode(new DefaultNodeData(c));
				add(childNode);
				if(c.isDirectory()) {
					childNode.add(c);
				}
			}
    	} else {
    		ResourceNode childNode = new ResourceNode(new DefaultNodeData(files));
    		add(childNode);
    	}
    }

    @Override
    public void add(MutableTreeNode newChild) {
    	super.add(newChild);
    	if(newChild instanceof DefaultMutableTreeNode) {
    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) newChild;
    		Object uo = node.getUserObject();
	    	if(uo instanceof TreeNodeData) {
				if(".img".equals(((TreeNodeData) uo).getExtension())) {
					DefaultNodeData obj;
					if(SystemUtil.isWindows()) {
						obj = new DefaultNodeData("Loading...");
						obj.setLoadingState(true);
					} else {
						obj = new DefaultNodeData("Not Supported in " + SystemUtil.OS);
					}
					node.add(new ResourceNode(obj));
				}
	    	}
    	}
    }

    @Override
    public void setUserObject(Object userObject) {
    	super.setUserObject(userObject);
    	if(userObject instanceof DefaultNodeData) {
    		((DefaultNodeData) userObject).setNode(this);
    	}
    }

    @Override
    public Object clone() {
    	ResourceNode newNode = (ResourceNode) super.clone();
    	if(userObject instanceof DefaultNodeData) {
    		newNode.setUserObject(((DefaultNodeData) userObject).clone());
    	}
    	return newNode;
    }
}
