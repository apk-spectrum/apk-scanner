package com.ApkInfo.UI;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.ApkInfo.Core.AdbWrapper;
import com.ApkInfo.Core.AdbWrapper.DeviceStatus;
import com.ApkInfo.Core.AdbWrapper.PackageListObject;
import com.ApkInfo.Core.PackageTreeDataManager;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.FilteredTreeModel;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;

import java.util.ArrayList;
import java.io.File;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
 
public class PackageTreeDlg extends JPanel
                      implements TreeSelectionListener, ActionListener{
	private static final long serialVersionUID = 813267847663868531L;
	private JTextField textFieldapkPath;
    private JTree tree;
    private DefaultMutableTreeNode top;
    private JPanel gifPanel;

    static private JDialog dialog;
    private String selDevice;
    private String selPackage;
    private String tmpApkPath;
    
    public String getSelectedDevice() {
    	return selDevice;
    }
    
    public String getSelectedPackage() {
    	return selPackage;
    }
    
    public File getSelectedFile() {
    	if(tmpApkPath != null)
    		return new File(tmpApkPath);
    	return null;
    }
    
    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
	
    class DeviceString {
    	String Devicename;
    	String Label;
    	
    	@Override
    	public String toString() {
    		return Label;
    	}
    	
    }
    
    public PackageTreeDlg() {
        super(new BorderLayout());
        makeTreeForm();
        addTreeList();
    }
    
    private void addTreeList() {
    	
    	top.removeAllChildren();
    	tree.updateUI();
    	Thread t = new Thread(new Runnable() {
			public void run(){
					ArrayList<DeviceStatus> DeviceList = AdbWrapper.scanDevices();
				
					System.out.println(DeviceList.size());
					
					if(DeviceList.size() == 0) {
						final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
						JOptionPane.showMessageDialog(null, "Device not found!\nplease check Connected","Warning", JOptionPane.WARNING_MESSAGE, Appicon);
						return;
					} else {
						gifPanel.setVisible(true);
						for(int i=0; i< DeviceList.size(); i++) {
							PackageTreeDataManager PackageManager = new PackageTreeDataManager(DeviceList.get(i).name);
							ArrayList<PackageListObject> ArrayDataObject = PackageManager.getDataArray();
							
							DeviceString tempDeviceString = new DeviceString();
							
							tempDeviceString.Devicename = DeviceList.get(i).name;
							tempDeviceString.Label = DeviceList.get(i).name +"("+ DeviceList.get(i).model + ")";
							
							createDeviceNodes(top, tempDeviceString, ArrayDataObject);
							
						}
						
						gifPanel.setVisible(false);
					}
				}
		    private void createDeviceNodes(DefaultMutableTreeNode top, DeviceString DeviceString, ArrayList<PackageListObject> ArrayDataObject) {
		        DefaultMutableTreeNode deviceName = new DefaultMutableTreeNode(DeviceString);
		        
		        DefaultMutableTreeNode priv_app = new DefaultMutableTreeNode("priv-app");
		        DefaultMutableTreeNode systemapp = new DefaultMutableTreeNode("app");
		        DefaultMutableTreeNode system = new DefaultMutableTreeNode("system");
		        DefaultMutableTreeNode dataapp = new DefaultMutableTreeNode("app");
		        DefaultMutableTreeNode data = new DefaultMutableTreeNode("data");
		        
		        top.add(deviceName);
		        
		        deviceName.add(system);		        
		        deviceName.add(data);
		        	        
		        system.add(priv_app);
		        system.add(systemapp);
		        
		        data.add(dataapp);
		        
		        for(int i=0; i< ArrayDataObject.size(); i++) {
		        	//System.out.println(ArrayDataObject.get(i).codePath + " : " + ArrayDataObject.get(i).label);
		        	
		        	DefaultMutableTreeNode temp = new DefaultMutableTreeNode(ArrayDataObject.get(i));		        	
		        			        	
		        	//temp.setUserObject(ArrayDataObject.get(i));
		        	
		        	if(ArrayDataObject.get(i).codePath.indexOf("/system/priv-app/") >-1) {
		        		priv_app.add(temp);		        		
		        	} else if(ArrayDataObject.get(i).codePath.indexOf("/system/app/") >-1) {
		        		systemapp.add(temp);
		        	} else if(ArrayDataObject.get(i).codePath.indexOf("/data/app/") >-1) {
		        		dataapp.add(temp);
		        	}
		        }
		        tree.updateUI();
                for (int i = 0; i < tree.getRowCount(); i++) {
                    tree.expandRow(i);
                  }
		        System.out.println("end  loading package : " + DeviceString.Devicename);
		    }
			});
		t.start();     
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
                        	if(node.getChildCount()==0)
                        	{
	                            JPopupMenu menu = new JPopupMenu ();
	                            
	                            
	                            menu.add ( new JMenuItem ( "Open" ) ).addActionListener(new ActionListener(){ 
	                            	   public void actionPerformed(ActionEvent e) {
	                            		   OpenPackage();
	                            	   }});
	                            menu.add ( new JMenuItem ( "Pull (로컬에 저장)" ) ).addActionListener(new ActionListener(){ 
	                            	   public void actionPerformed(ActionEvent e) {
	                            		   PullPackage();
	                            	   }});
	                            menu.add ( new JMenuItem ( "내보내기(미구현)" ) ).addActionListener(new ActionListener(){ 
	                            	   public void actionPerformed(ActionEvent e) {
	                            		   
	                            	   }});
	                            menu.show ( tree, e.getX (), e.getY () );
                        	}
                            
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
 
        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);
 
        //Create the HTML viewing pane.
        textFieldapkPath = new JTextField();
        textFieldapkPath.setEditable(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        final JTextField textFilField = new JTextField(80);
        
        textFilField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke)
            {
                if(!(ke.getKeyChar()==27||ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
                {
                	System.out.println(textFilField.getText()+ ":" + Integer.valueOf(ke.getKeyChar()));
                	
                    FilteredTreeModel filteredModel = (FilteredTreeModel) tree.getModel();

                    filteredModel.setFilter(textFilField.getText());
                    DefaultTreeModel treeModel = (DefaultTreeModel) filteredModel.getTreeModel();
                    treeModel.reload();
                    
                    expandTree(tree);
                }
            }
            private void expandTree(final JTree tree) {
                for (int i = 0; i < tree.getRowCount(); i++) {
                  tree.expandRow(i);
                }
              }
        });
        
 
        JPanel tpanel = new JPanel();
        tpanel.add(new JLabel("Search : "));
        tpanel.add(textFilField);
        
        panel.add(treeView,BorderLayout.CENTER);
        
        
        StandardButton openbtn = new StandardButton("Open Package",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);		
        StandardButton refreshbtn = new StandardButton("Refresh",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);
        StandardButton exitbtn = new StandardButton("Exit",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);

        openbtn.addActionListener(this);
        refreshbtn.addActionListener(this);
        exitbtn.addActionListener(this);
        
        JPanel ButtonPanel = new JPanel();
        
        
        gifPanel = new JPanel();
        
        ImageIcon icon = Resource.IMG_LOADING.getImageIcon();
        JLabel GifLabel = new JLabel(icon);
        
        JLabel Loading = new JLabel("Loading...");
        
        gifPanel.add(Loading);
        gifPanel.add(GifLabel);
        
        gifPanel.setVisible(false);
        
        JPanel tpanel2 = new JPanel(new BorderLayout());
        
        tpanel2.add(tpanel, BorderLayout.CENTER);
        ButtonPanel.add(gifPanel);
        ButtonPanel.add(openbtn);
        ButtonPanel.add(refreshbtn);        
        ButtonPanel.add(exitbtn);
        
        tpanel2.add(ButtonPanel, BorderLayout.SOUTH);
        panel.add(tpanel2,BorderLayout.SOUTH);
        
        JPanel NorthPanel = new JPanel(new BorderLayout());
                
        
        NorthPanel.add(textFieldapkPath, BorderLayout.CENTER);
        
        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(panel);
        splitPane.setBottomComponent(NorthPanel);
                
        Dimension minimumSize = new Dimension(100, 50);
        textFieldapkPath.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(400);
        splitPane.setPreferredSize(new Dimension(500, 500));
 
        //Add the split pane to this panel.
        //add(splitPane);
        add(NorthPanel,BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
    }
    
	/** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();
        if (node == null) return;
        //Object nodeInfo = node.getUserObject();
        TreeNode [] treenode = node.getPath();
        TreePath path = new TreePath(treenode);
        textFieldapkPath.setText(path.toString());
        
    }
         
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
 
        //Create and set up the window.
    	dialog = new JDialog(new JFrame(), "PackageTree", true);
    	//dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
 
        //Add content to the window.
    	dialog.add(new PackageTreeDlg());
 
        //Display the window.
    	dialog.pack();
    	dialog.setVisible(true);
    	
    	System.out.println("package dialog closed");
    }
 
    public void showTreeDlg() {
        selDevice = null;
        selPackage = null;

        //Create and set up the window.
    	dialog = new JDialog(new JFrame(), "PackageTree", true);
    	dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //Add content to the window.
    	dialog.add(this);
 
        
    	//dialog.setResizable( false );
    	dialog.setLocationRelativeTo(null);
        
        //Display the window.
    	dialog.pack();
    	dialog.setVisible(true);
    	
    	System.out.println("package dialog closed");
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private void OpenPackage() {
    	System.out.println("open package");
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
		
		if(node.getChildCount() != 0) {
			System.out.println("not node!");
			return ;
		}
		
		PackageListObject tempObject = ((PackageListObject)node.getUserObject()); 
		
		System.out.println(tempObject.pacakge);
		System.out.println(tempObject.label);
		System.out.println(tempObject.codePath);
		
		DefaultMutableTreeNode deviceNode = null;
		
		for(deviceNode = node ; deviceNode.getUserObject() instanceof DeviceString==false; deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) {
			
		}
		
		System.out.println(deviceNode.getUserObject());
		
		selDevice = ((DeviceString)deviceNode.getUserObject()).Devicename;
		selPackage = tempObject.pacakge;
		
		dialog.dispose();
    }

    private void PullPackage() {
    	System.out.println("PullPackage()");
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
		
		if(node.getChildCount() != 0) {
			System.out.println("not node!");
			return ;
		}

		PackageListObject tempObject = ((PackageListObject)node.getUserObject()); 
		
		System.out.println(tempObject.pacakge);
		System.out.println(tempObject.label);
		System.out.println(tempObject.codePath);
		
		DefaultMutableTreeNode deviceNode = null;
		for(deviceNode = node ; deviceNode.getUserObject() instanceof DeviceString==false; deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) { }
		
		System.out.println(deviceNode.getUserObject());
		
		String device = ((DeviceString)deviceNode.getUserObject()).Devicename;
		String packageName = tempObject.pacakge;
		

		String apkPath = AdbWrapper.getPackageInfo(device, packageName).apkPath;
		if(apkPath == null) return;
		
		String saveFileName;
		if(apkPath.endsWith("base.apk")) {
			saveFileName = apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
		} else {
			saveFileName = apkPath.replaceAll(".*/", "");
		}

		JFileChooser jfc = new JFileChooser((String)Resource.PROP_LAST_FILE_SAVE_PATH.getData(""));
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("vnd.android.package-archive(.apk)","apk"));
		jfc.setSelectedFile(new File(saveFileName));
						
		jfc.showSaveDialog(null);
		File dir = jfc.getSelectedFile();
		
		if(dir == null) return;
		Resource.PROP_LAST_FILE_SAVE_PATH.setData(dir.getParentFile().getAbsolutePath());
		
		
		AdbWrapper.PullApk(device, apkPath, dir.getAbsolutePath(), null);
		//dir.isDirectory()
		
		//return dir.getPath();
		
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals("Open Package")) {
			OpenPackage();

		} else if(e.getActionCommand().equals("Refresh")) {
			System.out.println("refresh");
			
			addTreeList();
			
		} else if(e.getActionCommand().equals("Exit")) {
			System.out.println("exit");
			selDevice = null;
			selPackage = null;
			dialog.dispose();
		}
	}
}