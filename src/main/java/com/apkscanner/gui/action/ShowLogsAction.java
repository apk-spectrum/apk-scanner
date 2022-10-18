package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.dialog.LogDlg;
import com.apkspectrum.swing.AbstractUIAction;
import com.apkspectrum.swing.ActionEventHandler;

public class ShowLogsAction extends AbstractUIAction
{
	private static final long serialVersionUID = -5576560570755862238L;

	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_LOGS";

	public ShowLogsAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowLogs(getWindow(e));
	}

	private void evtShowLogs(Window owner) {
		LogDlg.showLogDialog(owner);
	}
}