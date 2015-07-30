package com.ApkInfo.UI;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

import java.net.URL;
import java.util.ArrayList;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
 
public class PackageTreeDlg extends JPanel
                      implements TreeSelectionListener, ActionListener{
    private JTextField textFieldapkPath;
    private JTree tree;
    private DefaultMutableTreeNode top;
    private JPanel gifPanel;
    static JFrame frame;
    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    
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
							
							createDeviceNodes(top,DeviceList.get(i).name +"("+DeviceList.get(i).model + ")",ArrayDataObject);
							
						}
						gifPanel.setVisible(false);
					}
				}
		    private void createDeviceNodes(DefaultMutableTreeNode top, String DeviceName, ArrayList<PackageListObject> ArrayDataObject) {
		        DefaultMutableTreeNode deviceName = new DefaultMutableTreeNode(DeviceName);;		        
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
		        
		        System.out.println("loading package List on device : " + DeviceName);
		        
		        for(int i=0; i< ArrayDataObject.size(); i++) {
		        	//System.out.println(ArrayDataObject.get(i).codePath + " : " + ArrayDataObject.get(i).label);
		        	
		        	DefaultMutableTreeNode temp = new DefaultMutableTreeNode(ArrayDataObject.get(i).label);
		        	
		        	if(ArrayDataObject.get(i).codePath.indexOf("/system/priv-app/") >-1) {
		        		priv_app.add(temp);		        		
		        	} else if(ArrayDataObject.get(i).codePath.indexOf("/system/app/") >-1) {
		        		systemapp.add(temp);
		        	} else if(ArrayDataObject.get(i).codePath.indexOf("/data/app/") >-1) {
		        		dataapp.add(temp);
		        	}
		        }
		        tree.updateUI();
		        
		        System.out.println("end  loading package : " + DeviceName);
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
        tree = new JTree(model);
        
        
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
        
        final JTextField textFilField = new JTextField(50);
        
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
        Object nodeInfo = node.getUserObject();
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
        frame = new JFrame("PackageTree");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new PackageTreeDlg());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public void showTreeDlg() {
        //Create and set up the window.
        frame = new JFrame("PackageTree");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //Add content to the window.
        frame.add(this);
 
        
        frame.setResizable( false );
        frame.setLocationRelativeTo(null);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub		
		
		if(e.getActionCommand().equals("Open Package")) {
			System.out.println("open package");
		} else if(e.getActionCommand().equals("Refresh")) {
			System.out.println("refresh");
			
			addTreeList();
			
		} else if(e.getActionCommand().equals("Exit")) {
			System.out.println("exit");
			frame.dispose();
		}
	}
}