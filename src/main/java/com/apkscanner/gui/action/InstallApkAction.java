package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.installer.ApkInstallWizard;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.Log;

public class InstallApkAction extends AbstractApkScannerAction
{
	private static final long serialVersionUID = 6109198169080472765L;

	public static final String ACTION_COMMAND = "ACT_CMD_INSTALL_APK";

	public InstallApkAction(ApkActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtInstallApk(getWindow(e));
	}

	private void evtInstallApk(Window window) {
		ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) {
			Log.e("evtInstallApk() apkInfo is null");
			MessageBoxPool.show(window, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
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
