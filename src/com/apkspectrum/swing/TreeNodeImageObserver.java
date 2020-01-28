package com.apkspectrum.swing;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeNodeImageObserver implements ImageObserver {
	protected JTree tree;
	protected DefaultMutableTreeNode node;
	protected ImageIcon icon;
	protected boolean stopFlag;

	public TreeNodeImageObserver(JTree tree, DefaultMutableTreeNode node, ImageIcon icon) {
		this.tree = tree;
		this.node = node;
		this.icon = icon;
	}

	public void start() {
		stopFlag = false;
		if(icon != null) icon.setImageObserver(this);
	}

	public void stop() {
		stopFlag = true;
	}

	protected boolean isStop() {
		return stopFlag || !tree.getModel().getRoot().equals(node.getRoot());
	}

	@Override
	public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
		if(node == null) return false;

		if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
			TreePath path = new TreePath(node.getPath());
			Rectangle rect = tree.getPathBounds(path);
			if (rect != null) {
				tree.repaint(rect);
			}
		}

		if (isStop()) {
			icon.setImageObserver(null);
			return false;
		}

		return (flags & (ALLBITS | ABORT)) == 0;
	}
}