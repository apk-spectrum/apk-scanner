package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;
import com.apkspectrum.swing.AbstractUIAction;

public class NewWindowAction extends AbstractUIAction
{
	private static final long serialVersionUID = -6467181509381217346L;

	public static final String ACTION_COMMAND = "ACT_CMD_NEW_WINDOW";

	@Override
	public void actionPerformed(ActionEvent e) {
		Launcher.run();
	}
}
