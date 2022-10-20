package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.swing.ApkActionEventHandler;

public class OpenSettingsAction extends AbstractApkScannerAction
{
	private static final long serialVersionUID = 8082677456478099417L;

	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_SETTINGS";

	public OpenSettingsAction(ApkActionEventHandler h) { super(h); }

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