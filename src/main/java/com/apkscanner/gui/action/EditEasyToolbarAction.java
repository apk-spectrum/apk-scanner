package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import com.apkscanner.gui.easymode.dlg.EasyToolbarSettingDnDDlg;
import com.apkspectrum.swing.AbstractUIAction;

public class EditEasyToolbarAction extends AbstractUIAction {
    private static final long serialVersionUID = 9107709537919105382L;

    public static final String ACTION_COMMAND = "ACT_CMD_EDIT_EASY_TOOLBAR";

    @Override
    public void actionPerformed(ActionEvent e) {
        evtEditEasyToolbar(getWindow(e));
    }

    private void evtEditEasyToolbar(Window owner) {
        new EasyToolbarSettingDnDDlg((JFrame) owner, true);
    }
}
