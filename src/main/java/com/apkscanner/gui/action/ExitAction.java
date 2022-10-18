package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkspectrum.swing.AbstractUIAction;
import com.apkspectrum.swing.ActionEventHandler;

public class ExitAction extends AbstractUIAction
{
	private static final long serialVersionUID = 3760278157236396491L;

	public static final String ACTION_COMMAND = "ACT_CMD_EXIT";

	public ExitAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		Window owner = getWindow(e);
		if(owner != null) owner.dispose();
	}
}
