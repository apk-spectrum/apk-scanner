package com.apkscanner.gui.component.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTable;

/**
 * @version 1.0 11/22/98
 */

public class DefaultCellAttribute
	// implements CellAttribute ,CellSpan  {
	implements CellAttribute, CellSpan, ColoredCell, CellFont {

	//
	// !!!! CAUTION !!!!!
	// these values must be synchronized to Table data
	//
	protected int rowSize;
	protected int columnSize;
	protected int[][][] span;                   // CellSpan
	protected Color[][] foreground;             // ColoredCell
	protected Color[][] background;             //
	protected Font[][]  font;                   // CellFont

	protected JTable table;

	public DefaultCellAttribute() {
		this(1,1);
	}

	public DefaultCellAttribute(int numRows, int numColumns) {
		setSize(new Dimension(numColumns, numRows));
	}

	protected void initValue() {
		for(int i=0; i<span.length;i++) {
			for(int j=0; j<span[i].length; j++) {
				span[i][j][CellSpan.COLUMN] = 1;
				span[i][j][CellSpan.ROW]    = 1;
			}
		}
	}

	//
	// CellSpan
	//
	public int[] getSpan(int row, int column) {
		if (isOutOfBounds(row, column)) {
			int[] ret_code = {1,1};
			return ret_code;
		}
		return span[row][column];
	}

	public void setSpan(int[] span, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		this.span[row][column] = span;
	}

	public int[] getAnchorPoint(int row, int column) {
		int[] span = getSpan(row, column);
		if(span[ROW] >= 1 && span[COLUMN] >= 1) {
			return new int[] {row, column};
		}
		return new int[] {row + span[ROW], column + span[COLUMN]};
	}

	public boolean isVisible(int row, int column) {
		if (isOutOfBounds(row, column)) return false;
		if ((span[row][column][CellSpan.COLUMN] < 1)
				||(span[row][column][CellSpan.ROW]    < 1)) return false;
		return true;
	}

	public boolean isCombined(int row, int column) {
		int[] span = getSpan(row, column);
		return span[ROW] != 1 || span[COLUMN] != 1;
	}
	
	public boolean isPossibleCombine(int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return false;
		int    rowSpan  = rows.length;
		int columnSpan  = columns.length;
		int startRow    = rows[0];
		int startColumn = columns[0];
		for (int i=0;i<rowSpan;i++) {
			for (int j=0;j<columnSpan;j++) {
				if ((span[startRow +i][startColumn +j][CellSpan.COLUMN] != 1)
						||(span[startRow +i][startColumn +j][CellSpan.ROW]    != 1)) {
					//System.out.println("can't combine");
					return false;
				}
			}
		}
		return true;
	}

	public void combine(int[] rows, int[] columns) {
		if (!isPossibleCombine(rows, columns)) return;
		int    rowSpan  = rows.length;
		int columnSpan  = columns.length;
		int startRow    = rows[0];
		int startColumn = columns[0];
		for (int i=0,ii=0;i<rowSpan;i++,ii--) {
			for (int j=0,jj=0;j<columnSpan;j++,jj--) {
				span[startRow +i][startColumn +j][CellSpan.COLUMN] = jj;
				span[startRow +i][startColumn +j][CellSpan.ROW]    = ii;
				//System.out.println("r " +ii +"  c " +jj);
			}
		}
		span[startRow][startColumn][CellSpan.COLUMN] = columnSpan;
		span[startRow][startColumn][CellSpan.ROW]    =    rowSpan;

		if(table == null) return;
		table.setRowSelectionInterval(startRow, startRow);
		table.setColumnSelectionInterval(startColumn, startColumn);
	}

	public void split(int row, int column) {
		if (isOutOfBounds(row, column)) return;
		int[] anchor = getAnchorPoint(row, column);
		row = anchor[ROW];
		column = anchor[COLUMN];
		int columnSpan = span[row][column][CellSpan.COLUMN];
		int    rowSpan = span[row][column][CellSpan.ROW];
		for (int i=0;i<rowSpan;i++) {
			for (int j=0;j<columnSpan;j++) {
				span[row +i][column +j][CellSpan.COLUMN] = 1;
				span[row +i][column +j][CellSpan.ROW]    = 1;
			}
		}

		if(table == null) return;
		table.setRowSelectionInterval(row, row + rowSpan - 1);
		table.setColumnSelectionInterval(column, column + columnSpan - 1);
	}

	@Override
	public void setTable(JTable table) {
		this.table = table;
	}

	//
	// ColoredCell
	//
	public Color getForeground(int row, int column) {
		if (isOutOfBounds(row, column)) return null;
		return foreground[row][column];
	}
	public void setForeground(Color color, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		foreground[row][column] = color;
	}
	public void setForeground(Color color, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return;
		setValues(foreground, color, rows, columns);
	}
	public Color getBackground(int row, int column) {
		if (isOutOfBounds(row, column)) return null;
		return background[row][column];
	}
	public void setBackground(Color color, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		background[row][column] = color;
	}
	public void setBackground(Color color, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return;
		setValues(background, color, rows, columns);
	}
	//


	//
	// CellFont
	//
	public Font getFont(int row, int column) {
		if (isOutOfBounds(row, column)) return null;
		return font[row][column];
	}
	public void setFont(Font font, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		this.font[row][column] = font;
	}
	public void setFont(Font font, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return;
		setValues(this.font, font, rows, columns);
	}
	//


	//
	// CellAttribute
	//
	public void addColumn() {
		int[][][] oldSpan = span;
		int numRows    = rowSize;
		int numColumns = columnSize++;
		span = new int[numRows][numColumns + 1][2];
		System.arraycopy(oldSpan,0,span,0,numRows);
		for (int i=0;i<numRows;i++) {
			span[i][numColumns][CellSpan.COLUMN] = 1;
			span[i][numColumns][CellSpan.ROW]    = 1;
		}
	}

	public void addRow() {
		int[][][] oldSpan = span;
		int numRows    = rowSize++;
		int numColumns = columnSize;
		span = new int[numRows + 1][numColumns][2];
		System.arraycopy(oldSpan,0,span,0,numRows);
		for (int i=0;i<numColumns;i++) {
			span[numRows][i][CellSpan.COLUMN] = 1;
			span[numRows][i][CellSpan.ROW]    = 1;
		}
	}

	public void insertRow(int row) {
		if (isOutOfBounds(row > 0 ? row-1 : row, 0)) return;
		int[][][] oldSpan = span;
		int numRows    = rowSize++;
		int numColumns = columnSize;
		span = new int[numRows + 1][numColumns][2];

		System.arraycopy(oldSpan,0,span,0,row);
		System.arraycopy(oldSpan,row,span,row+1,numRows - row);

		for (int i=0;i<numColumns;i++) {
			if(span[row+1][i][CellSpan.ROW] < 0) {
				span[row][i][CellSpan.COLUMN] = span[row+1][i][CellSpan.COLUMN];
				span[row][i][CellSpan.ROW]    = span[row+1][i][CellSpan.ROW];
				if(span[row][i][CellSpan.COLUMN] == 0) {
					int rowSpan = span[row][i][CellSpan.ROW];
					span[row + rowSpan][i][CellSpan.ROW]++;	
				}
				for(int j = row+1; j < numRows && span[j][i][CellSpan.ROW] < 0; j++) {
					span[j][i][CellSpan.ROW]--;
				}
			} else {
				span[row][i][CellSpan.COLUMN] = 1;
				span[row][i][CellSpan.ROW]    = 1;
			}
		}
	}

	public void removeRow(int row) {
		if (isOutOfBounds(row > 0 ? row-1 : row, 0)) return;
		int[][][] oldSpan = span;
		int numRows    = rowSize--;
		int numColumns = columnSize;
		span = new int[numRows-1][numColumns][2];

		System.arraycopy(oldSpan,0,span,0,row);
		System.arraycopy(oldSpan,row+1,span,row,numRows - row - 1);

		for (int i=0;i<numColumns;i++) {
			int orgSpan = oldSpan[row][i][CellSpan.ROW];
			if(orgSpan == 1) continue;
			if(orgSpan < 0) {
				if(oldSpan[row][i][CellSpan.COLUMN] == 0) {
					span[row + orgSpan][i][CellSpan.ROW]--;
				}
				if(row >= numRows-1) continue;
				if(span[row][i][CellSpan.ROW] < 0) {
					span[row][i][CellSpan.ROW]++;
				}
			}
			if(row >= numRows-1) continue;
			if(orgSpan == 0) {
				if(span[row][i][CellSpan.COLUMN] < 0) {
					span[row][i][CellSpan.ROW] = 0;
				}
			} else if(orgSpan > 1) {
				span[row][i][CellSpan.ROW] = orgSpan - 1;
				span[row][i][CellSpan.COLUMN] = oldSpan[row][i][CellSpan.COLUMN];
			}
			for(int j = row+1; j < numRows-1 && span[j][i][CellSpan.ROW] < 0; j++) {
				span[j][i][CellSpan.ROW]++;
			}
		}
	}

	public Dimension getSize() {
		return new Dimension(rowSize, columnSize);
	}

	public void setSize(Dimension size) {
		columnSize = size.width;
		rowSize    = size.height;
		span = new int[rowSize][columnSize][2];   // 2: COLUMN,ROW
		foreground = new Color[rowSize][columnSize];
		background = new Color[rowSize][columnSize];
		font = new Font[rowSize][columnSize];
		initValue();
	}

	/*
public void changeAttribute(int row, int column, Object command) { }

public void changeAttribute(int[] rows, int[] columns, Object command) { }
	 */

	protected boolean isOutOfBounds(int row, int column) {
		if ((row    < 0)||(rowSize    <= row)
				||(column < 0)||(columnSize <= column)) {
			return true;
		}
		return false;
	}

	protected boolean isOutOfBounds(int[] rows, int[] columns) {
		if(rows.length == 0 && columns.length == 0) return true;
		for (int i=0;i<rows.length;i++) {
			if ((rows[i] < 0)||(rowSize <= rows[i])) return true;
		}
		for (int i=0;i<columns.length;i++) {
			if ((columns[i] < 0)||(columnSize <= columns[i])) return true;
		}
		return false;
	}

	protected void setValues(Object[][] target, Object value,
			int[] rows, int[] columns) {
		for (int i=0;i<rows.length;i++) {
			int row = rows[i];
			for (int j=0;j<columns.length;j++) {
				int column = columns[j];
				target[row][column] = value;
			}
		}
	}
}
/*
 * (swing1.1beta3)
 *
 */