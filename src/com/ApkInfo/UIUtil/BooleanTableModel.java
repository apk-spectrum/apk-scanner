package com.ApkInfo.UIUtil;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.ApkInfo.UI.PackageTreeDlg.FrameworkTableObject;

public class BooleanTableModel extends AbstractTableModel {
    String[] columns = {"", "device", "path"};

    ArrayList<FrameworkTableObject> arrayList;
    
     public BooleanTableModel(ArrayList<FrameworkTableObject> datalist) {
		// TODO Auto-generated constructor stub    	 
    	 arrayList = datalist;
	}
    public int getRowCount() {
        return arrayList.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    	
    	switch(columnIndex) {
    	case 0:
    		return arrayList.get(rowIndex).buse;        		
    	case 1:
    		return arrayList.get(rowIndex).deviceID +"(" + arrayList.get(rowIndex).location + ")";
    	case 2:
    		return arrayList.get(rowIndex).path;
    	}
        //return data[rowIndex][columnIndex];
    	return null;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
    	//System.out.println(columnIndex);
    	//return arrayList.get(columnIndex).getClass();
    	
    	switch(columnIndex) {
    	case 0:
    		return Boolean.class;  		
    	case 1:
    		return String.class;
    	case 2:
    		return String.class;
    	}
    	return null;
    }

	public boolean isCellEditable(int row, int col) {
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		if(col==0)
			return true;
		else return false;
	}
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        //data[row][col] = value;            
        
    	switch(col) {
    	case 0:
    		arrayList.get(row).buse = (Boolean) value;
    		break;        		
    	case 1:
    		arrayList.get(row).location = (String) value;
    		break;        		
    	case 2:
    		arrayList.get(row).path = (String) value;
    		break;
    	}
    	
        fireTableCellUpdated(row, col);
    }
}