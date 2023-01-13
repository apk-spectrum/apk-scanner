package com.apkscanner.gui.installer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.installer.OptionsBundle;
import com.apkspectrum.swing.HtmlEditorPane;

public class SimpleOptionPanel extends JPanel implements MouseListener, ActionListener {
    private static final long serialVersionUID = -1856410346702035872L;

    public static final String ACT_CMD_SIMPLE_INSTALL = "ACT_CMD_SIMPLE_INSTALL";
    public static final String ACT_CMD_SIMPLE_PUSH = "ACT_CMD_SIMPLE_PUSH";
    public static final String ACT_CMD_SET_ADVANCED_OPT = "ACT_CMD_SET_ADVANCED";

    private Collection<DeviceListData> deviceDataList;

    private HtmlEditorPane htmlEditor;
    private JButton btnInstall;
    private JButton btnPush;

    public SimpleOptionPanel(ActionListener listener) {
        setLayout(new GridBagLayout());

        // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx,
        // double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady)
        GridBagConstraints consts = new GridBagConstraints(0, 0, 1, 1, 1.0f, 1.0f,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 1, 0, 1), 0, 0);

        htmlEditor = new HtmlEditorPane();
        htmlEditor.setText(RFile.RAW_ADB_INSTALL_BUTTON_HTML.getString());
        htmlEditor.removeElementById("adb-push");
        htmlEditor.setInnerHTMLById("option-label", RStr.LABEL_WITH_BELOW_OPTIONS.get());
        StringBuilder options = new StringBuilder();
        options.append("<li>").append(RStr.BTN_REPLACE_EXISTING_APP.get()).append("</li>");
        options.append("<li>").append(RStr.BTN_ALLOW_DOWNGRADE.get()).append("</li>");
        htmlEditor.setInnerHTMLById("install-options", options.toString());

        btnInstall = new JButton(htmlEditor.getText());
        btnInstall.setHorizontalAlignment(SwingConstants.CENTER);
        btnInstall.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnInstall.setIcon(RImg.TOOLBAR_INSTALL.getImageIcon());;
        btnInstall.setIconTextGap(20);
        btnInstall.addActionListener(listener);
        btnInstall.addMouseListener(this);
        btnInstall.setActionCommand(ACT_CMD_SIMPLE_INSTALL);
        add(btnInstall, consts);

        consts.gridy++;
        consts.weighty = 0.2f;

        htmlEditor.setText(RFile.RAW_ADB_INSTALL_BUTTON_HTML.getString());
        htmlEditor.removeElementById("adb-install");
        htmlEditor.setInnerHTMLById("option-label", RStr.LABEL_WITH_OPTIONS.get());
        htmlEditor.setOuterHTMLById("syspath",
                "<span id=\"syspath\">/SYSTEM/(APP or PRIV-APP)</span>");

        btnPush = new JButton(htmlEditor.getText());
        btnPush.addActionListener(listener);
        btnPush.addMouseListener(this);
        btnPush.setActionCommand(ACT_CMD_SIMPLE_PUSH);
        add(btnPush, consts);

        consts.gridy++;
        consts.weighty = 0.0f;
        JButton btnAdvanced = new JButton(String.format(
                "<html><body><h3 style=\"font-weight: normal;\">%s</h3></body></html>",
                RStr.BTN_ADVANCED_OPTIONS_LAB.get()));
        btnAdvanced.addActionListener(this);
        btnAdvanced.addActionListener(listener);
        btnAdvanced.setActionCommand(ACT_CMD_SET_ADVANCED_OPT);
        add(btnAdvanced, consts);
    }

    public void setDeviceListData(Collection<DeviceListData> deviceDataList) {
        this.deviceDataList = deviceDataList;
        if (deviceDataList == null || deviceDataList.isEmpty()) return;

        boolean hasSystemApp = false;
        boolean hasPriveApp = false;
        boolean isPushPosssible = false;

        OptionsBundle bundle = null;
        for (DeviceListData data : deviceDataList) {
            bundle = data.getOptionsBundle();
            if (!bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_PUSH)) {
                if (!isPushPosssible) isPushPosssible = true;
                String path = bundle.getTargetSystemPath();
                if (!hasSystemApp) hasSystemApp = path.startsWith("/system/app/");
                if (!hasPriveApp) hasPriveApp = path.startsWith("/system/priv-app/");
            }
        }
        if (bundle == null) return;

        if (bundle.isSetLaunch()) {
            StringBuilder opt = new StringBuilder();
            opt.append("<li id=\"launch-af-installed\">").append(RStr.BTN_LAUNCH_AF_INSTALLED.get())
                    .append("<br>").append(bundle.getLaunchActivity()).append("</li>");
            htmlEditor.setText(btnInstall.getText());
            if (htmlEditor.getElementById("launch-af-installed") != null) {
                htmlEditor.setOuterHTMLById("launch-af-installed", opt.toString());
            } else {
                htmlEditor.insertElementFirst("install-options", opt.toString());
            }
            btnInstall.setText(htmlEditor.getText());
        } else {
            htmlEditor.setText(btnInstall.getText());
            htmlEditor.removeElementById("launch-af-installed");
            btnInstall.setText(htmlEditor.getText());
        }

        if (isPushPosssible) {
            htmlEditor.setText(btnPush.getText());
            if (hasSystemApp && hasPriveApp) {
                htmlEditor.setOuterHTMLById("syspath",
                        "<span id=\"syspath\">/SYSTEM/(APP or PRIV-APP)</span>");
            } else if (hasSystemApp) {
                htmlEditor.setOuterHTMLById("syspath", "<span id=\"syspath\">/SYSTEM/APP</span>");
            } else if (hasPriveApp) {
                htmlEditor.setOuterHTMLById("syspath",
                        "<span id=\"syspath\">/SYSTEM/PRIV-APP</span>");
            }
            StringBuilder opt = new StringBuilder();
            opt.append("<li>").append(RStr.BTN_REBOOT_AF_PUSHED.get()).append("</li>");
            if (bundle.isSetWithLib32() || bundle.isSetWithLib64()) {
                opt.append("<li>").append(RStr.LABEL_WITH_LIBRARIES.get()).append("(");
                if (bundle.isSetWithLib64()) {
                    opt.append(bundle.getWithLib64Arch()).append(" > ")
                            .append(bundle.getWithLib64ToPath());
                } else {
                    opt.append(bundle.getWithLib32Arch()).append(" > ")
                            .append(bundle.getWithLib32ToPath());
                }
                opt.append(")</li>");
            }
            htmlEditor.setInnerHTMLById("install-options", opt.toString());
            btnPush.setText(htmlEditor.getText());
        }

        btnPush.setVisible(isPushPosssible);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (deviceDataList == null) return;
        String actCmd = arg0.getActionCommand();
        for (DeviceListData data : deviceDataList) {
            OptionsBundle bundle = data.getOptionsBundle();
            switch (actCmd) {
                case ACT_CMD_SET_ADVANCED_OPT:
                    if (bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_INSTALL)) {
                        bundle.set(OptionsBundle.FLAG_OPT_NOT_INSTALL);
                    } else {
                        bundle.set(OptionsBundle.FLAG_OPT_INSTALL);
                    }
                    break;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        if (deviceDataList == null) return;
        String actCmd = ((JButton) arg0.getSource()).getActionCommand();
        for (DeviceListData data : deviceDataList) {
            OptionsBundle bundle = data.getOptionsBundle();
            switch (actCmd) {
                case ACT_CMD_SIMPLE_INSTALL:
                    if (bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_INSTALL)) {
                        bundle.set(OptionsBundle.FLAG_OPT_NOT_INSTALL);
                    } else {
                        bundle.set(OptionsBundle.FLAG_OPT_INSTALL);
                    }
                    break;
                case ACT_CMD_SIMPLE_PUSH:
                    if (bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_PUSH)) {
                        bundle.set(OptionsBundle.FLAG_OPT_NOT_INSTALL);
                    } else {
                        bundle.set(OptionsBundle.FLAG_OPT_PUSH);
                    }
                    break;
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent arg0) {}

    @Override
    public void mouseClicked(MouseEvent arg0) {}

    @Override
    public void mousePressed(MouseEvent arg0) {}

    @Override
    public void mouseReleased(MouseEvent arg0) {}
}
