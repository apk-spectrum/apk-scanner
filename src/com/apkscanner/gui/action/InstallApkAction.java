package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.installer.ApkInstallWizard;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public class InstallApkAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_INSTALL_APK";

	public InstallApkAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtInstallApk(getWindow(e));
	}

	private void evtInstallApk(Window window) {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) {
			Log.e("evtInstallApk() apkScanner is null");
			return;
		}

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null) {
			Log.e("evtInstallApk() apkInfo is null");
			return;
		}

		ApkInstallWizard wizard = null;
		if(window instanceof JFrame) {
			wizard = new ApkInstallWizard(apkInfo.filePath, (JFrame)window);
		} else if(window instanceof JDialog) {
			wizard = new ApkInstallWizard(apkInfo.filePath, (JDialog)window);
		} else {
			wizard = new ApkInstallWizard(apkInfo.filePath, (JDialog)null);
		}
		wizard.start();
	}
}
