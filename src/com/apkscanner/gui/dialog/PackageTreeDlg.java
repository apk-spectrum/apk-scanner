package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.ApkInstaller.ApkInstallerListener;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.gui.messagebox.JTextOptionPane;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.FilteredTreeModel;
import com.apkscanner.gui.util.SimpleCheckTableModel;
import com.apkscanner.gui.util.SimpleCheckTableModel.TableRowObject;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class PackageTreeDlg extends JPanel implements TreeSelectionListener, ActionListener, WindowListener{
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
	private JFrame parentframe;
	private JDialog dialog;
	static private int result;
	private String selDevice;
	private String selPackage;
	private String selApkPath;
	private String tmpApkPath;
	private JTextField textFilField;
	private FilteredTreeModel filteredModel;

	public class FrameworkTableObject implements TableRowObject {
		public Boolean buse;
		public String location;
		public String deviceID;
		public String path;

		FrameworkTableObject() {}
		FrameworkTableObject(Boolean buse, String location, String deviceID, String path) {
			this.buse = buse;
			this.location = location;
			this.deviceID = deviceID;
			this.path = path;
		}

		@Override
		public Object get(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return buse;
			case 1:
				return deviceID +"(" + location + ")";
			case 2:
				return path;
			}
			return null;
		}

		@Override
		public void set(int columnIndex, Object obj) {
			switch(columnIndex) {
			case 0:
				buse = (Boolean) obj;
				break;
			case 1:
				location = (String) obj;
				break;
			case 2:
				path = (String) obj;
				break;
			}
		}
	}

	private ArrayList<TableRowObject> tableListArray = new ArrayList<TableRowObject>();
	private JTable table;

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

	//Optionally play with line styles.  Possible values are
	//"Angled" (the default), "Horizontal", and "None".

	public PackageTreeDlg() {
		super(new BorderLayout());
		AdbServerMonitor.startServerAndCreateBridgeAsync();

		makeTreeForm();
		addTreeList();
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
		FrameworkTableObject temp = new FrameworkTableObject();

		temp.buse = true;
		temp.location = device.getName();
		temp.deviceID = device.getSerialNumber();

		temp.path = tempObject.getApkPath();

		tableListArray.add(temp);
		((AbstractTableModel) table.getModel()).fireTableDataChanged();

		table.updateUI();

	}

	private void addTreeList()
	{
		if(!refreshbtn.isVisible()) {
			Log.i("Already refreshing...");
			return;
		}

		tableListArray.clear();
		table.updateUI();

		top.removeAllChildren();
		tree.updateUI();

		refreshbtn.setVisible(false);
		gifPanel.setVisible(true);

		Thread t = new Thread(new Runnable() {
			public void run() {


				createDeviceNodes(top);

				gifPanel.setVisible(false);
				refreshbtn.setVisible(true);
			}

			private void createDeviceNodes(DefaultMutableTreeNode top)
			{
				AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
				IDevice[] devList = adb.getDevices();

				DefaultMutableTreeNode[] devTree = new DefaultMutableTreeNode[devList.length];

				if(devList.length == 0) {					
					top.add(new DefaultMutableTreeNode(Resource.STR_MSG_DEVICE_NOT_FOUND.getString().replace("\n", " ")));									
				}

				for(int i = 0; i < devList.length; i++) {
					devTree[i] = new DefaultMutableTreeNode(devList[i]);
					top.add(devTree[i]);
					if(devList[i].isOnline()) {
						devTree[i].add(new DefaultMutableTreeNode(Resource.STR_LABEL_LOADING.getString()));
					} else {
						devTree[i].add(new DefaultMutableTreeNode(Resource.STR_MSG_DEVICE_UNAUTHORIZED.getString()));
					}
				}
				tree.updateUI();
				expandOrCollapsePath(tree, new TreePath(top.getPath()),3,0, true);

				String[] columnNames = {"", Resource.STR_LABEL_DEVICE.getString(), Resource.STR_LABEL_PATH.getString()};
				for(int i = 0; i < devList.length; i++) {
					if(devList[i].isOnline()) {
						DefaultMutableTreeNode priv_app = new DefaultMutableTreeNode("priv-app");
						DefaultMutableTreeNode systemapp = new DefaultMutableTreeNode("app");
						DefaultMutableTreeNode framework_app = new DefaultMutableTreeNode("framework");
						DefaultMutableTreeNode system = new DefaultMutableTreeNode("system");
						DefaultMutableTreeNode dataapp = new DefaultMutableTreeNode("app");
						DefaultMutableTreeNode data = new DefaultMutableTreeNode("data");
						DefaultMutableTreeNode recently = new DefaultMutableTreeNode("Recently activity package");
						DefaultMutableTreeNode running = new DefaultMutableTreeNode("Currently running package");

						system.add(priv_app);
						system.add(systemapp);
						system.add(framework_app);

						data.add(dataapp);

						PackageInfo[] packages = PackageManager.getPackageList(devList[i]);
						for(PackageInfo info: packages) {
							DefaultMutableTreeNode temp = new DefaultMutableTreeNode(info);

							if(info.getApkPath().startsWith("/system/priv-app/")) {
								priv_app.add(temp);		        		
							} else if(info.getApkPath().startsWith("/system/app/")) {
								systemapp.add(temp);
							} else if(info.getApkPath().startsWith("/system/framework/")) {
								framework_app.add(temp);

								FrameworkTableObject tableObject = new FrameworkTableObject();


								if(info.getApkPath().startsWith("/system/framework/framework-res.apk") || info.getApkPath().startsWith("/system/framework/twframework-res.apk")) {
									tableObject.buse = true;
								} else {
									tableObject.buse = false;
								}


								tableObject.location = devList[i].getName();
								tableObject.deviceID = devList[i].getSerialNumber();
								tableObject.path = ((PackageInfo)temp.getUserObject()).getApkPath(); 

								tableListArray.add(tableObject);
							} else if(info.getApkPath().startsWith("/data/app/")) {
								dataapp.add(temp);
							}
						}

						String[] recentPackages = PackageManager.getRecentlyActivityPackages(devList[i]);
						for(String pkg: recentPackages) {
							for(PackageInfo obj: packages) {
								if(obj.packageName.equals(pkg)) {
									recently.add(new DefaultMutableTreeNode(obj));
								}
							}
						}

						String[] runningPackages = PackageManager.getCurrentlyRunningPackages(devList[i]);
						for(String pkg: runningPackages) {
							for(PackageInfo obj: packages) {
								if(obj.packageName.equals(pkg)) {
									running.add(new DefaultMutableTreeNode(obj));
								}
							}
						}

						devTree[i].removeAllChildren();
						devTree[i].add(recently);
						devTree[i].add(running);
						devTree[i].add(system);
						devTree[i].add(data);
						//add table

						table.setModel(new SimpleCheckTableModel(columnNames, tableListArray));
						table.setPreferredScrollableViewportSize(new Dimension(0,80));
						setJTableColumnsWidth(table,550,10,120,410);

						tree.updateUI();
						expandOrCollapsePath(tree, new TreePath(top.getPath()),3,0, true);
					}
				}

				String defalutResPath = (String)Resource.PROP_FRAMEWORK_RES.getData();
				if(defalutResPath != null) {
					for(String s: defalutResPath.split(";")) {
						if(s.isEmpty()) continue;
						tableListArray.add(new FrameworkTableObject(false, "local", "local", s));
					}
					table.setModel(new SimpleCheckTableModel(columnNames, tableListArray));
					table.setPreferredScrollableViewportSize(new Dimension(0,80));
					setJTableColumnsWidth(table,550,10,120,410);
				}

				//Log.i("end  loading package : " + dev.device);

				if(textFilField != null) {
					if(textFilField.getText().length() >0){
						makefilter(textFilField.getText());
					}
				}
			}
		});
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
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

	private boolean uninstallApk(final IDevice device, final PackageInfo packageInfo)
	{

		String errMessage = null;
		if(!packageInfo.isSystemApp()) {
			errMessage = PackageManager.uninstallPackage(packageInfo);
		} else {
			errMessage = PackageManager.removePackage(packageInfo);
			if(errMessage == null || errMessage.isEmpty()) {
				try {
					device.reboot(null);
				} catch (TimeoutException | AdbCommandRejectedException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		if(errMessage != null && !errMessage.isEmpty()) {
			final String errMsg = errMessage;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JTextOptionPane.showTextDialog(null, Resource.STR_MSG_FAILURE_UNINSTALLED.getString() + "\nConsol output:", errMsg,  Resource.STR_LABEL_ERROR.getString(), JTextOptionPane.ERROR_MESSAGE,
							null, new Dimension(300, 50));
				}
			});

			return false;
		}

		return true;
	}

	private void makeTreeForm() {
		//Create the nodes.
		top =
				new DefaultMutableTreeNode("Device");
		//createNodes(top);

		//Create a tree that allows one selection at a time.

		FilteredTreeModel model = new FilteredTreeModel(new DefaultTreeModel(top));

		MouseListener ml = new MouseAdapter() {
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
									OpenPackage();
								}});
							menuitemOpen.setIcon(Resource.IMG_TREE_MENU_OPEN.getImageIcon());                                                        
							menu.add(menuitemOpen);

							JMenuItem menuitemSave = new JMenuItem(Resource.STR_BTN_SAVE.getString() );                                                        
							menuitemSave.addActionListener(new ActionListener(){ 
								public void actionPerformed(ActionEvent e) {
									PullPackage();
								}});
							menuitemSave.setIcon(Resource.IMG_TREE_MENU_SAVE.getImageIcon());                                                        
							menu.add(menuitemSave);

							JMenuItem menuitemDel = new JMenuItem(Resource.STR_BTN_DEL.getString() );                                                        
							menuitemDel.addActionListener(new ActionListener(){ 
								public void actionPerformed(ActionEvent e) {
									Removepackage();
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


							//                            menu.add ( new JMenuItem ( Resource.STR_BTN_OPEN.getString() ) ).addActionListener(new ActionListener(){ 
							//                            	   public void actionPerformed(ActionEvent e) {
							//                            		   OpenPackage();
							//                            	   }});
							//                            menu.add ( new JMenuItem ( Resource.STR_BTN_SAVE.getString() ) ).addActionListener(new ActionListener(){ 
							//                            	   public void actionPerformed(ActionEvent e) {
							//                            		   PullPackage();
							//                            	   }});
							//                            menu.add ( new JMenuItem ( Resource.STR_BTN_DEL.getString() ) ).addActionListener(new ActionListener(){ 
							//                           	   public void actionPerformed(ActionEvent e) {
							//                           		   Removepackage();
							//                           	   }});
							//                            menu.add ( new JMenuItem ( Resource.STR_SETTINGS_RES.getString() ) ).addActionListener(new ActionListener(){ 
							//                         	   public void actionPerformed(ActionEvent e) {
							//                         		   	addframeworkresIntree();
							//                         	   }});

							//menu.add ( new JMenuItem ( Resource.STR_BTN_EXPORT.getString() ) ).addActionListener(new ActionListener(){ 
							//	   public void actionPerformed(ActionEvent e) {
							//		   
							//	   }});
							menu.show ( tree, e.getX (), e.getY () );
						}
					}
					else if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {                    	
						OpenPackage();
					}
				}
			}
		};


		tree = new JTree(model);
		tree.addMouseListener(ml);

		tree.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 6248791058116909814L;
			private ImageIcon iconApk = Resource.IMG_TREE_APK.getImageIcon();
			private ImageIcon iconDevice = Resource.IMG_TREE_DEVICE.getImageIcon();
			private ImageIcon iconTop = Resource.IMG_TREE_TOP.getImageIcon();
			private ImageIcon iconFolder = Resource.IMG_TREE_FOLDER.getImageIcon();

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
				} else if(level==1) {
					setIcon(iconDevice);
				} else {
					if(node.getUserObject() instanceof PackageInfo) {
						setIcon(iconApk);
					} else {
						setIcon(iconFolder);
					}
				}                
				return c;
			}
		});

		//Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		//Create the HTML viewing pane.
		textFieldapkPath = new JTextField();
		textFieldapkPath.setEditable(false);

		textFilField = new JTextField();


		textFilField.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke) {
				if(ke.getKeyCode() == KeyEvent.VK_DOWN) {
					KeyEvent key = new KeyEvent(tree, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
					tree.dispatchEvent(key);	        		
				} else if (ke.getKeyCode() == KeyEvent.VK_UP) {
					KeyEvent key = new KeyEvent(tree, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
					tree.dispatchEvent(key);        			
				}
			}

			public void keyReleased(KeyEvent ke) {

				if(textFilField.getText().length() ==0) {
					collapseAll(tree);
					expandOrCollapsePath(tree, new TreePath(top.getPath()),3,0, true);            		
					return;
				}

				if(!(ke.getKeyChar()==27||ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
				{
					//Log.i(textFilField.getText()+ ":" + Integer.valueOf(ke.getKeyChar()));                	

					if(ke.getKeyChar()==10) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)
								tree.getLastSelectedPathComponent();
						if(node != null) {
							OpenPackage();
						}                		
					} else {
						makefilter (textFilField.getText());
					}

				}
			}

			@SuppressWarnings("unused")
			private void expandTree(final JTree tree) {
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
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

		JPanel ButtonPanel = new JPanel(new BorderLayout());

		gifPanel = new JPanel();

		ImageIcon icon = Resource.IMG_WAIT_BAR.getImageIcon();
		JLabel GifLabel = new JLabel(icon);

		JLabel Loading = new JLabel(Resource.STR_LABEL_LOADING.getString());

		gifPanel.add(Loading);
		gifPanel.add(GifLabel);

		gifPanel.setVisible(false);

		JPanel ButtonPanelWest = new JPanel();
		ButtonPanelWest.add(gifPanel);
		ButtonPanelWest.add(refreshbtn);

		JPanel ButtonPanelEast = new JPanel();
		ButtonPanelEast.add(openbtn);
		ButtonPanelEast.add(exitbtn);     

		ButtonPanel.add(ButtonPanelWest, BorderLayout.WEST);

		ButtonPanel.add(ButtonPanelEast, BorderLayout.EAST);     



		Dimension minimumSize = new Dimension(100, 50);
		textFieldapkPath.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);

		//Add the split pane to this panel.
		//add(splitPane);
		JPanel panelnorth = new JPanel(new BorderLayout());                
		JPanel panelsourth = new JPanel(new BorderLayout());        
		JPanel panelsearch = new JPanel(new BorderLayout());

		panelsearch.add(new JLabel(Resource.STR_LABEL_SEARCH.getString() + " : "), BorderLayout.WEST);
		panelsearch.add(textFilField, BorderLayout.CENTER);

		panelnorth.add(textFieldapkPath,BorderLayout.NORTH);
		panelnorth.add(treeView,BorderLayout.CENTER);
		panelnorth.add(panelsearch,BorderLayout.SOUTH);


		panelsourth.add(checkboxUseframework, BorderLayout.NORTH);
		panelsourth.add(ListPanel, BorderLayout.CENTER);
		panelsourth.add(ButtonPanel,BorderLayout.SOUTH);

		add(panelnorth,BorderLayout.CENTER);
		add(panelsourth,BorderLayout.SOUTH);





		//add(NorthPanel,BorderLayout.NORTH);
		//add(panel, BorderLayout.CENTER);

	}

	public void forselectionTree () {
		DefaultMutableTreeNode currentNode = top.getNextNode();



		//DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)filteredModel.getRoot();
		do {
			//Log.i(currentNode.getLevel());
			//Log.i(currentNode.getUserObject());
			//Log.i(filteredModel.getChildCount(currentNode));

			//Log.i(tree.isCollapsed(new TreePath(currentNode.getPath())));

			if(currentNode.getLevel()==3 && filteredModel.getChildCount(currentNode) > 0) {
				//for(int i=0; i<filteredModel.getChildCount(currentNode); i++) {

				TreePath temptreePath = new TreePath(((DefaultMutableTreeNode)(filteredModel.getChild(currentNode, 0))).getPath());

				tree.setSelectionPath(temptreePath);
				tree.scrollPathToVisible(temptreePath);
				return;
				//}
			}

			//	           if (currentNode.getLevel()==4 && tree.isVisible(new TreePath(currentNode.getPath())) == true) {
			//	        	   tree.setSelectionPath(new TreePath(currentNode.getPath()));	        		        	   
			//	        	   break;
			//	           }
			currentNode = currentNode.getNextNode();
		}
		while (currentNode != null);
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();
		if (node == null) return;
		//Object nodeInfo = node.getUserObject();

		if(node.getDepth() > 0 || node.getLevel() < 3) {
			TreeNode [] treenode = node.getPath();
			TreePath path = new TreePath(treenode);
			textFieldapkPath.setText(path.toString());
		} else {
			PackageInfo tempObject = ((PackageInfo)node.getUserObject());
			textFieldapkPath.setText(tempObject.getApkPath() + " - " + tempObject.packageName);
		}
	}

	private void expandTree(final JTree tree) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private void makefilter (String temp){
		filteredModel = (FilteredTreeModel) tree.getModel();
		filteredModel.setFilter(temp);
		DefaultTreeModel treeModel = (DefaultTreeModel) filteredModel.getTreeModel();
		treeModel.reload();


		expandTree(tree);
		forselectionTree ();
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 */
	@SuppressWarnings("unused")
	private void createAndShowGUI(Component component)
	{
		final PackageTreeDlg ptg = new PackageTreeDlg();

		//Create and set up the window.
		dialog = new JDialog(new JFrame(), Resource.STR_TREE_OPEN_PACKAGE.getString(), true);
		dialog.setIconImage(Resource.IMG_USB_ICON.getImageIcon().getImage());
		//dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 8368291008098324014L;
			public void actionPerformed(ActionEvent e) {
				result = CANCEL_OPTION;
				dialog.dispose();
			}
		});

		KeyStroke vk_f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(vk_f5, "VK_F5");
		dialog.getRootPane().getActionMap().put("VK_F5", new AbstractAction() {
			private static final long serialVersionUID = -5281980076592985530L;
			public void actionPerformed(ActionEvent e) {
				ptg.addTreeList();

			}
		});

		//Add content to the window.
		dialog.add(ptg);

		dialog.addWindowListener(this); 

		//Display the window.
		dialog.pack();
		dialog.setBounds(100, 100, 600, 400);
		dialog.setMinimumSize(new Dimension(600, 400));

		dialog.setLocationRelativeTo(component);
		dialog.setVisible(true);
		dialog.dispose();

		Log.i("package dialog closed");
	}

	public int showTreeDlg(Component component)
	{
		result = APPROVE_OPTION;
		selDevice = null;
		selPackage = null;
		selApkPath = null;
		parentframe = (JFrame) component;

		//Create and set up the window.
		dialog = new JDialog(new JFrame(), Resource.STR_TREE_OPEN_PACKAGE.getString(), true);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setIconImage(Resource.IMG_USB_ICON.getImageIcon().getImage());

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 8368291008098324014L;
			public void actionPerformed(ActionEvent e) {
				selDevice = null;
				selPackage = null;
				selApkPath = null;
				result = CANCEL_OPTION;
				dialog.dispose();
			}
		});

		KeyStroke vk_f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(vk_f5, "VK_F5");
		dialog.getRootPane().getActionMap().put("VK_F5", new AbstractAction() {
			private static final long serialVersionUID = -5281980076592985530L;
			public void actionPerformed(ActionEvent e) {
				addTreeList();
			}
		});

		//Add content to the window.
		dialog.add(this);

		dialog.addWindowListener( this);

		//dialog.setResizable( false );
		//dialog.setLocationRelativeTo(null);

		//Display the window.
		dialog.pack();
		//dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);

		dialog.setBounds(0, 0, 600, 400);
		dialog.setMinimumSize(new Dimension(600, 400));

		dialog.setLocationRelativeTo(component);
		dialog.setVisible(true);
		dialog.dispose();

		return result;
	}

	private void OpenPackage()
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

		dialog.dispose();
	}

	private void Removepackage()
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

	private void PullPackage()
	{
		Log.i("PullPackage()");

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

		if(node.getChildCount() != 0) {
			Log.i("not node!");
			return ;
		}

		PackageInfo tempObject = ((PackageInfo)node.getUserObject()); 

		Log.i(tempObject.packageName);
		Log.i(tempObject.getLabel());
		Log.i(tempObject.getApkPath());

		DefaultMutableTreeNode deviceNode = null;
		for(deviceNode = node ; !(deviceNode.getUserObject() instanceof IDevice); deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) { }

		Log.i(deviceNode.getUserObject().toString());

		final String device = ((IDevice)deviceNode.getUserObject()).getSerialNumber();

		final String apkPath = tempObject.getApkPath();
		if(apkPath == null) return;

		String saveFileName;
		if(apkPath.endsWith("base.apk")) {
			saveFileName = apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
		} else {
			saveFileName = apkPath.replaceAll(".*/", "");
		}

		final File destFile = ApkFileChooser.saveApkFile(parentframe, saveFileName);
		if(destFile == null) return;

		ApkInstaller apkInstaller = new ApkInstaller(device, new ApkInstallerListener() {
			StringBuilder sb = new StringBuilder();
			@Override
			public void OnError(int cmdType, String device) {
				JTextOptionPane.showTextDialog(null, Resource.STR_MSG_FAILURE_PULLED.getString() + "\n\nConsol output", sb.toString(),  Resource.STR_LABEL_ERROR.getString(), JTextOptionPane.ERROR_MESSAGE,
						null, new Dimension(400, 100));
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				int n = ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_SUCCESS_PULL_APK.getString() + "\n" + destFile.getAbsolutePath(),
						Resource.STR_LABEL_QUESTION.getString(),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
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
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) { sb.append(msg); }
		});		
		apkInstaller.pullApk(apkPath, destFile.getAbsolutePath());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(Resource.STR_BTN_OPEN.getString())) {
			OpenPackage();
		} else if(e.getActionCommand().equals(Resource.STR_BTN_CANCEL.getString())) {
			//Log.i("exit");
			selDevice = null;
			selPackage = null;
			selApkPath = null;
			result = CANCEL_OPTION;
			dialog.dispose();
		} else if(e.getSource() == refreshbtn) {
			addTreeList();
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

				FrameworkTableObject temp = new FrameworkTableObject();

				temp.buse = true;
				temp.location = "local";
				temp.deviceID = "local";
				temp.path = file;

				tableListArray.add(temp);
				((AbstractTableModel) table.getModel()).fireTableDataChanged();
				table.updateUI();

				//    			for(String f: resList) {
				//    				if(file.equals(f)) return;
				//    			}
				//    			resList.add(file);
				//    			jlist.setListData(resList.toArray(new String[0]));
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

	public void collapseAll(JTree tree)
	{
		int row = tree.getRowCount() - 1;
		while (row >= 0) {
			tree.collapseRow(row);
			row--;
		}
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
	@Override
	public void windowOpened(WindowEvent e) {		
		textFilField.requestFocus();
	}
	@Override
	public void windowClosing(WindowEvent e) {
		selDevice = null;
		selPackage = null;
		selApkPath = null;
		result = CANCEL_OPTION;
	}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
}