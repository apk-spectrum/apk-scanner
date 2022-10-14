package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import com.apkscanner.gui.easymode.dlg.EasyToolbarSettingDnDDlg;
import com.apkspectrum.swing.AbstractUIAction;

@SuppressWarnings("serial")
public class EditEasyToolbarAction extends AbstractUIAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_EDIT_EASY_TOOLBAR";

	@Override
	public void actionPerformed(ActionEvent e) {
		evtEditEasyToolbar(getWindow(e));
	}

	private void evtEditEasyToolbar(Window owner) {
		new EasyToolbarSettingDnDDlg((JFrame) owner, true);
	}
}