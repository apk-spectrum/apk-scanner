package com.apkspectrum.swing.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * @version 1.0 11/22/98
 */

public class DefaultCellAttribute
	// implements CellAttribute ,CellSpan  {
	implements CellAttribute, CellSpan, CellColor, CellFont {

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

	public DefaultCellAttribute() {
		this(0,0);
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

	public void setSize(Dimension size) {
		columnSize = size.width;
		rowSize    = size.height;
		span = new int[rowSize][columnSize][2];   // 2: COLUMN,ROW
		foreground = new Color[rowSize][columnSize];
		background = new Color[rowSize][columnSize];
		font = new Font[rowSize][columnSize];
		initValue();
	}

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

	@Override
	public CellSpan getCellSpan() {
		return (CellSpan) this;
	}

	@Override
	public CellFont getCellFont() {
		return (CellFont) this;
	}

	@Override
	public CellColor getCellColor() {
		return (CellColor) this;
	}

	//
	// CellSpan
	//
	@Override
	public int[] getSpan(int row, int column) {
		if (isOutOfBounds(row, column)) {
			int[] ret_code = {1,1};
			return ret_code;
		}
		return new int[] {span[row][column][ROW], span[row][column][COLUMN]};
	}

	@Override
	public void setSpan(int[] span, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		this.span[row][column] = span;
	}

	@Override
	public int[] getAnchorPoint(int row, int column) {
		int[] span = getSpan(row, column);
		if(span[ROW] >= 1 && span[COLUMN] >= 1) {
			return new int[] {row, column};
		}
		return new int[] {row + span[ROW], column + span[COLUMN]};
	}

	@Override
	public boolean isVisible(int row, int column) {
		if (isOutOfBounds(row, column)) return false;
		if ((span[row][column][CellSpan.COLUMN] < 1)
				||(span[row][column][CellSpan.ROW]    < 1)) return false;
		return true;
	}

	@Override
	public boolean isCombined(int row, int column) {
		int[] span = getSpan(row, column);
		return span[ROW] != 1 || span[COLUMN] != 1;
	}

	@Override
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

	@Override
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
	}

	@Override
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
	}

	@Override
	public boolean isPossibleMove(int start, int end, int to) {
		int gap = end - start;
		if(gap < 0 || start == to
				|| isOutOfBounds(start, 0) || isOutOfBounds(end, 0)
				|| isOutOfBounds(to, 0) || isOutOfBounds(to + gap, 0)) {
			return false;
		}
		boolean isToDown = start < to; 
		if(isToDown) {
			to = end + (to - start); 
		}
		for(int i = 0; i < columnSize; i++) {
			int startSpan = span[start][i][CellSpan.ROW];
			if(startSpan < 0 || (startSpan > 1 &&
					(start + startSpan - 1) > end)) return false;
			int endSpan = span[end][i][CellSpan.ROW];
			if(endSpan > 1) return false;
			else if(endSpan < 0 && end < rowSize - 1
					&& span[end+1][i][CellSpan.ROW] < endSpan) {
				return false;
			}
			int toSpan = span[to][i][CellSpan.ROW];
			if(toSpan < 0 || (isToDown && toSpan > 1)) return false;
		}
		return true;
	}

	//
	// ColoredCell
	//

	@Override
	public Color getForeground(int row, int column) {
		if (isOutOfBounds(row, column)) return null;
		return foreground[row][column];
	}

	@Override
	public void setForeground(Color color, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		foreground[row][column] = color;
	}

	@Override
	public void setForeground(Color color, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return;
		setValues(foreground, color, rows, columns);
	}

	@Override
	public Color getBackground(int row, int column) {
		if (isOutOfBounds(row, column)) return null;
		return background[row][column];
	}

	@Override
	public void setBackground(Color color, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		background[row][column] = color;
	}

	@Override
	public void setBackground(Color color, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return;
		setValues(background, color, rows, columns);
	}
	//


	//
	// CellFont
	//
	@Override
	public Font getFont(int row, int column) {
		if (isOutOfBounds(row, column)) return null;
		return font[row][column];
	}

	@Override
	public void setFont(Font font, int row, int column) {
		if (isOutOfBounds(row, column)) return;
		this.font[row][column] = font;
	}

	@Override
	public void setFont(Font font, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) return;
		setValues(this.font, font, rows, columns);
	}
	//


	//
	// CellAttribute
	//
	@Override
	public void addColumn() {
		int[][][] oldSpan = span;
		int numRows    = rowSize;
		int numColumns = columnSize++;
		span = new int[numRows][numColumns + 1][2];
		//System.arraycopy(oldSpan,0,span,0,numRows);
		for (int i=0;i<numRows;i++) {
			System.arraycopy(oldSpan[i],0,span[i],0,numColumns);
		}
		for (int i=0;i<numRows;i++) {
			span[i][numColumns][CellSpan.COLUMN] = 1;
			span[i][numColumns][CellSpan.ROW]    = 1;
		}
	}

	@Override
	public void addRow() {
		addRowCount(1);
	}

	private void addRowCount(int count) {
		if(count <= 0) return;
		int[][][] oldSpan = span;
		int numRows    = rowSize;
		int numColumns = columnSize;
		rowSize += count;
		span = new int[rowSize][numColumns][2];
		System.arraycopy(oldSpan,0,span,0,numRows);
		for(int row = numRows; row < rowSize; row++) {
			for (int i=0;i<numColumns;i++) {
				span[row][i][CellSpan.COLUMN] = 1;
				span[row][i][CellSpan.ROW]    = 1;
			}
		}
	}

	private void removeRowCount(int count) {
		if(count <= 0) return;
		if(count > rowSize) count = rowSize;
		int[][][] oldSpan = span;
		int numColumns = columnSize;
		rowSize -= count;
		span = new int[rowSize][numColumns][2];
		System.arraycopy(oldSpan,0,span,0,rowSize);
		int lastRow = rowSize - 1; 
		if(lastRow >= 0) {
			for (int i=0;i<numColumns;i++) {
				int rowSpan = span[lastRow][i][CellSpan.ROW];
				if(rowSpan == 1) continue;
				if(rowSpan >= 0) {
					span[lastRow][i][CellSpan.ROW] = 1;
				} else if(span[lastRow][i][CellSpan.COLUMN] == 0) {
					span[lastRow + rowSpan][i][CellSpan.ROW] = -rowSpan + 1;	
				}
			}
		}
	}

	private void addColumnCount(int count) {
		if(count <= 0) return;
		int[][][] oldSpan = span;
		int numRows    = rowSize;
		int numColumns = columnSize;
		columnSize += count;
		span = new int[numRows][columnSize][2];
		//System.arraycopy(oldSpan,0,span,0,numRows);
		for (int i=0;i<numRows;i++) {
			System.arraycopy(oldSpan[i],0,span[i],0,numColumns);
		}
		for(int col = numColumns; col < columnSize; col++) {
			for (int i=0;i<numRows;i++) {
				span[i][col][CellSpan.COLUMN] = 1;
				span[i][col][CellSpan.ROW]    = 1;
			}
		}
	}

	private void removeColumnCount(int count) {
		if(count <= 0) return;
		if(count > columnSize) count = columnSize;
		int[][][] oldSpan = span;
		int numRows    = rowSize;
		columnSize -= count;
		span = new int[numRows][columnSize][2];
		System.arraycopy(oldSpan,0,span,0,rowSize);
		int lastCol = columnSize - 1; 
		if(lastCol >= 0) {
			for (int i=0;i<numRows;i++) {
				int colSpan = span[i][lastCol][CellSpan.COLUMN];
				if(colSpan == 1) continue;
				if(colSpan >= 0) {
					span[i][lastCol][CellSpan.COLUMN] = 1;
				} else if(span[i][lastCol][CellSpan.ROW] == 0) {
					span[i][lastCol + colSpan][CellSpan.COLUMN] = -colSpan + 1;	
				}
			}
		}
	}

	@Override
	public void insertRow(int row) {
		if (row != 0 && columnSize != 0
				&& isOutOfBounds(row > 0 ? row-1 : row, 0)) return;
		int[][][] oldSpan = span;
		int numRows    = rowSize++;
		int numColumns = columnSize;
		span = new int[numRows + 1][numColumns][2];

		System.arraycopy(oldSpan,0,span,0,row);
		System.arraycopy(oldSpan,row,span,row+1,numRows - row);

		for (int i=0;i<numColumns;i++) {
			if(row < numRows && span[row+1][i][CellSpan.ROW] < 0) {
				span[row][i][CellSpan.COLUMN] = span[row+1][i][CellSpan.COLUMN];
				span[row][i][CellSpan.ROW]    = span[row+1][i][CellSpan.ROW];
				if(span[row][i][CellSpan.COLUMN] == 0) {
					int rowSpan = span[row][i][CellSpan.ROW];
					span[row + rowSpan][i][CellSpan.ROW]++;	
				}
				for(int j = row+1; j <= numRows && span[j][i][CellSpan.ROW] < 0; j++) {
					span[j][i][CellSpan.ROW]--;
				}
			} else {
				span[row][i][CellSpan.COLUMN] = 1;
				span[row][i][CellSpan.ROW]    = 1;
			}
		}
	}

	@Override
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

	@Override
	public Dimension getSize() {
		return new Dimension(rowSize, columnSize);
	}

	@Override
	public void setRowCount(int rowCount) {
        int old = rowSize;
        if (old == rowCount) return;
		if(old < rowCount) {
			addRowCount(rowCount - old);
		} else {
			removeRowCount(old - rowCount);
		}
	}

	@Override
	public void setColumnCount(int columnCount) {
		int old = columnSize;
        if (old == columnCount) return;
		if(old < columnCount) {
			addColumnCount(columnCount - old);
		} else {
			removeColumnCount(old - columnCount);
		}
	}

	@Override
	public void moveRow(int start, int end, int to) {
		if(!getCellSpan().isPossibleMove(start, end, to)) return;

		int[][][] oldSpan = span;
		span = new int[rowSize][columnSize][2];

		if(start > to) { // to up
			int src = 0;
			int dest = 0;
			int size = to;
			//System.out.println("1 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
			src = start;
			dest += size;
			size = end-start+1;
			//System.out.println("2 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
			src = to;
			dest += size;
			size = start-to;
			//System.out.println("3 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
			src = end+1;
			dest += size;
			size = rowSize-end-1;
			//System.out.println("4 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
		} else { // to down
			int src = 0;
			int dest = 0;
			int size = start;
			//System.out.println("1 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
			src = end+1;
			dest += size;
			size = to-start;
			//System.out.println("2 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
			src = start;
			dest += size;
			size = end-start+1;
			//System.out.println("3 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
			src = end+1 + (to-start);
			dest += size;
			size = rowSize-src;
			//System.out.println("4 src " + src + ",dest " + dest + ", len " + size);
			System.arraycopy(oldSpan,src,span,dest,size);
		}
	}

	/*
	public void changeAttribute(int row, int column, Object command) { }
	
	public void changeAttribute(int[] rows, int[] columns, Object command) { }
	 */
}
/*
 * (swing1.1beta3)
 *
 */