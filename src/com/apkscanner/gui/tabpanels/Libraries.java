package com.apkscanner.gui.tabpanels;


import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil.FSStyle;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.ZipFileUtil;

/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
public class Libraries extends JPanel implements TabDataObject
{
	private static final long serialVersionUID = -8985157400085276691L;

	private MyTableModel mMyTableModel = null;
	private JTable table;

	private String apkFilePath;
	
	public Libraries()
	{
		super(new GridLayout(1, 0));
	}
	
	@Override
	public void initialize()
	{
		mMyTableModel = new MyTableModel();
		table = new JTable(mMyTableModel);
		
		//table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		
		setJTableColumnsWidth(table, 500, 1, 59, 30, 10);
		
		//Create the scroll pane and add the table to it.
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		//Add the scroll pane to this panel.
		add(scrollPane);
	}
  
	@Override
	public void setData(ApkInfo apkInfo)
	{
		if(mMyTableModel == null)
			initialize();
		apkFilePath = apkInfo.filePath;
		mMyTableModel.setData(apkInfo.libraries);
	}

	@Override
	public void setExtraData(ApkInfo apkInfo) { }
	
	@Override
	public void reloadResource()
	{
		if(mMyTableModel == null) return;
		mMyTableModel.loadResource();
		mMyTableModel.fireTableStructureChanged();
		setJTableColumnsWidth(table, 500, 1, 59, 30, 10);
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
		private static final long serialVersionUID = 8403482180817388452L;

		private String[] columnNames = { "Index", "Path", "Size"};
		
		private ArrayList<Object[]> data = new ArrayList<Object[]>();
		
		public MyTableModel() 
		{
			loadResource();
		}
				
		public void setData(String[] libList)
		{
			data.clear();
			if(libList == null) return;

			for(int i=0; i< libList.length; i++) {
				long size = ZipFileUtil.getFileSize(apkFilePath, libList[i]);
				long compressed = ZipFileUtil.getCompressedSize(apkFilePath, libList[i]);
				Object[] temp = { 
						i+1,
						libList[i], 
						FileUtil.getFileSize(size, FSStyle.FULL),
						String.format("%.2f", ((float)(size - compressed) / (float)size) * 100f) + " %"
				};
				data.add(temp);
			}
			fireTableDataChanged();
		}

		public void loadResource()
		{
			columnNames = new String[] {
				Resource.STR_LIB_COLUMN_INDEX.getString(),
				Resource.STR_LIB_COLUMN_PATH.getString(),
				Resource.STR_LIB_COLUMN_SIZE.getString(),
				Resource.STR_LIB_COLUMN_COMPRESS.getString()
			};
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
			data.get(row)[col] = value;
			fireTableCellUpdated(row, col);
		}
	
		@SuppressWarnings("unused")
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
