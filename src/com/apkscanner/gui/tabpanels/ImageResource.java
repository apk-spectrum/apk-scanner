package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.FilteredTreeModel;
import com.apkscanner.gui.util.ImageControlPanel;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class ImageResource extends JPanel implements TabDataObject, ActionListener
{
	private static final long serialVersionUID = -934921813626224616L;
    
	private ImageControlPanel photographLabel;
	private String[] nameList = null;
	private String apkFilePath = null;
	private JTree tree;
	private DefaultMutableTreeNode top;
	private ArrayList<String> FolderList = new ArrayList<String>(); 
	private ArrayList<String> FileList = new ArrayList<String>();
	private Boolean isFolderMode = false;
	private FilteredTreeModel filteredModel;
	private JTextField textField;
	private Boolean firstClick=false;
	
	private class ImageTreeObject {
		public String label;
		public Boolean isfolder;
		public String Filepath;
		
		public ImageTreeObject(String filepath, Boolean folder) {
			Filepath = filepath;
			isfolder = folder;
			
			if(isfolder) {
				label = getOnlyFoldername(filepath);
			} else {
				label = getOnlyFilename(filepath); 
			}
		}
		
		@Override
		public String toString() {
		    return this.label;
		}
	}
	
	public ImageResource()
	{

	}
	
    private void makeTreeForm() {
    	top = new DefaultMutableTreeNode("Loading...");
    
    	FilteredTreeModel model = new FilteredTreeModel(new DefaultTreeModel(top));
    	
    	tree = new JTree(model);
    	
    	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);    	
    }
    
	private String getOnlyFilename(String str) {
		return str.substring(str.lastIndexOf(File.separator)+1, str.length());
	}
    
	private String getOnlyFoldername(String str) {
		return str.substring(0, str.lastIndexOf(File.separator));
	}
    
    private final List<DefaultMutableTreeNode> getSearchNodes(DefaultMutableTreeNode root) {
        List<DefaultMutableTreeNode> searchNodes = new ArrayList<DefaultMutableTreeNode>();

        Enumeration<?> e = root.preorderEnumeration();
        while(e.hasMoreElements()) {
            searchNodes.add((DefaultMutableTreeNode)e.nextElement());
        }
        return searchNodes;
    }
	
    public final DefaultMutableTreeNode findNode(String searchString) {

        List<DefaultMutableTreeNode> searchNodes = getSearchNodes((DefaultMutableTreeNode)tree.getModel().getRoot());
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        DefaultMutableTreeNode foundNode = null;
        int bookmark = -1;

        if( currentNode != null ) {
            for(int index = 0; index < searchNodes.size(); index++) {
                if( searchNodes.get(index) == currentNode ) {
                    bookmark = index;
                    break;
                }
            }
        }

        for(int index = bookmark + 1; index < searchNodes.size(); index++) {    
            if(searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
                foundNode = searchNodes.get(index);
                break;
            }
        }

        if( foundNode == null ) {
            for(int index = 0; index <= bookmark; index++) {    
                if(searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
                    foundNode = searchNodes.get(index);
                    break;
                }
            }
        }
        return foundNode;
    }   
	
    private void SetTreeForm(Boolean mode) {
    	tree.removeAll();
    	FolderList.clear();
    	FileList.clear();
    	top = new DefaultMutableTreeNode(getOnlyFilename(this.apkFilePath));
    	FilteredTreeModel model = new FilteredTreeModel(new DefaultTreeModel(top));
    	tree.setModel(model);
    	if(isFolderMode) {
	    	for(int i=0; i<this.nameList.length; i++) {
	    		ImageTreeObject ImageNode= new ImageTreeObject(this.nameList[i], false);
	    		DefaultMutableTreeNode imagepath = new DefaultMutableTreeNode(ImageNode);
	    		
	    		String foldertemp =	getOnlyFoldername(this.nameList[i]);
	    		
	    		if(FolderList.contains(foldertemp)) {
	    			DefaultMutableTreeNode findnode = findNode(foldertemp);
	    			findnode.add(imagepath);
	    			
	    		} else {
	    			ImageTreeObject folderNode= new ImageTreeObject(this.nameList[i], true);
	    			FolderList.add(foldertemp);
	    			DefaultMutableTreeNode foldernode = new DefaultMutableTreeNode(folderNode);
	    			
	    			foldernode.add(imagepath);
	    			top.add(foldernode);
	    		}
	    	}
    	} else {
	    	for(int i=0; i<this.nameList.length; i++) {
	    		ImageTreeObject ImageNode= new ImageTreeObject(this.nameList[i], false);
	    		DefaultMutableTreeNode imagepath = new DefaultMutableTreeNode(ImageNode);
	    		
	    		String filetemp =	getOnlyFilename(this.nameList[i]);
	    		
	    		if(FileList.contains(filetemp)) {
	    			DefaultMutableTreeNode findnode = findNode(filetemp);
	    			
	    			ImageTreeObject FolderObject= new ImageTreeObject(this.nameList[i], true);
	    			DefaultMutableTreeNode FolderNode = new DefaultMutableTreeNode(FolderObject);
	    			
	    			findnode.add(FolderNode);
	    		} else {
	    			ImageTreeObject FolderObject= new ImageTreeObject(this.nameList[i], true);
	    			DefaultMutableTreeNode FolderNode = new DefaultMutableTreeNode(FolderObject);
	    			
	    			imagepath.add(FolderNode);	    			
	    			FileList.add(filetemp);
	    			
	    			top.add(imagepath);
	    		}
	    	}
    	}
    	expandOrCollapsePath(tree, new TreePath(top.getPath()),1,0, true);
    }
    
    public static void expandOrCollapsePath (JTree tree,TreePath treePath,int level,int currentLevel,boolean expand) {
//      System.err.println("Exp level "+currentLevel+", exp="+expand);
      if (expand && level<=currentLevel && level>0) return;

      TreeNode treeNode = ( TreeNode ) treePath.getLastPathComponent();
      TreeModel treeModel=tree.getModel();
      if ( treeModel.getChildCount(treeNode) >= 0 ) {
         for ( int i = 0; i < treeModel.getChildCount(treeNode); i++  ) {
            TreeNode n = ( TreeNode )treeModel.getChild(treeNode, i);
            TreePath path = treePath.pathByAddingChild( n );
            expandOrCollapsePath(tree,path,level,currentLevel+1,expand);
         }
         if (!expand && currentLevel<level) return;
      }      
      if (expand) {
         tree.expandPath( treePath );
//         System.err.println("Path expanded at level "+currentLevel+"-"+treePath);
      } else {
         tree.collapsePath(treePath);
//         System.err.println("Path collapsed at level "+currentLevel+"-"+treePath);
      }
   }
    private void drawImageOnPanel() {
    	//int selRow = tree.getRowForLocation(e.getX(), e.getY());
        //TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
        if(node ==null) return;
		if(node ==top) return;
			
		String imgPath = "jar:file:"+apkFilePath.replaceAll("#", "%23")+"!/";
		ImageTreeObject tempObject = (ImageTreeObject)node.getUserObject();
		imgPath = imgPath + tempObject.Filepath;
		
		if(imgPath.endsWith(".qmg")) {
			imgPath = Resource.IMG_QMG_IMAGE_ICON.getPath();
		}
		try {
			//photographLabel.setIcon(new ImageIcon(
			//		new URL(imgPath)));

			photographLabel.setImage(new ImageIcon( new URL(imgPath)));
			photographLabel.repaint();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} 
    }
    private void TreeInit() {
    	
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 6248791058116909814L;
			private ImageIcon iconApk = Resource.IMG_TREE_APK.getImageIcon();
        	private ImageIcon iconFolder = Resource.IMG_TREE_FOLDER.getImageIcon();
        	
            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean selected, boolean expanded,
                    boolean isLeaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value,
                        selected, expanded, isLeaf, row, focused);
                
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                int level = nodo.getLevel();
                if(top==nodo) {
                	setIcon(iconApk);
                	return c;
                }
                
                ImageTreeObject tempObject = (ImageTreeObject)nodo.getUserObject();                
                
                if(tempObject.isfolder) {
                	setIcon(iconFolder);
                } else {
                	ImageTreeObject temp = (ImageTreeObject)nodo.getUserObject();
                	String jarPath = "jar:file:"+apkFilePath.replaceAll("#", "%23")+"!/";
                	ImageIcon tempIcon = null;
    				if(temp.Filepath.endsWith(".qmg")) {
    					tempIcon = new ImageIcon(ImageScaler.getScaledImage(Resource.IMG_QMG_IMAGE_ICON.getImageIcon(),32,32));
    				} else {
    					try {
							tempIcon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(new URL(jarPath+temp.Filepath)),32,32));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}                	
                	setIcon(tempIcon);
                }             
                return c;
            }
        });
    	
    	
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {            	
            	drawImageOnPanel();
            }
        };
        tree.addKeyListener(new KeyAdapter()
        {
        	public void keyReleased(KeyEvent ke) {
        		//if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
        			drawImageOnPanel();
        		//}
        	}
        });        
        
        tree.addMouseListener(ml);        
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
    
    private void forselectionTree () {
        DefaultMutableTreeNode currentNode = top.getNextNode();
        do {
        		if(currentNode.getLevel()==2 && filteredModel.getChildCount(currentNode) > 0) {
    		        	
    		        	TreePath temptreePath = new TreePath(((DefaultMutableTreeNode)(filteredModel.getChild(currentNode, 0))).getPath());
    		        	
		        		tree.setSelectionPath(temptreePath);
		        		tree.scrollPathToVisible(temptreePath);
		        		return;
		        }
	           currentNode = currentNode.getNextNode();
           }
        while (currentNode != null);
    }
    
    
	@Override
	public void initialize()
	{
		makeTreeForm();
		TreeInit();
		
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(300, 400));
		scroll.repaint();
		
		photographLabel = new ImageControlPanel();
		//photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		//photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		//photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		        
		this.setLayout(new GridLayout(1, 1));
		
		textField = new JTextField("click...for find image");
		
		
		textField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke) {
            	
            	if(textField.getText().length() ==0) {
            		expandOrCollapsePath(tree, new TreePath(top.getPath()),2,0, true);
            		return;
            	}
            	
                if(!(ke.getKeyChar()==27||ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
                {
                	makefilter (textField.getText());
                }
            }
        });
		textField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
            	if(!firstClick) {
            		firstClick = true;
            		textField.setText("");
            	}
			}
		});
		
		
		
		JPanel TreePanel = new JPanel(new BorderLayout());
		JPanel TreeModePanel = new JPanel();
		
	    JRadioButton ForderModeRadioButton  = new JRadioButton("folder");
	    ForderModeRadioButton.addActionListener(this);
	    
	    JRadioButton ImageModeRadioButton  = new JRadioButton("image");
	    ImageModeRadioButton.addActionListener(this);
	    ImageModeRadioButton.setSelected(true);
	    
	    JLabel TreeModeLabel = new JLabel("Tree Mode : ");
	    	    
	    TreeModePanel.add(TreeModeLabel);
	    TreeModePanel.add(ImageModeRadioButton);
	    TreeModePanel.add(ForderModeRadioButton);
	    
        ButtonGroup group = new ButtonGroup();
        group.add(ImageModeRadioButton);
        group.add(ForderModeRadioButton);        
	    
		TreePanel.add(textField, BorderLayout.SOUTH);
		TreePanel.add(scroll, BorderLayout.CENTER);
		TreePanel.add(TreeModePanel, BorderLayout.NORTH);
		
		
//		this.add(scroll);
//		this.add(photographLabel);	

		
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(TreePanel);
        splitPane.setRightComponent(photographLabel);
        
        
        Dimension minimumSize = new Dimension(100, 50);
        
        //splitPane.setDividerLocation(200);
        
        this.add(splitPane);
        
	}

	@Override
	public void setData(ApkInfo apkInfo)
	{
		if(tree == null)
			initialize();
				
		this.apkFilePath = apkInfo.filePath; 
		
		if(apkInfo.images == null) return;
		
		nameList = apkInfo.images;

		SetTreeForm(isFolderMode);
		
	}
    
	@Override
	public void reloadResource() {
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		if(arg0.getActionCommand().equals("image")) {
			isFolderMode = false;
			SetTreeForm(isFolderMode);
		} else {
			isFolderMode = true;
			SetTreeForm(isFolderMode);
		}		
	}
}