package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.swing.ApkActionEventHandler;

public class OpenPackageToNewAction extends OpenPackageAction
{
	private static final long serialVersionUID = -5459559486060412692L;

	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_PACKAGE_TO_NEW";

	public OpenPackageToNewAction(ApkActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenPackage(getWindow(e), true);
	}
}
