package com.apkscanner.gui.action;

import java.awt.Window;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class OpenDecodeJADXGUIAction extends OpenDecompilerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_JADXGUI";

	public OpenDecodeJADXGUIAction(ActionEventHandler h) { super(h); }

	protected void evtOpenDecompiler(final Window owner, final JButton button) {
		if(!hasCode(owner)) return;
		launchJadxGui(owner, button);
	}
}