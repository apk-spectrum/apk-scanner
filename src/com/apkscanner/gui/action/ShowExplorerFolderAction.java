package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

@SuppressWarnings("serial")
public class ShowExplorerFolderAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_EXPLORER_FOLDER";

	public ShowExplorerFolderAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowExplorerFolder();
	}

	private void evtShowExplorerFolder() {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowExplorer() apkInfo is null");
			return;
		}

		SystemUtil.openFileExplorer(apkInfo.filePath);
	}
}