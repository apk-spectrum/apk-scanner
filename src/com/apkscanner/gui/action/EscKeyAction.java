package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.UIController;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;

@SuppressWarnings("serial")
public class EscKeyAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_ESC_KEY_EVENT";

	public EscKeyAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtEscKeyAction(getWindow(e));
	}

	private void evtEscKeyAction(Window owner) {
		switch(RProp.I.ESC_ACTION.get()) {
		case RConst.INT_ESC_ACT_NONE:
			return;
		case RConst.INT_ESC_ACT_CHANG_UI_MODE:
			if(owner instanceof MainUI) {
				UIController.changeToEasyGui();
			} else {
				UIController.changeToMainGui();
			}
			break;
		case RConst.INT_ESC_ACT_EXIT:
			owner.dispose();
			return;
		}
	}
}
