package com.apkscanner.gui.tabpanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.WidgetInfo;
import com.apkscanner.gui.component.ImageScaler;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;

public class Widgets extends AbstractTabbedPanel
{
	private static final long serialVersionUID = 4881638983501664860L;

	private SimpleTableModel tableModel;

	public Widgets() {
		setLayout(new GridLayout(1, 0));
		setTitle(RComp.TABBED_WIDGET);
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		JTable table = new JTable(tableModel = new WidgetTableModel(),
				new SimpleTableColumnModel(100, 60, 68, 240, 15, 40));
		table.setAutoCreateColumnsFromModel(true);
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
		table.setRowHeight(100);

		add(new JScrollPane(table));
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status)
	{
		if(!Status.WIDGET_COMPLETED.equals(status)) return;

		if(tableModel == null)
			initialize();
		tableModel.setData(apkInfo);

		setDataSize(apkInfo.widgets.length, true, false);
		setTabbedVisible(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX);
	}

	class WidgetTableModel extends SimpleTableModel {
		private static final long serialVersionUID = 2567370181372859791L;

		WidgetTableModel() {
			super(	RStr.WIDGET_COLUMN_IMAGE,
					RStr.WIDGET_COLUMN_LABEL,
					RStr.WIDGET_COLUMN_SIZE,
					RStr.WIDGET_COLUMN_ACTIVITY,
					RStr.WIDGET_COLUMN_ENABLED,
					RStr.WIDGET_COLUMN_TYPE);
		}

		@Override
		public void setData(ApkInfo apkInfo) {
			data.clear();
			if(apkInfo.widgets == null) return;

			String preferLang = RProp.S.PREFERRED_LANGUAGE.get();
			for(WidgetInfo w: apkInfo.widgets) {
				ImageIcon previewImage = null;
				try {
					ResourceInfo[] icons = w.icons;
					String icon = icons[icons.length-1].name;
					if(icon.toLowerCase().endsWith(".webp")) {
						previewImage = new ImageIcon(ImageIO.read(new URL(icon)));
					} else {
						previewImage = new ImageIcon(new URL(icon));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(previewImage != null) {
					previewImage.setImage(ImageScaler.getMaintainAspectRatioImage(previewImage,100,100));
				}

				String label = ApkInfoHelper.getResourceValue(w.lables, preferLang);
				if(label == null) label = ApkInfoHelper.getResourceValue(apkInfo.manifest.application.labels, preferLang);
				String enabled = (w.enabled == null || w.enabled) ? "O" : "X";
				Object[] temp = { previewImage , label, w.size, w.name, enabled, w.type};
				data.add(temp);
			}

			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int row, int col) { return col > 0; }
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
			setText(Objects.toString(value, ""));
			return this;
		}
	}
}