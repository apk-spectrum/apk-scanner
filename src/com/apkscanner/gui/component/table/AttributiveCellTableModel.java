package com.apkscanner.gui.component.table;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @version 1.0 11/22/98
 */

public class AttributiveCellTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 287820920999697514L;

	protected CellAttribute cellAtt;

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

	@Override
	@SuppressWarnings("rawtypes")
	public void setDataVector(Vector dataVector, Vector columnIdentifiers) {
		cellAtt = new DefaultCellAttribute(dataVector.size(),
				columnIdentifiers.size());
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
	}

	@Override
	public void moveRow(int start, int end, int to) {
		if(!cellAtt.getCellSpan().isPossibleMove(start, end, to)) {
			return;
		}
		cellAtt.moveRow(start, end, to);
		super.moveRow(start, end, to);
	}

	@Override
    public void removeRow(int row) {
        cellAtt.removeRow(row);
        super.removeRow(row);
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
