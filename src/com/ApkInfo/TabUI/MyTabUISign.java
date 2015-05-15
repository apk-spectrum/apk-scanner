package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.JList;
import javax.swing.JTextPane;






/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ApkInfo.UI.MainUI;

public class MyTabUISign extends JPanel{
	JTextArea textArea;
	
	ArrayList<Object[]> mCertList;
	
    public MyTabUISign() {
    	
    	mCertList = MainUI.GetMyApkInfo().CertList;
    	
        String[] labels = new String[mCertList.size()];
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        for(int i=0; i< labels.length; i++) {
        	labels[i] = (String)mCertList.get(i)[0];        	
        }
        
        final JList jlist = new JList(labels);
        
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        JScrollPane scrollPane1 = new JScrollPane(jlist);
        scrollPane1.setPreferredSize(new Dimension(50, 400));
        
        c.weightx = 0.1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        
        this.add(scrollPane1, c);        
        textArea = new JTextArea();
        textArea.setEditable(false);
        final JScrollPane scrollPane2 = new JScrollPane(textArea);
        scrollPane2.setPreferredSize(new Dimension(50, 400));
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        
        this.add(scrollPane2, c);
        
        //this.setLayout(new GridLayout(1,2));

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent listSelectionEvent) {
        	    
              textArea.setText((String)mCertList.get(jlist.getSelectedIndex())[1]);                
          }
        };
        jlist.addListSelectionListener(listSelectionListener);

        MouseListener mouseListener = new MouseAdapter() {
          public void mouseClicked(MouseEvent mouseEvent) {
            JList theList = (JList) mouseEvent.getSource();
            if (mouseEvent.getClickCount() == 2) {
              int index = theList.locationToIndex(mouseEvent.getPoint());
              if (index >= 0) {
                Object o = theList.getModel().getElementAt(index);
                System.out.println("Double-clicked on: " + o.toString());
              }
            }
          }
        };
        jlist.addMouseListener(mouseListener);
    }
}

