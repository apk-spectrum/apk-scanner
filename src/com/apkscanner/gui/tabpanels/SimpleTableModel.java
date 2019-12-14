package com.apkscanner.gui.tabpanels;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.LanguageChangeListener;
import com.apkscanner.resource.RStr;

public abstract class SimpleTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 9006137446600751953L;

	protected ArrayList<Object[]> data = new ArrayList<Object[]>();

	protected RStr[] columnNames;
	{
		RStr.addLanguageChangeListener(new LanguageChangeListener() {
			@Override
			public void languageChange(String oldLang, String newLang) {
				fireTableStructureChanged();
			}
		});
	}

	public SimpleTableModel(RStr... res) {
		columnNames = res;
	}

	abstract public void setData(ApkInfo apkInfo);

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col].get();
	}

	@Override
	public Object getValueAt(int row, int col) {
		return data.get(row)[col];
	}

	@Override
	public Class<? extends Object> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}
}
