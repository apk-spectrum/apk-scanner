package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.permissionmanager.DeclaredPermissionInfo;
import com.apkspectrum.core.permissionmanager.PermissionGroupInfoExt;
import com.apkspectrum.core.permissionmanager.PermissionInfoExt;
import com.apkspectrum.core.permissionmanager.PermissionManager;
import com.apkspectrum.core.permissionmanager.PermissionRepository.SourceCommit;
import com.apkspectrum.core.permissionmanager.RevokedPermissionInfo;
import com.apkspectrum.core.permissionmanager.RevokedPermissionInfo.RevokedSource;
import com.apkspectrum.core.permissionmanager.UnitInformation;
import com.apkspectrum.core.permissionmanager.UnitRecord;
import com.apkspectrum.data.apkinfo.PermissionGroupInfo;
import com.apkspectrum.data.apkinfo.PermissionInfo;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.swing.ImageScaler;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.WindowSizeMemorizer;
import com.apkspectrum.swing.tabbedpaneui.CloseableTabbedPaneLayerUI;
import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;
import com.apkspectrum.util.Log;

public class PermissionHistoryPanel extends JPanel implements ItemListener, ActionListener {
    private static final long serialVersionUID = -3567803690045423840L;

    private static final String DIFF_FORMAT =
            "<html><body><font style=\"color:red\">%s</font></body></html>";
    private static final int DIFF_PREFIX_LEN = "<html><body><font style=\"color:red\">".length();
    private static final int DIFF_SUFFIX_LEN = "</font></body></html>".length();

    private static final String[] HISTORY_GROUP_COLUMNS = new String[] {"API Level", "Action",
            "Priority", "Label", "Descripton", "Comment", "Request"};
    private static final String[] HISTORY_PERM_COLUMNS =
            new String[] {"API Level", "Action", "ProtectionLevel", "PermissionGroup", "Label",
                    "Descripton", "Comment", "permissionFlags"};

    private static final int IN_PACKAGE = 0;
    private static final int ON_ANDROID = 1;

    private static final String ACT_CMD_IN_PACKAGE = "FILTER_IN_PACKAGE";
    private static final String ACT_CMD_ON_ANDROID = "FILTER_ON_ANDROID";

    private JDialog dialog;

    private JComboBox<Integer> sdkVersions;
    private JCheckBox byGroup;
    private JCheckBox withLable;

    private JLabel collapseFilterLabel;
    private JLabel collapseFilterCount;
    private JLabel extendFilterLabel;
    private JLabel extendFilterCount;

    private JRadioButton inPackage;
    private JRadioButton onAndroid;

    private JTextField filterTextField;

    private PermissionTable permTable;

    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    private JTextArea description;
    private JTabbedPane extraTabbedPanel;

    private Map<String, JCheckBox> flagCheckBoxs = new HashMap<>();

    private PermissionManager[] cachePermMangers = new PermissionManager[2];
    private PermissionManager permManager;
    private String[] historyTableHeader;

    public PermissionHistoryPanel() {
        setLayout(new GridBagLayout());

        // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx,
        // double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady)
        GridBagConstraints gridConst =
                new GridBagConstraints(0, 0, 1, 1, 1.0f, 0, GridBagConstraints.CENTER,
                        GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0);
        add(makeSdkSelectPanel(), gridConst);

        final JComponent filterCollapsePanel = makeFilterCollapsePanel();
        final JComponent filterExtendPanel = makeFilterExtendPanel();
        collapseFilterLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filterCollapsePanel.setVisible(false);
                filterExtendPanel.setVisible(true);
            }
        });
        extendFilterLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filterCollapsePanel.setVisible(true);
                filterExtendPanel.setVisible(false);
                refreshFilterLabel();
            }
        });

        gridConst.gridy++;
        gridConst.insets.top = 4;
        gridConst.insets.bottom = 5;
        add(filterCollapsePanel, gridConst);
        gridConst.insets.top = 0;

        gridConst.gridy++;
        gridConst.insets.bottom = 5;
        add(filterExtendPanel, gridConst);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        splitPane.setTopComponent(new JScrollPane(permTable = new PermissionTable()));
        splitPane.setBottomComponent(new JLayer<JTabbedPane>(
                extraTabbedPanel = makeExtraTabbedPanel(), new CloseableTabbedPaneLayerUI()));
        splitPane.setDividerLocation(300);

        gridConst.gridy++;
        gridConst.weighty = 1.0f;
        gridConst.fill = GridBagConstraints.BOTH;
        gridConst.insets.bottom = 5;
        add(splitPane, gridConst);
    }

    public JPanel makeSdkSelectPanel() {
        JPanel sdkSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sdkSelectPanel.add(new JLabel("SDK Ver. "));

        sdkVersions = new JComboBox<Integer>();
        final ListCellRenderer<? super Integer> oldRenderer = sdkVersions.getRenderer();
        sdkVersions.setRenderer(new ListCellRenderer<Integer>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Integer> list,
                    Integer value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = null;
                Component c = oldRenderer.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                label = c instanceof JLabel ? (JLabel) c : new JLabel();
                label.setText(value > 0 ? "API Level " + value : "API Levels");
                return label;
            }
        });
        sdkSelectPanel.add(sdkVersions);

        byGroup = new JCheckBox(RStr.LABEL_BY_GROUP.get());
        byGroup.setSelected(true);
        byGroup.addItemListener(this);
        sdkSelectPanel.add(byGroup);

        withLable = new JCheckBox(RStr.LABEL_WITH_LABEL.get());
        withLable.setSelected(true);
        withLable.addItemListener(this);
        sdkSelectPanel.add(withLable);

        JPanel sdkOptions = new JPanel(new BorderLayout());
        sdkOptions.add(sdkSelectPanel);

        JLabel refer = new JLabel(RStr.LABEL_REFERENCE_N_LEVELS.get());
        refer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PermissionReferencePanel().showDialog(null);
            }
        });

        JPanel sdkEastPanel = new JPanel();
        sdkEastPanel.add(refer);
        sdkOptions.add(sdkEastPanel, BorderLayout.EAST);

        return sdkOptions;
    }

    public JComponent makeFilterCollapsePanel() {
        final JPanel filterCollapsePanel = new JPanel();
        filterCollapsePanel.setLayout(new BoxLayout(filterCollapsePanel, BoxLayout.X_AXIS));
        collapseFilterLabel = new JLabel("");
        collapseFilterLabel.setIcon((Icon) UIManager.get("Tree.collapsedIcon"));
        filterCollapsePanel.add(collapseFilterLabel);
        filterCollapsePanel.add(Box.createHorizontalGlue());
        collapseFilterCount = new JLabel("");
        filterCollapsePanel.add(collapseFilterCount);
        return filterCollapsePanel;
    }

    public JComponent makeFilterExtendPanel() {
        // filterExtendPanel
        final JPanel filterExtendPanel = new JPanel();
        filterExtendPanel.setLayout(new BoxLayout(filterExtendPanel, BoxLayout.Y_AXIS));
        filterExtendPanel.setVisible(false);
        filterExtendPanel.setPreferredSize(new Dimension(0, 20));

        Box box = Box.createHorizontalBox();
        box.setAlignmentX(0f);
        extendFilterLabel = new JLabel(RStr.LABEL_FILTER.get() + " : ");
        extendFilterLabel.setIcon((Icon) UIManager.get("Tree.expandedIcon"));
        extendFilterLabel.setAlignmentX(0f);
        box.add(extendFilterLabel);
        inPackage = new JRadioButton(RStr.LABEL_USED_IN_PACKAGE.get());
        inPackage.setActionCommand(ACT_CMD_IN_PACKAGE);
        inPackage.addActionListener(this);
        box.add(inPackage);
        onAndroid = new JRadioButton(RStr.LABEL_ALL_ON_ANDROID.get());
        onAndroid.setActionCommand(ACT_CMD_ON_ANDROID);
        onAndroid.addActionListener(this);
        box.add(onAndroid);
        filterExtendPanel.add(box);

        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(inPackage);
        bGroup.add(onAndroid);
        inPackage.setSelected(true);

        box = Box.createHorizontalBox();
        box.setAlignmentX(0f);
        box.add(new JLabel(RStr.LABEL_SEARCH.get() + ":"));
        filterTextField = new JTextField();
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            private void setFilter() {
                permTable.setFilterText(filterTextField.getText().trim());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setFilter();
            }
        });
        box.add(filterTextField);
        filterExtendPanel.add(box);

        final JPanel flagsPanel = new JPanel(new GridLayout(0, 5, 0, 0));
        ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // if(evt.getStateChange() != ItemEvent.SELECTED) return;
                String level = ((AbstractButton) e.getSource()).getActionCommand();;
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    permTable.setFilter(level);
                } else {
                    permTable.clearFilter(level);
                }
                if (!level.startsWith("|")) {
                    permTable.clearFilter(PermissionInfo.PROTECTION_MASK_FLAGS);
                    int flag = permTable.getFilteredFlags();
                    flagsPanel.setVisible(flag != 0);
                    for (Component c : flagsPanel.getComponents()) {
                        if (!(c instanceof JCheckBox)) continue;
                        ((JCheckBox) c).removeItemListener(this);
                        ((JCheckBox) c).setSelected(false);
                        ((JCheckBox) c).addItemListener(this);
                    }
                    flagsPanel.removeAll();
                    String filteredFlags = PermissionInfo
                            .protectionToString(PermissionInfo.PROTECTION_MASK_BASE | flag);
                    for (String key : filteredFlags.split("\\|")) {
                        if (key.isEmpty() || !flagCheckBoxs.containsKey(key)) continue;
                        flagsPanel.add(flagCheckBoxs.get(key));
                    }
                    Log.v("getFilteredFlags 0x" + Integer.toHexString(flag));
                }
                refreshPermsCount();
            }
        };

        JPanel flags = new JPanel(new GridLayout(0, 5, 0, 0));
        flags.setAlignmentX(0f);
        flags.setBorder(new TitledBorder("Protection Levels"));
        String allFlags =
                PermissionInfoExt.protectionFlagsToString(PermissionInfo.PROTECTION_MASK_BASE);
        for (String flag : allFlags.split("\\|")) {
            JCheckBox ckBox = new JCheckBox(flag);
            ckBox.setMinimumSize(new Dimension(0, 20));
            ckBox.setActionCommand(flag);
            ckBox.setToolTipText(flag);
            ckBox.setSelected(true);
            ckBox.addItemListener(listener);
            flags.add(ckBox);
            flagCheckBoxs.put(flag, ckBox);
        }
        filterExtendPanel.add(flags);

        flagsPanel.setVisible(false);
        flagsPanel.setAlignmentX(0f);
        flagsPanel.setBorder(new TitledBorder("Protection Flags"));
        allFlags = PermissionInfo.protectionToString(
                PermissionInfo.PROTECTION_MASK_BASE | PermissionInfo.PROTECTION_MASK_FLAGS);
        for (String flag : allFlags.split("\\|")) {
            if (flag.isEmpty()) continue;
            JCheckBox ckBox = new JCheckBox(flag);
            ckBox.setMinimumSize(new Dimension(0, 20));
            ckBox.setActionCommand("|" + flag);
            ckBox.setToolTipText(flag);
            ckBox.addItemListener(listener);
            flagCheckBoxs.put(flag, ckBox);
        }
        filterExtendPanel.add(flagsPanel);

        box = Box.createHorizontalBox();
        box.setAlignmentX(0f);
        box.add(Box.createHorizontalGlue());
        extendFilterCount = new JLabel("");
        box.add(extendFilterCount);
        filterExtendPanel.add(box);

        return filterExtendPanel;
    }

    public JTabbedPane makeExtraTabbedPanel() {
        description = new JTextArea();
        description.setEditable(false);
        description.setLineWrap(true);

        historyTableModel = new DefaultTableModel() {
            private static final long serialVersionUID = -5182372671185877580L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setCellSelectionEnabled(false);
        historyTable.setRowSelectionAllowed(true);
        // historyTable.setRowHeight(20);d
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // historyTable.getSelectionModel().addListSelectionListener(this);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (row > -1 && mouseEvent.getClickCount() == 2) {
                    boolean withCtrl = (mouseEvent.getModifiersEx() & RConst.CTRL_SHIFT_MASK) != 0;
                    if (table.getSelectedRow() == -1) {
                        if (!withCtrl) return;
                        table.setRowSelectionInterval(row, row);
                    }
                    TableModel model = table.getModel();
                    if (model instanceof DefaultTableModel) {
                        addDescriptionTab(
                                (Vector<?>) ((DefaultTableModel) model).getDataVector().get(row),
                                !withCtrl);
                    }
                }
            }
        });
        KeyStrokeAction.registerKeyStrokeActions(historyTable, JComponent.WHEN_IN_FOCUSED_WINDOW,
                new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, RConst.CTRL_MASK),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, RConst.SHIFT_MASK)},
                this);

        extraTabbedPanel = new JTabbedPane();
        String tabbedStyle = RProp.S.TABBED_UI_THEME.get();
        extraTabbedPanel.setOpaque(true);
        TabbedPaneUIManager.setUI(extraTabbedPanel, tabbedStyle);

        extraTabbedPanel.addTab("Description", new JScrollPane(description));
        extraTabbedPanel.addTab("History", new JScrollPane(historyTable));

        return extraTabbedPanel;
    }

    @Override
    public void itemStateChanged(ItemEvent evt) {
        if (evt.getSource() instanceof JCheckBox) {
            permTable.resizedColumnSize();
        } else {
            if (evt.getStateChange() != ItemEvent.SELECTED) return;
            int sdkLevel = (int) evt.getItem();
            permManager.setSdkVersion(sdkLevel);
        }
        refreshPermTable();
        setBaseFilter();
    }

    private void setDescription(UnitRecord<?> record, Object info) {
        StringBuilder sb = new StringBuilder();
        if (record != null) {
            sb.append(record.name).append("  -  Added in API level ").append(record.getAddedSdk());
            if (record.getDeprecatedSdk() > 0) {
                sb.append(", Deprecated in API level ").append(record.getDeprecatedSdk());
            }
            if (record.getRemovedSdk() > 0) {
                sb.append(", Removed in API level ").append(record.getRemovedSdk());
            }

            if (info instanceof RevokedPermissionInfo) {
                sb.append("\n\n[Revoked reason]\n")
                        .append(((RevokedPermissionInfo) info).getReasonText());
            }

            if (info instanceof UnitInformation) {
                UnitInformation unitInfo = (UnitInformation) info;
                String level = unitInfo.getProtectionLevel();
                String label = unitInfo.getLabel();
                String desc = unitInfo.getDescription();
                String comment = unitInfo.getNonLocalizedDescription();

                if (level != null) {
                    sb.append("\n\n[Protection Level] ").append(level);
                }
                if (label != null) {
                    sb.append("\n\n[Label] ").append(label);
                }
                if (desc != null) {
                    sb.append("\n\n[Description]\n").append(desc);
                }
                if (comment != null && !comment.trim().isEmpty()) {
                    sb.append("\n\n[Non Localized Description]\n").append(comment);
                }
            }
        } else if (info instanceof RevokedPermissionInfo) {
            RevokedPermissionInfo revokedInfo = (RevokedPermissionInfo) info;
            sb.append(revokedInfo.name);
            String level = revokedInfo.protectionLevel;
            if (level != null) {
                sb.append("\n\n[Protection Level] ").append(level);
            }
            sb.append("\n\n[Revoked reason]\n").append(revokedInfo.getReasonText());
        } else if (info instanceof PermissionInfo) {
            PermissionInfo permInfo = (PermissionInfo) info;
            sb.append(permInfo.name);
            String level = permInfo.protectionLevel;
            if (level != null) {
                sb.append("\n\n[Protection Level] ").append(level);
            }
            String label = permInfo.getLabel();
            String desc = permInfo.getDescription();
            if (label != null) {
                sb.append("\n\n[Label] ").append(label);
            }
            if (desc != null) {
                sb.append("\n\n[Description]\n").append(desc);
            }
        } else {
            sb.append("No have description");
        }
        description.setText(sb.toString());
        description.setCaretPosition(0);
    }

    private void setHistoryData(UnitRecord<?> record) {
        historyTableModel.setRowCount(0);
        if (record == null) {

        } else {
            boolean isGroupRecord = record.isPermissionGroupRecord();
            historyTableHeader = isGroupRecord ? HISTORY_GROUP_COLUMNS : HISTORY_PERM_COLUMNS;
            historyTableModel.setColumnIdentifiers(historyTableHeader);

            historyTable.getColumnModel().getColumn(1).setPreferredWidth(40);
            if (isGroupRecord) {
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
            for (UnitInformation info : histories) {
                if (preInfo != null) {
                    Vector<Object> data = new Vector<>(10);
                    int apiLevel = info != null ? info.getApiLevel() : record.addedSdk;
                    if (info != null && apiLevel != record.latestSdk) apiLevel++;
                    if (apiLevel == preInfo.getApiLevel()) {
                        data.add(apiLevel + (apiLevel == record.latestSdk ? " ~ Latest" : ""));
                    } else {
                        data.add(apiLevel + " ~ " + preInfo.getApiLevel());
                    }
                    if (apiLevel <= sdk && sdk <= preInfo.getApiLevel()) {
                        selectRow = historyTableModel.getRowCount();
                    }
                    if (apiLevel == record.removedSdk) {
                        data.add(String.format(DIFF_FORMAT, "Remove"));
                    } else if (apiLevel == record.deprecatedSdk) {
                        data.add(String.format(DIFF_FORMAT, "Deprecate"));
                    } else if (apiLevel == record.addedSdk) {
                        data.add(String.format(apiLevel != 1 ? DIFF_FORMAT : "%s", "Add"));
                    } else {
                        data.add("Edit");
                    }
                    if (isGroupRecord) {
                        diff = info != null && preInfo.getPriority() != info.getPriority();
                        String prio = preInfo.getPriority() != -1
                                ? Integer.toString(preInfo.getPriority())
                                : "null";
                        data.add(String.format((diff ? DIFF_FORMAT : "%s"), prio));
                    } else {
                        diff = info != null
                                && preInfo.getProtectionLevel() != info.getProtectionLevel();
                        data.add(String.format((diff ? DIFF_FORMAT : "%s"),
                                preInfo.getProtectionLevel()));
                        diff = info != null
                                && preInfo.getPermissionGroup() != info.getPermissionGroup();
                        String gname = preInfo.getPermissionGroup();
                        gname = gname != null ? gname.replaceAll("android.permission-group", "")
                                : null;
                        data.add(String.format((diff ? DIFF_FORMAT : "%s"), gname));
                    }
                    diff = info != null && preInfo.getLabel() != info.getLabel();
                    data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getLabel()));
                    diff = info != null && preInfo.getDescription() != info.getDescription();
                    data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getDescription()));
                    diff = info != null && preInfo.getNonLocalizedDescription() != info
                            .getNonLocalizedDescription();
                    data.add(String.format((diff ? DIFF_FORMAT : "%s"),
                            preInfo.getNonLocalizedDescription()));
                    if (isGroupRecord) {
                        diff = info != null && preInfo.getRequest() != info.getRequest();
                        data.add(String.format((diff ? DIFF_FORMAT : "%s"), preInfo.getRequest()));
                    } else {
                        diff = info != null
                                && preInfo.getPermissionFlags() != info.getPermissionFlags();
                        data.add(String.format((diff ? DIFF_FORMAT : "%s"),
                                preInfo.getPermissionFlags()));
                    }
                    historyTableModel.addRow(data);
                }
                preInfo = info;
            }
            historyTable.setRowSelectionInterval(selectRow, selectRow);
        }
    }

    private void addDescriptionTab(Vector<?> rowData, boolean moveNewTabbed) {
        String apiLevel = (String) rowData.get(0);
        Component c = null;
        for (int i = extraTabbedPanel.getTabCount() - 1; i >= 2; --i) {
            if (extraTabbedPanel.getTitleAt(i).startsWith(apiLevel)) {
                c = extraTabbedPanel.getComponentAt(i);
                break;
            }
        }
        if (c == null) {
            JTextArea desc = new JTextArea();
            desc.setEditable(false);

            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < historyTableHeader.length; i++) {
                sb.append("[").append(historyTableHeader[i]).append("]");
                String data = (String) rowData.get(i);
                if (data.startsWith("<html>")) {
                    data = data.substring(DIFF_PREFIX_LEN, data.length() - DIFF_SUFFIX_LEN);
                }
                sb.append(data.contains("\n") ? "\n" : " ");
                if (historyTableHeader[i].equals("PermissionGroup") && data.startsWith(".")) {
                    sb.append("android.permission-group");
                }
                sb.append(data).append("\n\n");
            }
            desc.setText(sb.toString());
            desc.setCaretPosition(0);
            c = new JScrollPane(desc);
            extraTabbedPanel.addTab(apiLevel + "  ", c);
        }
        if (moveNewTabbed) {
            extraTabbedPanel.setSelectedComponent(c);
        }
    }

    public void setPermissionManager(PermissionManager manager) {
        cachePermMangers[IN_PACKAGE] = manager;
        if (cachePermMangers[ON_ANDROID] == null) {
            cachePermMangers[ON_ANDROID] = PermissionManager.createAllPermissionManager();
            cachePermMangers[ON_ANDROID].setSdkVersion(manager.getSdkVersion());
        }

        boolean hasPerms = manager != null && !manager.isEmpty();
        inPackage.setEnabled(hasPerms);
        inPackage.setSelected(hasPerms);
        onAndroid.setSelected(!hasPerms);
        this.permManager = hasPerms ? manager : cachePermMangers[ON_ANDROID];
        setSdkApiLevels();
        refreshPermTable();
        setBaseFilter();
        refreshFilterLabel();
    }

    public void setFilterText(String text) {
        filterTextField.setText(text);
        refreshFilterLabel();
    }

    private void setSdkApiLevels() {
        sdkVersions.removeItemListener(this);
        sdkVersions.removeAllItems();
        for (SourceCommit sdk : PermissionManager.getPermissionRepository().sources) {
            if (sdk.getCommitId() == null) continue;
            sdkVersions.addItem(sdk.getSdkVersion());
        }
        if (permManager.getSdkVersion() > 0) {
            sdkVersions.setSelectedItem(permManager.getSdkVersion());
        } else {
            sdkVersions.setSelectedIndex(sdkVersions.getItemCount() - 1);
            permManager.setSdkVersion(sdkVersions.getSelectedIndex());
        }
        sdkVersions.addItemListener(this);
    }

    private void refreshPermTable() {
        DefaultTableModel model = (DefaultTableModel) permTable.getModel();
        model.setRowCount(0);

        permTable.filterClear();

        Vector<Object> rowData = null;
        for (PermissionGroupInfoExt g : permManager.getPermissionGroups()) {
            Icon icon = null;
            try {
                icon = new ImageIcon(new URL(g.getIconPath()));
            } catch (MalformedURLException e) {
            }

            if (byGroup.isSelected()) {
                rowData = new Vector<>(5);
                rowData.addElement("");
                for (Object o : new Object[] {icon, g.name, g.getLabel(),
                        PermissionInfoExt.protectionFlagsToString(g.protectionFlags)}) {
                    rowData.addElement(new SortedData(o, g.getPriority(), true, permTable));
                }
                rowData.addElement(g);
                model.addRow(rowData);
            }

            for (PermissionInfo info : g.permissions) {
                rowData = new Vector<>(5);
                String level = (info.protectionLevel != null && !info.protectionLevel.isEmpty())
                        ? info.protectionLevel
                        : PermissionInfo.protectionToString(0);
                for (Object o : new Object[] {"",
                        byGroup.isSelected() ? UIManager.get("Tree.leafIcon") : icon, info.name,
                        info.getLabel(), level}) {
                    rowData.addElement(new SortedData(o, g.getPriority(), false, permTable));
                }
                rowData.addElement(info);
                model.addRow(rowData);

            }
        }

        refreshPermsCount();
    }

    private void refreshPermsCount() {
        String count = null;
        if (byGroup.isSelected()) {
            count = String.format(RStr.LABEL_GROUP_COUNT_FORMAT.get(), permTable.getGroupCount(),
                    permTable.getPermissionCount());
        } else {
            count = String.format(RStr.LABEL_PERM_COUNT_FORMAT.get(),
                    permTable.getPermissionCount());
        }
        collapseFilterCount.setText(count);
        extendFilterCount.setText(count);
    }

    private void setBaseFilter() {
        int hasBaseLevels = 0;
        for (PermissionGroupInfoExt g : permManager.getPermissionGroups()) {
            for (PermissionInfo info : g.permissions) {
                hasBaseLevels |= (getProtectionFlags(info) & PermissionInfo.PROTECTION_MASK_BASE);
            }
        }
        String allFlags =
                PermissionInfoExt.protectionFlagsToString(PermissionInfo.PROTECTION_MASK_BASE);
        for (String flag : allFlags.split("\\|")) {
            if (flag.isEmpty() || !flagCheckBoxs.containsKey(flag)) continue;
            JCheckBox ckBox = flagCheckBoxs.get(flag);
            int level = PermissionInfo.parseProtectionLevel(flag);
            boolean has = (hasBaseLevels & (1 << level)) != 0;
            ckBox.setEnabled(has);
            ckBox.setSelected(has);
        }
    }

    private void refreshFilterLabel() {
        StringBuilder label = new StringBuilder();
        label.append(RStr.LABEL_FILTER.get()).append(" : ");
        if (permManager == cachePermMangers[ON_ANDROID]) {
            label.append(RStr.LABEL_ALL_ON_ANDROID_SHORT.get()).append(", ");
        } else {
            label.append(RStr.LABEL_USED_IN_PACKAGE_SHORT.get()).append(", ");
        }

        String filterText = permTable.getFilterText().trim();
        if (filterText.startsWith("android.permission-group.")) {
            filterText = filterText.replaceAll("android.permission-group.", ".");
        }
        if (filterText.length() > 13) {
            filterText = filterText.substring(0, 10) + "...";
        }
        if (filterText.isEmpty()) {
            filterText = RStr.LABEL_FILTER_NONE.get();
        }
        label.append(filterText).append(", ");

        String level = "";
        boolean hasAllLevel = true;
        String allFlags =
                PermissionInfoExt.protectionFlagsToString(PermissionInfo.PROTECTION_MASK_BASE);
        for (String flag : allFlags.split("\\|")) {
            if (flag.isEmpty() || !flagCheckBoxs.containsKey(flag)) continue;
            JCheckBox ckBox = flagCheckBoxs.get(flag);
            if (ckBox.isEnabled()) {
                if (!ckBox.isSelected())
                    hasAllLevel = false;
                else {
                    if (!level.isEmpty()) level += "|";
                    String tmp = ckBox.getText();
                    level += tmp.substring(0, 1).toUpperCase();
                    if (tmp.contains("OrS")) level += "S";
                }
            }
        }
        label.append(hasAllLevel ? RStr.LABEL_FILTER_ALL.get() : level);

        collapseFilterLabel.setText(label.toString());
    }

    public void showDialog(Window owner) {
        dialog = new JDialog(owner);

        dialog.setTitle(RStr.LABEL_PERMISSION_INFO.get());
        dialog.setIconImage(RImg.APP_ICON.getImage());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);

        dialog.setModal(false);
        dialog.setLayout(new BorderLayout());

        WindowSizeMemorizer.apply(dialog, new Dimension(700, 600));

        dialog.setLocationRelativeTo(owner);

        dialog.add(this, BorderLayout.CENTER);

        dialog.setVisible(true);

        KeyStrokeAction.registerKeyStrokeActions(dialog.getRootPane(),
                JComponent.WHEN_IN_FOCUSED_WINDOW,
                new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, RConst.ALT_MASK),
                        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, RConst.ALT_MASK),
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, RConst.CTRL_MASK),
                        KeyStroke.getKeyStroke(KeyEvent.VK_F, RConst.CTRL_MASK),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)},
                this);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source instanceof JRadioButton) {
            PermissionManager manager = null;
            if (ACT_CMD_IN_PACKAGE.equals(evt.getActionCommand())) {
                manager = cachePermMangers[IN_PACKAGE];
            } else if (ACT_CMD_ON_ANDROID.equals(evt.getActionCommand())) {
                manager = cachePermMangers[ON_ANDROID];
            }
            if (manager != null && manager != permManager) {
                manager.setSdkVersion(permManager.getSdkVersion());
                permManager = manager;
                refreshPermTable();
                setBaseFilter();
            }
        } else {
            int idx;
            switch (evt.getActionCommand()) {
                case "alt pressed RIGHT":
                    idx = extraTabbedPanel.getSelectedIndex();
                    idx = ++idx % extraTabbedPanel.getTabCount();
                    extraTabbedPanel.setSelectedIndex(idx);
                    break;
                case "alt pressed LEFT":
                    idx = extraTabbedPanel.getSelectedIndex();
                    idx = (--idx + extraTabbedPanel.getTabCount()) % extraTabbedPanel.getTabCount();
                    extraTabbedPanel.setSelectedIndex(idx);
                    break;
                case "ctrl pressed W":
                    idx = extraTabbedPanel.getSelectedIndex();
                    if (idx >= 2) extraTabbedPanel.removeTabAt(idx);
                    break;
                case "ctrl pressed F":
                    break;
                case "ctrl pressed ENTER":
                    JTable table = (JTable) ((KeyStrokeAction) source).getComponent();
                    boolean withCtrl = (evt.getModifiers()
                            & (ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK)) != 0;
                    int row = table.getSelectedRow();
                    if (row == -1) return;
                    TableModel model = table.getModel();
                    if (model instanceof DefaultTableModel) {
                        addDescriptionTab(
                                (Vector<?>) ((DefaultTableModel) model).getDataVector().get(row),
                                !withCtrl);
                    }
                    break;
                case "pressed ESCAPE":
                    dialog.dispose();
                    break;
            }
        }
    }

    private int getProtectionFlags(PermissionInfo info) {
        int protectionFlags = 0;
        if (info instanceof PermissionInfoExt) {
            protectionFlags = ((PermissionInfoExt) info).getProtectionFlags();
        } else {
            String level = info.protectionLevel;
            if (level == null || level.isEmpty()) {
                protectionFlags = (1 << PermissionInfo.PROTECTION_NORMAL);
            } else {
                protectionFlags = PermissionInfoExt.parseProtectionFlags(info.protectionLevel);
            }
        }
        return protectionFlags;
    }

    private class PermissionTable extends JTable {
        private static final long serialVersionUID = -5002494238794399060L;

        private PermissionFilter filter;
        private String filterText = "";

        public PermissionTable() {
            super(new PermissionTableModel());

            setCellSelectionEnabled(false);
            setRowSelectionAllowed(true);
            setRowHeight(18);

            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            // getSelectionModel().addListSelectionListener(this);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    JTable table = (JTable) mouseEvent.getSource();
                    Point point = mouseEvent.getPoint();
                    int row = table.rowAtPoint(point);
                    int col = table.columnAtPoint(point);
                    if (row > -1 && table.getSelectedRow() != -1 && (mouseEvent.getClickCount() == 2
                            || col == getColumnModel().getColumnIndex(""))) {
                        expandOrCollapse(row);
                    }
                }
            });
            setRowSorter(new PermissionSorter(getModel()));
            filter = (PermissionFilter) ((TableRowSorter<? extends TableModel>) getRowSorter())
                    .getRowFilter();
            setDefaultCoumnSize();
        }

        public void setDefaultCoumnSize() {
            TableColumnModel colModel = getColumnModel();

            // new String[] {"", "Icon", "Name", "Label", "Protection Level"}
            // column 0 - "" : TreeIcon
            int colIdx = colModel.getColumnIndex("");
            TableColumn column = colModel.getColumn(colIdx);
            column.setResizable(false);
            column.setMinWidth(0);

            // column 1 - Icon : Group Icon
            colIdx = colModel.getColumnIndex("Icon");
            column = colModel.getColumn(colIdx);
            column.setMaxWidth(48);
            column.setPreferredWidth(36);

            // column 2 - Name
            colIdx = colModel.getColumnIndex("Name");
            column = colModel.getColumn(colIdx);
            column.setPreferredWidth(250);

            // column 4 - Protection Level
            colIdx = colModel.getColumnIndex("Protection Level");
            column = colModel.getColumn(colIdx);
            column.setPreferredWidth(250);

            // column 5 - Hidden Data
            colIdx = colModel.getColumnIndex("Data");
            column = colModel.getColumn(colIdx);
            column.setResizable(false);
            column.setMinWidth(0);
            column.setPreferredWidth(0);
            column.setWidth(0);

            resizedColumnSize();
        }

        public void resizedColumnSize() {
            TableColumnModel colModel = getColumnModel();

            // new String[] {"", "Icon", "Name", "Label", "Protection Level"}
            // column 0 - "" : TreeIcon
            int colIdx = colModel.getColumnIndex("");
            TableColumn column = colModel.getColumn(colIdx);
            if (byGroup.isSelected()) {
                column.setPreferredWidth(16);
                column.setWidth(16);
            } else {
                column.setPreferredWidth(0);
                column.setWidth(0);
            }

            // column 3 - Label
            colIdx = colModel.getColumnIndex("Label");
            column = colModel.getColumn(colIdx);
            if (withLable.isSelected()) {
                column.setResizable(true);
                column.setMinWidth(15);
                column.setPreferredWidth(250);
            } else {
                column.setResizable(false);
                column.setMinWidth(0);
                column.setPreferredWidth(0);
                column.setWidth(0);
            }
        }

        private void expandOrCollapse(int row) {
            if (row < 0 || !byGroup.isSelected()) return;

            int col = getColumnModel().getColumnIndex("Name");
            if (col < 0) return;
            String name = getValueAt(row, col).toString();
            col = getColumnModel().getColumnIndex("");

            if (containCollapseGroup(name)) {
                removeCollapseGroup(name);
            } else {
                addCollapseGroup(name);
            }
            ((DefaultTableModel) getModel()).fireTableDataChanged();
        }

        public void setFilterText(String text) {
            filterText = text;
            text = text.toUpperCase();
            if (!text.equals(filter.getFilterText())) {
                filter.setFilterText(!text.isEmpty() ? text : null);
                filter.clearFilter(PermissionInfo.PROTECTION_MASK_FLAGS);
                ((DefaultTableModel) getModel()).fireTableDataChanged();
                refreshPermsCount();
            }
        }

        public String getFilterText() {
            return filterText;
        }

        public void addCollapseGroup(String groupName) {
            if (filter.containCollapseGroup(groupName)) return;
            filter.addCollapseGroup(groupName);
        }

        public void removeCollapseGroup(String groupName) {
            if (!filter.containCollapseGroup(groupName)) return;
            filter.removeCollapseGroup(groupName);
        }

        public boolean containCollapseGroup(String groupName) {
            return filter.containCollapseGroup(groupName);
        }

        public void setFilter(int protectionLevel) {
            filter.setFilter(protectionLevel);
            ((DefaultTableModel) getModel()).fireTableDataChanged();
        }

        public void clearFilter(int protectionLevel) {
            filter.clearFilter(protectionLevel);
            ((DefaultTableModel) getModel()).fireTableDataChanged();
        }

        public void setFilter(String protectionLevel) {
            setFilter(convertFilterLevel(protectionLevel));
        }

        public void clearFilter(String protectionLevel) {
            clearFilter(convertFilterLevel(protectionLevel));
        }

        private int convertFilterLevel(String protectionLevel) {
            if (!protectionLevel.startsWith("|")) {
                return PermissionInfoExt.parseProtectionFlags(protectionLevel);
            } else {
                return PermissionInfo.parseProtectionLevel(protectionLevel);
            }
        }

        public int getGroupCount() {
            return filter.getGroupCount();
        }

        public int getPermissionCount() {
            return filter.getPermissionCount();
        }

        public int getFilteredFlags() {
            return filter.getFilteredFlags();
        }

        @SuppressWarnings("unused")
        public String[] getCollapseGroups() {
            return filter.getCollapseGroups();
        }

        public void filterClear() {
            filter.clear();
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);

            if (e.getValueIsAdjusting()) return;

            int row = getSelectedRow();
            int col = getColumnModel().getColumnIndex("Name");
            if (row < 0 || col < 0) return;

            while (extraTabbedPanel.getTabCount() > 2)
                extraTabbedPanel.removeTabAt(2);

            String name = getValueAt(row, col).toString();

            col = getColumnModel().getColumnIndex("Data");
            Object info = getValueAt(row, col);
            boolean isGroup = byGroup.isSelected() && info instanceof PermissionGroupInfo;

            UnitRecord<?> record = null;
            if (isGroup) {
                record = permManager.getPermissionGroupRecord(name);
            } else {
                record = permManager.getPermissionRecord(name);
            }
            setDescription(record, info);
            setHistoryData(record);
        }

        @Override
        public int getRowHeight() {
            return getColumnModel().getColumn(1).getWidth();
        }
    }

    private class PermissionTableModel extends DefaultTableModel {
        private static final long serialVersionUID = -5182372671185877580L;

        PermissionTableModel() {
            setColumnIdentifiers(
                    new String[] {"", "Icon", "Name", "Label", "Protection Level", "Data"});
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex <= 1 ? Icon.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (byGroup.isSelected() && column == 0) {
                Object data = super.getValueAt(row, 5);
                if (data instanceof PermissionGroupInfoExt) {
                    if (permTable.containCollapseGroup(super.getValueAt(row, 2).toString())) {
                        return UIManager.get("Tree.collapsedIcon");
                    }
                    return UIManager.get("Tree.expandedIcon");
                }
            }
            return super.getValueAt(row, column);
        }
    }

    private class PermissionSorter extends TableRowSorter<TableModel> implements RowSorterListener {

        public PermissionSorter(TableModel model) {
            super(model);
            setComparator();
            setRowFilter(new PermissionFilter());
            addRowSorterListener(this);
        }

        private void setComparator() {
            setMaxSortKeys(1);

            setComparator(0, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return 0;
                }
            });

            setComparator(1, new Comparator<SortedData>() {
                @Override
                public int compare(SortedData o1, SortedData o2) {
                    SortOrder odrder = getSortKeys().get(0).getSortOrder();
                    if (byGroup.isSelected()) {
                        if (o1.weight == o2.weight && !((o1.data == null && o2.data == null)
                                || (o1.data != null && o1.data.equals(o2.data)))) {
                            if (odrder == SortOrder.DESCENDING) {
                                return (o1.isParent) ? 1 : -1;
                            } else {
                                return (o1.isParent) ? -1 : 1;
                            }
                        }
                        return o1.weight - o2.weight;
                    } else {
                        if (o1.weight == o2.weight) {
                            return o1.data.toString().compareTo(o2.data.toString());
                        }
                        return o1.weight - o2.weight;
                    }
                }
            });

            Comparator<?> normal = new Comparator<SortedData>() {
                @Override
                public int compare(SortedData o1, SortedData o2) {
                    SortOrder odrder = getSortKeys().get(0).getSortOrder();
                    if (byGroup.isSelected()) {
                        if (o1.weight == o2.weight) {
                            if (((o1.data == null && o2.data == null)
                                    || (o1.data != null && o1.data.equals(o2.data))))
                                return 0;
                            if (o1.isParent) {
                                return odrder == SortOrder.DESCENDING ? 1 : -1;
                            } else if (o2.isParent) {
                                return odrder == SortOrder.DESCENDING ? -1 : 1;
                            }
                            return o1.data.toString().compareTo(o2.data.toString());
                        }
                        if (odrder == SortOrder.DESCENDING) return o1.weight - o2.weight;
                        return o2.weight - o1.weight;
                    } else {
                        return o1.data.toString().compareTo(o2.data.toString());
                    }
                }
            };

            for (int i = 2; i < getModel().getColumnCount(); i++) {
                setComparator(i, normal);
            }
        }

        @Override
        public void sorterChanged(RowSorterEvent e) {
            if (e.getType() == Type.SORT_ORDER_CHANGED && byGroup.isSelected()) {
                List<? extends SortKey> keys = getSortKeys();
                if (keys.isEmpty()) return;
                SortKey key = keys.get(0);
                if (key.getColumn() != 0) return;
                switch (key.getSortOrder()) {
                    case UNSORTED:
                        return;
                    case ASCENDING:
                        for (PermissionGroupInfoExt g : permManager.getPermissionGroups()) {
                            permTable.addCollapseGroup(g.name);
                        }
                        break;
                    case DESCENDING:
                        permTable.filterClear();
                        break;
                }
                ((DefaultTableModel) getModel()).fireTableDataChanged();
            }
        }
    }

    public class PermissionFilter extends RowFilter<TableModel, Integer> {
        private List<String> filterCollapseGroups = new ArrayList<>();
        private String filterText = "";
        private int baseFilterOut = 0;
        private int flagFilterIn = 0;
        private int filteredFlags = 0;
        private int groupCount = 0;
        private int permCount = 0;

        public void setFilterText(String text) {
            filterText = text;
        }

        public String getFilterText() {
            return filterText;
        }

        public void addCollapseGroup(String groupName) {
            if (filterCollapseGroups.contains(groupName)) return;
            filterCollapseGroups.add(groupName);
        }

        public void removeCollapseGroup(String groupName) {
            if (!filterCollapseGroups.contains(groupName)) return;
            filterCollapseGroups.remove(groupName);
        }

        public boolean containCollapseGroup(String groupName) {
            return filterCollapseGroups.contains(groupName);
        }

        public String[] getCollapseGroups() {
            return filterCollapseGroups.toArray(new String[filterCollapseGroups.size()]);
        }

        public void setFilter(int protectionLevel) {
            baseFilterOut &= ~(protectionLevel & PermissionInfo.PROTECTION_MASK_BASE);
            flagFilterIn |= (protectionLevel & PermissionInfo.PROTECTION_MASK_FLAGS);
        }

        public void clearFilter(int protectionLevel) {
            baseFilterOut |= (protectionLevel & PermissionInfo.PROTECTION_MASK_BASE);
            flagFilterIn &= ~(protectionLevel & PermissionInfo.PROTECTION_MASK_FLAGS);
        }

        public void clear() {
            filterText = "";
            filterCollapseGroups.clear();
            baseFilterOut = 0;
            flagFilterIn = 0;
        }

        public int getGroupCount() {
            return groupCount;
        }

        public int getPermissionCount() {
            return permCount;
        }

        public int getFilteredFlags() {
            return filteredFlags;
        }

        private void resetCount() {
            filteredFlags = 0;
            groupCount = 0;
            permCount = 0;
        }

        @Override
        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            Integer id = entry.getIdentifier();
            if (id == null) return false;
            if (id == 0) resetCount();
            Object data = entry.getValue(5);
            boolean ret = checkFilter(data);
            if (ret) {
                if (data instanceof PermissionGroupInfo)
                    groupCount++;
                else if (data instanceof PermissionInfo) permCount++;
            } else if (data instanceof PermissionInfo) {
                int flags = getProtectionFlags(((PermissionInfo) data));
                filteredFlags |= (flags & PermissionInfo.PROTECTION_MASK_FLAGS);
            }
            ret = ret && checkCollapseGroups(data);
            return ret;
        }

        private boolean checkCollapseGroups(Object data) {
            if (byGroup.isSelected() && !filterCollapseGroups.isEmpty()) {
                if (data instanceof PermissionGroupInfoExt) {
                    return true;
                }
                if (data instanceof RevokedPermissionInfo) {
                    RevokedPermissionInfo info = (RevokedPermissionInfo) data;
                    switch (info.source) {
                        case DECLARED:
                            return !filterCollapseGroups
                                    .contains(PermissionManager.GROUP_NAME_DECLARED);
                        case RECORD:
                        case UNKNOWN:
                            return !filterCollapseGroups
                                    .contains(PermissionManager.GROUP_NAME_REVOKED);
                    }
                } else if (data instanceof DeclaredPermissionInfo) {
                    return !filterCollapseGroups.contains(PermissionManager.GROUP_NAME_DECLARED);
                }
                PermissionInfo info = (PermissionInfo) data;
                if (info.permissionGroup == null || info.permissionGroup.isEmpty()) {
                    return !filterCollapseGroups.contains(PermissionManager.GROUP_NAME_UNSPECIFIED);
                }
                return !filterCollapseGroups.contains(info.permissionGroup);
            }
            return true;
        }

        private boolean checkFilter(Object data) {
            if (data instanceof PermissionGroupInfoExt) {
                for (PermissionInfo info : ((PermissionGroupInfoExt) data).permissions) {
                    if (checkPermissionInfo(info)) return true;
                }
                return false;
            } else if (data instanceof PermissionInfo) {
                return checkPermissionInfo((PermissionInfo) data);
            }
            return true;
        }

        private boolean checkPermissionInfo(PermissionInfo info) {
            if (baseFilterOut != 0) {
                int protectionFlags = getProtectionFlags(info);
                if ((protectionFlags & baseFilterOut) != 0
                        && (protectionFlags & flagFilterIn) == 0) {
                    return false;
                }
            }

            if (filterText == null || filterText.isEmpty()) {
                return true;
            }

            if (PermissionManager.GROUP_NAME_DECLARED.toUpperCase().equals(filterText)) {
                if (info instanceof DeclaredPermissionInfo) {
                    return true;
                }
                if (info instanceof RevokedPermissionInfo) {
                    RevokedPermissionInfo revoked = (RevokedPermissionInfo) info;
                    if (revoked.source == RevokedSource.DECLARED) {
                        return true;
                    }
                }
                return false;
            } else if (PermissionManager.GROUP_NAME_REVOKED.toUpperCase().equals(filterText)) {
                if (info instanceof RevokedPermissionInfo) {
                    RevokedPermissionInfo revoked = (RevokedPermissionInfo) info;
                    if (revoked.source != RevokedSource.DECLARED) {
                        return true;
                    }
                }
                return false;
            } else if (PermissionManager.GROUP_NAME_UNSPECIFIED.toUpperCase().equals(filterText)) {
                return !(info instanceof RevokedPermissionInfo)
                        && !(info instanceof DeclaredPermissionInfo)
                        && (info.permissionGroup == null || info.permissionGroup.isEmpty());
            }

            boolean ret = include(info.name);
            ret = ret || include(info.permissionGroup);
            ret = ret || include(info.protectionLevel);
            if (!ret && info.labels != null) {
                for (ResourceInfo res : info.labels) {
                    ret = include(res.name);
                    if (ret) break;
                }
            }
            if (!ret && info.descriptions != null) {
                for (ResourceInfo res : info.descriptions) {
                    ret = include(res.name);
                    if (ret) break;
                }
            }
            return ret;
        }

        private boolean include(String str) {
            return str != null && str.toUpperCase().contains(filterText);
        }
    }

    private class SortedData implements Icon {
        Object data;
        int weight;
        boolean isParent;
        Icon cacheImage;
        JTable table;

        private SortedData(Object data, int weight, boolean isParent, JTable table) {
            this.data = data != null ? data : "";
            this.weight = weight;
            this.isParent = isParent;
            if (this.data.getClass().equals(ImageIcon.class)) {
                this.table = table;
            }
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (!(data instanceof Icon)) return;
            if (table != null) {
                int size = table.getRowHeight();
                if (cacheImage == null || cacheImage.getIconHeight() != size) {
                    if (((Icon) data).getIconHeight() == table.getRowHeight()) {
                        cacheImage = (Icon) data;
                    } else {
                        cacheImage =
                                ImageScaler.getScaledImageIcon((ImageIcon) data, size, size, false);
                    }
                }
                cacheImage.paintIcon(c, g, x, y);
            } else {
                ((Icon) data).paintIcon(c, g, x, y);
            }
        }

        @Override
        public int getIconWidth() {
            if (!(data instanceof Icon)) return -1;
            if (table != null) {
                return table.getRowHeight();
            }
            return ((Icon) data).getIconWidth();
        }

        @Override
        public int getIconHeight() {
            if (!(data instanceof Icon)) return -1;
            if (table != null) {
                return table.getRowHeight();
            }
            return ((Icon) data).getIconHeight();
        }

        @Override
        public String toString() {
            return (data != null) ? data.toString() : "";
        }
    }
}
