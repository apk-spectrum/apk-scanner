package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

@SuppressWarnings("serial")
public class ShowExplorerAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_EXPLORER";

	public ShowExplorerAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowExplorer();
	}

	private void evtShowExplorer() {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowExplorer() apkInfo is null");
			return;
		}

		switch(RProp.S.DEFAULT_EXPLORER.get()) {
		case RConst.STR_EXPLORER_ARCHIVE:
			SystemUtil.openArchiveExplorer(apkInfo.filePath);
			break;
		case RConst.STR_EXPLORER_FOLDER:
			SystemUtil.openFileExplorer(apkInfo.filePath);
			break;
		default:
			Log.e("evtShowExplorer() unknown type : " + RProp.S.DEFAULT_EXPLORER.get());
			break;
		}
	}
}