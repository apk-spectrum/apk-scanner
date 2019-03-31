package com.apkscanner.gui.component.table;

import javax.swing.JTable;

/**
 * @version 1.0 11/22/98
 */
public interface CellSpan {
	public final int ROW    = 0;
	public final int COLUMN = 1;

	public int[] getSpan(int row, int column);
	public void setSpan(int[] span, int row, int column);

	public int[] getAnchorPoint(int row, int column);

	public boolean isVisible(int row, int column);
	public boolean isCombined(int row, int column);
	public boolean isPossibleCombine(int[] rows, int[] columns);

	public void combine(int[] rows, int[] columns);
	public void split(int row, int column);

	public void setTable(JTable table);
}
/*
 * (swing1.1beta3)
 *
 */
