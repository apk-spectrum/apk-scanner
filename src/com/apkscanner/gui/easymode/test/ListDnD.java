package com.apkscanner.gui.easymode.test;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ListDnD {
    ReportingListTransferHandler arrayListHandler =
                         new ReportingListTransferHandler();
 
    private JPanel getContent() {
        JPanel panel = new JPanel(new GridLayout(1,0));
        panel.add(getListComponent("left"));
        panel.add(getListComponent2("right"));
        return panel;
    }

	private JScrollPane getListComponent(String s) {
        DefaultListModel model = new DefaultListModel();
        for(int j = 0; j < 3; j++)
            model.addElement(s + " " + (j+1));
        JList list = new JList(model);
        list.setName(s);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setTransferHandler(arrayListHandler);        
        list.setDragEnabled(true);
        return new JScrollPane(list);
    }
     
    private JScrollPane getListComponent2(String s) {
        DefaultListModel model = new DefaultListModel();
        for(int j = 0; j < 7; j++)
            model.addElement(s + " " + (j+1));
        JList list = new JList(model);
        list.setName(s);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setTransferHandler(arrayListHandler);        
        list.setDragEnabled(true);
        return new JScrollPane(list);
    }
 
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new ListDnD().getContent());
        f.setSize(400,200);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}