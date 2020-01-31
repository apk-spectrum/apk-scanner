package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.easymode.dlg.EasyToolbarCertDlg;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.util.Log;

@SuppressWarnings("serial")
public class ShowCertDlgAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_SIGN_DLG";

	public ShowCertDlgAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowSignDlg(getWindow(e));
	}

	private void evtShowSignDlg(Window owner) {
		ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowExplorer() apkInfo is null");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}
		new EasyToolbarCertDlg((JFrame) owner, true, apkInfo);
	}
}