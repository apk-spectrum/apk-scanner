package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.swing.ApkActionEventHandler;

public class DeviceLaunchChooseAction extends DeviceLaunchAppAction {
    private static final long serialVersionUID = -7631003296016494886L;

    public static final String ACTION_COMMAND = "ACT_CMD_LAUNCH_CHOOSE_APP";

    public DeviceLaunchChooseAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evtLaunchByChooseApp(getWindow(e), null);
    }
}
