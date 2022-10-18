package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.dialog.AboutDlg;
import com.apkspectrum.swing.AbstractUIAction;
import com.apkspectrum.swing.ActionEventHandler;

public class ShowAboutAction extends AbstractUIAction
{
	private static final long serialVersionUID = 861592549811066128L;

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