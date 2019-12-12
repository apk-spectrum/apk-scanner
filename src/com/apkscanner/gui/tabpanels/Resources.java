package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.external.BytecodeViewerLauncher;
import com.apkscanner.tool.external.Dex2JarWrapper;
import com.apkscanner.tool.external.ImgExtractorWrapper;
import com.apkscanner.tool.external.JADXLauncher;
import com.apkscanner.tool.external.JDGuiLauncher;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

public class Resources extends AbstractTabbedPanel {
	private static final long serialVersionUID = -934921813626224616L;

	private ResourceTree resTree;
	private ResourceContentsPanel contentPanel;

	private String apkFilePath = null;
	private String tempWorkPath = null;
	private String[] resourcesWithValue = null;

	private JTextField textField;
	JButton findicon;
	JButton refreshicon;

	static public TreeFocusChanger treefocuschanger;

	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_FIND = "TREE FIND";
	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH = "TREE REFRESH";

	static public abstract interface TreeFocusChanger {
		public void setTreeFocus(String path, int line, String string);
		public String getImagefilePath(String findfilename);
	}

	public Resources() {
		setTitle(RStr.TAB_RESOURCES.get(), RStr.TAB_RESOURCES.get());
		setTabbedEnabled(false);
	}

	private void makeTreeFocusChanger() {
		treefocuschanger = new TreeFocusChanger() {
			@Override
			public void setTreeFocus(String path, int line, String string) {
				Log.v("path : " + path + ", line " + line + ", text " + string);
				setSeletected();

				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) resTree.getModel().getRoot();

				@SuppressWarnings("unchecked")
				Enumeration<TreeNode> e = rootNode.depthFirstEnumeration();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
					if (node.getUserObject() instanceof ResourceObject) {
						ResourceObject temp = (ResourceObject) node.getUserObject();
						if (temp.path.equals(path)) {
							TreePath treepath = new TreePath(node.getPath());
							resTree.setSelectionPath(treepath);
							resTree.scrollPathToVisible(treepath);
							contentPanel.selectContentAndLine(line, string);
							return;
						}
					} else {
						continue;
					}
				}
			}

			@Override
			public String getImagefilePath(String findfilename) {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) resTree.getModel().getRoot();

				@SuppressWarnings("unchecked")
				Enumeration<TreeNode> e = rootNode.depthFirstEnumeration();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
					if (node.getUserObject() instanceof ResourceObject) {
						ResourceObject temp = (ResourceObject) node.getUserObject();

						if (temp.isFolder)
							continue;
						if (!temp.path.toLowerCase().endsWith(".png"))
							continue;

						if (temp.path.contains(findfilename)) {
							return temp.path;
						}
					} else {
						continue;
					}
				}
				return null;
			}
		};
	}

	static public TreeFocusChanger getTreeFocuschanger() {
		return treefocuschanger;
	}

	private void openContent() {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
		if (node == null || !(node.getUserObject() instanceof ResourceObject)) {
			return;
		}

		if (node.getChildCount() > 0)
			return;

		final ResourceObject resObj = (ResourceObject) node.getUserObject();

		String resPath = null;
		if(resObj.type == ResourceType.LOCAL) {
			resPath = resObj.path;
		} else {
			resPath = tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
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

				if (resObj.attr == ResourceObject.ATTR_FS_IMG) {

				}
			}
		}

		if (resObj.path.toLowerCase().endsWith(".dex")) {
			String data = RProp.S.DEFAULT_DECORDER.get();
			Log.v("PROP_DEFAULT_DECORDER : " + data);
			if(data.matches(".*!.*#.*@.*")) {
				IPlugIn plugin = PlugInManager.getPlugInByActionCommand(data);
				if(plugin != null
						&& plugin instanceof IExternalTool
						&& ((IExternalTool)plugin).isDecorderTool() ) {
					((IExternalTool)plugin).launch(resPath);
				} else {
					data = (String) RProp.DEFAULT_DECORDER.getDefaultValue();
				}
			}

			switch(data) {
			case RConst.STR_DECORDER_JD_GUI:
				if (resObj.getLoadingState() == false) {
					resObj.setLoadingState(true);

					resTree.repaint();
					Dex2JarWrapper.convert(resPath, new Dex2JarWrapper.DexWrapperListener() {
						@Override
						public void onError(String message) {
						}

						@Override
						public void onSuccess(String jarFilePath) {
							JDGuiLauncher.run(jarFilePath);
						}

						@Override
						public void onCompleted() {
							resObj.setLoadingState(false);
						}
					});
				}
				break;
			case RConst.STR_DECORDER_JADX_GUI:
				JADXLauncher.run(resPath);
				break;
			case RConst.STR_DECORDER_BYTECOD:
				BytecodeViewerLauncher.run(resPath);
				break;
			}
			Log.i("dex file");
		} else if (resObj.path.toLowerCase().endsWith(".apk")) {
			Launcher.run(resPath);
		} else if (resObj.attr == ResourceObject.ATTR_FS_IMG) {

		} else {
			SystemUtil.openFile(resPath);
		}
	}

	private void treeInit() {
		resTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					TreePath selPath = resTree.getPathForLocation(e.getX(), e.getY());
					if (selPath != null) {
						openContent();
					}
				}
			}
		});

		resTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				if (node.getUserObject() instanceof ResourceObject) {
					ResourceObject resObj = (ResourceObject) node.getUserObject();
					if(resObj.attr == ResourceObject.ATTR_FS_IMG) {
						loadFsImg(node);
					}
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) { }
		});

		resTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
				if(node == null) return;
				contentPanel.selectContent(node.getUserObject());
			}
		});
	}

	private void loadFsImg(final DefaultMutableTreeNode node) {
		new SwingWorker<String, Void>() {
			@Override
			protected String doInBackground() throws Exception {
				ResourceObject resObj = (ResourceObject) node.getUserObject();
				String imgPath = tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
				String extPath = imgPath + "_";
				if(!new File(imgPath).exists()) {
					ZipFileUtil.unZip(apkFilePath, resObj.path, imgPath);
				}
				ImgExtractorWrapper.extracte(imgPath, extPath);
				return extPath;
			}

			@Override
			protected void done() {
				String topPath = null;
				try {
					topPath = get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				if(topPath == null || topPath.isEmpty()) return;
				File root = new File(topPath);
				if(!root.exists() || !root.isDirectory()) return;

				node.removeAllChildren();
				addNodes(node, root);
				resTree.updateUI();
			}

			private void addNodes(DefaultMutableTreeNode node, File dir) {
				for(File c: dir.listFiles()) {
					DefaultMutableTreeNode childNode = new ResourceNode(new ResourceObject(c));
					node.add(childNode);
					if(c.isDirectory()) {
						addNodes(childNode, c);
					}
				}
			}
		}.execute();
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
				resTree.repaint();
			}
		}

		void searchTree(String str) {
			if(str.length() > 0) {
				refreshicon.setEnabled(true);
			} else {
				refreshicon.setEnabled(false);
			}
			resTree.searchTree(str);
		}
	}

	@Override
	public void initialize() {
		this.setLayout(new GridLayout(1, 1));


		resTree = new ResourceTree();
		treeInit();
		makeTreeFocusChanger();

		textField = new JTextField("");
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				((JTextField) (arg0.getSource())).setBackground(new Color(255, 255, 255));
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				((JTextField) (arg0.getSource())).setBackground(new Color(178, 235, 244));
			}
		});

		findicon = new JButton(RImg.RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		refreshicon = new JButton(RImg.RESOURCE_TREE_TOOLBAR_REFRESH.getImageIcon(16, 16));

		findicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setEnabled(false);

		findicon.setName(RESOURCE_TREE_TOOLBAR_BUTTON_FIND);
		refreshicon.setName(RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH);

		findicon.setFocusPainted(false);
		refreshicon.setFocusPainted(false);

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

		JPanel TreeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		TreeButtonPanel.add(findicon);
		TreeButtonPanel.add(refreshicon);

		JPanel TreeModePanel = new JPanel(new BorderLayout());
		TreeModePanel.add(textField, BorderLayout.CENTER);
		TreeModePanel.add(TreeButtonPanel, BorderLayout.EAST);

		JScrollPane treeNaviScroll = new JScrollPane(TreeModePanel);
		treeNaviScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		treeNaviScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		treeNaviScroll.setBorder(new EmptyBorder(0,0,0,0));

		JPanel TreePanel = new JPanel(new BorderLayout());
		TreePanel.add(treeNaviScroll, BorderLayout.NORTH);
		TreePanel.add(new JScrollPane(resTree), BorderLayout.CENTER);

		contentPanel = new ResourceContentsPanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(TreePanel);
		splitPane.setRightComponent(contentPanel);
		splitPane.setDividerLocation(200);

		this.add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status) {
		if(!Status.RESOURCE_COMPLETED.equals(status)) {
			if(Status.RES_DUMP_COMPLETED.equals(status)) {
				setExtraData(apkInfo);
			}
			return;
		}

		if (resTree == null)
			initialize();

		if(apkInfo.resources == null)
			return;

		resTree.addTreeNodes(apkInfo.filePath, apkInfo.resources);
		contentPanel.setData(apkInfo);

		setDataSize(apkInfo.resources.length, true, false);

		apkFilePath = apkInfo.filePath;
		tempWorkPath = apkInfo.tempWorkPath;
	}

	public void setExtraData(ApkInfo apkInfo) {
		if (apkInfo != null) {
			resourcesWithValue = apkInfo.resourcesWithValue;
			if (contentPanel == null)
				return;
			contentPanel.setData(apkInfo);
		} else {
			resourcesWithValue = null;
			if (contentPanel == null)
				return;
			contentPanel.setData(null);
		}
	}

	@Override
	public void reloadResource() {
		setTitle(RStr.TAB_RESOURCES.get(), RStr.TAB_RESOURCES.get());
	}
}