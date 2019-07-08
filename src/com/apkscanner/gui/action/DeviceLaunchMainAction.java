package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkscanner.resource.RConst;

@SuppressWarnings("serial")
public class DeviceLaunchMainAction extends DeviceLaunchAppAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_LAUNCH_MAIN_APP";

	public DeviceLaunchMainAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtLaunchApp(getWindow(e), null, RConst.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY);
	}
}
