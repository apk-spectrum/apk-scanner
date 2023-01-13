package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;

import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.dialog.ApkSignerWizard;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.Log;

public class SignApkAction extends AbstractApkScannerAction {
    private static final long serialVersionUID = 5083283219636726549L;

    public static final String ACTION_COMMAND = "ACT_CMD_SIGN_APK";

    public SignApkAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evtSignApkFile(getWindow(e));
    }

    private void evtSignApkFile(Window owner) {
        ApkInfo apkInfo = getApkInfo();
        if (apkInfo == null || apkInfo.filePath == null || !new File(apkInfo.filePath).exists()) {
            Log.e("evtSignApkFile() apkInfo is null");
            MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
            return;
        }
        ApkSignerWizard wizard = new ApkSignerWizard((JFrame) owner);
        wizard.setApk(apkInfo.filePath);
        wizard.setVisible(true);
    }
}
