package com.ApkInfo.TabUI;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
public class MyTabUILib extends JPanel {
  public MyTabUILib() {
    super(new GridLayout(1, 0));

    ArrayList<String> LibList = new ArrayList<String>();
        
    
    
    JTable table = new JTable(new MyTableModel());

    table.setPreferredScrollableViewportSize(new Dimension(500, 70));

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);

    //Add the scroll pane to this panel.
    add(scrollPane);
  }

  class MyTableModel extends AbstractTableModel {
    private String[] columnNames = { "First Name", "Last Name", "Sport",
        "# of Years", "Vegetarian" };

    private Object[][] data = {
        { "Mary", "Campione", "Snowboarding", new Integer(5),
            new Boolean(false) },
        { "Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
        { "Kathy", "Walrath", "Knitting", new Integer(2),
            new Boolean(false) },
        { "Sharon", "Zakhour", "Speed reading", new Integer(20),
            new Boolean(true) },
        { "Philip", "Milne", "Pool", new Integer(10),
            new Boolean(false) } };

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.length;
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
        return false;
      
    }

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    public void setValueAt(Object value, int row, int col) {

      data[row][col] = value;
      fireTableCellUpdated(row, col);
    }

    private void printDebugData() {
      int numRows = getRowCount();
      int numCols = getColumnCount();

      for (int i = 0; i < numRows; i++) {
        System.out.print("    row " + i + ":");
        for (int j = 0; j < numCols; j++) {
          System.out.print("  " + data[i][j]);
        }
        System.out.println();
      }
      System.out.println("--------------------------");
    }
  }
}
