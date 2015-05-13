package com.ApkInfo.TabUI;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.TabUI.MyTabUILib.MyTableModel;

/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
public class MyTabUIWidget extends JPanel {
	public ArrayList<Object[]> arrWidgets;
	 public MyTabUIWidget() {
	    super(new GridLayout(1, 0));
	    
	    JTable table = new JTable(new MyTableModel());
	    arrWidgets = new ArrayList<Object[]>(); 
	    //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
	    
	    for(int i=0; i< 5; i++) {
	    	
	    	ImageIcon myimageicon = new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/popup_dvideo_norm.png");
	    	
	    	myimageicon.setImage(getScaledImage(myimageicon,100,100));
	    	
	    	Object[] temp = {myimageicon,
					"2X2","com.google.android.apps.gmm.appwidget.CreateDirectionsShortcutActivity", "ANDROID" };
	    	arrWidgets.add(temp);
	    	
	    }
	    for(int i=0; i< 5; i++) {
	    	table.setRowHeight(i, 100);
	    }
	    
	    setJTableColumnsWidth(table, 500, 20,5,65,10);
	    
	    
	    //Create the scroll pane and add the table to it.
	    
	    table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
	    
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
		  
		  private String[] columnNames = { "Image", "Size", "Activity", "Type"};
		      
		    MyTableModel() {
		  }
		
	    public int getColumnCount() {
	      return columnNames.length;
	    }

	    public int getRowCount() {
	      return arrWidgets.size();
	    }

	    public String getColumnName(int col) {
	      return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {    	
	      return arrWidgets.get(row)[col];
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

	    	arrWidgets.get(row)[col] = value;
	      fireTableCellUpdated(row, col);
	    }

	    private void printDebugData() {
	      int numRows = getRowCount();
	      int numCols = getColumnCount();

	      for (int i = 0; i < numRows; i++) {
	        System.out.print("    row " + i + ":");
	        for (int j = 0; j < numCols; j++) {
	          System.out.print("  " + arrWidgets.get(i)[j]);
	        }
	        System.out.println();
	      }
	      System.out.println("--------------------------");
	    }
	  }
	private Image getScaledImage(ImageIcon temp, int w, int h){
		
		Image srcImg = temp.getImage();
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();
	    return resizedImg;
	}
	    
	  class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

		  public MultiLineCellRenderer() {
		    setLineWrap(true);
		    setWrapStyleWord(true);
		    setOpaque(true);
		  }

		  public Component getTableCellRendererComponent(JTable table, Object value,
		      boolean isSelected, boolean hasFocus, int row, int column) {
		    if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(table.getBackground());
		    }
		    setFont(table.getFont());
		    if (hasFocus) {
		      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		      if (table.isCellEditable(row, column)) {
		        setForeground(UIManager.getColor("Table.focusCellForeground"));
		        setBackground(UIManager.getColor("Table.focusCellBackground"));
		      }
		    } else {
		      setBorder(new EmptyBorder(1, 2, 1, 2));
		    }
		    setText((value == null) ? "" : value.toString());
		    return this;
		  }
		}
}