package com.apkscanner.gui.action;

import java.awt.Window;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class OpenDecodeByteCodeAction extends OpenDecompilerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_BYTECODE";

	public OpenDecodeByteCodeAction(ActionEventHandler h) { super(h); }

	protected void evtOpenDecompiler(final Window owner, final JButton button) {
		if(!hasCode(owner)) return;
		launchByteCodeViewer(owner, button);
	}
}