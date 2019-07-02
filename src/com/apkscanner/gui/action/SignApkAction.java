package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.dialog.ApkSignerWizard;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public class SignApkAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SIGN_APK";

	public SignApkAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtSignApkFile(getWindow(e));
	}

	private void evtSignApkFile(Window owner) {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null || apkInfo.filePath == null
				|| !new File(apkInfo.filePath).exists()) {
			Log.e("evtSignApkFile() apkInfo is null");
			return;
		}
		ApkSignerWizard wizard = new ApkSignerWizard((JFrame)owner);
		wizard.setApk(apkInfo.filePath);
		wizard.setVisible(true);
	}
}
