package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
public class MyTabUISign extends JPanel {
  private boolean DEBUG = false;

  protected String[] columnToolTips = { null, null,
      "The person's favorite sport to participate in",
      "The number of years the person has played the sport",
      "If checked, the person eats no meat" };
  

}