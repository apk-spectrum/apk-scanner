package com.apkscanner.gui.component.table;

import java.awt.Color;

public interface ColoredCell {
	public Color getForeground(int row, int column);
	public void setForeground(Color color, int row, int column);
	public void setForeground(Color color, int[] rows, int[] columns);

	public Color getBackground(int row, int column);
	public void setBackground(Color color, int row, int column);
	public void setBackground(Color color, int[] rows, int[] columns);
}
