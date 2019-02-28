package com.apkscanner.gui.easymode.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import com.apkscanner.core.permissionmanager.PermissionGroupInfoExt;
import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.gui.easymode.test.ColumnGroup;
import com.apkscanner.gui.easymode.test.headtable.GroupableTableHeader;
import com.apkscanner.resource.Resource;

public class EasyPermissionDlg extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final int USES_TABLE = 0;
	private final int DECLARED_TABLE = 1;
	private final String[] subname = { "Uses", "Declared" };

	JButton btngetDeviceGranted;
	JLabel sdkversion;

	JTabbedPane tabbedpane;

	private JTable[] permissiontable = { null, null };

	// ArrayList<String> protectionLevelCalumn = new ArrayList<String>();

	private String apkFilePath;
	private TableRowSorter<PermissionUsesTableModel> sorter;

	// Arrays.asList(data));

	PermissionUsesTableModel[] model = { null, null };

	public EasyPermissionDlg(Frame frame, boolean modal, ApkInfo apkInfo) {
		super(frame, Resource.STR_BASIC_PERMISSIONS.getString(), modal);
		this.setSize(700, 700);
		// this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(frame);
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		PermissionManager permissionManager = new PermissionManager();
		permissionManager.setTreatSignAsRevoked((boolean) Resource.PROP_PERM_TREAT_SIGN_AS_REVOKED.getData());
		permissionManager.addUsesPermission(apkInfo.manifest.usesPermission);
		permissionManager.addUsesPermission(apkInfo.manifest.usesPermissionSdk23);
		if(!permissionManager.isEmpty()) {
			Integer selectSdkVer = apkInfo.manifest.usesSdk.targetSdkVersion;
			permissionManager.setSdkVersion(selectSdkVer != null ? selectSdkVer : 28);
		}

		model[USES_TABLE] = new PermissionUsesTableModel();
		makeusesPermissionRow(permissionManager, permissionManager.getPermissions(), model[USES_TABLE]);

		permissionManager.clearPermissions();
		permissionManager.addDeclarePemission(apkInfo.manifest.permission);
		model[DECLARED_TABLE] = new PermissionUsesTableModel();
		makeusesPermissionRow(permissionManager, permissionManager.getDeclarePermissions(), model[DECLARED_TABLE]);
		tabbedpane = new JTabbedPane();
		int i = 0;
		for (JTable table : permissiontable) {
			table = new JTable(model[i]) {
				protected JTableHeader createDefaultTableHeader() {
					return new GroupableTableHeader(columnModel);
				}
			};
			sorter = new TableRowSorter<PermissionUsesTableModel>(model[i]);

			makeProtectionColumn(table);
			table.setRowSorter(sorter);
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.setRowHeight(36);
			table.setDefaultRenderer(String.class, new TableColorRenderer());
			setColumnWidths(table, 36, 350);
			JScrollPane scrollPane = new JScrollPane(table);

			tabbedpane.addTab(subname[i] + "(" + table.getRowCount() + ")", null, scrollPane, subname[i]);
			if (table.getRowCount() == 0) {
				tabbedpane.setEnabledAt(i, false);
			}
			i++;
		}

		add(tabbedpane, BorderLayout.CENTER);

		JPanel temppanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		btngetDeviceGranted = new JButton("permission granted on device");
		sdkversion = new JLabel("@SDK 27");
		temppanel.add(btngetDeviceGranted);
		temppanel.add(sdkversion);

		add(temppanel, BorderLayout.NORTH);

		this.setVisible(true);
	}

	private void makeusesPermissionRow(PermissionManager manager, PermissionInfo[] arraypermissionInfo,
			PermissionUsesTableModel tablemodel) {
		if (arraypermissionInfo == null) return;

		for (PermissionInfo info : arraypermissionInfo) {
			PermissionGroupInfoExt group = manager.getPermissionGroup(info.permissionGroup);
			try {
				Object[] obj = new Object[tablemodel.getColumnCount()];
				String[] permissions = info.protectionLevel.split("\\|");
				int j = 0;
				obj[j++] = group != null ? new ImageIcon(new URL(group.getIconPath())) {
					public String toString() {
						return "";
					}
				} : null;
				obj[j++] = info.name;
				int i = 0;

				obj[j + i++] = permissions[0];
				String Flagstr = "";
				for (int k = 1; k < permissions.length; k++) {
					Flagstr += permissions[k] + ((k == permissions.length - 1) ? "" : " | ");
				}

				obj[j + i++] = Flagstr;

				obj[j + i] = new Boolean(true);
				tablemodel.add(obj);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	public void setColumnWidths(JTable table, int... widths) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < widths.length; i++) {
			if (i < columnModel.getColumnCount()) {
				switch (i) {
				case 0:
					columnModel.getColumn(i).setMaxWidth(widths[i]);
					break;
				default:

				}
				columnModel.getColumn(i).setMinWidth(widths[i]);
				columnModel.getColumn(i).setWidth(widths[i]);
			} else
				break;
		}
	}

	private void makeProtectionColumn(JTable table) {
		final int START_PROTECTION_INDEX = 2;
		TableColumnModel cm = table.getColumnModel();
		ColumnGroup g_protection = new ColumnGroup("Protection Level");

		for (int i = 0; i < 2; i++) {
			g_protection.add(cm.getColumn(START_PROTECTION_INDEX + i));
		}

		GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
		header.addColumnGroup(g_protection);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(btngetDeviceGranted)) {

		}
	}

	class TableColorRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			// CustomExcel temp =
			// table.gettabletableModel.getTableDataList().get(table.convertRowIndexToModel(row));

			int convertrow = table.convertRowIndexToModel(row);

			String str = table.getModel().getValueAt(convertrow, 2).toString();

			if (str.indexOf("dangerous") > -1) {
				c.setForeground(Color.RED);
			} else if (str.indexOf("signature") > -1) {
				c.setForeground(Color.BLUE);
			} else {
				c.setForeground(Color.BLACK);
			}

			return c;
		}
	}

	class PermissionUsesTableModel extends AbstractTableModel {
		private Object[] columnNames = { "Icon", "Name", "Base", "Flag", "granted" };
		// final String[] protectionLevelCalumn = {"Icon", "Name",
		// "Base","Flag",
		// "granted"};
		ArrayList<Object[]> usesTableData = new ArrayList<Object[]>();

		PermissionUsesTableModel() {
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public void add(Object[] obj) {
			usesTableData.add(obj);
		}

		public int getRowCount() {
			return usesTableData.size();
		}

		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		public Object getValueAt(int row, int col) {

			return usesTableData.get(row)[col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			usesTableData.get(row)[col] = value;
			fireTableCellUpdated(row, col);
		}
	}
}
