package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.dialog.LogDlg;

@SuppressWarnings("serial")
public class ShowLogsAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_LOGS";

	public ShowLogsAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowAbout(getWindow(e));
	}

	private void evtShowAbout(Window owner) {
		LogDlg.showLogDialog(owner);
	}
}