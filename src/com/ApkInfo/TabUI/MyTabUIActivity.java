package com.ApkInfo.TabUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class MyTabUIActivity extends JPanel {
	  private boolean DEBUG = false;
	  JTextArea textArea;
	  JPanel IntentPanel;
	  JLabel IntentLabel;
	  protected String[] columnToolTips = { null, null,
	      "The person's favorite sport to participate in",
	      "The number of years the person has played the sport",
	      "If checked, the person eats no meat" };

	  public MyTabUIActivity() {
	    super(new GridLayout(2, 0));

	    final JTable table = new JTable(new MyTableModel()) {

	      //Implement table cell tool tips.
	      public String getToolTipText(MouseEvent e) {
	        String tip = null;
	        java.awt.Point p = e.getPoint();
	        int rowIndex = rowAtPoint(p);
	        int colIndex = columnAtPoint(p);
	        int realColumnIndex = convertColumnIndexToModel(colIndex);

	        if (realColumnIndex == 2) { //Sport column
	          tip = "This person's favorite sport to "
	              + "participate in is: "
	              + getValueAt(rowIndex, colIndex);
	        } else if (realColumnIndex == 4) { //Veggie column
	          TableModel model = getModel();
	          String firstName = (String) model.getValueAt(rowIndex, 0);
	          String lastName = (String) model.getValueAt(rowIndex, 1);
	          Boolean veggie = (Boolean) model.getValueAt(rowIndex, 4);
	          if (Boolean.TRUE.equals(veggie)) {
	            tip = firstName + " " + lastName + " is a vegetarian";
	          } else {
	            tip = firstName + " " + lastName
	                + " is not a vegetarian";
	          }
	        } else {
	          //You can omit this part if you know you don't
	          //have any renderers that supply their own tool
	          //tips.
	          tip = super.getToolTipText(e);
	        }
	        return tip;
	      }

	      //Implement table header tool tips.
	      protected JTableHeader createDefaultTableHeader() {
	        return new JTableHeader(columnModel) {
	          public String getToolTipText(MouseEvent e) {
	            String tip = null;
	            java.awt.Point p = e.getPoint();
	            int index = columnModel.getColumnIndexAtX(p.x);
	            int realIndex = columnModel.getColumn(index)
	                .getModelIndex();
	            return columnToolTips[realIndex];
	          }
	        };
	      }
	    };

	    ListSelectionModel cellSelectionModel = table.getSelectionModel();
	    
	cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
	      String selectedData = null;
	      if(e.getValueIsAdjusting()) {
		      System.out.println("Selected: " + table.getSelectedRow());
		      System.out.println("Selected: " + table.getValueAt(table.getSelectedRow(),table.getSelectedColumn()));
	      }
	    }
	
	  });
		
		setJTableColumnsWidth(table, 500, 4,60,18,18);
	    //Create the scroll pane and add the table to it.
	    JScrollPane scrollPane = new JScrollPane(table);
	    
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane2 = new JScrollPane(textArea);
        //scrollPane2.setPreferredSize(new Dimension(300, 500));
        
        textArea.setText("\nasdassdasd\nasdas\nasdassdasd\na\nasdassdasdsadasd\nasdasdasdasdsadasd\nasdassd\nasdasdasdasdsadasd\nasdasdasdasdsadasd\nasdasdasdasdsadasd\nasdasdasdasdsadasd\nasdasdasdasdsadasd\n");
        
        IntentPanel = new JPanel();
        IntentLabel = new JLabel("Intent filter");
        
        IntentPanel.setLayout(new BorderLayout());
        
        //IntentLabel.setPreferredSize(new Dimension(300, 100));
        IntentPanel.add(IntentLabel, BorderLayout.NORTH);
        IntentPanel.add(scrollPane2, BorderLayout.CENTER);
        
	    //Add the scroll pane to this panel.
	    add(scrollPane);
	    add(IntentPanel);
	  }

	   public void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
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
	    private String[] columnNames = { "Index", "Class", "Type",
	        "Startup" };

	    private Object[][] data = {
	        { "0","Mary", "Campione", "Snowboarding"},
	        { "1","Alison", "Huml", "Rowing"},
	        { "2","Kathy", "Walrath", "Knitting"},
	        { "3","Sharon", "Zakhour", "Speed reading"},
	        { "4","Philip", "Milne", "Pool"} };

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
	      if (col < 2) {
	        return false;
	      } else {
	        return true;
	      }
	    }

	    /*
	     * Don't need to implement this method unless your table's data can
	     * change.
	     */
	    public void setValueAt(Object value, int row, int col) {
	      if (DEBUG) {
	        System.out.println("Setting value at " + row + "," + col
	            + " to " + value + " (an instance of "
	            + value.getClass() + ")");
	      }

	      data[row][col] = value;
	      fireTableCellUpdated(row, col);

	      if (DEBUG) {
	        System.out.println("New value of data:");
	        printDebugData();
	      }
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