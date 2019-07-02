package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.dialog.AboutDlg;

@SuppressWarnings("serial")
public class ShowAboutAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_ABOUT";

	public ShowAboutAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowAbout(getWindow(e));
	}

	private void evtShowAbout(Window owner) {
		AboutDlg.showAboutDialog(owner);
	}
}