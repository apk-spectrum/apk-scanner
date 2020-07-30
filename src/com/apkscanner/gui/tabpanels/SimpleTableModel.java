package com.apkscanner.gui.tabpanels;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.apkscanner.resource.RStr;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.resource.LanguageChangeListener;

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
		return getRowCount() > 0 && getValueAt(0, c) != null ? getValueAt(0, c).getClass() : Object.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}
}
