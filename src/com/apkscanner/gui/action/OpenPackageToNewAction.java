package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.swing.ActionEventHandler;

@SuppressWarnings("serial")
public class OpenPackageToNewAction extends OpenPackageAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_PACKAGE_TO_NEW";

	public OpenPackageToNewAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenPackage(getWindow(e), true);
	}
}
