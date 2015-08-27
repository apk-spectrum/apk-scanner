package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JList;




import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ApkInfo.Resource.Resource;


public class MyTabUISign extends JPanel implements ComponentListener{
	private static final long serialVersionUID = 4333997417315260023L;
	final JList<String> jlist;
	JTextArea textArea;
	
	String mCertSummary = null;
	ArrayList<String> mCertList = null;
	
    public MyTabUISign() {
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        jlist = new JList<String>();

        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        JScrollPane scrollPane1 = new JScrollPane(jlist);
        scrollPane1.setPreferredSize(new Dimension(50, 400));
        
        c.weightx = 0.1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        
        this.add(scrollPane1, c);        
        textArea = new JTextArea();
        textArea.setEditable(false);
        final JScrollPane scrollPane2 = new JScrollPane(textArea);
        scrollPane2.setPreferredSize(new Dimension(50, 400));
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        
        this.add(scrollPane2, c);
        
        //this.setLayout(new GridLayout(1,2));

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent listSelectionEvent) {
        	  if(mCertList == null) return;
        	  if(jlist.getSelectedIndex() > -1) {
        		  if(mCertList.size() > 1) {
        			  if(jlist.getSelectedIndex() >= 1)
        				  textArea.setText(mCertList.get(jlist.getSelectedIndex()-1));
        			  else 
        				  textArea.setText(mCertSummary);
        		  } else {
        			  textArea.setText(mCertList.get(jlist.getSelectedIndex()));
        		  }
	              textArea.setCaretPosition(0);
        	  }
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
                //Object o = theList.getModel().getElementAt(index);
                //Log.i("Double-clicked on: " + o.toString());
              }
            }
          }
        };
        jlist.addMouseListener(mouseListener);
    }
    
    public void setData(String summury, ArrayList<String> data) {
    	mCertSummary = summury;
    	mCertList = data;
    	reloadResource();
        jlist.setSelectedIndex(0);
    }
    
    public void reloadResource() {
    	jlist.removeAll();
    	if(mCertList == null) return;
    	int listSize = mCertList.size();
    	String[] labels;
    	if(listSize > 1) {
    		listSize++;
    		labels = new String[listSize];
    		labels[0] = Resource.STR_CERT_SUMMURY.getString();
	        for(int i=1; i < listSize; i++) {
	        	labels[i] = Resource.STR_CERT_CERTIFICATE.getString() + "[" + i + "]";
	        }
    	} else if (listSize == 1) {
    		labels = new String[listSize];
    		labels[0] = Resource.STR_CERT_CERTIFICATE.getString() + "[1]";
    	} else {
    		labels = new String[] {};
    	}
        jlist.setListData(labels);
    }

	@Override
	public void componentResized(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		
	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}
}

