package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

import org.json.simple.JSONValue;

import com.android.ddmlib.AdbVersion;
import com.apkscanner.gui.TabbedPanel;
import com.apkscanner.gui.ToolBar;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.jna.FileInfo;
import com.apkspectrum.jna.FileVersion;
import com.apkspectrum.logback.Log;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.plugin.gui.PlugInSettingPanel;
import com.apkspectrum.swing.ApkFileChooser;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.WindowSizeMemorizer;
import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;
import com.apkspectrum.tool.adb.AdbVersionManager;
import com.apkspectrum.util.SystemUtil;

public class SettingDlg extends JDialog implements ActionListener {
    private static final long serialVersionUID = -854353051241196941L;

    private static final String ACT_CMD_EDITOR_EXPLOERE = "ACT_CMD_EDITOR_EXPLOERE";
    private static final String ACT_CMD_ADB_EXPLOERE = "ACT_CMD_ADB_EXPLOERE";
    private static final String ACT_CMD_CREATE_SHORTCUT = "ACT_CMD_CREATE_SHORTCUT";
    private static final String ACT_CMD_ASSOCIATE_APK_FILE = "ACT_CMD_ASSOCIATE_APK_FILE";
    private static final String ACT_CMD_ADD_RES_APK_FILE = "ACT_CMD_ADD_RES_APK_FILE";
    private static final String ACT_CMD_REMOVE_RES_APK_FILE = "ACT_CMD_REMOVE_RES_APK_FILE";

    private static final String ACT_CMD_SAVE = "ACT_CMD_SAVE";
    private static final String ACT_CMD_EXIT = "ACT_CMD_EXIT";

    private String propStrLanguage;
    private String propStrEditorPath;
    private ArrayList<String> propRecentEditors;
    private boolean propUiBooster;
    private int propEscAction;

    private String propPreferredLanguage;
    private String propframeworkResPath;
    private ArrayList<String> frameworkResPath;

    private String propTheme;
    private String propTabbedUI;
    private String propFont;
    private int propFontSize;
    private int propFontStyle;
    private boolean propSaveWinSize;

    private boolean propAdbShared;
    private String propAdbPath;
    private boolean propDeviceMonitoring;
    private int propLaunchActivity;
    private boolean propTryUnlock;
    private boolean propLaunchAfInstalled;
    private boolean propAlwaysExtendToolbar;

    private boolean propEasyGui;

    private String propPluginSettings;

    private boolean needUpdateUI;

    private static String fontOfTheme;

    private JPanel previewPanel;
    private JInternalFrame mPreviewFrame;
    private TabbedPanel mPreviewTabbedPanel;
    private ToolBar mPreviewToolBar;


    private JComboBox<String> jcbLanguage;
    private JComboBox<String> jcbEditors;
    private JCheckBox jckUiBooster;
    private JComboBox<String> jcbEscAction;

    private JTextField jtbPreferLang;
    private JList<String> jlFrameworkRes;

    private JRadioButton jrbUseCurrentRunningVer;
    private JRadioButton jrbEasymode;

    private JComboBox<String> jcbAdbPaths;
    private JCheckBox jckEnableDeviceMonitoring;
    private JComboBox<String> jcbLaunchOptions;
    private JComboBox<String> jcbToolbarExtendOptions;
    private JCheckBox jckTryUnlock;
    private JCheckBox jckLauchAfInstalled;

    private JComboBox<String> jcbTheme;
    private JComboBox<String> jcbTabbedUI;
    private JComboBox<String> jcbFont;
    private JComboBox<Integer> jcbFontSize;
    private JToggleButton jtbFontBold;
    private JToggleButton jtbFontItalic;

    private JCheckBox jckRememberWinSize;


    private class EditorItemRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = -151339243781300421L;

        private class CellItem {
            public CellItem(String path) {
                File file = new File(path);
                text = getFileDescription(path);
                if (text == null || text.isEmpty()) {
                    text = file.getName();
                }
                icon = FileSystemView.getFileSystemView().getSystemIcon(file);
            }

            String text;
            Icon icon;
        }

        private HashMap<String, CellItem> items;

        public EditorItemRenderer() {
            setOpaque(false);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);

            items = new HashMap<String, CellItem>();
        }

        private CellItem getCellItem(String path) {
            if (items.containsKey(path)) {
                return items.get(path);
            }
            CellItem cellItem = new CellItem(path);
            items.put(path, cellItem);
            return cellItem;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            CellItem cellItem = getCellItem((String) value);
            setText(cellItem.text);
            setIcon(cellItem.icon);
            return this;
        }

        private String getFileDescription(String filePath) {
            String desc = null;
            if (SystemUtil.isWindows()) {
                try {
                    FileVersion fileVersion = new FileVersion(filePath);
                    for (FileInfo info : fileVersion.getFileInfos()) {
                        desc = info.getFileDescription();
                        if (desc != null && !desc.isEmpty()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            return desc;
        }
    }

    private class ThremeSimpleNameItemRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = 6776371348942306134L;

        private HashMap<String, String> simpleNameMap = new HashMap<String, String>();

        public ThremeSimpleNameItemRenderer() {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                simpleNameMap.put(info.getClassName(), info.getName());
            }
            for (TabbedPaneUIManager.TabbedPaneUIInfo info : TabbedPaneUIManager.getUIThemes()) {
                simpleNameMap.put(info.getClassName(), info.getName());
            }
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String className = (String) value;
            if (simpleNameMap.containsKey(className)) {
                setText(simpleNameMap.get(className));
            } else {
                setText(className);
            }
            return this;
        }
    }

    private class ResourceLangItemRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = 3001512366576666099L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value.toString().isEmpty()) {
                setText("en");
            } else {
                setText(value.toString());
            }
            return this;
        }
    }

    private class FontItemRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = 3001512366576666099L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value.toString().isEmpty()) {
                setText(RStr.SETTINGS_THEME_FONT.get() + " - " + fontOfTheme);
            } else {
                setText(value.toString());
            }
            return this;
        }
    }

    private class AdbItemRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = 2836930268446481101L;

        private HashMap<String, AdbVersion> items;
        private AdbVersion latestVer;

        public AdbItemRenderer(HashMap<String, AdbVersion> map, AdbVersion ver) {
            items = map;
            latestVer = ver;
        }

        private AdbVersion getVersion(String path) {
            AdbVersion version = items.get(path.trim());
            if (version == null) {
                version = AdbVersionManager.getAdbVersion(path.trim());
                if (AdbVersionManager.checkAdbVersion(version)) {
                    Log.w("Warring: unsupported adb : " + path.trim());
                } else if (latestVer == null || version.compareTo(latestVer) > 0) {
                    latestVer = version;
                }
                items.put(path.trim(), version);
            }
            return version;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value == null || value.toString().isEmpty()) {
                setText(RStr.SETTINGS_ADB_AUTO_SEL.get() + " - " + latestVer);
            } else {
                String support = "";
                AdbVersion version = getVersion(value.toString());
                if (!AdbVersionManager.checkAdbVersion(version)) {
                    support = "[Unsupported]";
                }
                setText(support + getVersion(value.toString()) + " - " + value.toString());
            }
            return this;
        }
    }

    private class FontChangedListener implements ItemListener, ActionListener {
        private void changePreviewFont() {
            if (jcbFont == null || jcbFontSize == null || mPreviewFrame == null) return;

            int style = Font.PLAIN;
            if (jtbFontBold.isSelected()) style |= Font.BOLD;
            if (jtbFontItalic.isSelected()) style |= Font.ITALIC;

            String newFont = (String) jcbFont.getSelectedItem();
            setUIFont(new javax.swing.plaf.FontUIResource(newFont, style,
                    (int) jcbFontSize.getSelectedItem()));
            SwingUtilities.updateComponentTreeUI(mPreviewFrame);

            if (jcbTabbedUI != null && mPreviewTabbedPanel != null) {
                String className = (String) jcbTabbedUI.getSelectedItem();
                TabbedPaneUIManager.setUI(mPreviewTabbedPanel, className);
            }
        }

        @Override
        public void itemStateChanged(ItemEvent arg0) {
            changePreviewFont();
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            changePreviewFont();
        }
    }

    public SettingDlg(Window owner) {
        super(owner);

        readSettings();

        Object font = UIManager.get("Label.font");
        UIManager.put("Label.font", null);
        fontOfTheme = new Font(new JLabel().getFont().getFamily(), Font.PLAIN, 12).getFamily();
        UIManager.put("Label.font", font);

        initialize(owner);
    }

    private void initialize(Window window) {
        setTitle(RStr.SETTINGS_TITLE.get());
        setIconImage(RImg.TOOLBAR_SETTING.getImage());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        WindowSizeMemorizer.apply(this, new Dimension(650, 520));

        setResizable(true);
        setLocationRelativeTo(window);
        setModal(true);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.addTab(RStr.TAB_SETTING_GENERIC.get(), null, makeGenericPanel(),
                RStr.TAB_SETTING_GENERIC_LAB.get());
        tabbedPane.addTab(RStr.TAB_SETTING_ANALYSIS.get(), null, makeAnalysisPanel(),
                RStr.TAB_SETTING_ANALYSIS_LAB.get());
        tabbedPane.addTab(RStr.TAB_SETTING_DEVICE.get(), null, makeDevicePanel(),
                RStr.TAB_SETTING_DEVICE_LAB.get());
        tabbedPane.addTab(RStr.TAB_SETTING_DISPLAY.get(), null, makeDisplayPanel(),
                RStr.TAB_SETTING_DISPLAY_LAB.get());
        tabbedPane.addTab(RStr.TAB_SETTING_PLUGINS.get(), null, new PlugInSettingPanel(),
                RStr.TAB_SETTING_PLUGINS_LAB.get());

        JPanel ctrPanel = new JPanel(new FlowLayout());
        JButton savebutton = new JButton(RStr.BTN_SAVE.get());
        savebutton.setActionCommand(ACT_CMD_SAVE);
        savebutton.addActionListener(this);
        savebutton.setFocusable(false);
        ctrPanel.add(savebutton);

        JButton exitbutton = new JButton(RStr.BTN_CANCEL.get());
        exitbutton.setActionCommand(ACT_CMD_EXIT);
        exitbutton.addActionListener(this);
        exitbutton.setFocusable(false);
        ctrPanel.add(exitbutton);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(ctrPanel, BorderLayout.SOUTH);

        getContentPane().add(contentPane);

        KeyStrokeAction.registerKeyStrokeAction(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACT_CMD_EXIT, this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Log.v("windowClosing");
                resotreSetting();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                Log.v("windowClosed");
                resotreSetting();
            }
        });
    }

    private void readSettings() {
        propStrLanguage = RProp.S.LANGUAGE.get();

        propStrEditorPath = SystemUtil.getRealPath(RProp.S.EDITOR.get());

        String recentEditors = RProp.S.RECENT_EDITOR.get();
        propRecentEditors = new ArrayList<String>();
        for (String s : recentEditors.split(File.pathSeparator)) {
            if (!s.isEmpty()) {
                String realPath = SystemUtil.getRealPath(s);
                if (realPath != null && !realPath.equalsIgnoreCase(propStrEditorPath)) {
                    propRecentEditors.add(s);
                }
            }
        }

        propUiBooster = RProp.B.USE_UI_BOOSTER.get();

        propEscAction = RProp.I.ESC_ACTION.get();

        propTheme = RProp.S.CURRENT_THEME.get();

        propTabbedUI = RProp.S.TABBED_UI_THEME.get();
        propTabbedUI = propTabbedUI.replace("com.apkscanner.gui.theme.tabbedpane",
                TabbedPaneUIManager.THEMES_PACKAGE);

        propFont = RProp.S.BASE_FONT.get();

        propFontSize = RProp.I.BASE_FONT_SIZE.get();

        propFontStyle = RProp.I.BASE_FONT_STYLE.get();

        propSaveWinSize = RProp.B.SAVE_WINDOW_SIZE.get();

        propPreferredLanguage = RProp.S.PREFERRED_LANGUAGE.get();

        propframeworkResPath = RProp.S.FRAMEWORK_RES.get();
        frameworkResPath = new ArrayList<String>();
        for (String s : propframeworkResPath.split(File.pathSeparator)) {
            if (!s.isEmpty()) {
                frameworkResPath.add(s);
            }
        }

        propEasyGui = RProp.B.USE_EASY_UI.get();

        propAdbShared = RProp.B.ADB_POLICY_SHARED.get();

        propAdbPath = (RProp.S.ADB_PATH.get()).trim();

        propDeviceMonitoring = RProp.B.ADB_DEVICE_MONITORING.get();

        propLaunchActivity = RProp.I.LAUNCH_ACTIVITY_OPTION.get();

        propTryUnlock = RProp.B.TRY_UNLOCK_AF_LAUNCH.get();

        propLaunchAfInstalled = RProp.B.LAUNCH_AF_INSTALLED.get();

        propAlwaysExtendToolbar = RProp.B.ALWAYS_TOOLBAR_EXTENDED.get();

        propPluginSettings = JSONValue.toJSONString(PlugInManager.getChangedProperties());
    }

    private void saveSettings() {
        if (!propStrLanguage.equals(jcbLanguage.getSelectedItem())) {
            RProp.LANGUAGE.setData(jcbLanguage.getSelectedItem());
        }

        if (!jcbEditors.getSelectedItem().equals(propStrEditorPath)) {
            String editorPath = SystemUtil.getRealPath((String) jcbEditors.getSelectedItem());
            if (propRecentEditors.contains(editorPath)) {
                propRecentEditors.remove(editorPath);
            }
            if (propStrEditorPath != null) {
                propRecentEditors.add(0, propStrEditorPath);
            }
            RProp.EDITOR.setData(editorPath);

            StringBuilder recentEditors = new StringBuilder();
            for (String editor : propRecentEditors) {
                recentEditors.append(editor);
                recentEditors.append(File.pathSeparator);
            }
            RProp.RECENT_EDITOR.setData(recentEditors.toString());
        }

        if (propUiBooster != jckUiBooster.isSelected()) {
            RProp.USE_UI_BOOSTER.setData(jckUiBooster.isSelected());
        }

        if (propEscAction != jcbEscAction.getSelectedIndex()) {
            RProp.ESC_ACTION.setData(jcbEscAction.getSelectedIndex());
        }

        if (!propPreferredLanguage.equals(jtbPreferLang.getText())) {
            RProp.PREFERRED_LANGUAGE.setData(jtbPreferLang.getText().replaceAll(" ", ""));
        }

        String resPaths = "";
        for (String f : frameworkResPath) {
            if (f.isEmpty()) continue;
            resPaths += f + ";";
        }
        if (!propframeworkResPath.equals(resPaths)) {
            RProp.FRAMEWORK_RES.setData(resPaths);
        }

        if (propEasyGui != jrbEasymode.isSelected()) {
            RProp.USE_EASY_UI.setData(jrbEasymode.isSelected());
        }

        if (propAdbShared != jrbUseCurrentRunningVer.isSelected()) {
            RProp.ADB_POLICY_SHARED.setData(jrbUseCurrentRunningVer.isSelected());
        }

        if (!propAdbPath.equals(jcbAdbPaths.getSelectedItem())) {
            RProp.ADB_PATH.setData(jcbAdbPaths.getSelectedItem());
        }

        if (propDeviceMonitoring != jckEnableDeviceMonitoring.isSelected()) {
            RProp.ADB_DEVICE_MONITORING.setData(jckEnableDeviceMonitoring.isSelected());
        }

        if (propLaunchActivity != jcbLaunchOptions.getSelectedIndex()) {
            RProp.LAUNCH_ACTIVITY_OPTION.setData(jcbLaunchOptions.getSelectedIndex());
        }

        if (propTryUnlock != jckTryUnlock.isSelected()) {
            RProp.TRY_UNLOCK_AF_LAUNCH.setData(jckTryUnlock.isSelected());
        }

        if (propLaunchAfInstalled != jckLauchAfInstalled.isSelected()) {
            RProp.LAUNCH_AF_INSTALLED.setData(jckLauchAfInstalled.isSelected());
        }

        needUpdateUI = false;
        if (!propTheme.equals(jcbTheme.getSelectedItem())) {
            RProp.CURRENT_THEME.setData(jcbTheme.getSelectedItem());
            needUpdateUI = true;
        }
        if (!propTabbedUI.equals(jcbTabbedUI.getSelectedItem())) {
            RProp.TABBED_UI_THEME.setData(jcbTabbedUI.getSelectedItem());
            needUpdateUI = true;
        }
        if (!propFont.equals(jcbFont.getSelectedItem())) {
            RProp.BASE_FONT.setData(jcbFont.getSelectedItem());
            needUpdateUI = true;
        }
        if (propFontSize != (int) jcbFontSize.getSelectedItem()) {
            RProp.BASE_FONT_SIZE.setData(jcbFontSize.getSelectedItem());
            needUpdateUI = true;
        }
        int style = Font.PLAIN;
        if (jtbFontBold.isSelected()) style |= Font.BOLD;
        if (jtbFontItalic.isSelected()) style |= Font.ITALIC;
        if (propFontStyle != style) {
            RProp.BASE_FONT_STYLE.setData(style);
            needUpdateUI = true;
        }
        if (propSaveWinSize != jckRememberWinSize.isSelected()) {
            RProp.SAVE_WINDOW_SIZE.setData(jckRememberWinSize.isSelected());
        }

        if (propAlwaysExtendToolbar != (jcbToolbarExtendOptions.getSelectedIndex() == 1)) {
            RProp.ALWAYS_TOOLBAR_EXTENDED.setData(jcbToolbarExtendOptions.getSelectedIndex() == 1);
            needUpdateUI = true;
        }

        String pluginSetting = JSONValue.toJSONString(PlugInManager.getChangedProperties());
        if (propPluginSettings != null && !propPluginSettings.equals(pluginSetting)) {
            PlugInManager.saveProperty();
            needUpdateUI = true;
        }
        if (propEasyGui != jrbEasymode.isSelected()) {
            needUpdateUI = true;
        }
    }

    JPanel makeGenericPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);

        // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx,
        // double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady)
        GridBagConstraints rowHeadConst = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 0, 10), 0, 0);
        GridBagConstraints contentConst = new GridBagConstraints(1, 0, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0);

        panel.add(new JLabel(RStr.SETTINGS_LANGUAGE.get()), rowHeadConst);

        jcbLanguage = new JComboBox<String>(RStr.getSupportedLanguages());
        jcbLanguage.setRenderer(new ResourceLangItemRenderer());
        jcbLanguage.setSelectedItem(propStrLanguage);
        propStrLanguage = (String) jcbLanguage.getSelectedItem();
        panel.add(jcbLanguage, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        panel.add(new JLabel(RStr.SETTINGS_EDITOR.get()), rowHeadConst);

        final JTextField editorPath = new JTextField();
        editorPath.setEditable(false);

        jcbEditors = new JComboBox<String>();
        jcbEditors.setRenderer(new EditorItemRenderer());
        jcbEditors.setEditable(false);
        jcbEditors.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                editorPath.setText(arg0.getItem().toString());
            }
        });

        if (propStrEditorPath != null) {
            jcbEditors.addItem(propStrEditorPath);
        }
        for (String editor : propRecentEditors) {
            jcbEditors.addItem(editor);
        }

        if (SystemUtil.isWindows()) {
            try {
                for (String suffix : new String[] {".txt", "txtfile", "textfile", ".xml", ".log"}) {
                    String cmdLine = SystemUtil.getOpenCommand(suffix);
                    if (cmdLine != null && cmdLine.contains("%1")) {
                        String cmd = cmdLine.replaceAll("\"?(.*\\.[eE][xX][eE])\"?.*", "$1");
                        if (!cmd.equals(cmdLine)) {
                            String path = SystemUtil.getRealPath(cmd);
                            if (path != null && !propRecentEditors.contains(path)
                                    && !path.equalsIgnoreCase(propStrEditorPath)) {
                                jcbEditors.addItem(path);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JButton btnExplorer = new JButton(RStr.BTN_SELF_SEARCH.get());
        btnExplorer.setToolTipText(RStr.BTN_SELF_SEARCH_LAB.get());
        btnExplorer.setMargin(new Insets(-1, 10, -1, 10));
        btnExplorer.setActionCommand(ACT_CMD_EDITOR_EXPLOERE);
        btnExplorer.addActionListener(this);


        JPanel txtEditPane = new JPanel(new BorderLayout(5, 5));
        txtEditPane.add(jcbEditors, BorderLayout.CENTER);
        txtEditPane.add(btnExplorer, BorderLayout.EAST);
        txtEditPane.add(editorPath, BorderLayout.SOUTH);

        contentConst.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtEditPane, contentConst);
        contentConst.fill = GridBagConstraints.NONE;

        rowHeadConst.gridy++;
        contentConst.gridy++;

        panel.add(new JLabel(RStr.SETTINGS_TOOLBAR.get()), rowHeadConst);

        JPanel toolbarPane = new JPanel();
        toolbarPane.setLayout(new BoxLayout(toolbarPane, BoxLayout.Y_AXIS));

        jcbToolbarExtendOptions = new JComboBox<String>(new String[] {
                RStr.SETTINGS_TOOLBAR_WITH_SHIFT.get(), RStr.SETTINGS_TOOLBAR_WITHOUT_SHIFT.get()});
        jcbToolbarExtendOptions.setEditable(false);
        jcbToolbarExtendOptions.setSelectedIndex(propAlwaysExtendToolbar ? 1 : 0);
        toolbarPane.add(jcbToolbarExtendOptions);

        jckEnableDeviceMonitoring = new JCheckBox(RStr.SETTINGS_ADB_MONITOR.get());
        jckEnableDeviceMonitoring.setSelected(propDeviceMonitoring);
        jckEnableDeviceMonitoring.addActionListener(this);
        jckEnableDeviceMonitoring.setAlignmentX(1);
        toolbarPane.add(jckEnableDeviceMonitoring);

        panel.add(toolbarPane, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        if (SystemUtil.isWindows()) {
            String exePath = RFile.ETC_APKSCANNER_EXE.getPath();

            JPanel etcBtnPanel = new JPanel();
            if (!SystemUtil.hasShortCut(exePath, RStr.APP_NAME.get())) {
                JButton btnShortcut = new JButton(RStr.BTN_CREATE_SHORTCUT.get());
                btnShortcut.setToolTipText(RStr.BTN_CREATE_SHORTCUT_LAB.get());
                btnShortcut.setActionCommand(ACT_CMD_CREATE_SHORTCUT);
                btnShortcut.addActionListener(this);
                btnShortcut.setIcon(RImg.ADD_TO_DESKTOP.getImageIcon(32, 32));
                btnShortcut.setVerticalTextPosition(JLabel.CENTER);
                btnShortcut.setHorizontalTextPosition(JLabel.RIGHT);
                etcBtnPanel.add(btnShortcut);
            }

            JButton btnAssociate = new JButton();
            if (!SystemUtil.isAssociatedWithFileType(".apk", exePath)) {
                btnAssociate.setText(RStr.BTN_ASSOC_FTYPE.get());
                btnAssociate.setToolTipText(RStr.BTN_ASSOC_FTYPE_LAB.get());
                btnAssociate.setIcon(RImg.ASSOCIATE_APK.getImageIcon(32, 32));
            } else {
                btnAssociate.setText(RStr.BTN_UNASSOC_FTYPE.get());
                btnAssociate.setToolTipText(RStr.BTN_UNASSOC_FTYPE_LAB.get());
                btnAssociate.setIcon(RImg.UNASSOCIATE_APK.getImageIcon(32, 32));
            }
            btnAssociate.setActionCommand(ACT_CMD_ASSOCIATE_APK_FILE);
            btnAssociate.addActionListener(this);
            btnAssociate.setVerticalTextPosition(JLabel.CENTER);
            btnAssociate.setHorizontalTextPosition(JLabel.RIGHT);

            etcBtnPanel.add(btnAssociate);

            panel.add(etcBtnPanel, contentConst);

            rowHeadConst.gridy++;
            contentConst.gridy++;
        }

        panel.add(new JLabel(RStr.LABEL_UI_MODE.get()), rowHeadConst);

        JPanel selectEasyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel EasyPanel = new JPanel(new BorderLayout());

        JRadioButton jrbStandard = new JRadioButton(RStr.SETTINGS_EASYMODE_OPTION_STANDARD.get());
        jrbEasymode = new JRadioButton(RStr.SETTINGS_EASYMODE_OPTION_EASY.get());

        final JLabel preview = new JLabel("", JLabel.CENTER);
        preview.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        // preview.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        ActionListener radioAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (jrbEasymode.isSelected()) {
                    preview.setIcon(RImg.PREVIEW_EASY.getImageIcon(250, 150));
                } else {
                    preview.setIcon(RImg.PREVIEW_ORIGINAL.getImageIcon(250, 150));
                }
            }
        };
        jrbEasymode.addActionListener(radioAction);
        jrbStandard.addActionListener(radioAction);


        if (propEasyGui) {
            jrbEasymode.setSelected(true);
            preview.setIcon(RImg.PREVIEW_EASY.getImageIcon(250, 150));
        } else {
            jrbStandard.setSelected(true);
            preview.setIcon(RImg.PREVIEW_ORIGINAL.getImageIcon(250, 150));
        }

        ButtonGroup adbPolicyGroup = new ButtonGroup();
        adbPolicyGroup.add(jrbStandard);
        adbPolicyGroup.add(jrbEasymode);

        GridBagConstraints c = new GridBagConstraints();

        selectEasyPanel.add(jrbStandard, c);
        selectEasyPanel.add(jrbEasymode, c);
        selectEasyPanel.add(new JLabel(RStr.LABEL_CHANGE_UI_SHORTKEY.get()));

        EasyPanel.add(selectEasyPanel, BorderLayout.SOUTH);
        EasyPanel.add(preview, BorderLayout.CENTER);

        panel.add(EasyPanel, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        jckUiBooster = new JCheckBox(RStr.SETTINGS_USE_UI_BOOSTER.get());
        jckUiBooster.setSelected(propUiBooster);
        jckUiBooster.addActionListener(this);
        jckUiBooster.setAlignmentX(1);

        panel.add(new JLabel(RStr.LABEL_UI_BOOSTER.get()), rowHeadConst);
        panel.add(jckUiBooster, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        jcbEscAction = new JComboBox<String>();
        jcbEscAction.setEditable(false);
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case RConst.INT_ESC_ACT_NONE:
                    jcbEscAction.addItem(RStr.SETTINGS_ESC_ACT_NONE.get());
                    break;
                case RConst.INT_ESC_ACT_CHANG_UI_MODE:
                    jcbEscAction.addItem(RStr.SETTINGS_ESC_ACT_CHANG_UI_MODE.get());
                    break;
                case RConst.INT_ESC_ACT_EXIT:
                    jcbEscAction.addItem(RStr.SETTINGS_ESC_ACT_EXIT.get());
                    break;
            }
        } ;
        jcbEscAction.setSelectedIndex(propEscAction);

        panel.add(new JLabel(RStr.LABEL_ESC_KEY.get()), rowHeadConst);
        panel.add(jcbEscAction, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        rowHeadConst.gridwidth = 2;
        rowHeadConst.weighty = 1;
        panel.add(new JPanel(), rowHeadConst);

        return panel;
    }

    JPanel makeAnalysisPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);

        GridBagConstraints contentConst = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0);

        panel.add(new JLabel(RStr.SETTINGS_PREFERRED_LANG.get()), contentConst);
        contentConst.gridx++;

        contentConst.weightx = 1;
        contentConst.fill = GridBagConstraints.HORIZONTAL;
        jtbPreferLang = new JTextField(propPreferredLanguage);
        panel.add(jtbPreferLang, contentConst);

        contentConst.gridy++;
        panel.add(new JLabel(RStr.SETTINGS_PREFERRED_EX.get()), contentConst);

        contentConst.gridy++;
        contentConst.gridx = 0;
        contentConst.gridwidth = 2;
        contentConst.weightx = 0;
        // contentConst.fill = GridBagConstraints.NONE;

        panel.add(new JLabel(), contentConst);
        contentConst.gridy++;

        panel.add(new JLabel(RStr.SETTINGS_RES.get()), contentConst);
        contentConst.gridy++;

        JPanel resPanel = new JPanel(new GridBagLayout());

        GridBagConstraints resConst = new GridBagConstraints(0, 0, 1, 2, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0);


        jlFrameworkRes = new JList<String>();
        jlFrameworkRes.setListData(frameworkResPath.toArray(new String[0]));

        JScrollPane scrollPane1 = new JScrollPane(jlFrameworkRes);
        scrollPane1.setPreferredSize(new Dimension(50, 400));
        resPanel.add(scrollPane1, resConst);

        resConst.gridx = 1;
        resConst.gridheight = 1;
        resConst.weightx = 0;
        resConst.fill = GridBagConstraints.NONE;

        JButton btnAdd = new JButton(RStr.BTN_ADD.get());
        btnAdd.setActionCommand(ACT_CMD_ADD_RES_APK_FILE);
        btnAdd.addActionListener(this);
        btnAdd.setFocusable(false);
        resPanel.add(btnAdd, resConst);
        resConst.gridy++;

        JButton btnRemove = new JButton(RStr.BTN_DEL.get());
        btnRemove.setActionCommand(ACT_CMD_REMOVE_RES_APK_FILE);
        btnRemove.addActionListener(this);
        btnRemove.setFocusable(false);
        resPanel.add(btnRemove, resConst);

        panel.add(resPanel, contentConst);
        contentConst.gridy++;

        contentConst.weighty = 1;
        panel.add(new JPanel(), contentConst);

        return panel;
    }

    JPanel makeDevicePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);

        // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx,
        // double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady)
        GridBagConstraints rowHeadConst = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 0, 10), 0, 0);
        GridBagConstraints contentConst = new GridBagConstraints(1, 0, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0);

        contentConst.gridx = 0;
        contentConst.gridwidth = 2;
        contentConst.fill = GridBagConstraints.BOTH;


        JPanel adbPolicyPanel = new JPanel(new GridLayout(0, 1));
        adbPolicyPanel.setBorder(new TitledBorder(RStr.SETTINGS_ADB_POLICY.get()));

        jrbUseCurrentRunningVer = new JRadioButton(RStr.SETTINGS_ADB_SHARED.get());
        JRadioButton rbRestartAdbServer = new JRadioButton(RStr.SETTINGS_ADB_RESTART.get());

        final JLabel jlbAdbPolicyLabel = new JLabel();
        ActionListener radioAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (jrbUseCurrentRunningVer.isSelected()) {
                    jlbAdbPolicyLabel.setText(RStr.SETTINGS_ADB_SHARED_LAB.get());
                } else {
                    jlbAdbPolicyLabel.setText(RStr.SETTINGS_ADB_RESTART_LAB.get());
                }
            }
        };
        jrbUseCurrentRunningVer.addActionListener(radioAction);
        rbRestartAdbServer.addActionListener(radioAction);

        ButtonGroup adbPolicyGroup = new ButtonGroup();
        adbPolicyGroup.add(jrbUseCurrentRunningVer);
        adbPolicyGroup.add(rbRestartAdbServer);

        adbPolicyPanel.add(jrbUseCurrentRunningVer);
        adbPolicyPanel.add(rbRestartAdbServer);

        if (propAdbShared) {
            jrbUseCurrentRunningVer.setSelected(true);
        } else {
            rbRestartAdbServer.setSelected(true);
        }

        GridBagConstraints adbPathConst = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0);
        JPanel adbPathPanel = new JPanel(new GridBagLayout());

        adbPathPanel.add(new JLabel(RStr.SETTINGS_ADB_PATH.get()), adbPathConst);
        adbPathConst.gridx++;

        jcbAdbPaths = new JComboBox<String>();
        jcbAdbPaths.setEditable(false);
        jcbAdbPaths.setSelectedItem(propAdbPath);
        jcbAdbPaths.addItem("");

        new SwingWorker<HashMap<String, AdbVersion>, Void>() {
            @Override
            protected HashMap<String, AdbVersion> doInBackground() throws Exception {
                AdbVersionManager.loadCache();
                AdbVersionManager.loadDefaultAdbs();
                return AdbVersionManager.getAdbListFromCacheMap();
            }

            @Override
            protected void done() {
                try {
                    HashMap<String, AdbVersion> adbMap = get();
                    AdbVersion lastestVers = null;
                    for (Entry<String, AdbVersion> entry : adbMap.entrySet()) {
                        jcbAdbPaths.addItem(entry.getKey());
                        AdbVersion ver = entry.getValue();
                        if (lastestVers == null
                                || (ver != null && ver.compareTo(lastestVers) > 0)) {
                            lastestVers = ver;
                        }
                    }
                    jcbAdbPaths.setRenderer(new AdbItemRenderer(adbMap, lastestVers));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            };
        }.execute();

        adbPathConst.weightx = 1;
        adbPathConst.fill = GridBagConstraints.BOTH;
        adbPathPanel.add(jcbAdbPaths, adbPathConst);
        adbPathConst.weightx = 0;
        adbPathConst.fill = GridBagConstraints.NONE;

        adbPathConst.gridx++;

        JButton btnExplorer = new JButton(RStr.BTN_SELF_SEARCH.get());
        btnExplorer.setToolTipText(RStr.BTN_SELF_SEARCH_LAB.get());
        btnExplorer.setMargin(new Insets(-1, 10, -1, 10));
        btnExplorer.setActionCommand(ACT_CMD_ADB_EXPLOERE);
        btnExplorer.addActionListener(this);

        adbPathPanel.add(btnExplorer, adbPathConst);

        adbPolicyPanel.add(adbPathPanel);

        if (propAdbShared) {
            jlbAdbPolicyLabel.setText(RStr.SETTINGS_ADB_SHARED_LAB.get());
        } else {
            jlbAdbPolicyLabel.setText(RStr.SETTINGS_ADB_RESTART_LAB.get());
        }
        adbPolicyPanel.add(jlbAdbPolicyLabel);

        panel.add(adbPolicyPanel, contentConst);


        contentConst.fill = GridBagConstraints.NONE;
        rowHeadConst.gridy++;
        contentConst.gridy++;


        panel.add(new JPanel(), contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;



        JPanel launchPolicyPanel = new JPanel(new GridLayout(0, 1));
        launchPolicyPanel.setBorder(new TitledBorder(RStr.SETTINGS_LAUNCH_OPTION.get()));


        jcbLaunchOptions = new JComboBox<String>(new String[] {RStr.SETTINGS_LAUNCHER_OR_MAIN.get(),
                RStr.SETTINGS_LAUNCHER_ONLY.get(), RStr.SETTINGS_LAUNCHER_CONFIRM.get()});
        jcbLaunchOptions.setEditable(false);
        jcbLaunchOptions.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {

            }

        });
        jcbLaunchOptions.setSelectedIndex(propLaunchActivity);
        launchPolicyPanel.add(jcbLaunchOptions);

        jckTryUnlock = new JCheckBox(RStr.SETTINGS_TRY_UNLOCK.get());
        jckTryUnlock.setSelected(propTryUnlock);
        launchPolicyPanel.add(jckTryUnlock);

        jckLauchAfInstalled = new JCheckBox(RStr.SETTINGS_LAUNCH_INSTALLED.get());
        jckLauchAfInstalled.setSelected(propLaunchAfInstalled);
        launchPolicyPanel.add(jckLauchAfInstalled);

        contentConst.fill = GridBagConstraints.BOTH;
        panel.add(launchPolicyPanel, contentConst);

        contentConst.fill = GridBagConstraints.NONE;

        contentConst.gridx = 1;
        contentConst.gridwidth = 1;

        rowHeadConst.gridy++;
        contentConst.gridy++;

        rowHeadConst.weighty = 1;
        panel.add(new JPanel(), rowHeadConst);

        return panel;
    }

    JPanel makeDisplayPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);

        GridBagConstraints rowHeadConst = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 0, 10), 0, 0);
        GridBagConstraints contentConst = new GridBagConstraints(1, 0, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0);

        panel.add(new JLabel(RStr.SETTINGS_THEME.get()), rowHeadConst);

        jcbTheme = new JComboBox<String>();
        jcbTheme.setFocusable(false);
        jcbTheme.setRenderer(new ThremeSimpleNameItemRenderer());
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            jcbTheme.addItem(info.getClassName());
        }
        jcbTheme.setSelectedItem(propTheme);
        jcbTheme.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                String theme = (String) arg0.getItem();

                setUIFont(null);
                try {
                    UIManager.setLookAndFeel(theme);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException e1) {
                    e1.printStackTrace();
                }

                fontOfTheme =
                        new Font(new JLabel().getFont().getFamily(), Font.PLAIN, 12).getFamily();
                setUIFont(
                        new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));

                SwingUtilities.updateComponentTreeUI(SettingDlg.this);

                if (jcbFont != null && jcbFontSize != null && mPreviewFrame != null) {
                    String newFont = (String) jcbFont.getSelectedItem();
                    // if(newFont.isEmpty()) newFont = fontOfTheme;
                    setUIFont(new javax.swing.plaf.FontUIResource(newFont, java.awt.Font.PLAIN,
                            (int) jcbFontSize.getSelectedItem()));
                    SwingUtilities.updateComponentTreeUI(mPreviewFrame);
                }

                if (jcbTabbedUI != null && mPreviewTabbedPanel != null) {
                    String className = (String) jcbTabbedUI.getSelectedItem();
                    TabbedPaneUIManager.setUI(mPreviewTabbedPanel, className);
                }

                if (jtbFontBold != null) {
                    jtbFontBold.setPreferredSize(null);
                    Dimension fCompSize = jtbFontBold.getPreferredSize();
                    if (fCompSize.height > fCompSize.width) {
                        jtbFontBold.setPreferredSize(
                                new Dimension(fCompSize.height, fCompSize.height));
                    }
                }
                if (jtbFontItalic != null) {
                    jtbFontItalic.setPreferredSize(null);
                    Dimension fCompSize = jtbFontItalic.getPreferredSize();
                    if (fCompSize.height > fCompSize.width) {
                        jtbFontItalic.setPreferredSize(
                                new Dimension(fCompSize.height, fCompSize.height));
                    }
                }
            }
        });
        panel.add(jcbTheme, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        panel.add(new JLabel(RStr.SETTINGS_TABBED_UI.get()), rowHeadConst);

        jcbTabbedUI = new JComboBox<String>(TabbedPaneUIManager.getUIClassNames());
        jcbTabbedUI.setFocusable(false);
        jcbTabbedUI.setRenderer(new ThremeSimpleNameItemRenderer());
        jcbTabbedUI.setSelectedItem(propTabbedUI);
        jcbTabbedUI.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (mPreviewTabbedPanel == null) return;
                String className = (String) arg0.getItem();
                TabbedPaneUIManager.setUI(mPreviewTabbedPanel, className);
            }
        });
        panel.add(jcbTabbedUI, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        panel.add(new JLabel(RStr.SETTINGS_FONT.get()), rowHeadConst);

        FontChangedListener fontChangedListener = new FontChangedListener();

        String[] fontFamilyNames =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(fontFamilyNames);

        jcbFont = new JComboBox<String>(fontFamilyNames);
        jcbFont.setRenderer(new FontItemRenderer());
        jcbFont.addItem("");
        jcbFont.addItemListener(fontChangedListener);
        jcbFont.setSelectedItem(propFont);

        // Dimension fCompSize = jcbFont.getPreferredSize();

        jcbFontSize = new JComboBox<Integer>(
                new Integer[] {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
        jcbFontSize.addItemListener(fontChangedListener);
        jcbFontSize.setSelectedItem(propFontSize);
        // jcbFontSize.setPreferredSize(new Dimension(jcbFontSize.getPreferredSize().width,
        // fCompSize.height));

        jtbFontBold = new JToggleButton();
        jtbFontBold.setText("B");
        jtbFontBold.setFocusable(false);
        jtbFontBold.setMargin(new Insets(0, 0, 0, 0));
        Dimension fCompSize = jtbFontBold.getPreferredSize();
        if (fCompSize.height > fCompSize.width) {
            jtbFontBold.setPreferredSize(new Dimension(fCompSize.height, fCompSize.height));
        }
        jtbFontBold.addActionListener(fontChangedListener);
        jtbFontBold.setSelected((propFontStyle & Font.BOLD) != 0);

        jtbFontItalic = new JToggleButton();
        jtbFontItalic.setText("I");
        jtbFontItalic.setFocusable(false);
        jtbFontItalic.setMargin(new Insets(0, 0, 0, 0));
        fCompSize = jtbFontItalic.getPreferredSize();
        if (fCompSize.height > fCompSize.width) {
            jtbFontItalic.setPreferredSize(new Dimension(fCompSize.height, fCompSize.height));
        }
        jtbFontItalic.addActionListener(fontChangedListener);
        jtbFontItalic.setSelected((propFontStyle & Font.ITALIC) != 0);

        JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fontPanel.add(jcbFont);
        fontPanel.add(jcbFontSize);
        fontPanel.add(jtbFontBold);
        fontPanel.add(jtbFontItalic);

        panel.add(fontPanel, contentConst);

        rowHeadConst.gridy++;
        contentConst.gridy++;

        contentConst.gridx = 0;
        contentConst.gridwidth = 2;
        contentConst.fill = GridBagConstraints.BOTH;
        contentConst.weighty = 1;

        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(new TitledBorder(RStr.SETTINGS_THEME_PREVIEW.get()));
        previewPanel.setPreferredSize(new Dimension(0, 170));

        mPreviewFrame = new JInternalFrame(RStr.APP_NAME.get(), false, false, false, false);
        mPreviewToolBar = new ToolBar(null);
        mPreviewTabbedPanel = new TabbedPanel(propTabbedUI, null);
        mPreviewFrame.setFrameIcon(RImg.APP_ICON.getImageIcon(16, 16));
        mPreviewFrame.getContentPane().add(mPreviewToolBar, BorderLayout.NORTH);
        mPreviewFrame.getContentPane().add(mPreviewTabbedPanel, BorderLayout.CENTER);
        mPreviewFrame.setVisible(true);
        previewPanel.add(mPreviewFrame);

        panel.add(previewPanel, contentConst);
        rowHeadConst.gridy++;
        contentConst.gridy++;

        contentConst.weighty = 0;

        jckRememberWinSize = new JCheckBox(RStr.SETTINGS_REMEMBER_WINDOW_SIZE.get());
        jckRememberWinSize.setSelected(propSaveWinSize);
        panel.add(jckRememberWinSize, contentConst);

        contentConst.gridx = 1;
        contentConst.gridwidth = 1;

        rowHeadConst.gridy++;
        contentConst.gridy++;

        rowHeadConst.gridwidth = 2;
        // rowHeadConst.weighty = 1;
        panel.add(new JPanel(), rowHeadConst);

        return panel;
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                if (!"InternalFrame.titleFont".equals(key)) {
                    UIManager.put(key, f);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actCommand = e.getActionCommand();

        if (ACT_CMD_EDITOR_EXPLOERE.equals(actCommand)) {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

            File dir = jfc.getSelectedFile();
            if (dir != null) {
                String path = dir.getPath();
                jcbEditors.addItem(path);
                jcbEditors.setSelectedItem(path);
            }
        } else if (ACT_CMD_CREATE_SHORTCUT.equals(actCommand)) {
            String exePath = RFile.ETC_APKSCANNER_EXE.getPath();
            String shortCutName = RStr.APP_NAME.get();
            SystemUtil.createShortCut(exePath, shortCutName);
            if (SystemUtil.hasShortCut(exePath, shortCutName)) {
                ((JButton) e.getSource()).setVisible(false);
            }
        } else if (ACT_CMD_ASSOCIATE_APK_FILE.equals(actCommand)) {
            String exePath = RFile.ETC_APKSCANNER_EXE.getPath();
            JButton btn = (JButton) e.getSource();
            if (!SystemUtil.isAssociatedWithFileType(".apk", exePath)) {
                SystemUtil.setAssociateFileType(".apk", exePath);
                btn.setText(RStr.BTN_UNASSOC_FTYPE.get());
                btn.setToolTipText(RStr.BTN_UNASSOC_FTYPE_LAB.get());
                btn.setIcon(RImg.UNASSOCIATE_APK.getImageIcon(32, 32));
            } else {
                SystemUtil.unsetAssociateFileType(".apk", exePath);
                btn.setText(RStr.BTN_ASSOC_FTYPE.get());
                btn.setToolTipText(RStr.BTN_ASSOC_FTYPE_LAB.get());
                btn.setIcon(RImg.ASSOCIATE_APK.getImageIcon(32, 32));
            }
        } else if (ACT_CMD_SAVE.equals(actCommand)) {
            saveSettings();
            this.dispose();
        } else if (ACT_CMD_EXIT.equals(actCommand)) {
            resotreSetting();
            this.dispose();
        } else if (ACT_CMD_ADD_RES_APK_FILE.equals(actCommand)) {
            String file = ApkFileChooser.openApkFilePath(this);

            if (file == null || file.isEmpty()) return;
            for (String f : frameworkResPath) {
                if (file.equals(f)) return;
            }
            frameworkResPath.add(file);
            jlFrameworkRes.setListData(frameworkResPath.toArray(new String[0]));
        } else if (ACT_CMD_REMOVE_RES_APK_FILE.equals(actCommand)) {
            if (jlFrameworkRes.getSelectedIndex() < 0) return;
            frameworkResPath.remove(jlFrameworkRes.getSelectedIndex());
            jlFrameworkRes.setListData(frameworkResPath.toArray(new String[0]));
        } else if (ACT_CMD_ADB_EXPLOERE.equals(actCommand)) {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            if (SystemUtil.isWindows()) {
                jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Android Debug Bridge(.exe)", "exe"));
            }
            if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File file = jfc.getSelectedFile();
            AdbVersion ver = AdbVersionManager.getAdbVersion(file);
            if (AdbVersionManager.checkAdbVersion(ver)) {
                return;
            }
            String path = file.getAbsolutePath();

            jcbAdbPaths.setSelectedItem(path);
            if (!path.equals(jcbAdbPaths.getSelectedItem())) {
                jcbAdbPaths.addItem(path);
            }
            jcbAdbPaths.setSelectedItem(path);
        }
    }

    public boolean isNeedRestart() {
        return needUpdateUI;
    }

    private void resotreSetting() {
        Log.v("resotreSetting()");

        if (!propTheme.equals(jcbTheme.getSelectedItem())) {
            Log.v("resotre LookAndFeel");
            try {
                UIManager.setLookAndFeel(propTheme);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e1) {
                e1.printStackTrace();
            }
        }

        boolean needRestoreFont = false;
        if (!propTabbedUI.equals(jcbTabbedUI.getSelectedItem())) {
            needRestoreFont = true;
        } else if (!propFont.equals(jcbFont.getSelectedItem())) {
            needRestoreFont = true;
        } else if (propFontSize != (int) jcbFontSize.getSelectedItem()) {
            needRestoreFont = true;
        }
        if (needRestoreFont) {
            Log.v("resotre Font");
            setUIFont(new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));
        }

        String pluginSetting = JSONValue.toJSONString(PlugInManager.getChangedProperties());
        if (propPluginSettings != null && !propPluginSettings.equals(pluginSetting)) {
            Log.v("resotre Plugin property");
            PlugInManager.loadPlugIn();
            propPluginSettings = null;
        }
    }
}
