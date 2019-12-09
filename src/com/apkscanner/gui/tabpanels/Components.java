package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ActivityAliasInfo;
import com.apkscanner.data.apkinfo.ActivityInfo;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.data.apkinfo.ProviderInfo;
import com.apkscanner.data.apkinfo.ReceiverInfo;
import com.apkscanner.data.apkinfo.ServiceInfo;
import com.apkscanner.resource.RStr;
import com.apkscanner.util.Log;

public class Components extends AbstractTabbedPanel
{
	private static final long serialVersionUID = 8325900007802212630L;

	private RSyntaxTextArea xmltextArea;
	private JPanel intentPanel;
	private JLabel intentLabel;

	private MyTableModel tableModel = null;
	private JTable table = null;

	public ArrayList<Object[]> componentList = new ArrayList<Object[]>();

	public Components() {
		setLayout(new GridLayout(1, 0));
		setTitle(RStr.TAB_COMPONENTS.get(), RStr.TAB_COMPONENTS.get());
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		tableModel = new MyTableModel();
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1340713167587523626L;

			public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
			 Component c = super.prepareRenderer(tcr, row, column);
			 Color temp = null;

			 if(isRowSelected(row)) {
		          //c.setForeground(getSelectionForeground());
		          c.setBackground(Color.GRAY);
		        }else{
		        	String type = (String) componentList.get(row)[1];
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
				if(componentList == null || componentList.isEmpty()) return;
				if(table.getSelectedRow() > -1) {
					xmltextArea.setText((String) componentList.get(table.getSelectedRow())[6]);
					xmltextArea.setCaretPosition(0);
				}
			}
		});

		setJTableColumnsWidth(table, 500, 62, 10, 7, 7, 7, 7);
		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);


		xmltextArea  = new RSyntaxTextArea();
		//xmltextArea.createToolTip();

		JPanel textAreaPanel = new JPanel(new BorderLayout());

		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmltextArea.setCodeFoldingEnabled(true);
		xmltextArea.setMarkOccurrences(true);
		xmltextArea.setEditable(false);
		RTextScrollPane sp = new RTextScrollPane(xmltextArea);

		textAreaPanel.add(sp);

		//ErrorStrip errorStrip = new ErrorStrip(xmltextArea);
		//textAreaPanel.add(errorStrip,BorderLayout.LINE_END);

		intentPanel = new JPanel();
		intentLabel = new JLabel(RStr.ACTIVITY_LABEL_INTENT.get());

		intentPanel.setLayout(new BorderLayout());

		//IntentLabel.setPreferredSize(new Dimension(300, 100));
		intentPanel.add(intentLabel, BorderLayout.NORTH);
		intentPanel.add(textAreaPanel, BorderLayout.CENTER);

		//Add the scroll pane to this panel.
		//add(scrollPane);
		//add(IntentPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        splitPane.setTopComponent(scrollPane);
        splitPane.setBottomComponent(intentPanel);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);
        intentPanel.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200);
        //splitPane.setPreferredSize(new Dimension(500, 500));


        add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status)
	{
		if(!Status.ACTIVITY_COMPLETED.equals(status)) return;

		if(tableModel == null) initialize();

		componentList.clear();

		add(apkInfo.manifest.application.activity, apkInfo);
		add(apkInfo.manifest.application.activityAlias, apkInfo);
		add(apkInfo.manifest.application.service, apkInfo);
		add(apkInfo.manifest.application.receiver, apkInfo);
		add(apkInfo.manifest.application.provider, apkInfo);

		componentList.sort(new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				int type = getTypePriority(o1[1]) - getTypePriority(o2[1]);
				if(type != 0) return type;
				return ((String)o1[0]).compareToIgnoreCase((String)o2[0]);
			}

			private int getTypePriority(Object type) {
				switch((String) type) {
				case "launcher":		return 0;
				case "launcher-alias":	return 1;
				case "main":			return 2;
				case "activity":		return 3;
				case "main-alias":		return 4;
				case "activity-alias":	return 5;
				case "service":			return 6;
				case "receiver":		return 7;
				case "provider":		return 8;
				default:				return 9;
				}
			}
		});

		tableModel.fireTableDataChanged();
		table.getSelectionModel().setSelectionInterval(0, 0);

		setDataSize(ApkInfoHelper.getComponentCount(apkInfo), true, false);
		setTabbedVisible(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX);
	}

	private void add(ComponentInfo[] infoList, ApkInfo apkInfo) {
		if(infoList == null) return;

		for(ComponentInfo info: infoList) {
			String type = getType(info);
			String startUp = !(info instanceof ProviderInfo)
					&& (info.featureFlag & ApkInfo.APP_FEATURE_STARTUP) != 0 ? "O" : "X";
			String enabled = (info.enabled == null) || info.enabled ? "O" : "X";
			String exported = null;
			if(info.exported == null) {
				if(info instanceof ProviderInfo) {
					Integer target = apkInfo.manifest.usesSdk.targetSdkVersion;
					if(target == null) target = apkInfo.manifest.usesSdk.minSdkVersion;
					exported = (target == null || target < 17) ? "O" : "X";
				} else {
					exported = info.intentFilter != null && info.intentFilter.length > 0 ? "O" : "X";
				}
			} else {
				exported = info.exported ? "O" : "X";
			}
			String permission = null;
			if(info instanceof ProviderInfo) {
				ProviderInfo pInfo = (ProviderInfo) info;
				if(info.permission != null || (pInfo.readPermission != null && pInfo.writePermission != null)) {
					permission = "R/W";
				} else if(pInfo.readPermission != null) {
					permission = "Read";
				} else if(pInfo.writePermission != null) {
					permission = "Write";
				} else {
					permission = "X";
				}
			} else {
				permission = info.permission != null ? "O" : "X";
			}
			componentList.add(new Object[] {info.name, type, enabled, exported, permission, startUp, info.xmlString});
		}
	}

	private String getType(ComponentInfo info) {
		String type = null;
		if(info instanceof ActivityInfo || info instanceof ActivityAliasInfo) {
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
			if(info instanceof ActivityAliasInfo) {
				type += "-alias";
			}
		} else if(info instanceof ServiceInfo) {
			type = "service";
		} else if(info instanceof ReceiverInfo) {
			type = "receiver";
		} else if(info instanceof ProviderInfo) {
			type = "provider";
		} else {
			type = "unknown";
		}
		return type;
	}

	@Override
	public void reloadResource()
	{
		setTitle(RStr.TAB_COMPONENTS.get(), RStr.TAB_COMPONENTS.get());

		if(tableModel == null) return;
		tableModel.loadResource();
		tableModel.fireTableStructureChanged();
		setJTableColumnsWidth(table, 500, 62, 10, 7, 7, 7, 7);
		intentLabel.setText(RStr.ACTIVITY_LABEL_INTENT.get());
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
			return componentList.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return componentList.get(row)[col];
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