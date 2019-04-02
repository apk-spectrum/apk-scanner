package com.apkscanner.gui.component.table;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @version 1.0 11/22/98
 */

public class AttributiveCellTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 287820920999697514L;

	protected CellAttribute cellAtt;
	protected JTable table;

	public AttributiveCellTableModel() {
		super();
	}

	public AttributiveCellTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
	}

    @SuppressWarnings("rawtypes")
	public AttributiveCellTableModel(Vector columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

	public AttributiveCellTableModel(Object[] columnNames, int numRows) {
		super(columnNames, numRows);
	}

	@SuppressWarnings("rawtypes")
	public AttributiveCellTableModel(Vector<? extends Vector> data, Vector<?> columnNames) {
		super(data, columnNames);
	}

	public AttributiveCellTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	public void setTable(JTable table) {
		this.table = table;
		if(cellAtt.getCellSpan() != null) {
			cellAtt.getCellSpan().setTable(table);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setDataVector(Vector dataVector, Vector columnIdentifiers) {
		cellAtt = new DefaultCellAttribute(dataVector.size(),
				columnIdentifiers.size());
		if(cellAtt.getCellSpan() != null) {
			cellAtt.getCellSpan().setTable(table);
		}
		super.setDataVector(dataVector, columnIdentifiers);
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public void addColumn(Object columnName, Vector columnData) {
		cellAtt.addColumn();
		super.addColumn(columnName, columnData);
	}

	@Override
	public void setNumRows(int rowCount) {
		if(rowCount < 0) rowCount = 0;
		cellAtt.setRowCount(rowCount);
		super.setNumRows(rowCount);
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public void insertRow(int row, Vector rowData) {
		cellAtt.insertRow(row);
		super.insertRow(row, rowData);

		if(table != null) {
			int[] rows = table.getSelectedRows();
			int[] cols = table.getSelectedColumns();
			if(rows.length > 1 && cols.length > 0
					&& rows[0] <= row && row <= rows[rows.length-1]) {
				table.setRowSelectionInterval(rows[1], rows[rows.length-1]);
				table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
			}
		}
	}

	@Override
	public void moveRow(int start, int end, int to) {
		if(!cellAtt.getCellSpan().isPossibleMove(start, end, to)) {
			return;
		}
		cellAtt.moveRow(start, end, to);
		super.moveRow(start, end, to);

		if(table != null) {
			int[] rows = table.getSelectedRows();
			int[] cols = table.getSelectedColumns();
			if(rows.length > 0 && cols.length > 0) {
				table.setRowSelectionInterval(to, to + (rows.length-1));
				table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
			}
			table.repaint();
		}
	}

	@Override
    public void removeRow(int row) {
		int[] rows = table != null ? table.getSelectedRows() : null;
		int[] cols = table != null ? table.getSelectedColumns() : null;

        cellAtt.removeRow(row);
        super.removeRow(row);

        if(table != null && !dataVector.isEmpty()) {
			if(rows[0] >= dataVector.size()) {
				rows = new int[] { dataVector.size() - 1 };
			} else if(rows[rows.length-1] >= dataVector.size()) {
				rows[rows.length-1] = dataVector.size() - 1;
			}
			table.setRowSelectionInterval(rows[0], rows[0] >= rows[rows.length-1] ? rows[0] : rows[rows.length-1] - 1);
			table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
		}
    }

	@Override
	@SuppressWarnings("rawtypes")
    public void setColumnIdentifiers(Vector columnIdentifiers) {
		// Keep the attribute of span by skip the create a cellAtt
		cellAtt.setColumnCount(columnIdentifiers.size());
		super.setDataVector(dataVector, columnIdentifiers);
    }

	@Override
	public void setColumnCount(int columnCount) {
		if(columnCount < 0) columnCount = 0;
		cellAtt.setColumnCount(columnCount);
		super.setColumnCount(columnCount);
	}

	public CellSpan getCellSpan() {
		return (CellSpan) cellAtt;
	}

	public CellFont getCellFont() {
		return (CellFont) cellAtt;
	}

	public CellColor getCellColor() {
		return (CellColor) cellAtt;
	}

	public void setCellAttributeModel(CellAttribute newCellAtt) {
		int numColumns = getColumnCount();
		int numRows    = getRowCount();
		if ((newCellAtt.getSize().width  != numColumns) ||
				(newCellAtt.getSize().height != numRows)) {
			newCellAtt.setSize(new Dimension(numRows, numColumns));
		}
		cellAtt = newCellAtt;
		if(cellAtt != null && cellAtt.getCellSpan() != null) {
			cellAtt.getCellSpan().setTable(table);
		}
		fireTableDataChanged();
	}

	@Override
    public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex) != null
				? getValueAt(0, columnIndex).getClass()
				: super.getColumnClass(columnIndex);
    }

	/*
	public void changeCellAttribute(int row, int column, Object command) {
		cellAtt.changeAttribute(row, column, command);
	}

	public void changeCellAttribute(int[] rows, int[] columns, Object command) {
		cellAtt.changeAttribute(rows, columns, command);
	}
	 */
}
