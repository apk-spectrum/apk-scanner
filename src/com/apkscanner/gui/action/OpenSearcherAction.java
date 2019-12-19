package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.dialog.SearchDlg;
import com.apkscanner.gui.tabpanels.ResContentFocusChanger;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public class OpenSearcherAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_SEARCHER";

	public OpenSearcherAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenSearcher(getWindow(e));
	}

	private void evtOpenSearcher(Window owner) {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ResContentFocusChanger changer = (ResContentFocusChanger) handler.getData(ResContentFocusChanger.class);

		SearchDlg dialog = new SearchDlg(changer);
		dialog.setApkInfo(scanner.getApkInfo());
		dialog.setModal(false);
		dialog.setVisible(true);

		Log.d(dialog.sName);
	}
}