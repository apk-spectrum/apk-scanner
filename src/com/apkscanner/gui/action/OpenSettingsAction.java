package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.dialog.SettingDlg;

@SuppressWarnings("serial")
public class OpenSettingsAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_SETTINGS";

	public OpenSettingsAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtSettings(getWindow(e));
	}

	private void evtSettings(Window owner) {
		SettingDlg dlg = new SettingDlg(owner);
		dlg.setVisible(true);

		if(dlg.isNeedRestart()) {
			ApkScanner scanner = getApkScanner();
			if(scanner == null) return;

			if(scanner.getApkInfo() != null) {
				Launcher.run(scanner.getApkInfo().filePath);
			} else {
				Launcher.run();
			}
			owner.dispose();
		}
	}
}