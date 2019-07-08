package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;

@SuppressWarnings("serial")
public class NewWindowAction extends AbstractUIAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_NEW_WINDOW";

	@Override
	public void actionPerformed(ActionEvent e) {
		Launcher.run();
	}
}
