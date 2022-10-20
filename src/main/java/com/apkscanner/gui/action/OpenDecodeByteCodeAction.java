package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.Window;

import com.apkspectrum.swing.ApkActionEventHandler;

public class OpenDecodeByteCodeAction extends OpenDecompilerAction
{
	private static final long serialVersionUID = -859511095714425836L;

	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_BYTECODE";

	public OpenDecodeByteCodeAction(ApkActionEventHandler h) { super(h); }

	@Override
	protected void evtOpenDecompiler(final Window owner, final Component comp) {
		if(!hasCode(owner)) return;
		launchByteCodeViewer(owner, comp);
	}
}