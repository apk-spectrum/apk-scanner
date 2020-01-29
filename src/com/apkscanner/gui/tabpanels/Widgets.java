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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.data.apkinfo.WidgetInfo;
import com.apkspectrum.swing.ImageScaler;

public class Widgets extends AbstractTabbedPanel implements TreeSelectionListener, ListSelectionListener
{
	private static final long serialVersionUID = 4881638983501664860L;

	private JTable table;
	private SimpleTableModel tableModel;
	private ResourceTree resTree;
	private ResourceContentsPanel contentPanel;

	private String apkFilePath;

	public Widgets() {
		setLayout(new GridLayout(1, 0));
		setTitle(RComp.TABBED_WIDGET);
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		table = new JTable(tableModel = new WidgetTableModel(),
				new SimpleTableColumnModel(100, 60, 68, 240, 15, 40));
		table.setAutoCreateColumnsFromModel(true);
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
		table.setRowHeight(100);
		table.getSelectionModel().addListSelectionListener(this);

		resTree = new ResourceTree(null);
		resTree.addTreeSelectionListener(this);
		//resTree.setRootVisible(false);

		contentPanel = new ResourceContentsPanel(null);

		JSplitPane resPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		resPane.setLeftComponent(new JScrollPane(resTree));
		resPane.setRightComponent(contentPanel);
		resPane.setDividerLocation(200);

		JScrollPane scrollPane = new JScrollPane(table);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollPane, resPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(15);
        splitPane.setDividerLocation(230);
        splitPane.setResizeWeight(0.5);

		add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, int status)
	{
		if(ApkScanner.STATUS_WIDGET_COMPLETED != status) return;

		if(tableModel == null)
			initialize();
		tableModel.setData(apkInfo);
		contentPanel.setData(apkInfo);

		if(tableModel.getRowCount() > 0) {
			table.addRowSelectionInterval(0, 0);
		}

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

			apkFilePath = apkInfo.filePath;

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

				String label = ApkInfoHelper.getResourceValue(w.labels, preferLang);
				if(label == null) label = ApkInfoHelper.getResourceValue(apkInfo.manifest.application.labels, preferLang);
				String enabled = (w.enabled == null || w.enabled) ? "O" : "X";
				Object[] temp = { previewImage , label, w.size, w.tartget != null ? w.tartget : w.name, enabled, w.type, w };
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

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if(!e.isAddedPath()) return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				resTree.getLastSelectedPathComponent();
		if(node == null) return;
		contentPanel.selectContent(node.getUserObject());
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) return;
		if(tableModel.getRowCount() == 0) return;
		if(table.getSelectedRow() > -1) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			WidgetInfo info = (WidgetInfo) tableModel.getValueAt(row, 6);
			resTree.addTreeNodes(apkFilePath, info);
		}
	}
}