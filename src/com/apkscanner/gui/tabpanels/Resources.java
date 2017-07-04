package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.apkscanner.Launcher;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.FilteredTreeModel;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.gui.util.ResouceContentsPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.dex2jar.Dex2JarWrapper;
import com.apkscanner.tool.jd_gui.JDGuiLauncher;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

public class Resources extends JPanel implements TabDataObject {
	private static final long serialVersionUID = -934921813626224616L;

	private ResouceContentsPanel contentPanel;

	private String[] nameList = null;
	private String apkFilePath = null;
	private String tempWorkPath = null;
	// private String appIconPath = null;
	private String[] resourcesWithValue = null;

	private JTree tree;
	private DefaultMutableTreeNode top;
	private DefaultMutableTreeNode[] eachTypeNodes;

	private FilteredTreeModel filteredModel;
	private JTextField textField;
	JButton findicon;
	JButton refreshicon;
	//private Boolean isFilter = false;

	ResouceTreeCellRenderer renderer;

	private HashMap<String, Icon> fileIcon = new HashMap<String, Icon>();

	static public TreeFocusChanger treefocuschanger;

	ImageIcon Animateimageicon = Resource.IMG_RESOURCE_TREE_OPEN_JD.getImageIcon();
	NodeImageObserver ImageObserver;

	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_FIND = "TREE FIND";
	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH = "TREE REFRESH";

	public enum ResourceType {
		ANIMATION(0), ANIM(1), COLOR(2), DRAWABLE(3), MIPMAP(4), LAYOUT(5), MENU(6), RAW(7), VALUES(8), XML(9), ASSET(
				10), METAINF(11), ETC(12), COUNT(13);

		private int type;

		ResourceType(int type) {
			this.type = type;
		}

		int getInt() {
			return type;
		}

		public String toString() {
			if(this.equals(METAINF))
				return "META-INF";
			else
				return super.toString().toLowerCase();
		}
	}

	static public abstract interface TreeFocusChanger {
		public void setTreeFocus(String path, int line, String string);

		public String getImagefilePath(String findfilename);
	}

	public class ResourceObject {
		public static final int ATTR_AXML = 1;
		public static final int ATTR_XML = 2;
		public static final int ATTR_IMG = 3;
		public static final int ATTR_QMG = 4;
		public static final int ATTR_TXT = 5;
		public static final int ATTR_CERT = 6;
		public static final int ATTR_ETC = 7;

		public String label;
		public Boolean isFolder;
		public String path;
		public String config;
		public ResourceType type;
		public int attr;
		public int childCount;
		public Boolean isLoading;

		public ResourceObject(String path, boolean isFolder) {
			this.path = path;
			this.isFolder = isFolder;
			this.isLoading = false;
			if (path.startsWith("res/animation")) {
				type = ResourceType.ANIMATION;
			} else if (path.startsWith("res/anim")) {
				type = ResourceType.ANIM;
			} else if (path.startsWith("res/color")) {
				type = ResourceType.COLOR;
			} else if (path.startsWith("res/drawable")) {
				type = ResourceType.DRAWABLE;
			} else if (path.startsWith("res/mipmap")) {
				type = ResourceType.MIPMAP;
			} else if (path.startsWith("res/layout")) {
				type = ResourceType.LAYOUT;
			} else if (path.startsWith("res/menu")) {
				type = ResourceType.MENU;
			} else if (path.startsWith("res/raw")) {
				type = ResourceType.RAW;
			} else if (path.startsWith("res/values")) {
				type = ResourceType.VALUES;
			} else if (path.startsWith("res/xml")) {
				type = ResourceType.XML;
			} else if (path.startsWith("assets")) {
				type = ResourceType.ASSET;
			} else if(path.startsWith("META-INF")) {
				type = ResourceType.METAINF;
			} else {
				type = ResourceType.ETC;
			}

			if (type.getInt() <= ResourceType.XML.getInt()) {
				if (path.startsWith("res/" + type.toString() + "-"))
					config = path.replaceAll("res/" + type.toString() + "-([^/]*)/.*", "$1");
			}

			String extension = path.replaceAll(".*/", "").replaceAll(".*\\.", ".").toLowerCase();

			if (extension.endsWith(".xml")) {
				if (path.startsWith("res/") || path.equals("AndroidManifest.xml"))
					attr = ATTR_AXML;
				else
					attr = ATTR_XML;
			} else if (extension.endsWith(".png") || extension.endsWith(".jpg") || extension.endsWith(".gif")
					|| extension.endsWith(".bmp")) {
				attr = ATTR_IMG;
			} else if (extension.endsWith(".qmg")) {
				attr = ATTR_QMG;
			} else if (extension.endsWith(".txt") || extension.endsWith(".mk") || extension.endsWith(".html")
					|| extension.endsWith(".js") || extension.endsWith(".css") || extension.endsWith(".json")
					|| extension.endsWith(".props") || extension.endsWith(".properties")
					|| extension.endsWith(".mf") || extension.endsWith(".sf")) {
				attr = ATTR_TXT;
			} else if(extension.endsWith(".rsa") || extension.endsWith(".dsa") || extension.endsWith(".ec")) {
				attr = ATTR_CERT;
			} else {
				attr = ATTR_ETC;
			}

			if (isFolder) {
				label = getOnlyFoldername(path);
			} else {
				label = getOnlyFilename(path);
			}

			childCount = 0;
		}

		@Override
		public String toString() {
			String str = null;
			if (childCount > 0) {
				str = this.label + " (" + childCount + ")";
				;
			} else if (this.config != null) {
				str = this.label + " (" + this.config + ")";
			} else {
				str = this.label;
			}
			return str;
		}

		public void setLoadingState(Boolean state) {
			isLoading = state;
		}

		public Boolean getLoadingState() {
			return isLoading;
		}
	}

	public Resources() {

	}

	private void makeTreeForm() {
		top = new DefaultMutableTreeNode("Loading...");

		tree = new JTree(new DefaultTreeModel(top)) {
			private static final long serialVersionUID = 2164035864213808434L;

			@Override
			public void paintComponent(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				if (getSelectionCount() > 0) {
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
		};

		tree.setUI(new javax.swing.plaf.basic.BasicTreeUI() {
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

		tree.setOpaque(false);

		treefocuschanger = new TreeFocusChanger() {
			@Override
			public void setTreeFocus(String path, int line, String string) {
				Log.d("path : " + path + ", " + Resources.this.getParent());
				Container parent = (Resources.this.getParent());
				if(parent instanceof JTabbedPane) {
					JTabbedPane tabbed = (JTabbedPane)parent;
					int c = tabbed.getComponentCount();
					for(int i = 0; i<c; i++) {
						if(tabbed.getComponent(i).equals(Resources.this)) {
							tabbed.setSelectedIndex(i);
							break;
						}
					}
				}

				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> e = top.depthFirstEnumeration();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode node = e.nextElement();

					if (node.getUserObject() instanceof ResourceObject) {

						ResourceObject temp = (ResourceObject) node.getUserObject();
						if (temp.path.equals(path)) {
							TreePath treepath = new TreePath(node.getPath());
							tree.setSelectionPath(treepath);
							tree.scrollPathToVisible(treepath);
							contentPanel.selectContent(tree);
							contentPanel.selectContentAndLine(tree, line, string);
							return;
						}
					} else {
						continue;
					}
				}
			}

			@Override
			public String getImagefilePath(String findfilename) {
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> e = top.depthFirstEnumeration();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode node = e.nextElement();

					if (node.getUserObject() instanceof ResourceObject) {

						ResourceObject temp = (ResourceObject) node.getUserObject();

						if (temp.isFolder)
							continue;
						if (!temp.path.toLowerCase().endsWith(".png"))
							continue;

						if (temp.path.indexOf(findfilename) != -1) {
							return temp.path;
						}
					} else {
						continue;
					}
				}
				return null;
			}
		};

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	static public TreeFocusChanger getTreeFocuschanger() {
		return treefocuschanger;
	}

	/*
	static private class Node extends DefaultMutableTreeNode {
		public Node(Object userObject) {
			super(userObject);
		}

		public boolean isLeaf() {
			return false;
		}
	}
	 */

	DefaultMutableTreeNode createFilteredTree(DefaultMutableTreeNode parent, String filter) {
		int c = parent.getChildCount();
		DefaultMutableTreeNode fparent = new DefaultMutableTreeNode(parent.getUserObject());
		String temp;

		if (parent.getUserObject() instanceof ResourceObject) {
			temp = ((ResourceObject) (parent.getUserObject())).label;
		} else {
			temp = top.toString();
		}

		boolean matches = temp.toLowerCase().contains(filter.toLowerCase());
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

	class ResouceTreeCellRenderer extends DefaultTreeCellRenderer implements FocusListener {
		private static final long serialVersionUID = 6248791058116909814L;
		// private ImageIcon iconApk = Resource.IMG_TREE_APK.getImageIcon();
		// private ImageIcon iconFolder =
		// Resource.IMG_TREE_FOLDER.getImageIcon();

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean isLeaf, int row, boolean focused) {
			Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;

			// ((JLabel)c).setOpaque(true);
			((JLabel) c).setBackground(Color.RED);

			if (nodo == top) {
				setIcon(Resource.IMG_APK_FILE_ICON.getImageIcon());
				return c;
			}

			if (nodo.getUserObject() instanceof ResourceObject) {
				ResourceObject tempObject = (ResourceObject) nodo.getUserObject();

				if (!tempObject.isFolder) {
					ResourceObject temp = (ResourceObject) nodo.getUserObject();
					String urlFilePath = null;
					urlFilePath = apkFilePath.replaceAll("#", "%23");
					String jarPath = "jar:file:" + urlFilePath + "!/";
					Icon icon = null;

					switch (temp.attr) {
					case ResourceObject.ATTR_IMG:
						try {
							Image tempImage = null;
							tempImage = ImageScaler.getScaledImage(new ImageIcon(new URL(jarPath + temp.path)), 32, 32);
							icon = new ImageIcon(tempImage);
							tempImage.flush();
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
						break;
					case ResourceObject.ATTR_AXML:
					case ResourceObject.ATTR_XML:
						// tempImage =
						// ImageScaler.getScaledImage(Resource.IMG_RESOURCE_TREE_XML.getImageIcon(),16,16);
						break;
					case ResourceObject.ATTR_QMG:
						// tempImage =
						// ImageScaler.getScaledImage(Resource.IMG_QMG_IMAGE_ICON.getImageIcon(),32,32);
						break;
					case ResourceObject.ATTR_TXT:
						break;
					case ResourceObject.ATTR_CERT:
						break;
					case ResourceObject.ATTR_ETC:
						break;
					default:
					}

					if (icon != null) {
						setIcon(icon);
					} else {
						String suffix = tempObject.path.replaceAll(".*/", "");
						if (suffix.indexOf(".") > -1) {
							suffix = suffix.replaceAll(".*\\.", ".");
						} else {
							suffix = "";
						}
						if (temp.getLoadingState()) {

							if (ImageObserver == null) {
								ImageObserver = new NodeImageObserver(tree);
								Animateimageicon.setImageObserver(ImageObserver);
								ImageObserver.setnode(nodo);
								ImageObserver.setDrawFlag(true);
							}

							setIcon(Animateimageicon);

						} else {
							setIcon(getFileIcon(suffix));

						}

					}
				}
			} else {
				setIcon(getFileIcon("FOLDER"));
			}
			return c;
		}

		@Override
		public void focusGained(FocusEvent e) {
			e.getComponent().repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
			e.getComponent().repaint();
		}

		private Icon getFileIcon(String suffix) {
			Icon icon = fileIcon.get(suffix);
			if (icon == null) {
				Log.v("getIcon " + suffix);
				Image tempImage = null;
				if ("FOLDER".equals(suffix)) {
					tempImage = ImageScaler.getScaledImage(Resource.IMG_TREE_FOLDER.getImageIcon(), 16, 16);
					/*
					 * UIDefaults defaults = UIManager.getDefaults( ); Icon
					 * computerIcon = defaults.getIcon( "FileView.computerIcon"
					 * ); Icon floppyIcon = defaults.getIcon(
					 * "FileView.floppyDriveIcon" ); Icon diskIcon =
					 * defaults.getIcon( "FileView.hardDriveIcon" ); Icon
					 * fileIcon = defaults.getIcon( "FileView.fileIcon" ); Icon
					 * folderIcon = defaults.getIcon( "FileView.directoryIcon"
					 * );
					 * 
					 * icon = folderIcon;
					 */
				} else if (".xml".equals(suffix)) {
					tempImage = ImageScaler.getScaledImage(Resource.IMG_RESOURCE_TREE_XML.getImageIcon(), 16, 16);
					// } else if(".qmg".equals(suffix)) {
					// tempImage =
					// ImageScaler.getScaledImage(Resource.IMG_QMG_IMAGE_ICON.getImageIcon(),32,32);
				} else if (".dex".equals(suffix)) {
					icon = Resource.IMG_RESOURCE_TREE_CODE.getImageIcon();
				} else if (".arsc".equals(suffix)) {
					icon = Resource.IMG_RESOURCE_TREE_ARSC.getImageIcon();
				} else {
					try {
						File file = File.createTempFile("icon", suffix);
						FileSystemView view = FileSystemView.getFileSystemView();
						icon = view.getSystemIcon(file);
						file.delete();
					} catch (IOException ioe) {
					}
				}
				if (tempImage != null) {
					icon = new ImageIcon(tempImage);
					tempImage.flush();
				}
				if (icon != null) {
					fileIcon.put(suffix, icon);
				}
			}
			return icon;
		}
	}

	private String getOnlyFilename(String str) {
		String separator = (str.indexOf(File.separator) > -1) ? separator = File.separator : "/";
		return str.substring(str.lastIndexOf(separator) + 1, str.length());
	}

	private String getOnlyFoldername(String str) {
		String separator = (str.indexOf(File.separator) > -1) ? separator = File.separator : "/";
		return str.substring(0, str.lastIndexOf(separator));
	}

	private final List<DefaultMutableTreeNode> getSearchNodes(DefaultMutableTreeNode root) {
		List<DefaultMutableTreeNode> searchNodes = new ArrayList<DefaultMutableTreeNode>();

		Enumeration<?> e = root.preorderEnumeration();
		while (e.hasMoreElements()) {
			searchNodes.add((DefaultMutableTreeNode) e.nextElement());
		}
		return searchNodes;
	}

	public final DefaultMutableTreeNode findNode(String searchString) {

		List<DefaultMutableTreeNode> searchNodes = getSearchNodes((DefaultMutableTreeNode) tree.getModel().getRoot());
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		DefaultMutableTreeNode foundNode = null;
		int bookmark = -1;

		if (currentNode != null) {
			for (int index = 0; index < searchNodes.size(); index++) {
				if (searchNodes.get(index) == currentNode) {
					bookmark = index;
					break;
				}
			}
		}

		for (int index = bookmark + 1; index < searchNodes.size(); index++) {
			if (searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
				foundNode = searchNodes.get(index);
				break;
			}
		}

		if (foundNode == null) {
			for (int index = 0; index <= bookmark; index++) {
				if (searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
					foundNode = searchNodes.get(index);
					break;
				}
			}
		}
		return foundNode;
	}

	public final DefaultMutableTreeNode findNode(DefaultMutableTreeNode node, String string, boolean ignoreCase,
			boolean recursively) {
		DefaultMutableTreeNode ret = null;
		if (node == null) {
			node = (DefaultMutableTreeNode) tree.getModel().getRoot();
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

	private void setTreeForm(Boolean mode) {
		Thread thread = new Thread(new Runnable() {
			public void run()
			{
				try {
					final ArrayList<DefaultMutableTreeNode> topFiles = new ArrayList<DefaultMutableTreeNode>();

					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							tree.removeAll();

							top = new DefaultMutableTreeNode(getOnlyFilename(apkFilePath));
							// FilteredTreeModel model = new FilteredTreeModel(new
							// DefaultTreeModel(top));
							tree.setModel(new DefaultTreeModel(top));

							eachTypeNodes = new DefaultMutableTreeNode[ResourceType.COUNT.getInt()];

						}
					});

					final int CHUNK_SIZE = 30;
					for (int chunk = 0; chunk < nameList.length; chunk += CHUNK_SIZE) {
						final int start = chunk;
						EventQueue.invokeAndWait(new Runnable() {
							public void run() {
								for (int i = start; i < start + CHUNK_SIZE && i < nameList.length; i++) {
									if (nameList[i].endsWith("/") || nameList[i].startsWith("lib/")
											/*|| this.nameList[i].startsWith("META-INF/")*/)
										continue;

									ResourceObject resObj = new ResourceObject(nameList[i], false);
									if (nameList[i].indexOf("/") == -1) {
										topFiles.add(new DefaultMutableTreeNode(resObj));
										continue;
									}

									DefaultMutableTreeNode typeNode = eachTypeNodes[resObj.type.getInt()];

									if (typeNode == null) {
										typeNode = new DefaultMutableTreeNode(resObj.type.toString());
										eachTypeNodes[resObj.type.getInt()] = typeNode;
										if (resObj.type != ResourceType.ETC) {
											top.add(typeNode);
										}
									}

									DefaultMutableTreeNode findnode = null;
									if (resObj.type != ResourceType.ETC) {
										String fileName = getOnlyFilename(nameList[i]);
										findnode = findNode(typeNode, fileName, false, false);
									}

									if (findnode != null) {
										if (findnode.getChildCount() == 0) {
											ResourceObject obj = (ResourceObject) findnode.getUserObject();
											findnode.add(new DefaultMutableTreeNode(new ResourceObject(obj.path, false)));
											((ResourceObject) findnode.getUserObject()).childCount++;
										}
										findnode.add(new DefaultMutableTreeNode(resObj));
										((ResourceObject) findnode.getUserObject()).childCount++;
									} else {
										typeNode.add(new DefaultMutableTreeNode(resObj));
									}
								}
							}
						});
						Thread.yield();
					}

					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							if (eachTypeNodes[ResourceType.ETC.getInt()] != null) {
								top.add(eachTypeNodes[ResourceType.ETC.getInt()]);
							}

							for (DefaultMutableTreeNode node : topFiles) {
								top.add(node);
							}

							expandOrCollapsePath(tree, new TreePath(top.getPath()), 1, 0, true);
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

	public static void expandOrCollapsePath(JTree tree, TreePath treePath, int level, int currentLevel,
			boolean expand) {
		// System.err.println("Exp level "+currentLevel+", exp="+expand);
		if (expand && level <= currentLevel && level > 0)
			return;

		TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
		TreeModel treeModel = tree.getModel();
		if (treeModel.getChildCount(treeNode) >= 0) {
			for (int i = 0; i < treeModel.getChildCount(treeNode); i++) {
				TreeNode n = (TreeNode) treeModel.getChild(treeNode, i);
				TreePath path = treePath.pathByAddingChild(n);
				expandOrCollapsePath(tree, path, level, currentLevel + 1, expand);
			}
			if (!expand && currentLevel < level)
				return;
		}
		if (expand) {
			tree.expandPath(treePath);
			// System.err.println("Path expanded at level
			// "+currentLevel+"-"+treePath);
		} else {
			tree.collapsePath(treePath);
			// System.err.println("Path collapsed at level
			// "+currentLevel+"-"+treePath);
		}
	}

	private void openContent() {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null || !(node.getUserObject() instanceof ResourceObject)) {
			return;
		}

		if (node.getChildCount() > 0)
			return;

		final ResourceObject resObj = (ResourceObject) node.getUserObject();

		Log.d("" + resObj.isFolder);

		String resPath = tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
		File resFile = new File(resPath);
		if (!resFile.exists()) {
			if (!resFile.getParentFile().exists()) {
				if (FileUtil.makeFolder(resFile.getParentFile().getAbsolutePath())) {
					Log.i("sucess make folder : " + resFile.getParentFile().getAbsolutePath());
				}
			}
		}

		if (resObj != null && !resObj.isFolder) {
			String[] convStrings = null;
			if (resObj.attr == ResourceObject.ATTR_AXML) {
				convStrings = AaptNativeWrapper.Dump.getXmltree(apkFilePath, new String[] { resObj.path });
			} else if ("resources.arsc".equals(resObj.path)) {
				convStrings = resourcesWithValue;
				resPath += ".txt";
			} else {
				ZipFileUtil.unZip(apkFilePath, resObj.path, resPath);
			}

			if (convStrings != null) {
				StringBuilder sb = new StringBuilder();
				for (String s : convStrings)
					sb.append(s + "\n");
				try {
					FileWriter fw = new FileWriter(new File(resPath));
					fw.write(sb.toString());
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (resObj.path.toLowerCase().endsWith(".dex")) {
			Log.i("dex file");
			if (resObj.getLoadingState() == false) {
				resObj.setLoadingState(true);

				tree.repaint();
				Dex2JarWrapper.convert(resPath, new Dex2JarWrapper.DexWrapperListener() {
					private void resetUI() {
						resObj.setLoadingState(false);

						Animateimageicon.setImageObserver(null);
						ImageObserver.setDrawFlag(false);
						ImageObserver = null;
						tree.repaint();
					}

					@Override
					public void onError(String message) {
					}

					@Override
					public void onSuccess(String jarFilePath) {
						JDGuiLauncher.run(jarFilePath);
					}

					@Override
					public void onCompleted() {
						resetUI();
					}
				});
			}
		} else if (resObj.path.toLowerCase().endsWith(".apk")) {
			Launcher.run(resPath);
		} else {
			SystemUtil.openFile(resPath);
		}
	}

	public class NodeImageObserver implements ImageObserver {
		JTree tree;
		DefaultTreeModel model;
		TreeNode node;
		Boolean StopFlag;

		NodeImageObserver(JTree tree) {
			this.tree = tree;
			this.model = (DefaultTreeModel) tree.getModel();
			StopFlag = true;
		}

		public void setDrawFlag(Boolean state) {
			StopFlag = state;
		}

		public void setnode(TreeNode node) {
			this.node = node;
		}

		public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {

			if (!StopFlag)
				return false;

			if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
				TreePath path = new TreePath(model.getPathToRoot(node));
				Rectangle rect = tree.getPathBounds(path);
				if (rect != null) {
					tree.repaint(rect);
				}
			}

			return (flags & (ALLBITS | ABORT)) == 0;
		}

	}

	private void TreeInit() {

		renderer = new ResouceTreeCellRenderer();
		tree.setCellRenderer(renderer);
		tree.addFocusListener(renderer);
		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					if (selPath != null) {
						contentPanel.selectContent(tree);
					}
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					if (selPath != null) {
						openContent();
					}
				}
			}
		});

		tree.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				// if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
				contentPanel.selectContent(tree);
				// }
			}
		});
	}

	private void expandTree(final JTree tree) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	@SuppressWarnings("unused")
	private void makefilter(String temp) {
		filteredModel = (FilteredTreeModel) tree.getModel();
		filteredModel.setFilter(temp);
		filteredModel.reload();

		expandTree(tree);
		forselectionTree();
	}

	@SuppressWarnings("unused")
	private void forselectionTree() {
		DefaultMutableTreeNode currentNode = top.getNextNode();
		/*
		 * do { if(currentNode.getLevel()==3 &&
		 * filteredModel.getChildCount(currentNode) > 0) {
		 * 
		 * TreePath temptreePath = new
		 * TreePath(((DefaultMutableTreeNode)(filteredModel.getChild(
		 * currentNode, 0))).getPath());
		 * 
		 * tree.setSelectionPath(temptreePath);
		 * tree.scrollPathToVisible(temptreePath); return; } currentNode =
		 * currentNode.getNextNode(); } while (currentNode != null);
		 */
	}

	class TreeFindFildListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {

			if (arg0.getSource() instanceof JTextField) {
				String temp = ((JTextField) (arg0.getSource())).getText();
				searchTree(temp);
			} else if (arg0.getSource() instanceof JButton) {
				JButton temp = (JButton) (arg0.getSource());

				if (temp.getName().equals(RESOURCE_TREE_TOOLBAR_BUTTON_FIND)) {					
					String strtemp = textField.getText();
					searchTree(strtemp);
				} else if (temp.getName().equals(RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH)) {
					searchTree("");
				}
				tree.repaint();
			}

		}

		void searchTree(String str) {		

			if(str.length() > 0) {
				refreshicon.setEnabled(true);
				//isFilter = true;

			} else {
				refreshicon.setEnabled(false);
				//isFilter = false;

			}

			tree.setModel(new DefaultTreeModel(createFilteredTree(top, str)));			
			tree.repaint();
		}
	}

	@Override
	public void initialize() {
		this.setLayout(new GridLayout(1, 1));

		makeTreeForm();
		TreeInit();

		textField = new JTextField("");
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				((JTextField) (arg0.getSource())).setBackground(new Color(255, 255, 255));
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				((JTextField) (arg0.getSource())).setBackground(new Color(178, 235, 244));
			}
		});

		findicon = new JButton(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		refreshicon = new JButton(Resource.IMG_RESOURCE_TREE_TOOLBAR_REFRESH.getImageIcon(16, 16));

		findicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setEnabled(false);

		findicon.setName(RESOURCE_TREE_TOOLBAR_BUTTON_FIND);
		refreshicon.setName(RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH);

		findicon.setFocusPainted(false);
		refreshicon.setFocusPainted(false);

		JPanel TreeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

		JPanel TreeModePanel = new JPanel(new BorderLayout());
		// textField.setPreferredSize(new Dimension(10, 27));

		TreeFindFildListener findListener = new TreeFindFildListener();

		textField.addActionListener(findListener);
		findicon.addActionListener(findListener);
		refreshicon.addActionListener(findListener);

		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if(textField.getText().length() > 0) {
					findicon.setEnabled(true);
				} else {
					findicon.setEnabled(false);
				}
			}
			@Override
			public void keyPressed(KeyEvent arg0) {

			}
		});

		TreeModePanel.add(textField, BorderLayout.CENTER);
		TreeButtonPanel.add(findicon);
		TreeButtonPanel.add(refreshicon);

		TreeModePanel.add(TreeButtonPanel, BorderLayout.EAST);
		// End Tree navigator ----------

		JScrollPane treeScroll = new JScrollPane(tree);
		treeScroll.setPreferredSize(new Dimension(300, 400));
		treeScroll.repaint();

		AdjustmentListener adjustmentListener = new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				tree.repaint();
			}
		};
		treeScroll.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
		treeScroll.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);

		JPanel TreePanel = new JPanel(new BorderLayout());
		TreePanel.add(TreeModePanel, BorderLayout.NORTH);
		TreePanel.add(treeScroll, BorderLayout.CENTER);

		// imageViewerPanel = new ImageControlPanel();
		// imageViewerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
		// 5));
		// imageViewerPanel.setBackground(Color.BLACK);

		contentPanel = new ResouceContentsPanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(TreePanel);
		splitPane.setRightComponent(contentPanel);
		splitPane.setDividerLocation(200);

		this.add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo) {
		if (tree == null)
			initialize();

		this.apkFilePath = apkInfo.filePath;
		this.tempWorkPath = apkInfo.tempWorkPath;

		nameList = apkInfo.resources;
		contentPanel.setData(apkInfo);
		setTreeForm(false);
	}

	@Override
	public void setExtraData(ApkInfo apkInfo) {
		if (apkInfo != null) {
			resourcesWithValue = apkInfo.resourcesWithValue;
			if (contentPanel == null)
				return;
			contentPanel.setData(apkInfo);
			// if(apkInfo.manifest.application.icons != null &&
			// apkInfo.manifest.application.icons.length > 0) {
			// appIconPath =
			// apkInfo.manifest.application.icons[apkInfo.manifest.application.icons.length
			// - 1].name;
			// }
		} else {
			// appIconPath = null;
			resourcesWithValue = null;
			if (contentPanel == null)
				return;
			contentPanel.setData(null);
		}
	}

	@Override
	public void reloadResource() {

	}	
}