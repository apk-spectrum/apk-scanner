package com.apkscanner.gui.dialog;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class SimpleCheckTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = -645265173724630402L;
	
	
	public interface TableRowObject {
		public Object get(int col);
		public void set(int col, Object obj);
	}

	private String[] columnNames;
	private ArrayList<TableRowObject> bindData;
	
    public SimpleCheckTableModel(String[] columnNames) {
    	this.columnNames = columnNames;
    	this.bindData = null;
	}
    
    public SimpleCheckTableModel(String[] columnNames, ArrayList<TableRowObject> data) {
    	this.columnNames = columnNames;
    	this.bindData = data;
	}
    
    public void bindData(ArrayList<TableRowObject> data) {
    	this.bindData = data;
    }
    
    public int getSelectedRowCount() {
    	int count = 0;
    	for(TableRowObject rowObj: bindData) {
    		if((boolean)rowObj.get(0)) count++;
    	}
    	return count;
    }

    @Override
    public int getRowCount() {
        return bindData.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
    	return bindData.get(row).get(col);
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
    	if(col == 0)
    		return Boolean.class;
    	return String.class;
    }

    @Override
	public boolean isCellEditable(int row, int col) {
		if(col == 0)
			return true;
		return false;
	}
    
    @Override
    public void setValueAt(Object value, int row, int col) {
    	bindData.get(row).set(col, value);
    	
        fireTableCellUpdated(row, col);
    }
}