package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public class MyTabUIActivity extends JPanel {
	JTextArea textArea;
	
    public MyTabUIActivity() {
        String labels[] = { "Asadasd", "B22222", "C3333", "D44444", "E123123", "F234324", "G234234", "H234", "234I", "23423J" };
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        

        JList jlist = new JList(labels);
        
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
        JScrollPane scrollPane2 = new JScrollPane(textArea);
        scrollPane2.setPreferredSize(new Dimension(50, 400));
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        
        this.add(scrollPane2, c);
        
        //this.setLayout(new GridLayout(1,2));

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent listSelectionEvent) {
            System.out.print("First index: " + listSelectionEvent.getFirstIndex());
            System.out.print(", Last index: " + listSelectionEvent.getLastIndex());
            boolean adjust = listSelectionEvent.getValueIsAdjusting();
            System.out.println(", Adjusting? " + adjust);
            if (!adjust) {
              JList list = (JList) listSelectionEvent.getSource();
              int selections[] = list.getSelectedIndices();
              Object selectionValues[] = list.getSelectedValues();
              for (int i = 0, n = selections.length; i < n; i++) {
                if (i == 0) {
                  System.out.print("  Selections: ");
                }
                System.out.print(selections[i] + "/" + selectionValues[i] + " ");
                textArea.append(selectionValues[i].toString()+"\n");
                
              }
              System.out.println();
            }
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