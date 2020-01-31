package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ActivityAliasInfo;
import com.apkspectrum.data.apkinfo.ActivityInfo;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.ComponentInfo;
import com.apkspectrum.data.apkinfo.ProviderInfo;
import com.apkspectrum.data.apkinfo.ReceiverInfo;
import com.apkspectrum.data.apkinfo.ServiceInfo;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.TextPrompt;
import com.apkspectrum.swing.TextPrompt.Show;
import com.apkspectrum.util.Log;

public class Components extends AbstractTabbedPanel
{
	private static final long serialVersionUID = 8325900007802212630L;

	private RSyntaxTextArea xmltextArea;
	private JPanel intentPanel;
	private JLabel intentLabel;

	private JTable table;
	private SimpleTableModel tableModel;
	private JTextField textField;

	public Components() {
		setLayout(new BorderLayout());
		setTitle(RComp.TABBED_COMPONENTS);
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		table = new JTable(tableModel = new CompTableModel(), new SimpleTableColumnModel(310, 50, 35, 35, 35, 35)) {
			private static final long serialVersionUID = 1340713167587523626L;

			public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
				Component c = super.prepareRenderer(tcr, row, column);
				Color temp = null;

				if(isRowSelected(row)) {
					c.setBackground(Color.GRAY);
				} else {
					String type = (String) tableModel.getValueAt(convertRowIndexToModel(row), 1);
					if("activity".equals(type) || "main".equals(type)) {
						temp = new Color(0xB7F0B1);
					} else if("launcher".equals(type) || "launcher-alias".equals(type)) {
						temp = new Color(0x5D9657);
					} else if("activity-alias".equals(type) || "main-alias".equals(type)) {
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
		table.setAutoCreateColumnsFromModel(true);
		table.setRowSorter(new TableRowSorter<TableModel>(tableModel) {
			{
				setRowFilter(new RowFilter<TableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
						String fileType = RProp.S.COMP_FILTER_TYPE.get();
						int targetCol = RConst.COMPONENT_FILTER_TYPE_XML.equals(fileType) ? 6 : 0;

						String filterText = textField.getText().trim();
						String data = (String) tableModel.getValueAt(entry.getIdentifier(), targetCol);

						return data.toLowerCase().contains(filterText.toLowerCase());
					}
				});
				for(int i=0; i<6; i++) setSortable(i, false);
			}
		});

		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(tableModel.getRowCount() == 0) return;
				if(table.getSelectedRow() > -1) {
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					xmltextArea.setText((String) tableModel.getValueAt(row, 6));
					xmltextArea.setCaretPosition(0);

					String text = textField.getText().trim();
					SearchEngine.markAll(xmltextArea, new SearchContext(text, false));
				} else {
					xmltextArea.setText("");
				}
			}
		});

		xmltextArea  = new RSyntaxTextArea();

		JPanel textAreaPanel = new JPanel(new BorderLayout());

		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmltextArea.setCodeFoldingEnabled(true);
		xmltextArea.setMarkOccurrences(true);
		xmltextArea.setEditable(false);
		RTextScrollPane sp = new RTextScrollPane(xmltextArea);

		textAreaPanel.add(sp);

		intentPanel = new JPanel();
		intentLabel = new JLabel();
		RComp.LABEL_XML_CONSTRUCTION.set(intentLabel);

		intentPanel.setLayout(new BorderLayout());

		intentPanel.add(intentLabel, BorderLayout.NORTH);
		intentPanel.add(textAreaPanel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(table);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        splitPane.setDividerSize(15);
        splitPane.setOneTouchExpandable(true);
        splitPane.setTopComponent(scrollPane);
        splitPane.setBottomComponent(intentPanel);
        splitPane.setDividerLocation(170);
        splitPane.setResizeWeight(0.5);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);
        intentPanel.setMinimumSize(minimumSize);

        textField = new JTextField();
        textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) { setFilter(); }

			@Override
			public void insertUpdate(DocumentEvent e) { setFilter(); }

			@Override
			public void changedUpdate(DocumentEvent e) { setFilter(); }

			private void setFilter() {
				int row = table.getSelectedRow();
				String curClass = null;
				if(row > -1) curClass = (String) tableModel
								.getValueAt(table.convertRowIndexToModel(row), 0);
				tableModel.fireTableDataChanged();
				if(curClass != null) {
					int col = table.convertColumnIndexToView(0);
					for(row = 0; row < table.getRowCount(); row++) {
						if(curClass.equals(table.getValueAt(row, col))) {
							table.setRowSelectionInterval(row, row);
							break;
						}
					}
				}
				if(table.getSelectedRow() == -1 && table.getRowCount() > 0) {
					table.setRowSelectionInterval(0, 0);
				}
			}
		});
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent evt) {
				textField.setBackground(new Color(255, 255, 255));
			}

			@Override
			public void focusGained(FocusEvent evt) {
				textField.setBackground(new Color(178, 235, 244));
			}
		});

		KeyStrokeAction.registerKeyStrokeAction(textField, JComponent.WHEN_FOCUSED,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textField.setText("");
			}
		});

		final TextPrompt tp7 = new TextPrompt(null, textField, Show.FOCUS_LOST);
		tp7.setForeground( Color.DARK_GRAY );
		tp7.changeAlpha(0.5f);
		tp7.changeStyle(Font.BOLD + Font.ITALIC);

		String[] petStrings = { RConst.COMPONENT_FILTER_TYPE_XML, RConst.COMPONENT_FILTER_TYPE_CLASS };
		final JComboBox<String> filterTypeCombobox = new JComboBox<>(petStrings);
		filterTypeCombobox.setActionCommand("ACT_CMD_FILTER");
		filterTypeCombobox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fileType = filterTypeCombobox.getSelectedItem().toString();
				RProp.S.COMP_FILTER_TYPE.set(fileType);

				RComp res = RConst.COMPONENT_FILTER_TYPE_XML.equals(fileType)
						? RComp.COMPONENT_FILTER_PROMPT_XML : RComp.COMPONENT_FILTER_PROMPT_NAME;
				res.set(tp7);

				textField.setText(textField.getText());
			}
		});
		filterTypeCombobox.setSelectedItem(RProp.S.COMP_FILTER_TYPE.get());

		JPanel filterPanel = new JPanel(new BorderLayout(1, 0));
		filterPanel.setBorder(new EmptyBorder(0, 0, 2, 0));
		filterPanel.add(filterTypeCombobox, BorderLayout.WEST);
		filterPanel.add(textField, BorderLayout.CENTER);

		add(filterPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
	}

	@Override
	public void setData(ApkInfo apkInfo, int status)
	{
		if(ApkScanner.STATUS_ACTIVITY_COMPLETED != status) return;

		if(tableModel == null)
			initialize();
		tableModel.setData(apkInfo);
		table.getSelectionModel().setSelectionInterval(0, 0);

		setDataSize(ApkInfoHelper.getComponentCount(apkInfo), true, false);
		setTabbedVisible(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX);
	}

	class CompTableModel extends SimpleTableModel {
		private static final long serialVersionUID = 5291910634830167294L;

		CompTableModel() {
			super(	RStr.COMPONENT_COLUME_CLASS,
					RStr.COMPONENT_COLUME_TYPE,
					RStr.COMPONENT_COLUME_ENABLED,
					RStr.COMPONENT_COLUME_EXPORT,
					RStr.COMPONENT_COLUME_PERMISSION,
					RStr.COMPONENT_COLUME_STARTUP );
		}

		@Override
		public void setData(ApkInfo apkInfo) {
			data.clear();

			add(apkInfo.manifest.application.activity, apkInfo);
			add(apkInfo.manifest.application.activityAlias, apkInfo);
			add(apkInfo.manifest.application.service, apkInfo);
			add(apkInfo.manifest.application.receiver, apkInfo);
			add(apkInfo.manifest.application.provider, apkInfo);

			Collections.sort(data, new Comparator<Object[]>() {
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

			fireTableDataChanged();
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
				data.add(new Object[] {info.name, type, enabled, exported, permission, startUp, info.xmlString});
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
	}
}