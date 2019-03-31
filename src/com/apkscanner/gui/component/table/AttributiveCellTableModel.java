package com.apkscanner.gui.component.table;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * @version 1.0 11/22/98
 */

public class AttributiveCellTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 287820920999697514L;
	protected CellAttribute cellAtt;
	protected JTable table;

	public AttributiveCellTableModel() {
		this((Vector<?>)null, 0);
	}

	public AttributiveCellTableModel(int numRows, int numColumns) {
		Vector<Object> names = new Vector<Object>(numColumns);
		names.setSize(numColumns);
		//setColumnIdentifiers(names);
		this.columnIdentifiers = names;
		dataVector = new Vector<>();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows,numColumns);
	}

	public AttributiveCellTableModel(Vector<?> columnNames, int numRows) {
		//setColumnIdentifiers(columnNames);
		this.columnIdentifiers = columnNames != null ? columnNames : new Vector<Object>(0);
		dataVector = new Vector<>();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows, columnNames != null ? columnNames.size() : 0);
	}

	public AttributiveCellTableModel(Object[] columnNames, int numRows) {
		this(convertToVector(columnNames), numRows);
	}

	public AttributiveCellTableModel(Vector<Object> data, Vector<Object> columnNames) {
		setDataVector(data, columnNames);
	}

	public AttributiveCellTableModel(Object[][] data, Object[] columnNames) {
		setDataVector(data, columnNames);
	}

	public void setTable(JTable table) {
		this.table = table;
		if(cellAtt instanceof CellSpan) {
			((CellSpan)cellAtt).setTable(table);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setDataVector(Vector newData, Vector columnNames) {
		if (newData == null)
			throw new IllegalArgumentException(
					"setDataVector() - Null parameter");
		dataVector = new Vector<>();
		// setColumnIdentifiers(columnNames);
		this.columnIdentifiers = columnNames;
		dataVector = newData;

		cellAtt = new DefaultCellAttribute(dataVector.size(),
				columnIdentifiers.size());
		if(cellAtt instanceof CellSpan) {
			((CellSpan)cellAtt).setTable(table);
		}

		newRowsAdded(new TableModelEvent(this, 0, getRowCount() - 1,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addColumn(Object columnName, Vector columnData) {
		if (columnName == null)
			throw new IllegalArgumentException("addColumn() - null parameter");
		columnIdentifiers.addElement(columnName);
		int index = 0;
		Enumeration<?> eeration = dataVector.elements();
		while (eeration.hasMoreElements()) {
			Object value;
			if ((columnData != null) && (index < columnData.size()))
				value = columnData.elementAt(index);
			else
				value = null;
			eeration.nextElement();
			((Vector<Object>)eeration.nextElement()).addElement(value);
			index++;
		}

		cellAtt.addColumn();

		fireTableStructureChanged();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addRow(Vector rowData) {
		if (rowData == null) {
			rowData = new Vector(getColumnCount());
		} else {
			rowData.setSize(getColumnCount());
		}
		dataVector.addElement(rowData);
		cellAtt.addRow();

		newRowsAdded(new TableModelEvent(this, getRowCount()-1, getRowCount()-1,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertRow(int row, Vector rowData) {
		if (rowData == null) {
			rowData = new Vector(getColumnCount());
		} else {
			rowData.setSize(getColumnCount());
		}
		dataVector.insertElementAt(rowData, row);
		cellAtt.insertRow(row);

		newRowsAdded(new TableModelEvent(this, row, row,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));

		if(table != null) {
			int[] rows = table.getSelectedRows();
			int[] cols = table.getSelectedColumns();
			table.setRowSelectionInterval(rows[1], rows[rows.length-1]);
			table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
		}
	}

	@Override
    public void removeRow(int row) {
		int[] rows = table != null ? table.getSelectedRows() : null;
		int[] cols = table != null ? table.getSelectedColumns() : null;

        dataVector.removeElementAt(row);
        cellAtt.removeRow(row);
        fireTableRowsDeleted(row, row);

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

	public CellAttribute getCellAttribute() {
		return cellAtt;
	}

	public void setCellAttribute(CellAttribute newCellAtt) {
		int numColumns = getColumnCount();
		int numRows    = getRowCount();
		if ((newCellAtt.getSize().width  != numColumns) ||
				(newCellAtt.getSize().height != numRows)) {
			newCellAtt.setSize(new Dimension(numRows, numColumns));
		}
		cellAtt = newCellAtt;
		if(cellAtt instanceof CellSpan) {
			((CellSpan)cellAtt).setTable(table);
		}
		fireTableDataChanged();
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
