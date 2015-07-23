package com.ApkInfo.UI;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

import com.ApkInfo.UIUtil.FilteredTreeModel;

import java.net.URL;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
 
public class TreeDemo extends JPanel
                      implements TreeSelectionListener {
    private JEditorPane htmlPane;
    private JTree tree;
    
    private static boolean DEBUG = false;
 
    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
     
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;
 
    public TreeDemo() {
        super(new BorderLayout());
 
        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("The Java Series");
        createNodes(top);
 
        //Create a tree that allows one selection at a time.
                        
        FilteredTreeModel model = new FilteredTreeModel(new DefaultTreeModel(top));
        tree = new JTree(model);
        
        
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
 
        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);
 
        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }
 
        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);
 
        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JButton searchButton = new JButton("search");
        final JTextField textFilField = new JTextField(30);
        
        textFilField.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent ke)
            {
                if(!(ke.getKeyChar()==27||ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
                {
                	System.out.println(textFilField.getText()+ "    : " + Integer.valueOf(ke.getKeyChar()));
                	
                    FilteredTreeModel filteredModel = (FilteredTreeModel) tree.getModel();
                    
                    if(Integer.valueOf(ke.getKeyChar())==8) {
                    	filteredModel.setFilter(textFilField.getText().substring(0, textFilField.getText().length()-1));
                    } else if(Integer.valueOf(ke.getKeyChar())==10) {
                    	filteredModel.setFilter(textFilField.getText());
                    } else {
                    	filteredModel.setFilter(textFilField.getText() + ke.getKeyChar());
                    }
                    
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
        
        JScrollPane htmlView = new JScrollPane(htmlPane);
 
        JPanel tpanel = new JPanel();
        tpanel.add(new JLabel("search : "));
        tpanel.add(textFilField);
        tpanel.add(searchButton);
        
        panel.add(tpanel, BorderLayout.CENTER);
        panel.add(treeView,BorderLayout.NORTH);
        
        JButton openbtn = new JButton("Open Package");
        JButton exitbtn = new JButton("Exit");
        JPanel ButtonPanel = new JPanel();
        ButtonPanel.add(openbtn);
        ButtonPanel.add(exitbtn);
        panel.add(ButtonPanel, BorderLayout.SOUTH);
        
        
        JPanel NorthPanel = new JPanel(new BorderLayout());
                
        
        NorthPanel.add(htmlView, BorderLayout.CENTER);
        
        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(panel);
        splitPane.setBottomComponent(NorthPanel);
                
        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(400);
        splitPane.setPreferredSize(new Dimension(500, 500));
 
        //Add the split pane to this panel.
        //add(splitPane);
        add(NorthPanel,BorderLayout.NORTH);
        add(panel, BorderLayout.SOUTH);
        
    }
 
    protected void findTree(String KeyWord) {
		// TODO Auto-generated method stub
		
    	
    	
    	
	}

	/** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();
        
        
        if (node == null) return;
 
        Object nodeInfo = node.getUserObject();
        
        TreeNode [] treenode = node.getPath();
        
        TreePath path = new TreePath(treenode);

        htmlPane.setText(path.toString());
    }
 
    private class BookInfo {
        public String bookName;
        public URL bookURL;
 
        public BookInfo(String book, String filename) {
            bookName = book;
            bookURL = getClass().getResource(filename);
            if (bookURL == null) {
                System.err.println("Couldn't find file: "
                                   + filename);
            }
        }
 
        public String toString() {
            return bookName;
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;
 
        category = new DefaultMutableTreeNode("system");        
        top.add(category);
        
        category = new DefaultMutableTreeNode("data");        
        top.add(category);
        
        for(int i=0; i< 3000; i++) {
        	
        	
        	DefaultMutableTreeNode temp = new DefaultMutableTreeNode("" + (Math.random()));
        	category.add(temp);
        }
        
        
        
// 
//        //original Tutorial
//        book = new DefaultMutableTreeNode(new BookInfo
//            ("The Java Tutorial: A Short Course on the Basics",
//            "tutorial.html"));
//        category.add(book);
 

    }
         
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }
 
        //Create and set up the window.
        JFrame frame = new JFrame("TreeDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new TreeDemo());
 
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
}