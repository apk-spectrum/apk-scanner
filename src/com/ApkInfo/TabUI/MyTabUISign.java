package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JList;


/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class MyTabUISign extends JPanel{
	private static final long serialVersionUID = 4333997417315260023L;
	final JList<String> jlist;
	JTextArea textArea;
	
	ArrayList<Object[]> mCertList = null;
	
    public MyTabUISign() {
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        jlist = new JList<String>();

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
        	  if(mCertList == null) return;
              textArea.setText((String)mCertList.get(jlist.getSelectedIndex())[1]);
              textArea.setCaretPosition(0);
              //textArea.requestFocus();
          }
        };
        jlist.addListSelectionListener(listSelectionListener);

        MouseListener mouseListener = new MouseAdapter() {
          @SuppressWarnings("unchecked")
		public void mouseClicked(MouseEvent mouseEvent) {
			JList<String> theList = (JList<String>) mouseEvent.getSource();
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
    
    public void setData(ArrayList<Object[]> data) {
    	mCertList = null;
    	jlist.removeAll();
        String[] labels = new String[data.size()];        
        for(int i=0; i< labels.length; i++) {
        	labels[i] = (String)data.get(i)[0];        	
        }
        jlist.setListData(labels);
    	mCertList = data;
        jlist.setSelectedIndex(0);
    }
}

