package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.Window;

import com.apkspectrum.swing.ApkActionEventHandler;

@SuppressWarnings("serial")
public class OpenDecodeJADXGUIAction extends OpenDecompilerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_JADXGUI";

	public OpenDecodeJADXGUIAction(ApkActionEventHandler h) { super(h); }

	@Override
	protected void evtOpenDecompiler(final Window owner, final Component comp) {
		if(!hasCode(owner)) return;
		launchJadxGui(owner, comp);
	}
}