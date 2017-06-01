package com.apkscanner.gui.util;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public final class FilteredTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = -716929594552075062L;

	private String filter = "";

	public FilteredTreeModel(TreeNode TreeNode) {
		super(TreeNode);
	}

	public void setFilter(final String filter) {
		String newFilter = filter != null ? filter : "";
		if(!this.filter.equals(newFilter)) { 
			this.filter = newFilter;
			reload();
		}
	}

	public String getFilter() {
		return filter;
	}

	public boolean isMatched(final Object node) {
		return node.toString().matches("(?i).*"+filter+".*");
	}

	private boolean recursiveMatch(final Object node, final String filter) {
		//boolean matches = node.toString().contains(filter);
		boolean matches = node.toString().matches("(?i).*"+filter+".*");

		int childCount = super.getChildCount(node);
		for (int i = 0; !matches && i < childCount; i++) {
			Object child = super.getChild(node, i);
			matches |= recursiveMatch(child, filter);
		}

		return matches;
	}

	@Override
	public Object getChild(final Object parent, final int index) {
		int count = 0;
		int childCount = super.getChildCount(parent);
		for (int i = 0; i < childCount; i++) {
			Object child = super.getChild(parent, i);
			if (recursiveMatch(child, filter)) {
				if (count == index) {
					return child;
				}
				count++;
			}
		}
		return null;
	}

	@Override
	public int getChildCount(final Object parent) {
		int count = 0;
		int childCount = super.getChildCount(parent);
		for (int i = 0; i < childCount; i++) {
			Object child = super.getChild(parent, i);
			if (recursiveMatch(child, filter)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object childToFind) {
		int childCount = super.getChildCount(parent);
		for (int i = 0; i < childCount; i++) {
			Object child = super.getChild(parent, i);
			if (recursiveMatch(child, filter)) {
				if (childToFind.equals(child)) {
					return i;
				}
			}
		}
		return -1;
	}
}
