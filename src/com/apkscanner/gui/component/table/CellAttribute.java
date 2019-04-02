package com.apkscanner.gui.component.table;

import java.awt.Dimension;

import javax.swing.JTable;

/**
 * @version 1.0 11/22/98
 */
public interface CellAttribute {
	public void addColumn();
	public void addRow();
	public void insertRow(int row);
	public void removeRow(int row);
	public Dimension getSize();
	public void setSize(Dimension size);
	public void setRowCount(int rowCount);
	public void setColumnCount(int columnCount);
	public void moveRow(int start, int end, int to);

	public void setTable(JTable table);
	public CellSpan getCellSpan();
	public CellFont getCellFont();
	public CellColor getCellColor();
}
/*
 * (swing1.1beta3)
 *
 */