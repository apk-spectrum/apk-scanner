package com.ApkInfo.TabUI;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.ApkInfo.Core.CoreApkTool;

/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
public class MyTabUILib extends JPanel {
	
  public MyTabUILib() {
    super(new GridLayout(1, 0));
    
    JTable table = new JTable(new MyTableModel());

    //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    
    setJTableColumnsWidth(table, 500, 4,65,31);
    
    //Create the scroll pane and add the table to it.
    
    JScrollPane scrollPane = new JScrollPane(table);

    //Add the scroll pane to this panel.
    add(scrollPane);
  }

  public static void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
	        double... percentages) {
	    double total = 0;
	    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
	        total += percentages[i];
	    }
	 
	    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
	        TableColumn column = table.getColumnModel().getColumn(i);
	        column.setPreferredWidth((int)
	                (tablePreferredWidth * (percentages[i] / total)));
	    }
	}
  
  class MyTableModel extends AbstractTableModel {
	  
	  private String[] columnNames = { "Index", "Path", "Size"};

	    private ArrayList<Object[]> data;
		 
	    ArrayList<String> LibList;  
		
	    MyTableModel() {
		LibList = CoreApkTool.findfileforLib(new File(CoreApkTool.DefaultPath+File.separator+"lib"));
		  
		data = new ArrayList<Object[]>();
		
		DecimalFormat df = new DecimalFormat("#,##0");
		for(int i=0; i< LibList.size(); i++) {
			long size = (new File(LibList.get(i))).length();
			Object[] temp = { 
				i+1, 
				LibList.get(i).replaceAll("^.*"+File.separator+File.separator+"lib"+File.separator+File.separator,"lib"+File.separator+File.separator), 
				CoreApkTool.getFileLength(size) + " ("+ df.format(size) + " Byte)"
			};
			data.add(temp);
		}		
		System.out.println("Lib Count : " + data.size());  
	  }
	
    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.size();
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {    	
      return data.get(row)[col];
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
    	if(col>0) {
    		return true;
    	} else return false;
    }

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    public void setValueAt(Object value, int row, int col) {

    	data.get(row)[col] = value;
      fireTableCellUpdated(row, col);
    }

    private void printDebugData() {
      int numRows = getRowCount();
      int numCols = getColumnCount();

      for (int i = 0; i < numRows; i++) {
        System.out.print("    row " + i + ":");
        for (int j = 0; j < numCols; j++) {
          System.out.print("  " + data.get(i)[j]);
        }
        System.out.println();
      }
      System.out.println("--------------------------");
    }
  }
}
