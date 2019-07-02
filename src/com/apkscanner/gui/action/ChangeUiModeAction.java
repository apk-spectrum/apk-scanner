package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.UIController;

@SuppressWarnings("serial")
public class ChangeUiModeAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_CHANGE_UI_MODE";

	public ChangeUiModeAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtChangeUiModeAction(getWindow(e));
	}

	private void evtChangeUiModeAction(Window owner) {
		if(owner instanceof MainUI) {
			UIController.changeToEasyGui();
		} else {
			UIController.changeToMainGui();
		}
	}
}
