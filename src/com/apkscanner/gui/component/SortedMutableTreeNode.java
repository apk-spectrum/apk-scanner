package com.apkscanner.gui.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class SortedMutableTreeNode extends DefaultMutableTreeNode
{
	private static final long serialVersionUID = -3207855054891986462L;

	protected Comparator<? super MutableTreeNode> comparator = new Comparator<MutableTreeNode>() {
		@Override
		public int compare(MutableTreeNode o1, MutableTreeNode o2) {
			if(o1.isLeaf() != o2.isLeaf()) {
				return o1.isLeaf() ? 1 : -1;
			}
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	};

    public SortedMutableTreeNode() {
    	super(null);
    }

    public SortedMutableTreeNode(Object userObject) {
    	super(userObject);
    }

    public SortedMutableTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public SortedMutableTreeNode(Comparator<? super MutableTreeNode> comparator) {
    	this(null, comparator);
    }

    public SortedMutableTreeNode(Object userObject, Comparator<? super MutableTreeNode> comparator) {
    	this(userObject, comparator, true);
    }

    public SortedMutableTreeNode(Object userObject, Comparator<? super MutableTreeNode> comparator, boolean allowsChildren) {
        super(userObject, allowsChildren);
        setComparator(comparator);
    }

	@Override
	public void add(MutableTreeNode newChild) {
		int idx = getChildCount();
		if(comparator != null) {
			for(; idx > 0; --idx) {
				MutableTreeNode e = (MutableTreeNode) getChildAt(idx - 1);
				if(comparator.compare(newChild, e) > 0) break;
			}
		}
        if(newChild != null && newChild.getParent() == this) idx--;
		super.insert(newChild, idx);

		if(idx == 0 && getChildCount() == 1 && getChildAt(0) == newChild) {
			TreeNode parent = getParent();
			if(parent instanceof SortedMutableTreeNode) {
				((SortedMutableTreeNode)parent).sort();
			}
		}
	}

	@Override
	public void insert(MutableTreeNode newChild, int childIndex) {
		if(comparator != null) {
			throw new UnsupportedOperationException("You can't add elements anywhere when was set a comparator.");
		}
		super.insert(newChild, childIndex);
	}

	public void sort() {
		@SuppressWarnings("unchecked")
		Vector<MutableTreeNode> c = (Vector<MutableTreeNode>) children;
		Collections.sort(c, getComparator());
	}

	public void sort(Comparator<? super MutableTreeNode> comparator) {
		if(comparator == null) return;
		setComparator(comparator);
		sort();
	}

	public void setComparator(Comparator<? super MutableTreeNode> comparator) {
		this.comparator = comparator;
	}

	public Comparator<? super MutableTreeNode> getComparator() {
		return comparator;
	}
}
