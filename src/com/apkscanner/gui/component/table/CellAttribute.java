package com.apkscanner.gui.component.table;

import java.awt.Dimension;

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
}
/*
 * (swing1.1beta3)
 *
 */