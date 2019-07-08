package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class OpenApkToNewAction extends OpenApkAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_APK_TO_NEW";

	public OpenApkToNewAction(ActionEventHandler h) {
		super(h);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenApkFile(getWindow(e), true);
	}
}
