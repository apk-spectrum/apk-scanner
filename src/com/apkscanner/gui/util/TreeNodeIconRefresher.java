package com.apkscanner.gui.util;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class TreeNodeIconRefresher implements ImageObserver {
	private JTree tree;
	private ArrayList<TreePath> treePaths = new ArrayList<TreePath>();

	public TreeNodeIconRefresher(JTree tree) {
		this.tree = tree;
	}

	public TreeNodeIconRefresher(JTree tree, int nodeSize) {
		this.tree = tree;
		treePaths = new ArrayList<TreePath>(nodeSize);
	}

	public void addTreeNode(TreeNode node) {
		if(tree == null || node == null) return;

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		TreePath treePath = new TreePath(model.getPathToRoot(node));

		synchronized(treePaths) {
			if(!treePaths.contains(treePath)) {
				treePaths.add(treePath);
			} else {
				//Log.v("existed path " + treePath.toString());
			}
		}
	}


	public void removeTreeNode(TreeNode node) {
		if(tree == null || node == null) return;

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		TreePath treePath = new TreePath(model.getPathToRoot(node));

		synchronized(treePaths) {
			if(treePaths.contains(treePath)) {
				treePaths.remove(treePath);
			} else {
				//Log.v("unknown path " + treePath.toString());
			}
		}
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if (tree == null) return false;

		if(!tree.isShowing()) {
			//Log.v("no visible");
			return false;
		}

		TreePath[] treePathCopy = null;
		synchronized (treePaths) {
			treePathCopy = treePaths.toArray(new TreePath[treePaths.size()]);
		}

		if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
			Rectangle treeVisibleRect = tree.getVisibleRect();
			for(TreePath path: treePathCopy) {
				Rectangle rect = tree.getPathBounds(path);
				if (rect != null && (treeVisibleRect == null || rect.intersects(treeVisibleRect))) {
					tree.repaint(rect);
				} else {
					synchronized (treePaths) {
						if(treePaths.contains(path)) {
							treePaths.remove(path);
						}
					}
				}
			}
		}

		synchronized (treePaths) {
			if(treePaths.isEmpty()) {
				//Log.v("treePaths empty");
				return false;
			}
		}

		return (infoflags & (ALLBITS | ABORT)) == 0;
	}
}
