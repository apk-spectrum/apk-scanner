package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.FilteredTreeModel;
import com.apkscanner.gui.util.SimpleCheckTableModel;
import com.apkscanner.gui.util.SimpleCheckTableModel.TableRowObject;
import com.apkscanner.gui.util.TreeNodeIconRefresher;
import com.apkscanner.gui.util.WindowSizeMemorizer;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.IPackageStateListener;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.WindowStateInfo;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class PackageTreeDlg extends JDialog implements TreeSelectionListener, ActionListener {
	private static final long serialVersionUID = 813267847663868531L;

	public static final int CANCEL_OPTION = 1;
	public static final int APPROVE_OPTION = 0;
	public static final int ERROR_OPTION = -1;

	private JTextField textFieldapkPath;
	private JTree tree;
	private DefaultMutableTreeNode top;
	private JPanel gifPanel;
	private JCheckBox checkboxUseframework;

	private JPanel ListPanel;
	private JButton refreshbtn;
	static private int result;
	private String selDevice;
	private String selPackage;
	private String selApkPath;
	private String tmpApkPath;
	private JTextField textSearchFilter;

	private final String[] columnNames = {"", Resource.STR_LABEL_DEVICE.getString(), Resource.STR_LABEL_PATH.getString()};
	private ArrayList<TableRowObject> tableListArray = new ArrayList<TableRowObject>();
	private JTable table;

	private DeviceHandler deviceHandler;

	// Must be accessing to pullingNodes in EventDispatchThread 
	private ArrayList<TreeNode> pullingNodes = new ArrayList<TreeNode>();

	public class FrameworkTableObject implements TableRowObject {
		public Boolean buse;
		public String location;
		public String deviceID;
		public String path;

		FrameworkTableObject(Boolean buse, String location, String deviceID, String path) {
			this.buse = buse;
			this.location = location;
			this.deviceID = deviceID;
			this.path = path;
		}

		@Override
		public Object get(int columnIndex) {
			switch(columnIndex) {
			case 0:	return buse;
			case 1:	return deviceID +"(" + location + ")";
			case 2:	return path;
			}
			return null;
		}

		@Override
		public void set(int columnIndex, Object obj) {
			switch(columnIndex) {
			case 0:	buse = (Boolean) obj; break;
			case 1:	location = (String) obj; break;
			case 2:	path = (String) obj; break;
			}
		}
	}

	public PackageTreeDlg(Window owner) {
		super(owner);
		initialize(owner);

		deviceHandler = new DeviceHandler();
		deviceHandler.execute();

		refreshTreeList(true);
	}

	private void initialize(Window window)
	{
		setTitle(Resource.STR_TREE_OPEN_PACKAGE.getString());
		setIconImage(Resource.IMG_USB_ICON.getImageIcon().getImage());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);
		setModal(true);
		setLayout(new BorderLayout());

		Dimension minSize = new Dimension(600, 400);
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(this, minSize);
		} else {
			setSize(minSize);
		}
		setMinimumSize(minSize);
		WindowSizeMemorizer.registeComponent(this);

		setLocationRelativeTo(window);

		addWindowListener(new WindowEventHandler());

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 8368291008098324014L;
			public void actionPerformed(ActionEvent e) {
				selDevice = null;
				selPackage = null;
				selApkPath = null;
				result = CANCEL_OPTION;

				deviceHandler.quit();
				dispose();
			}
		});

		KeyStroke vk_f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(vk_f5, "VK_F5");
		getRootPane().getActionMap().put("VK_F5", new AbstractAction() {
			private static final long serialVersionUID = -5281980076592985530L;
			public void actionPerformed(ActionEvent e) {
				refreshTreeList(false);
			}
		});


		top = new DefaultMutableTreeNode(Resource.STR_TREE_NODE_DEVICE.getString());

		tree = new JTree(new FilteredTreeModel(top));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new MouseEventHandler());

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 8466579635851211258L;

			private final ImageIcon iconApk = Resource.IMG_TREE_APK.getImageIcon(16,16);
			private final ImageIcon iconDevice = Resource.IMG_TREE_DEVICE.getImageIcon(16,16);
			private final ImageIcon iconTop = Resource.IMG_TREE_TOP.getImageIcon(16,16);
			private final ImageIcon iconFolder = Resource.IMG_TREE_FOLDER.getImageIcon();
			private final ImageIcon iconLoading = Resource.IMG_TREE_LOADING.getImageIcon();
			private final ImageIcon iconFavor = Resource.IMG_TREE_FAVOR.getImageIcon();

			private TreeNodeIconRefresher treeIconRefresher = new TreeNodeIconRefresher(tree);
			{
				iconLoading.setImageObserver(treeIconRefresher);
			}

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component c = super.getTreeCellRendererComponent(tree, value,
						selected, expanded, isLeaf, row, focused);

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				int level = node.getLevel();

				if(level==0) {
					setIcon(iconTop);
				} else if(node.getUserObject() instanceof IDevice) {
					setText(((IDevice)node.getUserObject()).getProperty(IDevice.PROP_DEVICE_MODEL));
					setIcon(iconDevice);
				} else {
					if(node.getUserObject() instanceof PackageInfo) {
						if(pullingNodes.contains(node)) {
							setIcon(iconLoading);
							treeIconRefresher.addTreeNode(node);
						} else {
							PackageInfo pack = (PackageInfo) node.getUserObject();
							if(pack.packageName.equals(PackageManager.getCurrentFocusPackage(pack.device, false))) {
								setIcon(iconFavor);
							} else {
								setIcon(iconApk);
							}
							treeIconRefresher.removeTreeNode(node);
						}
					} else if(node.getUserObject() instanceof String) {
						String nodeName = (String)node.getUserObject();
						if(nodeName.startsWith("*")) {
							setText(nodeName.substring(1));
							setIcon(iconFavor);
						} else if(nodeName.startsWith("#")) {
							setText(nodeName.substring(1));
							setIcon(iconLoading);
							treeIconRefresher.addTreeNode(node);
						} else {
							setIcon(iconFolder);
						}
					} else {
						setIcon(iconFolder);
					}
				}
				return c;
			}
		});

		//Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		textFieldapkPath = new JTextField();
		textFieldapkPath.setEditable(false);

		textSearchFilter = new JTextField();
		textSearchFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				setFilter();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				setFilter();
			}
		});

		textSearchFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						tree.getLastSelectedPathComponent();
				if(node != null) {
					openPackage();
				}
			}

		});

		textSearchFilter.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				if(ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP) {
					tree.dispatchEvent(ke);
				}
			}
		});

		ListPanel = makeListTable();
		ListPanel.setVisible(false);

		checkboxUseframework = new JCheckBox(Resource.STR_LABEL_USES_RESOURCE.getString());
		checkboxUseframework.setSelected(false);
		checkboxUseframework.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkboxUseframework.isSelected()) {
					ListPanel.setVisible(true);
				} else {
					ListPanel.setVisible(false);
				}    
			}
		});

		JButton openbtn = new JButton(Resource.STR_BTN_OPEN.getString());		
		refreshbtn = new JButton(Resource.STR_BTN_REFRESH.getString());
		JButton exitbtn = new JButton(Resource.STR_BTN_CANCEL.getString());

		openbtn.addActionListener(this);
		refreshbtn.addActionListener(this);
		exitbtn.addActionListener(this);

		JLabel GifLabel = new JLabel(Resource.IMG_WAIT_BAR.getImageIcon());
		JLabel Loading = new JLabel(Resource.STR_LABEL_LOADING.getString());

		gifPanel = new JPanel();
		gifPanel.add(Loading);
		gifPanel.add(GifLabel);
		gifPanel.setVisible(false);

		JPanel ButtonPanelWest = new JPanel();
		ButtonPanelWest.add(gifPanel);
		ButtonPanelWest.add(refreshbtn);

		JPanel ButtonPanelEast = new JPanel();
		ButtonPanelEast.add(openbtn);
		ButtonPanelEast.add(exitbtn);     

		JPanel ButtonPanel = new JPanel(new BorderLayout());
		ButtonPanel.add(ButtonPanelWest, BorderLayout.WEST);
		ButtonPanel.add(ButtonPanelEast, BorderLayout.EAST);     

		Dimension minimumSize = new Dimension(100, 50);
		treeView.setMinimumSize(minimumSize);

		//Add the split pane to this panel.
		//add(splitPane);
		JPanel panelnorth = new JPanel(new BorderLayout());                
		JPanel panelsourth = new JPanel(new BorderLayout());        
		JPanel panelsearch = new JPanel(new BorderLayout());

		panelsearch.add(new JLabel(Resource.STR_LABEL_SEARCH.getString() + " : "), BorderLayout.WEST);
		panelsearch.add(textSearchFilter, BorderLayout.CENTER);

		panelnorth.add(textFieldapkPath,BorderLayout.NORTH);
		panelnorth.add(treeView,BorderLayout.CENTER);
		panelnorth.add(panelsearch,BorderLayout.SOUTH);


		panelsourth.add(checkboxUseframework, BorderLayout.NORTH);
		panelsourth.add(ListPanel, BorderLayout.CENTER);
		panelsourth.add(ButtonPanel,BorderLayout.SOUTH);

		add(panelnorth,BorderLayout.CENTER);
		add(panelsourth,BorderLayout.SOUTH);
	}

	public String getSelectedDevice() {
		return selDevice;
	}

	public String getSelectedPackage() {
		return selPackage;
	}

	public String getSelectedApkPath() {
		return selApkPath;
	}

	public String getSelectedFrameworkRes() {
		String resList = null;
		if(checkboxUseframework.isSelected()) {
			resList = "";
			for(TableRowObject res: tableListArray) {
				FrameworkTableObject fwres = (FrameworkTableObject)res;
				if(!fwres.buse) continue;
				if(fwres.deviceID.equals("local")) {
					resList += fwres.path + ";";
				} else {
					resList += "@" + fwres.deviceID + fwres.path + ";";
				}
			}
		}
		return resList;
	}

	public File getSelectedFile() {
		if(tmpApkPath != null)
			return new File(tmpApkPath);
		return null;
	}

	private IDevice getCurrentSelectedDevice() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node.getChildCount() != 0) {
			Log.w("not node!");
			return null;
		}

		DefaultMutableTreeNode deviceNode = null;
		for(deviceNode = node ; !(deviceNode.getUserObject() instanceof IDevice); deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) { }

		return ((IDevice)deviceNode.getUserObject());
	}

	private void addframeworkresIntree() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node.getChildCount() != 0) {
			Log.w("not node!");
			return ;
		}
		PackageInfo tempObject = ((PackageInfo)node.getUserObject()); 		

		IDevice device = getCurrentSelectedDevice();
		//		for(deviceNode = node ; deviceNode.getUserObject() instanceof DeviceStatus==false; deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) { }
		//		Log.i(deviceNode.getUserObject());
		FrameworkTableObject temp = new FrameworkTableObject(true, device.getProperty(IDevice.PROP_DEVICE_MODEL), device.getSerialNumber(), tempObject.getApkPath());

		tableListArray.add(temp);
		((AbstractTableModel) table.getModel()).fireTableDataChanged();

		table.updateUI();

	}

	private void refreshTreeList(final boolean allowCache)
	{
		if(!refreshbtn.isVisible()) {
			Log.i("Already refreshing...");
			return;
		}

		tableListArray.clear();
		table.updateUI();

		top.removeAllChildren();
		top.add(new DefaultMutableTreeNode(Resource.STR_MSG_DEVICE_NOT_FOUND.getString().replace("\n", " ")));
		tree.expandPath(new TreePath(top.getPath()));
		tree.updateUI();

		SwingWorker<Integer, Object> task = new SwingWorker<Integer, Object>()
		{
			@Override
			protected Integer doInBackground() throws Exception {
				AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
				IDevice[] devices = adb.getDevices();
				for(IDevice device: devices) {
					if(!allowCache) {
						PackageManager.removeListCache(device);
					}
					deviceHandler.deviceConnected(device);
				}
				return devices.length;
			}
		};
		task.execute();

	}

	public static void expandOrCollapsePath (JTree tree,TreePath treePath,int level,int currentLevel,boolean expand) {
		//      System.err.println("Exp level "+currentLevel+", exp="+expand);
		if (expand && level<=currentLevel && level>0) return;

		TreeNode treeNode = ( TreeNode ) treePath.getLastPathComponent();
		TreeModel treeModel=tree.getModel();
		int childCnt = treeModel.getChildCount(treeNode);
		if ( childCnt > 0 ) {
			for ( int i = 0; i < childCnt; i++  ) {
				TreeNode n = ( TreeNode )treeModel.getChild(treeNode, i);
				if(((DefaultMutableTreeNode) n).getUserObject() instanceof PackageInfo) {
					return;
				}
				TreePath path = treePath.pathByAddingChild( n );
				expandOrCollapsePath(tree,path,level,currentLevel+1,expand);
			}
			if (!expand && currentLevel<level) return;
		} else {
			return;
		}
		if (expand) {
			tree.expandPath( treePath );
			//         System.err.println("Path expanded at level "+currentLevel+"-"+treePath);
		} else {
			tree.collapsePath(treePath);
			//         System.err.println("Path collapsed at level "+currentLevel+"-"+treePath);
		}
	}

	/** Required by TreeSelectionListener interface. */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();
		if (node == null) return;
		//Object nodeInfo = node.getUserObject();

		if(node.getUserObject() instanceof PackageInfo){
			PackageInfo tempObject = ((PackageInfo)node.getUserObject());
			textFieldapkPath.setText(tempObject.getApkPath() + " - " + tempObject.packageName);
		} else {
			TreeNode [] treenode = node.getPath();
			TreePath path = new TreePath(treenode);
			textFieldapkPath.setText(path.toString());
		}
	}

	private void expandAll(final JTree tree) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private void collapseAll(JTree tree)
	{
		for(int row = tree.getRowCount() - 1; row >= 0; row--) {
			tree.collapseRow(row);
		}
	}

	private void forSelectionTree(FilteredTreeModel filteredModel) {
		DefaultMutableTreeNode currentNode = top.getNextNode();
		while (currentNode != null) {
			if(filteredModel.isMatched(currentNode)
					&& currentNode.getUserObject() instanceof PackageInfo) {
				TreePath temptreePath = new TreePath(currentNode.getPath());
				tree.setSelectionPath(temptreePath);
				tree.scrollPathToVisible(temptreePath);
				return;
			}
			currentNode = currentNode.getNextNode();
		}
	}

	private void setFilter(){
		String filter = textSearchFilter.getText().trim();

		FilteredTreeModel filteredModel = (FilteredTreeModel) tree.getModel();
		filteredModel.setFilter(filter);

		if(!filter.isEmpty()) {
			expandAll(tree);
			forSelectionTree(filteredModel);
		} else {
			collapseAll(tree);
			expandOrCollapsePath(tree, new TreePath(top.getPath()),3,0, true);
		}
	}

	public int showTreeDlg()
	{
		result = APPROVE_OPTION;
		selDevice = null;
		selPackage = null;
		selApkPath = null;

		setVisible(true);

		return result;
	}

	private void openPackage()
	{
		Log.i("open package");

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node.getDepth() > 0 || node.getLevel() < 3) {
			return;
		}

		PackageInfo tempObject = ((PackageInfo)node.getUserObject()); 

		Log.i(tempObject.packageName);
		Log.i(tempObject.getLabel());
		Log.i(tempObject.getApkPath());

		DefaultMutableTreeNode deviceNode = null;

		for(deviceNode = node ; !(deviceNode.getUserObject() instanceof IDevice); deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) {

		}

		Log.i(deviceNode.getUserObject().toString());

		selDevice = ((IDevice)deviceNode.getUserObject()).getSerialNumber();
		selPackage = tempObject.packageName;
		selApkPath = tempObject.getRealApkPath();
		//selFrameworkRes = null;

		if(selApkPath == null) {
			Log.e("No Such File : " + tempObject.getApkPath());
			return;
		}

		deviceHandler.quit();
		dispose();
	}

	private boolean uninstallApk(final IDevice device, final PackageInfo packageInfo)
	{
		String errMessage = null;
		if(!packageInfo.isSystemApp()) {
			errMessage = PackageManager.uninstallPackage(packageInfo);
		} else {
			int n = MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.QUESTION_REMOVE_SYSTEM_APK);
			if(n == MessageBoxPane.NO_OPTION) {
				return false;
			}

			errMessage = PackageManager.removePackage(packageInfo);
			if(errMessage == null || errMessage.isEmpty()) {
				n = MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.QUESTION_REBOOT_SYSTEM);
				if(n == MessageBoxPane.YES_OPTION) {
					try {
						device.reboot(null);
					} catch (TimeoutException | IOException e) {
						e.printStackTrace();
					} catch (AdbCommandRejectedException e1) {
						Log.w(e1.getMessage());
					}
				}
			}
		}

		if(errMessage != null && !errMessage.isEmpty()) {
			final String errMsg = errMessage;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_FAILURE_UNINSTALLED, errMsg);
				}
			});

			return false;
		}

		return true;
	}

	private void clearData()
	{
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node == null) {
			Log.i("node == null");
			return;
		}

		IDevice device = getCurrentSelectedDevice();
		final PackageInfo packageInfo = ((PackageInfo)node.getUserObject()); 

		Log.i("clearData :" + device.getSerialNumber()  +","+ packageInfo.packageName);
		new SwingWorker<String, Object> () {
			@Override
			protected String doInBackground() throws Exception {
				return PackageManager.clearData(packageInfo);
			}

			@Override
			protected void done() {
				String errMessage = null;
				try {
					errMessage = get();
				} catch (InterruptedException | ExecutionException e) {
					errMessage = e.getMessage();
					e.printStackTrace();
				}
				if(errMessage == null) {
					MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_SUCCESS_CLEAR_DATA);
				} else {
					Log.e(errMessage);
					MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_FAILURE_CLEAR_DATA, errMessage);
				}
			};
		}.execute();
	}

	private void removePackage()
	{
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node == null) {
			Log.i("node == null");
			return;
		}

		IDevice device = getCurrentSelectedDevice();
		PackageInfo tempObject = ((PackageInfo)node.getUserObject()); 

		Log.i("remove :" + device.getSerialNumber()  +","+ tempObject.getCodePath());
		boolean run = uninstallApk(device, tempObject);

		if(run) {
			TreePath path = new TreePath(node.getPath());
			MutableTreeNode nodepath =(MutableTreeNode) path.getLastPathComponent();
			Log.i("Trying to remove tree : "+nodepath.toString());
			MutableTreeNode parent=(MutableTreeNode)nodepath.getParent();

			parent.remove(nodepath);
			//FilteredTreeModel model=(FilteredTreeModel)tree.getModel();
			//model.nodesWereRemoved(parent,new int[]{index},null);
			tree.updateUI();
		}
	}

	private void showDetailInfo()
	{
		Log.i("showDetailInfo()");

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node.getChildCount() != 0) {
			Log.i("not node!");
			return ;
		}

		PackageInfo info = ((PackageInfo)node.getUserObject()); 
		PackageInfoPanel packageInfoPanel = new PackageInfoPanel();
		packageInfoPanel.setPackageInfo(info);
		packageInfoPanel.showDialog(this);
	}

	private void pullPackage()
	{
		Log.i("pullPackage()");

		if(!EventQueue.isDispatchThread()) {
			Log.w("Must be accessing to pullingNodes in EventDispatchThread");
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						pullPackage();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}

		if(tree == null) {
			Log.e("tree is null");
			return;
		}

		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if(node == null || !(node.getUserObject() instanceof PackageInfo)) {
			Log.w("node is null or no node of PackageInfo!");
			return;
		}

		if(pullingNodes.contains(node)) {
			Log.e("already pulling..");
			return;
		}

		final PackageInfo packageInfo = (PackageInfo) node.getUserObject(); 

		Log.i(packageInfo.packageName);
		Log.i(packageInfo.getLabel());
		Log.i(packageInfo.getApkPath());

		final String apkPath = packageInfo.getApkPath();
		if(apkPath == null) {
			Log.e("apkPath is null");
			return;
		}

		DefaultMutableTreeNode deviceNode = null;
		for(deviceNode = ((DefaultMutableTreeNode)node.getParent());
				deviceNode != null && !(deviceNode.getUserObject() instanceof IDevice);
				deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) { }

		if(deviceNode == null) {
			Log.e("no such device node");
			return;
		}

		final IDevice device = (IDevice) deviceNode.getUserObject();
		Log.i(device.toString());

		String saveFileName;
		if(apkPath.endsWith("base.apk")) {
			saveFileName = apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
		} else {
			saveFileName = apkPath.replaceAll(".*/", "");
		}

		final File destFile = ApkFileChooser.saveApkFile(PackageTreeDlg.this, saveFileName);
		if(destFile == null) return;

		pullingNodes.add(node);

		new SwingWorker<String, Object> () {
			@Override
			protected String doInBackground() throws Exception {
				return PackageManager.pullApk(device, apkPath, destFile.getAbsolutePath());
			}

			@Override
			protected void done() {
				if(pullingNodes.contains(node)) {
					pullingNodes.remove(node);
				}

				String errMessage = null;
				try {
					errMessage = get();
				} catch (InterruptedException | ExecutionException e) {
					errMessage = e.getMessage();
					e.printStackTrace();
				}
				if(errMessage == null) {
					int n = MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.QUESTION_SUCCESS_PULL_APK, destFile.getAbsolutePath());
					switch(n) {
					case 0: // explorer
						SystemUtil.openFileExplorer(destFile);
						break;
					case 1: // open
						Launcher.run(destFile.getAbsolutePath());
						break;
					default:
						break;
					}
				} else {
					MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_FAILURE_PULLED, errMessage);
				}
			};
		}.execute();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(Resource.STR_BTN_OPEN.getString())) {
			openPackage();
		} else if(e.getActionCommand().equals(Resource.STR_BTN_CANCEL.getString())) {
			//Log.i("exit");
			selDevice = null;
			selPackage = null;
			selApkPath = null;
			result = CANCEL_OPTION;

			deviceHandler.quit();
			dispose();
		} else if(e.getSource() == refreshbtn) {
			refreshTreeList(false);
		}
	}

	public JPanel makeListTable ( )
	{
		JPanel panel = new JPanel(new BorderLayout());

		//JTable table = new JTable(new BooleanTableModel());
		table = new JTable();
		//table.setPreferredScrollableViewportSize(table.getPreferredSize());
		//table.setFillsViewportHeight(true);
		JButton addbtn= new JButton(Resource.STR_BTN_ADD.getString());

		addbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();			
				jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Resource.STR_LABEL_APK_FILE_DESC.getString(),"apk"));							

				if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
					return;

				File dir = jfc.getSelectedFile();
				String file = null;
				if(dir!=null) {
					file = dir.getPath();
				}

				Log.i("Select Apk File" + file);

				if(file == null || file.isEmpty()) return;

				FrameworkTableObject temp = new FrameworkTableObject(true, "local", "local", file);

				tableListArray.add(temp);
				((AbstractTableModel) table.getModel()).fireTableDataChanged();
				table.updateUI();

			}
		});

		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(true);
		setJTableColumnsWidth(table,550,10,120,410);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JScrollPane pane = new JScrollPane(table);

		//pane.setPreferredSize(new Dimension(550, 80) );

		panel.add(pane, BorderLayout.CENTER);
		panel.add(addbtn, BorderLayout.EAST);

		return panel;
	}

	public static void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
			double... percentages)
	{
		double total = 0;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
		}

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
		}
	}

	private void setRefreshBtnVisible(final boolean visible) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(gifPanel != null && gifPanel.isVisible() != !visible) {
					gifPanel.setVisible(!visible);
				}
				if(refreshbtn != null && refreshbtn.isVisible() != visible) {
					refreshbtn.setVisible(visible);
				}
			}
		});
	}

	class DeviceHandler extends SwingWorker<Object, Object> implements IDeviceChangeListener, IPackageStateListener {
		private boolean quit;
		Queue<IDevice> eventQueue = new LinkedList<IDevice>();

		class DevicePackageInfo {
			IDevice device;
			PackageInfo[] packages;
			PackageInfo[] displayedPackages;
			PackageInfo[] recentPackages;
			PackageInfo[] runningPackages;
		}

		public DeviceHandler() {
			AdbServerMonitor.startServerAndCreateBridgeAsync();
			registerEventListeners();
		}

		@Override
		protected Object doInBackground() throws Exception {
			boolean isWakeUp = false;
			while(!quit) {
				IDevice device = null;
				synchronized (eventQueue) {
					if(eventQueue.isEmpty()) {
						Log.v("Event queue is empty");
						setRefreshBtnVisible(true);
						eventQueue.wait();
						if(quit) break;
						isWakeUp = true;
					}
					device = eventQueue.poll();
					if(device != null && isWakeUp) {
						isWakeUp = false;
						setRefreshBtnVisible(false);
					}
				}

				if(device != null) {
					Log.v("start scanning device " + device);

					DevicePackageInfo devPack = new DevicePackageInfo();

					devPack.device = device;
					devPack.packages = PackageManager.getPackageList(device);

					if(devPack.packages != null && devPack.packages.length > 0) { 
						String[] pkgs = PackageManager.getRecentlyActivityPackages(device);
						ArrayList<PackageInfo> list = new ArrayList<PackageInfo>(pkgs.length);
						for(String name: pkgs) {
							for(PackageInfo obj: devPack.packages) {
								if(obj.packageName.equals(name)) {
									list.add(obj);
								}
							}
						}
						devPack.recentPackages = list.toArray(new PackageInfo[list.size()]);

						pkgs = PackageManager.getCurrentlyRunningPackages(device);
						list = new ArrayList<PackageInfo>(pkgs.length);
						for(String name: pkgs) {
							for(PackageInfo obj: devPack.packages) {
								if(obj.packageName.equals(name)) {
									list.add(obj);
								}
							}
						}
						devPack.runningPackages = list.toArray(new PackageInfo[list.size()]);

						WindowStateInfo[] winStates = PackageManager.getCurrentlyDisplayedPackages(device);
						list = new ArrayList<PackageInfo>(pkgs.length);
						for(WindowStateInfo info: winStates) {
							for(PackageInfo obj: devPack.packages) {
								if(obj.packageName.equals(info.packageName) && !list.contains(obj)) {
									if(info.isCurrentFocus) {
										list.add(0, obj);
									} else {
										list.add(obj);	
									}
								}
							}
						}
						devPack.displayedPackages = list.toArray(new PackageInfo[list.size()]);

						publish(devPack);
					}
				}
			}
			Log.v("doInBackground() Quit");
			return null;
		}

		public class SortDefaultMutableTreeNode extends DefaultMutableTreeNode {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public SortDefaultMutableTreeNode(Object userObject) {
				super(userObject);
			}
			
			@Override
			public void add(MutableTreeNode newChild) {
				super.add(newChild);				
				sort();
			}

			@SuppressWarnings("unchecked")
			public void sort() {
				Collections.sort(children, compare());
			}

			private Comparator<? super TreeNode> compare() {
				return new Comparator<TreeNode>() {
					@Override
					public int compare(TreeNode o1, TreeNode o2) {
						DefaultMutableTreeNode uo1 = (DefaultMutableTreeNode)o1;
						DefaultMutableTreeNode uo2 = (DefaultMutableTreeNode)o2;
						return uo1.getUserObject().toString().compareTo(uo2.getUserObject().toString());
					}
				};
			}
		}
	
		
		@Override
		protected void process(List<Object> chunks){
			for(Object param: chunks) {
				if(param instanceof DevicePackageInfo) {
					DevicePackageInfo devPack = (DevicePackageInfo)param;
					IDevice dev = devPack.device;

					if(dev.isOnline()) {
						DefaultMutableTreeNode devNode = getDeviceNode(dev);
						SortDefaultMutableTreeNode priv_app = new SortDefaultMutableTreeNode("priv-app");
						SortDefaultMutableTreeNode systemapp = new SortDefaultMutableTreeNode("app");
						DefaultMutableTreeNode framework_app = new DefaultMutableTreeNode("framework");
						DefaultMutableTreeNode system = new DefaultMutableTreeNode("system");
						SortDefaultMutableTreeNode dataapp = new SortDefaultMutableTreeNode("app");
						DefaultMutableTreeNode data = new DefaultMutableTreeNode("data");
						DefaultMutableTreeNode displayed = new DefaultMutableTreeNode("*" + Resource.STR_TREE_NODE_DISPLAYED.getString());
						DefaultMutableTreeNode recently = new DefaultMutableTreeNode("*" + Resource.STR_TREE_NODE_RECENTLY.getString());
						DefaultMutableTreeNode running = new DefaultMutableTreeNode("*" + Resource.STR_TREE_NODE_RUNNING_PROC.getString());

						data.add(dataapp);

						for(PackageInfo info: devPack.packages) {
							String apkPath = info.getApkPath(); 
							if(apkPath == null || apkPath.isEmpty()) {
								continue;
							}

							DefaultMutableTreeNode temp = new DefaultMutableTreeNode(info);

							if(apkPath.startsWith("/system/priv-app/")) {
								priv_app.add(temp);
							} else if(apkPath.startsWith("/system/app/")) {
								systemapp.add(temp);
							} else if(apkPath.startsWith("/system/framework/")) {
								framework_app.add(temp);

								FrameworkTableObject tableObject = new FrameworkTableObject(false, dev.getProperty(IDevice.PROP_DEVICE_MODEL), dev.getSerialNumber(), apkPath);

								if(apkPath.startsWith("/system/framework/framework-res.apk") || apkPath.startsWith("/system/framework/twframework-res.apk")) {
									tableObject.buse = true;
								}

								tableListArray.add(tableObject);
							} else if(apkPath.startsWith("/data/app/")) {
								dataapp.add(temp);
							}
						}

						if(priv_app.getChildCount() > 0) system.add(priv_app);
						if(systemapp.getChildCount() > 0) system.add(systemapp);
						if(framework_app.getChildCount() > 0) system.add(framework_app);

						for(PackageInfo obj: devPack.displayedPackages) {
							displayed.add(new DefaultMutableTreeNode(obj));
						}

						for(PackageInfo obj: devPack.recentPackages) {
							recently.add(new DefaultMutableTreeNode(obj));
						}

						for(PackageInfo obj: devPack.runningPackages) {
							running.add(new DefaultMutableTreeNode(obj));
						}

						devNode.removeAllChildren();
						if(displayed.getChildCount() > 0) devNode.add(displayed);
						if(recently.getChildCount() > 0) devNode.add(recently);
						if(running.getChildCount() > 0) devNode.add(running);
						if(system.getChildCount() > 0) devNode.add(system);
						if(dataapp.getChildCount() > 0) devNode.add(data);

						//expandOrCollapsePath(tree, new TreePath(devNode.getPath()),3,0, true);
						tree.updateUI();
						setFilter();

						table.setModel(new SimpleCheckTableModel(columnNames, tableListArray));
						table.setPreferredScrollableViewportSize(new Dimension(0,80));
						setJTableColumnsWidth(table,550,10,120,410);
					}
				}
			}
		}

		public void quit() {
			quit = true;
			synchronized (eventQueue) {
				eventQueue.notifyAll();	
			}
			unregisterEventListeners();
		}

		public void registerEventListeners() {
			AndroidDebugBridge.addDeviceChangeListener(this);
			PackageManager.addPackageStateListener(this);
		}

		public void unregisterEventListeners() {
			AndroidDebugBridge.removeDeviceChangeListener(this);
			PackageManager.removePackageStateListener(this);
		}

		private DefaultMutableTreeNode getDeviceNode(IDevice device) {
			for(int i = 0; i < top.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)top.getChildAt(i);
				if(node.getUserObject() instanceof IDevice 
						&& device.equals(node.getUserObject())) {
					return node;
				}
			}
			return null;
		}

		private DefaultMutableTreeNode addDeviceNode(IDevice device) {
			DefaultMutableTreeNode node = getDeviceNode(device);
			if(node == null) {
				removeNoDeviceNodes();
				node = new DefaultMutableTreeNode(device);
			}
			node.removeAllChildren();
			if(device.isOnline()) {
				node.add(new DefaultMutableTreeNode("#" + Resource.STR_LABEL_LOADING.getString()));

				synchronized (eventQueue) {
					if(!eventQueue.contains(device)) {
						eventQueue.add(device);
						eventQueue.notifyAll();
					} else {
						Log.v("Added device to event queue : " + device.getSerialNumber());
					}
				}
			} else {
				node.add(new DefaultMutableTreeNode(Resource.STR_MSG_DEVICE_UNAUTHORIZED.getString()));
			}
			top.add(node);
			tree.expandPath(new TreePath(node.getPath()));
			return node;
		}

		private void removeNoDeviceNodes() {
			for(int i = 0; i < top.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)top.getChildAt(i);
				if(!(node.getUserObject() instanceof IDevice)) {
					top.remove(node);
				}
			}
		}

		@Override
		public void deviceChanged(final IDevice device, int changeMask) { 
			Log.v("deviceChanged() " + device.getSerialNumber() + ", " + device.getState() + ", changeMask " + changeMask);
			if((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						addDeviceNode(device);
						tree.updateUI();
					}
				});
			}
		}

		@Override
		public void deviceConnected(final IDevice device) {
			Log.v("deviceConnected() " + device.getSerialNumber() + ", " + device.getState());

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					addDeviceNode(device);
					tree.updateUI();
				}
			});
		}

		@Override
		public void deviceDisconnected(final IDevice device) {
			Log.v("deviceDisconnected() " + device.getSerialNumber() + ", " + device.getState());

			synchronized (eventQueue) {
				if(eventQueue.contains(device)) {
					eventQueue.remove(device);
				}
			}

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					DefaultMutableTreeNode node = getDeviceNode(device);
					if(node != null) {
						top.remove(node);
						if(top.getChildCount() == 0) {
							top.add(new DefaultMutableTreeNode(Resource.STR_MSG_DEVICE_NOT_FOUND.getString().replace("\n", " ")));
						}
						tree.updateUI();
					}
				}
			});
		}

		@Override
		public void packageInstalled(PackageInfo packageInfo) {

		}

		@Override
		public void packageUninstalled(PackageInfo packageInfo) {

		}

		@Override
		public void enableStateChanged(PackageInfo packageInfo) {

		}
	}

	class WindowEventHandler extends WindowAdapter {
		@Override
		public void windowOpened(WindowEvent e) {		
			textSearchFilter.requestFocus();
		}

		@Override
		public void windowClosing(WindowEvent e) {
			selDevice = null;
			selPackage = null;
			selApkPath = null;
			result = CANCEL_OPTION;

			deviceHandler.quit();
		}
	}

	class MouseEventHandler extends MouseAdapter { 
		@Override
		public void mousePressed(MouseEvent e) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			if(selRow != -1) {
				if(e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
					TreePath path = tree.getPathForLocation ( e.getX (), e.getY () );
					tree.setSelectionPath(selPath);
					Rectangle pathBounds = tree.getUI ().getPathBounds ( tree, path );
					if ( pathBounds != null && pathBounds.contains ( e.getX (), e.getY () ) )
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)
								tree.getLastSelectedPathComponent();
						if(node.getDepth() > 0 || node.getLevel() < 3) {
							return;
						}

						JPopupMenu menu = new JPopupMenu ();

						JMenuItem menuitemOpen = new JMenuItem(Resource.STR_BTN_OPEN.getString() );                                                        
						menuitemOpen.addActionListener(new ActionListener(){ 
							public void actionPerformed(ActionEvent e) {
								openPackage();
							}});
						menuitemOpen.setIcon(Resource.IMG_TREE_MENU_OPEN.getImageIcon());                                                        
						menu.add(menuitemOpen);

						JMenuItem menuitemDetail = new JMenuItem(Resource.STR_BTN_DETAILS_INFO.getString() );                                                        
						menuitemDetail.addActionListener(new ActionListener(){ 
							public void actionPerformed(ActionEvent e) {
								showDetailInfo();
							}});
						menuitemDetail.setIcon(Resource.IMG_TREE_APK.getImageIcon());                                                        
						menu.add(menuitemDetail);

						JMenuItem menuitemSave = new JMenuItem(Resource.STR_BTN_SAVE.getString() );                                                        
						menuitemSave.addActionListener(new ActionListener(){ 
							public void actionPerformed(ActionEvent e) {
								pullPackage();
							}});
						menuitemSave.setIcon(Resource.IMG_TREE_MENU_SAVE.getImageIcon());                                                        
						menu.add(menuitemSave);

						JMenuItem menuitemClear = new JMenuItem(Resource.STR_MENU_CLEAR_DATA.getString() );                                                        
						menuitemClear.addActionListener(new ActionListener(){ 
							public void actionPerformed(ActionEvent e) {
								clearData();
							}});
						menuitemClear.setIcon(Resource.IMG_TREE_MENU_CLEARDATA.getImageIcon());                                                        
						menu.add(menuitemClear);

						JMenuItem menuitemDel = new JMenuItem(Resource.STR_BTN_DEL.getString() );                                                        
						menuitemDel.addActionListener(new ActionListener(){ 
							public void actionPerformed(ActionEvent e) {
								removePackage();
							}});
						menuitemDel.setIcon(Resource.IMG_TREE_MENU_DELETE.getImageIcon());                                                        
						menu.add(menuitemDel);

						JMenuItem menuitemaddframeworkres = new JMenuItem(Resource.STR_SETTINGS_RES.getString() );                                                        
						menuitemaddframeworkres.addActionListener(new ActionListener(){ 
							public void actionPerformed(ActionEvent e) {
								addframeworkresIntree();
							}});
						menuitemaddframeworkres.setIcon(Resource.IMG_TREE_MENU_LINK.getImageIcon());                                                        
						menu.add(menuitemaddframeworkres);

						menu.show ( tree, e.getX (), e.getY () );
					}
				}
				else if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {                    	
					openPackage();
				}
			}
		}
	}
}