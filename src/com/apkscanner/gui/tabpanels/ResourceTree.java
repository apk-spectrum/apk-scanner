package com.apkscanner.gui.tabpanels;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.WidgetInfo;
import com.apkscanner.gui.UiEventHandler;
import com.apkspectrum.swing.SortedMutableTreeNode;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.URITool;

public class ResourceTree extends JTree {
	private static final long serialVersionUID = 3376111906679444249L;

	private DefaultMutableTreeNode rootNode;

	public ResourceTree(final ActionListener listener) {
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
				Object resObj = node.getUserObject();
				if (resObj instanceof DefaultNodeData) {
					setIcon(((DefaultNodeData) resObj).getIcon(tree));
				} else if(resObj instanceof TreeNodeData) {
					setIcon(((TreeNodeData) resObj).getIcon());
				} else {
					String suffix = FileUtil.getSuffix(node.toString());
					setIcon(SystemUtil.getExtensionIcon(suffix));
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

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(listener == null) return;
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (getPathForLocation(e.getX(), e.getY()) == null) return;
					listener.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED,
							UiEventHandler.ACT_CMD_OPEN_RESOURCE_TREE_FILE, e.getWhen(), e.getModifiers()));
				}
			}
		});

		addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				if(listener == null) return;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				if (node.getUserObject() instanceof TreeNodeData) {
					TreeNodeData resObj = (TreeNodeData) node.getUserObject();
					if(".img".equals(resObj.getExtension())) {
						listener.actionPerformed(new ActionEvent(event, ActionEvent.ACTION_PERFORMED,
								UiEventHandler.ACT_CMD_LOAD_FS_IMG_FILE, System.currentTimeMillis(), 0));
					}
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) { }
		});
	}

	public void addTreeNodes(final String apkFilePath, final String[] resList) {
		File apkFile = new File(apkFilePath);
		final DefaultNodeData rootResObj = new DefaultNodeData(apkFile);
		rootResObj.setLoadingState(true);
		rootNode = new ResourceNode(rootResObj);

		final DefaultMutableTreeNode[] typeNodes =
				new DefaultMutableTreeNode[ResourceType.COUNT.getInt()];
		typeNodes[ResourceType.ROOTRES.getInt()] = rootNode;

		((DefaultTreeModel)getModel()).setRoot(rootNode);

		final String uriPath = "jar:" + apkFile.toURI() + "!/";

		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					final int CHUNK_SIZE = 30;
					for (int chunk = 0; chunk < resList.length; chunk += CHUNK_SIZE) {
						final int start = chunk;
						EventQueue.invokeAndWait(new Runnable() {
							public void run() {
								for (int i = start; i < start + CHUNK_SIZE && i < resList.length; i++) {
									if (resList[i].endsWith("/") || resList[i].startsWith("lib/")) continue;

									ResourceObject resObj = new ResourceObject(uriPath + URITool.encodeURI(resList[i]));
									DefaultMutableTreeNode node = new ResourceNode(resObj);

									ResourceType resType = resObj.getResourceType();

									DefaultMutableTreeNode parentNode = typeNodes[resType.getInt()];
									if (parentNode == null) {
										Object typeObj = new ResourceObject(resType);
										parentNode = new ResourceNode(typeObj);
										typeNodes[resType.getInt()] = parentNode;

										rootNode.add(parentNode);
									}

									if (resType.isMultiConfigType()) {
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
							rootResObj.setLoadingState(false);

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

	public void addTreeNodes(final String apkFilePath, final WidgetInfo widgetInfo) {
		TreeNodeData resObj = new UserNodeData(widgetInfo.name, ".xml", widgetInfo.xmlString);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(resObj);
		((DefaultTreeModel)getModel()).setRoot(rootNode = node);

		TreePath treepath = new TreePath(rootNode.getPath());

		node = makeLabelNode("Label", widgetInfo.resourceMap.get("label"));
		if(node != null) rootNode.add(node);

		final String uriPath = "jar:" + new File(apkFilePath).toURI() + "!/";

		Entry<String, ResourceInfo[]> resource = null;
		resource = widgetInfo.resourceMap.get("meta-data/resource");
		if(widgetInfo.xmlMetaData != null && resource != null && resource.getValue() != null) {
			resObj = new UserNodeData("META-DATA", resource.getKey(), widgetInfo.xmlMetaData);
			rootNode.add(node = new ResourceNode(resObj));
			TreePath metaDatPath = new TreePath(node.getPath());
			for(ResourceInfo res: resource.getValue()) {
				DefaultMutableTreeNode resNode = new DefaultMutableTreeNode(new WidgetResData(uriPath + URITool.encodeURI(res.name)));
				node.add(resNode);

				Entry<String, ResourceInfo[]> resSet = null;
				DefaultMutableTreeNode resChildNode = null;

				if(widgetInfo.resourceMap.containsKey(res.name + "/initialLayout")) {
					resSet = widgetInfo.resourceMap.get(res.name + "/initialLayout");
					resChildNode = null;
					for(ResourceInfo layoutRes: resSet.getValue()) {
						if(resChildNode == null) {
							resChildNode = new SortedMutableTreeNode(new WidgetResData("Initial Layout",
									resSet.getKey().replaceAll("@.*/", "@"), uriPath + URITool.encodeURI(layoutRes.name)));
							resNode.add(resChildNode);
						}
						resChildNode.add(new DefaultMutableTreeNode(new WidgetResData(uriPath + URITool.encodeURI(layoutRes.name))));
					}
				}

				if(widgetInfo.resourceMap.containsKey(res.name + "/previewImage")) {
					resSet = widgetInfo.resourceMap.get(res.name + "/previewImage");
					resChildNode = null;
					for(ResourceInfo iconRes: resSet.getValue()) {
						if(resChildNode == null) {
							resChildNode = new SortedMutableTreeNode(new WidgetResData("Preview Image",
									resSet.getKey().replaceAll("@.*/", "@"), uriPath + URITool.encodeURI(iconRes.name)));
							resNode.add(resChildNode);
						}
						resChildNode.add(new DefaultMutableTreeNode(new WidgetResData(uriPath + URITool.encodeURI(iconRes.name))));
					}
				}

				if(widgetInfo.resourceMap.containsKey(res.name + "/shortcuts")) {
					ResourceInfo[] shortcuts = widgetInfo.resourceMap.get(res.name + "/shortcuts").getValue();
					if(shortcuts == null || shortcuts.length == 0) continue;
					List<TreePath> shortCutPath = new ArrayList<>();
					for(ResourceInfo shortcut: shortcuts) {
						String xmlString = widgetInfo.resourceMap.get(shortcut.name + "xml").getKey();
						DefaultMutableTreeNode shortcutNode = new DefaultMutableTreeNode(new UserNodeData(shortcut.configuration, ".xml", xmlString));
						resNode.add(shortcutNode);

						DefaultMutableTreeNode shortcutResNode = null;
						shortcutResNode = makeLabelNode("Long Label", widgetInfo.resourceMap.get(shortcut.name + "shortcutLongLabel"));
						if(shortcutResNode != null) shortcutNode.add(shortcutResNode);
						shortcutResNode = makeLabelNode("Short Label", widgetInfo.resourceMap.get(shortcut.name + "shortcutShortLabel"));
						if(shortcutResNode != null) shortcutNode.add(shortcutResNode);
						shortcutResNode = makeLabelNode("Disabled Label", widgetInfo.resourceMap.get(shortcut.name + "shortcutDisabledMessage"));
						if(shortcutResNode != null) shortcutNode.add(shortcutResNode);

						if(widgetInfo.resourceMap.containsKey(shortcut.name + "icon")) {
							resSet = widgetInfo.resourceMap.get(shortcut.name + "icon");
							resChildNode = null;
							for(ResourceInfo iconRes: resSet.getValue()) {
								if(resChildNode == null) {
									resChildNode = new SortedMutableTreeNode(new WidgetResData("Icon",
											resSet.getKey().replaceAll("@.*/", "@"), uriPath + URITool.encodeURI(iconRes.name)));
									shortcutNode.add(resChildNode);
								}
								resChildNode.add(new DefaultMutableTreeNode(new WidgetResData(uriPath + URITool.encodeURI(iconRes.name))));
							}
						}

						if(widgetInfo.shortcutId.equals(shortcut.configuration)) {
							shortCutPath.add(new TreePath(shortcutNode.getPath()));
						}
					}

					if(!shortCutPath.isEmpty()) {
						treepath = shortCutPath.get(0);
						for(TreePath path: shortCutPath)
							expandPath(path);
					}
				}
			}
			expandPath(metaDatPath);
		}

		setSelectionPath(treepath);
	}

	private DefaultMutableTreeNode makeLabelNode(String nodeName, Entry<String, ResourceInfo[]> resSet) {
		if(resSet == null || resSet.getValue() == null || resSet.getValue().length == 0) {
			return null;
		}
		StringBuilder labelBuilder = new StringBuilder();
		for(ResourceInfo r: resSet.getValue()) {
			if(r.configuration == null || r.configuration.isEmpty() || "default".equals(r.configuration)) {
				labelBuilder.append(r.name != null ? r.name : "No such label");
			} else {
				labelBuilder.append("[").append(r.configuration).append("] ").append(r.name);
			}
			labelBuilder.append("\n");
		}
		UserNodeData resObj = new UserNodeData(nodeName, resSet.getKey(),
				resSet.getKey().replaceAll("@.*/", "@"), labelBuilder.toString().trim());
		ResourceNode node = new ResourceNode(resObj);
		return node;
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
			TreeNodeData resObj = null;
			if (childNode.getUserObject() instanceof TreeNodeData) {
				resObj = (TreeNodeData) childNode.getUserObject();
			}
			if (resObj.getLabel().equals(string) || (ignoreCase && resObj.getLabel().equalsIgnoreCase(string))) {
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

		if (parent.getUserObject() instanceof TreeNodeData) {
			temp = ((TreeNodeData) (parent.getUserObject())).getLabel();
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

	public boolean setSelectNodeByPath(String path) {
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getModel().getRoot();

		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> e = rootNode.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			if (node.getUserObject() instanceof TreeNodeData) {
				TreeNodeData temp = (TreeNodeData) node.getUserObject();
				if (temp.getPath().equals(path)) {
					TreePath treepath = new TreePath(node.getPath());
					setSelectionPath(treepath);
					scrollPathToVisible(treepath);
					return true;
				}
			}
		}
		return false;
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
