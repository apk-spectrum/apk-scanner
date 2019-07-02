package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class DeviceLaunchChooseAction extends DeviceLaunchAppAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_LAUNCH_CHOOSE_APP";

	public DeviceLaunchChooseAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtLaunchByChooseApp(getWindow(e));
	}
}
