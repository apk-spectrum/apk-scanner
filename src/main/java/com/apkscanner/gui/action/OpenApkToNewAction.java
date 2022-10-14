package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.swing.ApkActionEventHandler;

@SuppressWarnings("serial")
public class OpenApkToNewAction extends OpenApkAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_APK_TO_NEW";

	public OpenApkToNewAction(ApkActionEventHandler h) {
		super(h);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenApkFile(getWindow(e), true);
	}
}
