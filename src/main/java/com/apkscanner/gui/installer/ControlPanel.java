package com.apkscanner.gui.installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.apkscanner.resource.RStr;
import com.apkspectrum.logback.Log;

public class ControlPanel extends JPanel {
    private static final long serialVersionUID = 5959656550868421305L;

    public static final String CTR_ACT_CMD_NEXT = "CTR_ACT_CMD_NEXT";
    public static final String CTR_ACT_CMD_PREVIOUS = "CTR_ACT_CMD_PREVIOUS";
    public static final String CTR_ACT_CMD_OK = "CTR_ACT_CMD_OK";
    public static final String CTR_ACT_CMD_CANCEL = "CTR_ACT_CMD_CANCEL";
    public static final String CTR_ACT_CMD_SHOW_LOG = "CTR_ACT_CMD_SHOW_LOG";
    public static final String CTR_ACT_CMD_RESTART = "CTR_ACT_CMD_RESTART";

    private JButton btnNext;
    private JButton btnPre;
    private JButton btnOk;
    private JButton btnCancel;
    private JButton btnShowLog;
    private JButton btnRestart;

    public ControlPanel(ActionListener listener) {
        super(new BorderLayout());

        btnNext = getButton(RStr.BTN_INSTALL.get(), CTR_ACT_CMD_NEXT, listener);
        btnPre = getButton(RStr.BTN_PREVIOUS.get(), CTR_ACT_CMD_PREVIOUS, listener);
        btnOk = getButton(RStr.BTN_CLOSE.get(), CTR_ACT_CMD_OK, listener);
        btnCancel = getButton(RStr.BTN_CANCEL.get(), CTR_ACT_CMD_CANCEL, listener);
        btnShowLog = getButton("Show Log", CTR_ACT_CMD_SHOW_LOG, listener);
        btnRestart = getButton("Restart", CTR_ACT_CMD_RESTART, listener);

        JPanel stepPanel = new JPanel();

        stepPanel.add(btnPre);
        stepPanel.add(btnNext);
        stepPanel.add(btnRestart);
        stepPanel.add(btnOk);

        JPanel ShowPanel = new JPanel();
        ShowPanel.add(btnShowLog);
        ShowPanel.add(btnCancel);

        add(ShowPanel, BorderLayout.WEST);
        add(stepPanel, BorderLayout.EAST);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.LIGHT_GRAY);
        add(separator, BorderLayout.NORTH);

        // set status
        setStatus(ApkInstallWizard.STATUS_INIT);
    }

    private JButton getButton(String text, String actCmd, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setActionCommand(actCmd);
        btn.addActionListener(listener);
        return btn;
    }

    private void setVisibleButtons(boolean next, boolean pre, boolean ok, boolean cancel,
            boolean showlog, boolean restart) {
        btnNext.setVisible(next);
        btnPre.setVisible(pre);
        btnOk.setVisible(ok);
        btnCancel.setVisible(cancel);
        btnShowLog.setVisible(showlog);
        btnRestart.setVisible(restart);
    }

    public void setStatus(int status) {
        Log.d("" + status);

        btnNext.setEnabled(status != ApkInstallWizard.STATUS_WAIT_FOR_DEVICE);

        switch (status) {
            case ApkInstallWizard.STATUS_INIT:
                setVisibleButtons(false, false, false, false, false, false);
                break;
            case ApkInstallWizard.STATUS_APK_VERIFY:
                setVisibleButtons(false, false, false, false, false, false);
                break;
            case ApkInstallWizard.STATUS_SIMPLE_OPTION:
                setVisibleButtons(false, false, false, true, false, false);
                break;
            case ApkInstallWizard.STATUS_SET_OPTIONS:
                setVisibleButtons(true, true, false, true, false, false);
                break;
            case ApkInstallWizard.STATUS_INSTALLING:
                setVisibleButtons(false, false, false, false, false, false);
                break;
            case ApkInstallWizard.STATUS_COMPLETED:
                setVisibleButtons(false, false, true, false, false, false);
                break;
            case ApkInstallWizard.STATUS_APK_VERTIFY_ERROR:
                setVisibleButtons(false, false, true, false, false, false);
                break;
            default:
                break;
        }
    }

    public void setNextButtonEnable(Boolean flag) {
        btnNext.setEnabled(flag);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 0; // VerticalScrollBar as needed
        d.height = 35;

        return d;
    }
}
