package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

@SuppressWarnings("serial")
public class ShowExplorerArchiveAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_EXPLORER_ARCHIVE";

	public ShowExplorerArchiveAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowExplorerArchive();
	}

	private void evtShowExplorerArchive() {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowExplorer() apkInfo is null");
			return;
		}

		SystemUtil.openArchiveExplorer(apkInfo.filePath);
	}
}