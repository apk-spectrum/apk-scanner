package com.apkscanner.gui.tabpanels;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.apkscanner.util.SystemUtil;

public class ResourceTree extends JTree {
	private static final long serialVersionUID = 3376111906679444249L;

	private DefaultMutableTreeNode rootNode;

	public ResourceTree() {
		super(new DefaultTreeModel(new DefaultMutableTreeNode()));
		rootNode = (DefaultMutableTreeNode) getModel().getRoot();

		setOpaque(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 6248791058116909814L;
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof ResourceObject) {
					ResourceObject resObj = (ResourceObject) node.getUserObject();
					if(resObj.getLoadingState()) {
						setIcon(resObj.getIconWithObserver(tree));
					} else {
						setIcon(resObj.getIcon());
					}
				} else {
					setIcon(SystemUtil.getExtensionIcon(ResourceObject.getExtension(node.toString())));
				}
				return c;
			}
		});

		setUI(new BasicTreeUI() {
			@Override
			public Rectangle getPathBounds(JTree tree, TreePath path) {
				if (tree != null && treeState != null) {
					return getPathBounds(path, tree.getInsets(), new Rectangle());
				}
				return null;
			}

			private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
				bounds = treeState.getBounds(path, bounds);
				if (bounds != null) {
					bounds.width = tree.getWidth();
					bounds.y += insets.top;
				}
				return bounds;
			}
		});
	}

	public void addTreeNodes(final String apkFilePath, final String[] resList) {
		rootNode = new ResourceNode(new ResourceObject(new File(apkFilePath)));

		final DefaultMutableTreeNode[] typeNodes =
				new DefaultMutableTreeNode[ResourceType.COUNT.getInt()];
		typeNodes[ResourceType.ROOTRES.getInt()] = rootNode;

		((DefaultTreeModel)getModel()).setRoot(rootNode);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					final int CHUNK_SIZE = 30;
					for (int chunk = 0; chunk < resList.length; chunk += CHUNK_SIZE) {
						final int start = chunk;
						EventQueue.invokeAndWait(new Runnable() {
							public void run() {
								for (int i = start; i < start + CHUNK_SIZE && i < resList.length; i++) {
									if (resList[i].endsWith("/") || resList[i].startsWith("lib/")
											/*|| this.nameList[i].startsWith("META-INF/")*/) continue;

									ResourceObject resObj = new ResourceObject(resList[i]);
									DefaultMutableTreeNode node = new ResourceNode(resObj);

									DefaultMutableTreeNode parentNode = typeNodes[resObj.type.getInt()];
									if (parentNode == null) {
										Object typeObj = new ResourceObject(resObj.type);
										parentNode = new ResourceNode(typeObj);
										typeNodes[resObj.type.getInt()] = parentNode;

										rootNode.add(parentNode);
									}

									if (resObj.type.isMultiConfigType()) {
										DefaultMutableTreeNode findnode = findNode(parentNode, resObj.getFileName(), false, false);
										if (findnode != null) {
											if (findnode.isLeaf()) {
												findnode.add((DefaultMutableTreeNode) findnode.clone());
											}
											parentNode = findnode;
										}
									}
									parentNode.add(node);
								}
							}
						});
						Thread.yield();
					}

					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							expandOrCollapsePath(new TreePath(rootNode.getPath()), 1, 0, true);

							for(DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getFirstChild();
									node != null; node = node.getNextSibling()) {
								if("AndroidManifest.xml".equals(node.toString())) {
									TreePath treepath = new TreePath(node.getPath());
									setSelectionPath(treepath);
									//scrollPathToVisible(treepath);
									break;
								}
							}
						}
					});
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	public final DefaultMutableTreeNode findNode(DefaultMutableTreeNode node, String string, boolean ignoreCase,
			boolean recursively) {
		DefaultMutableTreeNode ret = null;
		if (node == null) {
			node = (DefaultMutableTreeNode) getModel().getRoot();
			if (node == null)
				return null;
		}

		DefaultMutableTreeNode childNode = null;
		if (node.getChildCount() > 0) {
			childNode = (DefaultMutableTreeNode) node.getFirstChild();
		}
		while (childNode != null) {
			ResourceObject resObj = null;
			if (childNode.getUserObject() instanceof ResourceObject) {
				resObj = (ResourceObject) childNode.getUserObject();
			}
			if (resObj.label.equals(string) || (ignoreCase && resObj.label.equalsIgnoreCase(string))) {
				ret = childNode;
				break;
			}
			if (recursively && childNode.getDepth() > 0) {
				ret = findNode(childNode, string, ignoreCase, recursively);
				if (ret != null)
					break;
			}
			childNode = childNode.getNextSibling();
		}

		return ret;
	}

	public void searchTree(String str) {
		((DefaultTreeModel) getModel()).setRoot(createFilteredTree(rootNode, str));
		repaint();
	}

	private DefaultMutableTreeNode createFilteredTree(DefaultMutableTreeNode parent, String filter) {
		int c = parent.getChildCount();
		DefaultMutableTreeNode fparent = (DefaultMutableTreeNode) parent.clone();
		String temp;

		if (parent.getUserObject() instanceof ResourceObject) {
			temp = ((ResourceObject) (parent.getUserObject())).label;
		} else {
			temp = parent.toString();
		}
		temp = temp.toLowerCase();

		boolean matches = false;
		String[] pattern = filter.toLowerCase().split(";");
		for(String p: pattern) {
			if(temp.contains(p)) {
				matches = true;
				break;
			}
		}

		for (int i = 0; i < c; ++i) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent.getChildAt(i);
			DefaultMutableTreeNode f = createFilteredTree(n, filter);
			if (f != null) {
				fparent.add(f);
				matches = true;
			}
		}
		return matches ? fparent : null;
	}

	public void expandOrCollapsePath(TreePath treePath, int level, int currentLevel,
			boolean expand) {
		// System.err.println("Exp level "+currentLevel+", exp="+expand);
		if (expand && level <= currentLevel && level > 0)
			return;

		TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
		TreeModel treeModel = getModel();
		if (treeModel.getChildCount(treeNode) >= 0) {
			for (int i = 0; i < treeModel.getChildCount(treeNode); i++) {
				TreeNode n = (TreeNode) treeModel.getChild(treeNode, i);
				TreePath path = treePath.pathByAddingChild(n);
				expandOrCollapsePath(path, level, currentLevel + 1, expand);
			}
			if (!expand && currentLevel < level)
				return;
		}
		if (expand) {
			expandPath(treePath);
		} else {
			collapsePath(treePath);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (getSelectionCount() > 0) {
			if(getSelectionRows() == null) {
				TreePath treepath = new TreePath(getModel().getRoot());
				setSelectionPath(treepath);
			}
			for (int i : getSelectionRows()) {
				Rectangle r = getRowBounds(i);
				g.setColor(((DefaultTreeCellRenderer) getCellRenderer()).getBackgroundSelectionColor());
				// g.setColor(Color.BLUE);
				g.fillRect(0, r.y, getWidth(), r.height);
			}
		}
		super.paintComponent(g);
		if (getLeadSelectionPath() != null) {
			Rectangle r = getRowBounds(getRowForPath(getLeadSelectionPath()));
			g.setColor(hasFocus()
					? ((DefaultTreeCellRenderer) getCellRenderer()).getBackgroundSelectionColor().darker()
							: ((DefaultTreeCellRenderer) getCellRenderer()).getBackgroundSelectionColor());
			// g.setColor(Color.RED);
			g.drawRect(0, r.y, getWidth() - 1, r.height - 1);
		}
	}
}
