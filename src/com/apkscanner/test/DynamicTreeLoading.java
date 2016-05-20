package com.apkscanner.test;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

import com.apkscanner.util.Log;

public class DynamicTreeLoading extends JFrame
{
	private static final long serialVersionUID = 4549528078290137966L;
	//STATICS
     private static final int NUM_OF_NODES = 20;
     private static final int SCROLLPANE_WIDTH = 200;
     private static final int SCROLLPANE_HEIGHT = 500;
     
     //DATA
     private int currentNodeCount = 0;
     private DefaultMutableTreeNode rootNode;
     
     //GUI
     private JScrollPane scrollPane;
     private JTree tree;
     
     public DynamicTreeLoading()
     {
          this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          this.setContentPane(getScrollPane());
          this.pack();
          
          initTree();
          
          this.setVisible(true);
          Runtime r = Runtime.getRuntime();
  		DecimalFormat format = new DecimalFormat("###,###,###.##");
  		Log.d("Total Memony " + format.format(r.totalMemory()));
     }
     
     private void initTree()
     {
          //Find out how many nodes we should start with (+ 1 so we know we are off the page)
          Rectangle singleNodeRect = getTree().getRowBounds(0);
          int viewPortHeight = getScrollPane().getViewport().getHeight();
          int numOfStartNodes = (viewPortHeight / singleNodeRect.height) + 1;
          
          //Add the nodes
          DefaultTreeModel tm = (DefaultTreeModel)getTree().getModel();
          for(int i=0; i<numOfStartNodes; i++)
          {
               tm.insertNodeInto(new DefaultMutableTreeNode("Node " + i), getRootNode(), getRootNode().getChildCount());
          }
          
          //Make sure our count is correct so we know how many we currently have added
          currentNodeCount = numOfStartNodes;
          
          //Make sure the tree starts expanded
          getTree().expandRow(0);
     }
     
     private JScrollPane getScrollPane()
     {
          if(scrollPane == null)
          {
               scrollPane = new JScrollPane(getTree());
               scrollPane.setPreferredSize(new Dimension(SCROLLPANE_WIDTH, SCROLLPANE_HEIGHT));
               scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
               scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
               {
                      public void adjustmentValueChanged(AdjustmentEvent e)
                      {
                           Rectangle singleNodeRect = getTree().getRowBounds(0);
                           Rectangle visibleRect = getTree().getVisibleRect();
                           
                           //If we are SUPPOSED TO SHOW more nodes than we currently have... add them to the tree
                           int supposedNodeCount = (int)((visibleRect.getY()+visibleRect.getHeight()) / singleNodeRect.getHeight()) + 1;
                           if(supposedNodeCount > currentNodeCount)
                           {
                                DefaultTreeModel tm = (DefaultTreeModel)getTree().getModel();
                                for(int i=currentNodeCount+1; i<=supposedNodeCount; i++)
                                {
                                     tm.insertNodeInto(new DefaultMutableTreeNode("Node " + i), getRootNode(), getRootNode().getChildCount());
                                }
                                
                                currentNodeCount = supposedNodeCount;
                           }
                      }
               });
          }
          return scrollPane;
     }
     
     private JTree getTree()
     {
          if(tree == null)
          {
               tree = new JTree(getRootNode());
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
               tree.setPreferredSize(new Dimension(tree.getPreferredSize().width, singleNodeRect.height*(NUM_OF_NODES+1)));
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
     
     public static void main(String args[])
     {
          new DynamicTreeLoading();
     }
}