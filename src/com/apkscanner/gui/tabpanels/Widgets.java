package com.apkscanner.gui.tabpanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.WidgetInfo;
import com.apkscanner.gui.component.ImageScaler;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;

/**
 * TableToolTipsDemo is just like TableDemo except that it sets up tool tips for
 * both cells and column headers.
 */
public class Widgets extends AbstractTabbedPanel
{
	private static final long serialVersionUID = 4881638983501664860L;

	private MyTableModel tableModel = null;
	private JTable table = null;
	private ArrayList<Object[]> arrWidgets = new ArrayList<Object[]>();

	public Widgets() {
		setLayout(new GridLayout(1, 0));
		setTitle(RStr.TAB_WIDGETS.get(), RStr.TAB_WIDGETS.get());
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		table = new JTable(tableModel = new MyTableModel());
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());

		setJTableColumnsWidth(table, 500, 20, 15, 17, 60, 3, 10);

		add(new JScrollPane(table));
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status)
	{
		if(!Status.WIDGET_COMPLETED.equals(status)) return;

		arrWidgets.clear();
		if(apkInfo.widgets == null) return;
		if(tableModel == null) initialize();

		String preferLang = RProp.S.PREFERRED_LANGUAGE.get();
		for(WidgetInfo w: apkInfo.widgets) {
			ImageIcon myimageicon = null;
			try {
				ResourceInfo[] icons = w.icons;
				String icon = icons[icons.length-1].name;
				if(icon.toLowerCase().endsWith(".webp")) {
					myimageicon = new ImageIcon(ImageIO.read(new URL(icon)));
				} else {
					myimageicon = new ImageIcon(new URL(icon));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(myimageicon != null) {
				myimageicon.setImage(ImageScaler.getMaintainAspectRatioImage(myimageicon,100,100));
			}

			String label = ApkInfoHelper.getResourceValue(w.lables, preferLang);
			if(label == null) label = ApkInfoHelper.getResourceValue(apkInfo.manifest.application.labels, preferLang);
			String enabled = (w.enabled == null || w.enabled) ? "O" : "X";
			Object[] temp = { myimageicon , label, w.size, w.name, enabled, w.type};
			arrWidgets.add(temp);
		}

		tableModel.fireTableDataChanged();
		for(int i=0; i< arrWidgets.size(); i++) {
			table.setRowHeight(i, 100);
		}

		setDataSize(apkInfo.widgets.length, true, false);
		setTabbedVisible(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX);
	}

	@Override
	public void reloadResource()
	{
		setTitle(RStr.TAB_WIDGETS.get(), RStr.TAB_WIDGETS.get());

		if(tableModel == null) return;
		tableModel.loadResource();
		tableModel.fireTableStructureChanged();
		setJTableColumnsWidth(table, 500, 20,15,17,60,10);
		for(int i=0; i< arrWidgets.size(); i++) {
			table.setRowHeight(i, 100);
		}
	}

	public static void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
												double... percentages) {
		double total = 0;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
		}

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
		}
	}

	class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 2567370181372859791L;

		private String[] columnNames = null;

		MyTableModel() {
			loadResource();
		}

		public void loadResource() {
			columnNames = new String[] {
				RStr.WIDGET_COLUMN_IMAGE.get(),
				RStr.WIDGET_COLUMN_LABEL.get(),
				RStr.WIDGET_COLUMN_SIZE.get(),
				RStr.WIDGET_COLUMN_ACTIVITY.get(),
				RStr.WIDGET_COLUMN_ENABLED.get(),
				RStr.WIDGET_COLUMN_TYPE.get(),
			};
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return arrWidgets.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return arrWidgets.get(row)[col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class<? extends Object> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			if(col>0) {
				return true;
			} else return false;
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {
			arrWidgets.get(row)[col] = value;
			fireTableCellUpdated(row, col);
		}
	}

	class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
		private static final long serialVersionUID = -4421652692115836378L;

		public MultiLineCellRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setBackground(Color.LIGHT_GRAY);
			} else {
				setBackground(Color.WHITE);
			}
			setFont(table.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}
}