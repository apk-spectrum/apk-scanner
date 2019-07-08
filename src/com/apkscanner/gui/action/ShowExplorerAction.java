package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.messagebox.MessageBoxPool;
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
		boolean withShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
		evtShowExplorer(getWindow(e), withShift);
	}

	private void evtShowExplorer(Window owner, boolean withShift) {
		ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowExplorer() apkInfo is null");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}

		if(withShift) {
			SystemUtil.openFileExplorer(apkInfo.filePath);
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