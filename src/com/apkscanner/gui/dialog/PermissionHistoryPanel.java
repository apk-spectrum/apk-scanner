package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.apkscanner.core.permissionmanager.PermissionGroupInfoExt;
import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.core.permissionmanager.PermissionRepository.SourceCommit;
import com.apkscanner.core.permissionmanager.UnitInformation;
import com.apkscanner.core.permissionmanager.UnitRecord;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.gui.component.CloseableTabbedPaneLayerUI;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.gui.component.table.AttributiveCellTableModel;
import com.apkscanner.gui.component.table.CellSpan;
import com.apkscanner.gui.component.table.MultiSpanCellTable;
import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.gui.util.WindowSizeMemorizer;
import com.apkscanner.resource.Resource;

public class PermissionHistoryPanel extends JPanel implements ItemListener, ListSelectionListener, ActionListener {
	private static final long serialVersionUID = -3567803690045423840L;

	private static final String DIFF_FORMAT = "<html><body><font style=\"color:red\">%s</font></body></html>";
	private static final int DIFF_PREFIX_LEN = "<html><body><font style=\"color:red\">".length();
	private static final int DIFF_SUFFIX_LEN = "</font></body></html>".length();

	private static final String[] GROUP_COLUMNS = new String[] { "API Level", "Action", "Priority", "Label", "Descripton", "Comment", "Request" };
	private static final String[] PERM_COLUMNS = new String[] { "API Level", "Action", "ProtectionLevel", "PermissionGroup", "Label", "Descripton", "Comment", "permissionFlags" };

	private JDialog dialog;

	private JComboBox<Integer> sdkVersions;
	private JCheckBox byGroup;
	private JCheckBox withLable;

	private JTable permTable;
	private AttributiveCellTableModel permTableModel;

	private JTable historyTable;
	private AttributiveCellTableModel historyTableModel;

	private JTextArea description;
	private JTabbedPane extraTabbedPanel;

	//private int tableLayout = 0;

	private PermissionManager permManager;
	private String[] historyTableHeader;

	public PermissionHistoryPanel() {
		setLayout(new GridBagLayout());

		//GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady)
		GridBagConstraints gridConst = new GridBagConstraints(0,0,1,1,1.0f,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,5,5,5),0,0);

		JPanel sdkSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sdkSelectPanel.add(new JLabel("SDK Ver. "));

		sdkVersions = new JComboBox<Integer>();
		final ListCellRenderer<? super Integer> oldRenderer = sdkVersions.getRenderer();
		sdkVersions.setRenderer(new ListCellRenderer<Integer>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel label = null;
				Component c = oldRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				label = c instanceof JLabel ? (JLabel) c : new JLabel();
				label.setText(value > 0 ? "API Level " + value : "API Levels");
				return label;
			}
		});
		sdkSelectPanel.add(sdkVersions);

		byGroup = new JCheckBox("by Group");
		byGroup.setSelected(true);
		byGroup.addItemListener(this);
		sdkSelectPanel.add(byGroup);

		withLable = new JCheckBox("with Label");
		withLable.setSelected(true);
		withLable.addItemListener(this);
		sdkSelectPanel.add(withLable);

		add(sdkSelectPanel, gridConst);

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(new JLabel("▶ Filter : All permission of used in Package"));

		gridConst.gridy++;
		add(filterPanel, gridConst);

		gridConst.gridy++;
		gridConst.weighty = 1.0f;
		gridConst.fill = GridBagConstraints.BOTH;

		permTableModel = new AttributiveCellTableModel() {
			private static final long serialVersionUID = -5182372671185877580L;

			@Override
		    public Class<?> getColumnClass(int columnIndex) {
				if(columnIndex == 0) return ImageIcon.class;
				return getValueAt(0, columnIndex).getClass();
		    }

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		permTable = new MultiSpanCellTable(permTableModel);

		permTable.setCellSelectionEnabled(false);
		permTable.setRowSelectionAllowed(true);
		permTable.setRowHeight(36);

		permTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		permTable.getSelectionModel().addListSelectionListener(this);
		permTable.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent mouseEvent) {
		        JTable table = (JTable) mouseEvent.getSource();
		        Point point = mouseEvent.getPoint();
		        int row = table.rowAtPoint(point);
		        int col = table.columnAtPoint(point);
		        if (row > -1 && table.getSelectedRow() != -1
		        		&& (mouseEvent.getClickCount() == 2 || col == 0)) {
	        		expandOrCollapse(row);
		        }
		    }
		});

		refreshPermTableStructure();

		JScrollPane scroll = new JScrollPane(permTable);
		scroll.setAutoscrolls(false);

		description = new JTextArea();
		description.setEditable(false);
		description.setLineWrap(true);

		historyTableModel = new AttributiveCellTableModel() {
			private static final long serialVersionUID = -5182372671185877580L;
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};
		historyTable = new MultiSpanCellTable(historyTableModel);
		historyTable.setCellSelectionEnabled(false);
		historyTable.setRowSelectionAllowed(true);
		//historyTable.setRowHeight(20);d
		historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//historyTable.getSelectionModel().addListSelectionListener(this);
		historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		historyTable.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent mouseEvent) {
		        JTable table = (JTable) mouseEvent.getSource();
		        Point point = mouseEvent.getPoint();
		        int row = table.rowAtPoint(point);
		        if (row > -1 && mouseEvent.getClickCount() == 2) {
		        	boolean withCtrl = (mouseEvent.getModifiersEx()
		        			& (InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) != 0;
		        	if(table.getSelectedRow() == -1) {
		        		if(!withCtrl) return;
		        		table.setRowSelectionInterval(row, row);
		        	}
		        	TableModel model = table.getModel();
		        	if(model instanceof DefaultTableModel) {
		        		addDescriptionTab((Vector<?>) ((DefaultTableModel)model).getDataVector().get(row), !withCtrl);
		        	}
		        }
		    }
		});
		KeyStrokeAction.registerKeyStrokeActions(historyTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, false)
			}, this);

		JScrollPane historyScroll = new JScrollPane(historyTable);
		historyScroll.setAutoscrolls(false);

		extraTabbedPanel = new JTabbedPane();
		String tabbedStyle = (String) Resource.PROP_TABBED_UI_THEME.getData();
		extraTabbedPanel.setOpaque(true);
		TabbedPaneUIManager.setUI(extraTabbedPanel, tabbedStyle);

		extraTabbedPanel.addTab("Description", new JScrollPane(description));
		extraTabbedPanel.addTab("History", historyScroll);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitPane.setTopComponent(scroll);
		splitPane.setBottomComponent(new JLayer<JTabbedPane>(extraTabbedPanel, new CloseableTabbedPaneLayerUI()));
		splitPane.setDividerLocation(300);

		add(splitPane, gridConst);
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		if(evt.getSource() instanceof JCheckBox) {
			refreshPermTableStructure();
			refreshPermTable();
		} else {
			if(evt.getStateChange() != ItemEvent.SELECTED) return;
			int sdkLevel = (int)evt.getItem();
			permManager.setSdkVersion(sdkLevel);
			refreshPermTable();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if(evt.getValueIsAdjusting()) return;

		int row = permTable.getSelectedRow();
		int col = permTable.getColumnModel().getColumnIndex("Name");
		if(row < 0 || col < 0) return;

		while(extraTabbedPanel.getTabCount() > 2) extraTabbedPanel.removeTabAt(2);

		String name = (String) permTableModel.getValueAt(row, col);
		boolean isGroup = byGroup.isSelected()
				&& !(permTableModel.getValueAt(row, 0) instanceof String);

		UnitRecord<?> record = null;
		if(isGroup) {
			record = permManager.getPermissionGroupRecord(name);
		} else {
			record = permManager.getPermissionRecord(name);
		}
		setDescription(record);
		setHistoryData(record);
	}

	private void setDescription(UnitRecord<?> record) {
		if(record == null) {
			description.setText("No have description");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(record.name);
			sb.append("  -  Added in API level ");
			sb.append(record.getAddedSdk());
			if(record.getDeprecatedSdk() > 0) {
				sb.append(", Deprecated in API level ");
				sb.append(record.getDeprecatedSdk());
			}
			if(record.getRemovedSdk() > 0) {
				sb.append(", Removed in API level ");
				sb.append(record.getRemovedSdk());
			}

			UnitInformation info = (UnitInformation) record.getInfomation(permManager.getSdkVersion());
			if(info != null) {
				String label = info.getLabel();
				String desc = info.getDescription();
				String comment = info.getNonLocalizedDescription();

				if(label != null) {
					sb.append("\n\n[Label] ");
					sb.append(label);
				}
				if(desc != null) {
					sb.append("\n\n[Description]\n");
					sb.append(desc);
				}
				if(comment != null && !comment.trim().isEmpty()) {
					sb.append("\n\n[Non Localized Description]\n");
					sb.append(comment);
				}
			}
			description.setText(sb.toString());
		}
		description.setCaretPosition(0);
	}

	private void setHistoryData(UnitRecord<?> record) {
		historyTableModel.setRowCount(0);
		if(record == null) {

		} else {
			boolean isGroupRecord = record.isPermissionGroupRecord();
			historyTableHeader = isGroupRecord ? GROUP_COLUMNS : PERM_COLUMNS;
			historyTableModel.setColumnIdentifiers(historyTableHeader);

			historyTable.getColumnModel().getColumn(1).setPreferredWidth(40);
			if(isGroupRecord) {
				historyTable.getColumnModel().getColumn(2).setPreferredWidth(40);
				historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
				historyTable.getColumnModel().getColumn(4).setPreferredWidth(200);
			} else {
				historyTable.getColumnModel().getColumn(2).setPreferredWidth(130);
				historyTable.getColumnModel().getColumn(3).setPreferredWidth(130);
			}

			int sdk = permManager.getSdkVersion();
			int selectRow = 0;
			UnitInformation[] tmp = (UnitInformation[]) record.getHistories();
			UnitInformation[] histories = Arrays.copyOf(tmp, tmp.length + 1);
			boolean diff = false;
			UnitInformation preInfo = null;
			for(UnitInformation info: histories) {
				if(preInfo != null) {
					Vector<Object> data = new Vector<>(10);
					int apiLevel = info != null ? info.getApiLevel() : record.addedSdk;
					if(info != null && apiLevel != record.latestSdk) apiLevel++;
					if(apiLevel == preInfo.getApiLevel()) {
						data.add(apiLevel + (apiLevel == record.latestSdk ? " ~ Latest" : ""));
					} else {
						data.add(apiLevel + " ~ " + preInfo.getApiLevel());
					}
					if(apiLevel <= sdk && sdk <= preInfo.getApiLevel()) {
						selectRow = historyTableModel.getRowCount();
					}
					if(apiLevel == record.removedSdk) {
						data.add(String.format(DIFF_FORMAT, "Remove"));
					} else if(apiLevel == record.deprecatedSdk) {
						data.add(String.format(DIFF_FORMAT, "Deprecate"));
					} else if(apiLevel == record.addedSdk) {
						data.add(String.format(apiLevel != 1 ? DIFF_FORMAT : "%s", "Add"));
					} else {
						data.add("Edit");
					}
					if(isGroupRecord) {
						diff = info != null && preInfo.getPriority() != info.getPriority();
						data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getPriority()));
					} else {
						diff = info != null && preInfo.getProtectionLevel() != info.getProtectionLevel();
						data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getProtectionLevel()));
						diff = info != null && preInfo.getPermissionGroup() != info.getPermissionGroup();
						String gname = preInfo.getPermissionGroup();
						gname = gname != null ? gname.replaceAll("android.permission-group", "") : null;
						data.add(String.format((diff ? DIFF_FORMAT : "%s"), gname));
					}
					diff = info != null && preInfo.getLabel() != info.getLabel();
					data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getLabel()));
					diff = info != null && preInfo.getDescription() != info.getDescription();
					data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getDescription()));
					diff = info != null && preInfo.getNonLocalizedDescription() != info.getNonLocalizedDescription();
					data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getNonLocalizedDescription()));
					if(isGroupRecord) {
						diff = info != null && preInfo.getRequest() != info.getRequest();
						data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getRequest()));
					} else {
						diff = info != null && preInfo.getPermissionFlags() != info.getPermissionFlags();
						data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getPermissionFlags()));
					}
					historyTableModel.addRow(data);
				}
				preInfo = info;
			}
			historyTable.setRowSelectionInterval(selectRow, selectRow);
		}
	}

	private void expandOrCollapse(int row) {
		if(row < 0 || !byGroup.isSelected()) return;

		Object oldIcon = permTableModel.getValueAt(row, 0);
		if(oldIcon instanceof String) return;

		if(oldIcon.equals(UIManager.get("Tree.expandedIcon"))) {
			permTable.setValueAt(UIManager.get("Tree.collapsedIcon"), row++, 0);
			while(row < permTable.getRowCount() &&
					permTableModel.getValueAt(row, 0) instanceof String) {
				permTableModel.removeRow(row);
			}
		} else {
			permTable.setValueAt(UIManager.get("Tree.expandedIcon"), row, 0);
			int col = permTable.getColumnModel().getColumnIndex("Name");
			if(col < 0) return;
			String name = (String) permTableModel.getValueAt(row, col);

			final CellSpan cellAtt = (CellSpan) permTableModel.getCellSpan();
			Vector<Object> rowData = new Vector<>(5);
			for(PermissionInfo info: permManager.getGroupPermissions(name)) {
				row += 1;
				rowData = new Vector<>(5);
		        for (Object o : new Object[] { UIManager.get("Tree.leafIcon"), info.name, info.protectionLevel }) {
		        	rowData.addElement(o);
		        }
		        if(withLable.isSelected())	rowData.add(2, info.getLabel());
		        rowData.add(0, "");
				permTableModel.insertRow( row, rowData );
				//cellAtt.combine(new int[] {row, row+1}, new int[] {0} );
				//cellAtt.combine(new int[] {row+1}, new int[] {1,2} );
				if(withLable.isSelected() && (info.getLabel() == null || info.getLabel().isEmpty())) {
					cellAtt.combine(new int[] {row}, new int[] {2,3} );
				}
			}
		}
	}

	private void addDescriptionTab(Vector<?> rowData, boolean moveNewTabbed) {
		String apiLevel = (String)rowData.get(0);
		Component c = null;
		for(int i = extraTabbedPanel.getTabCount() - 1; i >= 2 ; --i) {
			if(extraTabbedPanel.getTitleAt(i).startsWith(apiLevel)) {
				c = extraTabbedPanel.getComponentAt(i);
				break;
			}
		}
		if(c == null) {
			JTextArea desc = new JTextArea();
			desc.setEditable(false);

			StringBuilder sb = new StringBuilder();
			for(int i = 2; i < historyTableHeader.length; i++) {
				sb.append("[").append(historyTableHeader[i]).append("]");
				String data = (String) rowData.get(i);
				if(data.startsWith("<html>")) {
					data = data.substring(DIFF_PREFIX_LEN, data.length() - DIFF_SUFFIX_LEN);
				}
				sb.append(data.contains("\n") ? "\n" : " ");
				if(historyTableHeader[i].equals("PermissionGroup") && data.startsWith(".")) {
					sb.append("android.permission-group");
				}
				sb.append(data).append("\n\n");
			}
			desc.setText(sb.toString());
			desc.setCaretPosition(0);
			c = new JScrollPane(desc);
			extraTabbedPanel.addTab(apiLevel + "  ", c);
		}
		if(moveNewTabbed) {
			extraTabbedPanel.setSelectedComponent(c);
		}
	}

	public void setPermissionManager(PermissionManager manager) {
		this.permManager = manager;
		setSdkApiLevels();
		refreshPermTable();
	}

	private void setSdkApiLevels() {
		sdkVersions.removeItemListener(this);
		sdkVersions.removeAllItems();
		for(SourceCommit sdk: PermissionManager.getPermissionRepository().sources) {
			if(sdk.getCommitId() == null) continue;
			sdkVersions.addItem(sdk.getSdkVersion());
		}
		sdkVersions.setSelectedItem(permManager.getSdkVersion());
		sdkVersions.addItemListener(this);
	}

	private void refreshPermTableStructure() {
		permTableModel.setRowCount(0);
		int colIdx = 0;
		Vector<Object> columns = new Vector<Object>(5);
        for (Object o : new String[] {"Icon", "Name", "Protection Level"}) {
        	columns.addElement(o);
        }
        boolean isWithLabel = withLable.isSelected();
        if(isWithLabel) {
			columns.add(2, "Label");
        }
		if(byGroup.isSelected()) {
			columns.add(0, "");
			permTableModel.setColumnIdentifiers(columns);
			permTableModel.fireTableStructureChanged();
			permTable.getColumnModel().getColumn(colIdx).setPreferredWidth(16);
			permTable.getColumnModel().getColumn(colIdx++).setResizable(false);
			permTable.getColumnModel().getColumn(colIdx).setPreferredWidth(36);
			permTable.getColumnModel().getColumn(colIdx++).setResizable(false);
			permTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250);
			permTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250);
			//permTable.getColumnModel().getColumn(4).setPreferredWidth(250);
		} else {
			permTableModel.setColumnIdentifiers(columns);
			permTableModel.fireTableStructureChanged();
			permTable.getColumnModel().getColumn(colIdx).setPreferredWidth(36);
			permTable.getColumnModel().getColumn(colIdx++).setResizable(false);
			permTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250);
			permTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250);
			//permTable.getColumnModel().getColumn(3).setPreferredWidth(250);
		}
		if(isWithLabel) {
			permTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250);
		}
	}

	private void refreshPermTable() {

		permTableModel.setRowCount(0);
		final CellSpan cellAtt = (CellSpan) permTableModel.getCellSpan();

		//int sdk = manager.getSdkVersion();
		Vector<Object> rowData = null;
		for(PermissionGroupInfoExt g: permManager.getPermissionGroups()) {
			//boolean isDanger = sdk >= 23 && g.hasDangerous();
			int row = permTableModel.getRowCount();

			ImageIcon icon = null;
			try {
				icon = new ImageIcon(new URL(g.getIconPath()));
			} catch (MalformedURLException e) { }

			if(byGroup.isSelected()) {
				rowData = new Vector<>(5);
		        for (Object o : new Object[] { UIManager.get("Tree.expandedIcon"), icon, g.name, PermissionInfo.protectionToString(g.protectionFlags) }) {
		        	rowData.addElement(o);
		        }
		        if(withLable.isSelected()) {
		        	rowData.add(3, g.getLabel());
		        }
		        permTableModel.addRow( rowData );
				//cellAtt.combine(new int[] {row, row+1}, new int[] {0} );
				//cellAtt.combine(new int[] {row+1}, new int[] {1,2} );
			}

			for(PermissionInfo info: g.permissions) {
				row += 1;
				rowData = new Vector<>(5);
		        for (Object o : new Object[] {byGroup.isSelected() ? UIManager.get("Tree.leafIcon") : icon, info.name, info.protectionLevel }) {
		        	rowData.addElement(o);
		        }
		        if(withLable.isSelected())	rowData.add(2, info.getLabel());
		        if(byGroup.isSelected())	rowData.add(0, "");
				permTableModel.addRow( rowData );
				//cellAtt.combine(new int[] {row, row+1}, new int[] {0} );
				//cellAtt.combine(new int[] {row+1}, new int[] {1,2} );
				if(withLable.isSelected() && (info.getLabel() == null || info.getLabel().isEmpty())) {
					if(!byGroup.isSelected()) {
						cellAtt.combine(new int[] {row}, new int[] {1,2} );
					} else {
						cellAtt.combine(new int[] {row}, new int[] {2,3} );
					}
				}
			}
		}
	}

	public void showDialog(Window owner) {
		dialog = new JDialog(owner);

		dialog.setTitle("Permission Info");
		dialog.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(true);
		dialog.setModal(false);
		dialog.setLayout(new BorderLayout());

		Dimension minSize = new Dimension(700, 600);
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(dialog, minSize);
		} else {
			dialog.setSize(minSize);
		}
		WindowSizeMemorizer.registeComponent(dialog);

		dialog.setLocationRelativeTo(owner);

		dialog.add(this, BorderLayout.CENTER);

		dialog.setVisible(true);

		KeyStrokeAction.registerKeyStrokeActions(dialog.getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
			KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false)
		}, this);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int keycode = Integer.parseInt(evt.getActionCommand());
		int idx;
		switch(keycode) {
		case KeyEvent.VK_RIGHT:
			idx = extraTabbedPanel.getSelectedIndex();
			idx = ++idx % extraTabbedPanel.getTabCount();
			extraTabbedPanel.setSelectedIndex(idx);
			break;
		case KeyEvent.VK_LEFT:
			idx = extraTabbedPanel.getSelectedIndex();
			idx = (--idx + extraTabbedPanel.getTabCount()) % extraTabbedPanel.getTabCount();
			extraTabbedPanel.setSelectedIndex(idx);
			break;
		case KeyEvent.VK_W:
			idx = extraTabbedPanel.getSelectedIndex();
			if(idx >= 2) extraTabbedPanel.removeTabAt(idx);
			break;
		case KeyEvent.VK_F:
			break;
		case KeyEvent.VK_ENTER:
			JTable table = (JTable) evt.getSource();
        	boolean withCtrl = (evt.getModifiers()
        			& (ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK)) != 0;
        	int row = table.getSelectedRow();
        	if(row == -1) return;
        	TableModel model = table.getModel();
        	if(model instanceof DefaultTableModel) {
        		addDescriptionTab((Vector<?>) ((DefaultTableModel)model).getDataVector().get(row), !withCtrl);
        	}
        	break;
		case KeyEvent.VK_ESCAPE:
			dialog.dispose();
			break;
		}
	}
}
