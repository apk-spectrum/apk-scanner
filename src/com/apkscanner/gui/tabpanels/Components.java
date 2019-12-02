package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ActivityAliasInfo;
import com.apkscanner.data.apkinfo.ActivityInfo;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ProviderInfo;
import com.apkscanner.data.apkinfo.ReceiverInfo;
import com.apkscanner.data.apkinfo.ServiceInfo;
import com.apkscanner.resource.RStr;
import com.apkscanner.util.Log;

public class Components extends AbstractTabbedPanel
{
	private static final long serialVersionUID = 8325900007802212630L;

	private JTextArea textArea;
	private JPanel IntentPanel;
	private JLabel IntentLabel;

	private MyTableModel TableModel = null;
	private JTable table = null;

	public ArrayList<Object[]> ComponentList = new ArrayList<Object[]>();

	public Components() {
		setLayout(new GridLayout(1, 0));
		setName(RStr.TAB_COMPONENTS.get());
		setToolTipText(RStr.TAB_COMPONENTS.get());
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		TableModel = new MyTableModel();
		table = new JTable(TableModel) {
			private static final long serialVersionUID = 1340713167587523626L;

			public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
			 Component c = super.prepareRenderer(tcr, row, column);
			 Color temp = null;

			 if(isRowSelected(row)) {
		          //c.setForeground(getSelectionForeground());
		          c.setBackground(Color.GRAY);
		        }else{
		        	String type = (String) ComponentList.get(row)[1];
					if("activity".equals(type) || "main".equals(type)) {
						temp = new Color(0xB7F0B1);
					} else if("launcher".equals(type)) {
						temp = new Color(0x5D9657);
					} else if("activity-alias".equals(type)) {
						temp = new Color(0x96E2E2);
					} else if("service".equals(type)) {
						temp = new Color(0xB2CCFF);
					} else if("receiver".equals(type)) {
						temp = new Color(0xCEF279);
					} else if("provider".equals(type)) {
						temp = new Color(0xFFE08C);
					} else {
						temp = new Color(0xC8C8C8);
					}
					c.setBackground(temp);
		        }
		        return c;
		      }
		};

		ListSelectionModel cellSelectionModel = table.getSelectionModel();

		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(ComponentList == null) return;
				if(table.getSelectedRow() > -1) {
					textArea.setText((String) ComponentList.get(table.getSelectedRow())[6]);
					textArea.setCaretPosition(0);
				}
			}
		});

		setJTableColumnsWidth(table, 500, 62, 10, 7, 7, 7, 7);
		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scrollPane2 = new JScrollPane(textArea);
		//scrollPane2.setPreferredSize(new Dimension(300, 500));

		IntentPanel = new JPanel();
		IntentLabel = new JLabel(RStr.ACTIVITY_LABEL_INTENT.get());

		IntentPanel.setLayout(new BorderLayout());

		//IntentLabel.setPreferredSize(new Dimension(300, 100));
		IntentPanel.add(IntentLabel, BorderLayout.NORTH);
		IntentPanel.add(scrollPane2, BorderLayout.CENTER);

		//Add the scroll pane to this panel.
		//add(scrollPane);
		//add(IntentPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        splitPane.setTopComponent(scrollPane);
        splitPane.setBottomComponent(IntentPanel);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);
        IntentPanel.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200);
        //splitPane.setPreferredSize(new Dimension(500, 500));


        add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status)
	{
		if(!Status.ACTIVITY_COMPLETED.equals(status)) {
			return;
		}

		if(TableModel == null)
			initialize();
		ComponentList.clear();

		if(apkInfo.manifest.application.activity != null) {
			for(ActivityInfo info: apkInfo.manifest.application.activity) {
				String type = null;
				if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0 && (info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
					type = "launcher";
				} else if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
					type = "main";
				} else if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
					Log.w("set launcher flag, but not main");
					type = "activity";
				} else {
					type = "activity";
				}
				String startUp = (info.featureFlag & ApkInfo.APP_FEATURE_STARTUP) != 0 ? "O" : "X";
				String enabled = (info.enabled == null) || info.enabled ? "O" : "X";
				String exported = null;
				if(info.exported == null) {
					exported = info.intentFilter != null && info.intentFilter.length > 0 ? "O" : "X";
				} else {
					exported = info.exported ? "O" : "X";
				}
				String permission = info.permission != null ? "O" : "X";
				if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
					ComponentList.add(0, new Object[] {info.name, type, enabled, exported, permission, startUp, info.getReport()});
				} else {
					ComponentList.add(new Object[] {info.name, type, enabled, exported, permission, startUp, info.getReport()});
				}
			}
		}
		if(apkInfo.manifest.application.activityAlias != null) {
			for(ActivityAliasInfo info: apkInfo.manifest.application.activityAlias) {
				String type = null;
				if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0 && (info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
					type = "launcher-alias";
				} else if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
					type = "main-alias";
				} else if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
					Log.w("set launcher flag, but not main");
					type = "activity-alias";
				} else {
					type = "activity-alias";
				}
				String startUp = (info.featureFlag & ApkInfo.APP_FEATURE_STARTUP) != 0 ? "O" : "X";
				String enabled = (info.enabled == null) || info.enabled ? "O" : "X";
				String exported = null;
				if(info.exported == null) {
					exported = info.intentFilter != null && info.intentFilter.length > 0 ? "O" : "X";
				} else {
					exported = info.exported ? "O" : "X";
				}
				String permission = info.permission != null ? "O" : "X";
				if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
					int i = 0;
					for(;i<ComponentList.size();i++) {
						String t = (String)((Object[])ComponentList.get(i))[1];
						if(t == null || !t.equals("launcher")) break;
					}
					ComponentList.add(i, new Object[] {info.name, type, enabled, exported, permission, startUp, info.getReport()});
				} else {
					ComponentList.add(new Object[] {info.name, type, enabled, exported, permission, startUp, info.getReport()});
				}
			}
		}
		if(apkInfo.manifest.application.service != null) {
			for(ServiceInfo info: apkInfo.manifest.application.service) {
				String startUp = (info.featureFlag & ApkInfo.APP_FEATURE_STARTUP) != 0 ? "O" : "X";
				String enabled = (info.enabled == null) || info.enabled ? "O" : "X";
				String exported = null;
				if(info.exported == null) {
					exported = info.intentFilter != null && info.intentFilter.length > 0 ? "O" : "X";
				} else {
					exported = info.exported ? "O" : "X";
				}
				String permission = info.permission != null ? "O" : "X";
				ComponentList.add(new Object[] {info.name, "service", enabled, exported, permission, startUp, info.getReport()});
			}
		}
		if(apkInfo.manifest.application.receiver != null) {
			for(ReceiverInfo info: apkInfo.manifest.application.receiver) {
				String startUp = (info.featureFlag & ApkInfo.APP_FEATURE_STARTUP) != 0 ? "O" : "X";
				String enabled = (info.enabled == null) || info.enabled ? "O" : "X";
				String exported = null;
				if(info.exported == null) {
					exported = info.intentFilter != null && info.intentFilter.length > 0 ? "O" : "X";
				} else {
					exported = info.exported ? "O" : "X";
				}
				String permission = info.permission != null ? "O" : "X";
				ComponentList.add(new Object[] {info.name, "receiver", enabled, exported, permission, startUp, info.getReport()});
			}
		}
		if(apkInfo.manifest.application.provider != null) {
			for(ProviderInfo info: apkInfo.manifest.application.provider) {
				String startUp = "X";
				String enabled = (info.enabled == null) || info.enabled ? "O" : "X";
				String exported = null;
				if(info.exported == null) {
					Integer target = apkInfo.manifest.usesSdk.targetSdkVersion;
					if(target == null) target = apkInfo.manifest.usesSdk.minSdkVersion;
					exported = (target == null || target < 17) ? "O" : "X";
				} else {
					exported = info.exported ? "O" : "X";
				}
				String permission = "X";
				if(info.permission != null || (info.readPermission != null && info.writePermission != null)) {
					permission = "R/W";
				} else if(info.readPermission != null) {
					permission = "Read";
				} else if(info.writePermission != null) {
					permission = "Write";
				}
				ComponentList.add(new Object[] {info.name, "provider", enabled, exported, permission, startUp, info.getReport()});
				//String startUp = (info.featureFlag & ActivityInfo.ACTIVITY_FEATURE_STARTUP) != 0 ? "O" : "X";
			}
		}

		//ActivityList.addAll(apkInfo.ActivityList);
		TableModel.fireTableDataChanged();

		setDataSize(ApkInfoHelper.getComponentCount(apkInfo), true, false);
		sendRequest(SEND_REQUEST_CURRENT_ENABLED);

		setTabbedVisible(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX);
		sendRequest(SEND_REQUEST_CURRENT_VISIBLE);
	}

	@Override
	public void reloadResource()
	{
		setName(RStr.TAB_COMPONENTS.get());
		setToolTipText(RStr.TAB_COMPONENTS.get());
		sendRequest(SEND_REQUEST_CHANGE_TITLE);

		if(TableModel == null) return;
		TableModel.loadResource();
		TableModel.fireTableStructureChanged();
		setJTableColumnsWidth(table, 500, 62, 10, 7, 7, 7, 7);
		IntentLabel.setText(RStr.ACTIVITY_LABEL_INTENT.get());
	}

	public void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
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
		private static final long serialVersionUID = 5291910634830167294L;

		private String[] columnNames = null;

		public MyTableModel()
		{
			loadResource();
		}

		public void loadResource()
		{
			columnNames = new String[] {
				RStr.ACTIVITY_COLUME_CLASS.get(),
				RStr.ACTIVITY_COLUME_TYPE.get(),
				RStr.ACTIVITY_COLUME_ENABLED.get(),
				RStr.ACTIVITY_COLUME_EXPORT.get(),
				RStr.ACTIVITY_COLUME_PERMISSION.get(),
				RStr.ACTIVITY_COLUME_STARTUP.get()
			};
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return ComponentList.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return ComponentList.get(row)[col];
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
			return true;
		}
	}
}