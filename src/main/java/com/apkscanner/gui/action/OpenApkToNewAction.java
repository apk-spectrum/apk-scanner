package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.swing.ApkActionEventHandler;

public class OpenApkToNewAction extends OpenApkAction {
    private static final long serialVersionUID = 2299977488304985752L;

    public static final String ACTION_COMMAND = "ACT_CMD_OPEN_APK_TO_NEW";

    public OpenApkToNewAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evtOpenApkFile(getWindow(e), true);
    }
}
