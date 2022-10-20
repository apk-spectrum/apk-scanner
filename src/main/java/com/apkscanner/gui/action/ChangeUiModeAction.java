package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.UIController;
import com.apkspectrum.swing.AbstractUIAction;
import com.apkspectrum.swing.ActionEventHandler;

public class ChangeUiModeAction extends AbstractUIAction
{
	private static final long serialVersionUID = 7503167002833066842L;

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
