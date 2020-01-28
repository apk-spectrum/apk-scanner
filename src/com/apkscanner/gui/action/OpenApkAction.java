package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkspectrum.swing.ApkFileChooser;
import com.apkspectrum.util.Log;

@SuppressWarnings("serial")
public class OpenApkAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_APK";

	public OpenApkAction(ActionEventHandler h) {
		super(h);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean withShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
		evtOpenApkFile(getWindow(e), withShift);
	}

	protected void evtOpenApkFile(Window owner, boolean newWindow) {
		final String apkFilePath = ApkFileChooser.openApkFilePath(owner);
		if(apkFilePath == null) {
			Log.v("Not choose apk file");
			return;
		}
		final ApkScanner scanner = getApkScanner();
		if(scanner == null) {
			Log.v("evtOpenApkFile() scanner is null");
			return;
		}
		if(!newWindow) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					scanner.clear(false);
					scanner.openApk(apkFilePath);
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		} else {
			Launcher.run(apkFilePath);
		}
	}
}
