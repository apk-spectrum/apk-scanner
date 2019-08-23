package com.apkscanner.gui.action;

import java.awt.Window;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class OpenDecodeJDGUIAction extends OpenDecompilerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_JDGUI";

	public OpenDecodeJDGUIAction(ActionEventHandler h) { super(h); }

	protected void evtOpenDecompiler(final Window owner, final JButton button) {
		if(!hasCode(owner)) return;
		launchJdGui(owner, button);
	}
}