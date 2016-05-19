package com.apkscanner.gui.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

public class DynamicTree {

    private JScrollPane scrollPane;
    private JTree tree;

    private static final int SCROLLPANE_WIDTH = 200;
    private static final int SCROLLPANE_HEIGHT = 500;
    
    //DATA
    private int currentNodeCount = 0;
    private DefaultMutableTreeNode rootNode;
    
    public JScrollPane MakeScrollPane(JTree temptree)
    {
    		  tree = temptree;
    		  
              tree.addTreeWillExpandListener(new TreeWillExpandListener() 
              {
                     public void treeWillExpand(TreeExpansionEvent evt) throws ExpandVetoException {} 
                     public void treeWillCollapse(TreeExpansionEvent evt) throws ExpandVetoException 
                     {
                          // Don't allow Root to collapse
                          if(evt.getPath().getParentPath() == null)
                          {
                               throw new ExpandVetoException(evt);
                          } 
                     }
              }); 
         
              //Set your JTree to be as big as its GOING to be so the scrollbar looks correct
              //There is (NUM_OF_NODES + 1) because we can't forget about the root node
              Rectangle singleNodeRect = tree.getRowBounds(0);
              tree.setPreferredSize(new Dimension(tree.getPreferredSize().width, singleNodeRect.height*(2000+1)));
    		  
              scrollPane = new JScrollPane(tree);
              scrollPane.setPreferredSize(new Dimension(SCROLLPANE_WIDTH, SCROLLPANE_HEIGHT));
              scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
              scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
              {
                     public void adjustmentValueChanged(AdjustmentEvent e)
                     {
                          Rectangle singleNodeRect = tree.getRowBounds(0);
                          Rectangle visibleRect = tree.getVisibleRect();
                          
                          //If we are SUPPOSED TO SHOW more nodes than we currently have... add them to the tree
                          int supposedNodeCount = (int)((visibleRect.getY()+visibleRect.getHeight()) / singleNodeRect.getHeight()) + 1;
                          if(supposedNodeCount > currentNodeCount)
                          {
                        		FilteredTreeModel filteredModel = (FilteredTreeModel) tree.getModel(); 
                        	  
                        		DefaultTreeModel tm= (DefaultTreeModel) filteredModel.getTreeModel();
                        		
                        	   //DefaultTreeModel tm = (DefaultTreeModel)getTree().getModel();
                               
                               
                               
                               for(int i=currentNodeCount+1; i<=supposedNodeCount; i++)
                               {
                                    tm.insertNodeInto(new DefaultMutableTreeNode("Node " + i), getRootNode(), getRootNode().getChildCount());
                               }
                               
                               currentNodeCount = supposedNodeCount;
                          }
                     }
              });         
         return scrollPane;
    }
	
    private JTree getTree()
    {
         if(tree == null)
         {
              tree = new JTree(getRootNode());

         }
         return tree;
    }
    
    private DefaultMutableTreeNode getRootNode()
    {
         if(rootNode == null)
         {
              rootNode = new DefaultMutableTreeNode("root");
         }
         return rootNode;
    }
}
