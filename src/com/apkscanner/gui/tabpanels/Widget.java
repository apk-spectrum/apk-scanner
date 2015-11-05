package com.apkscanner.gui.tabpanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.resource.Resource;

/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
public class Widget extends JPanel implements TabDataObject
{
	private static final long serialVersionUID = 4881638983501664860L;

	private MyTableModel TableModel = null; 
	private JTable table = null;
	private ArrayList<Object[]> arrWidgets = new ArrayList<Object[]>();

	public Widget() {
		super(new GridLayout(1, 0));
	}
	
	@Override
	public void initialize()
	{
		TableModel = new MyTableModel();
		table = new JTable(TableModel);
		
		setJTableColumnsWidth(table, 500, 20,15,17,60,10);	    
		
		//Create the scroll pane and add the table to it.
		
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		//Add the scroll pane to this panel.
		add(scrollPane);	
	}
	
	@Override
	public void setData(ApkInfo apkInfo)
	{
		//table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		arrWidgets.clear();
		if(apkInfo.widgets == null) return;
		if(TableModel == null) initialize();

		for(int i=0; i< apkInfo.widgets.length; i++) {
			ImageIcon myimageicon = null;
			try {
				myimageicon = new ImageIcon(new URL((String)apkInfo.widgets[i].icons[apkInfo.widgets[i].icons.length-1].name));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if(myimageicon != null) {
				myimageicon.setImage(ImageScaler.getMaxScaledImage(myimageicon,100,100));
			}
			
			String label = apkInfo.widgets[i].lables[0].name;
			if(label == null) label = apkInfo.manifest.application.labels[0].name;
			Object[] temp = { myimageicon , label, apkInfo.widgets[i].size, apkInfo.widgets[i].name, apkInfo.widgets[i].type};
			arrWidgets.add(temp);
		}

		TableModel.fireTableDataChanged();
		for(int i=0; i< arrWidgets.size(); i++) {
			table.setRowHeight(i, 100);
		}
	}
	
	@Override
	public void reloadResource()
	{
		if(TableModel == null) return;
		TableModel.loadResource();
		TableModel.fireTableStructureChanged();
		setJTableColumnsWidth(table, 500, 20,15,17,60,10);
		for(int i=0; i< arrWidgets.size(); i++) {
			table.setRowHeight(i, 100);
		}
	}

	public static void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
												double... percentages) {
		double total = 0;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
		}
 
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);		        
			column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
		}
	}
	  
	class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 2567370181372859791L;

		private String[] columnNames = null;

		MyTableModel() {
			loadResource();
		}
		
		public void loadResource()
		{
			columnNames = new String[] {
				Resource.STR_WIDGET_COLUMN_IMAGE.getString(),
				Resource.STR_WIDGET_COLUMN_LABEL.getString(),
				Resource.STR_WIDGET_COLUMN_SIZE.getString(),
				Resource.STR_WIDGET_COLUMN_ACTIVITY.getString(),
				Resource.STR_WIDGET_COLUMN_TYPE.getString(),
			};
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
		public Class<? extends Object> getColumnClass(int c) {
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

		@SuppressWarnings("unused")
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
	    
	class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
		private static final long serialVersionUID = -4421652692115836378L;

		public MultiLineCellRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			if (isSelected) {
				//setForeground(table.getSelectionForeground());
				setBackground(Color.LIGHT_GRAY);
			} else {
				//setForeground(table.getForeground());
				setBackground(Color.WHITE);
			}
			/*
			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(UIManager.getColor("Table.focusCellForeground"));
					setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			*/
			setFont(table.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}
}