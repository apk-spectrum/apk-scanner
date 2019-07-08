package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.tool.external.JADXLauncher;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

@SuppressWarnings("serial")
public class OpenDecodeJADXGUIAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_JADXGUI";

	public OpenDecodeJADXGUIAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenDecompiler(getWindow(e));
	}

	private void evtOpenDecompiler(final Window owner) {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null || apkInfo.filePath == null
				|| !new File(apkInfo.filePath).exists()) {
			Log.e("evtOpenJDGUI() apkInfo is null");
			return;
		}

		if(!ZipFileUtil.exists(apkInfo.filePath, "classes.dex")) {
			Log.e("No such file : classes.dex");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_CLASSES_DEX);
			return;
		}

		JADXLauncher.run(apkInfo.filePath);
	}
}