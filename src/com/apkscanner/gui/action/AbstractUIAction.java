package com.apkscanner.gui.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.util.Log;

@SuppressWarnings("serial")
public abstract class AbstractUIAction extends AbstractAction
{
	public static final String ACTION_COMMAND_FIELD = "ACTION_COMMAND";

	protected ActionEventHandler handler;

	public AbstractUIAction() { }

	public AbstractUIAction(ActionEventHandler h) {
		setHandler(h);
	}

	protected static Window getWindow(ActionEvent e) {
		Object source = null;

		if(e != null) {
			source = e.getSource();
		} else {
			AWTEvent event = EventQueue.getCurrentEvent();
			source = event.getSource();
		}

		if(source instanceof KeyStrokeAction) {
			source = ((KeyStrokeAction) source).getComponent();
		}

		if(source instanceof Component) {
			return SwingUtilities.getWindowAncestor((Component) source);
		}

		return null;
	}

	public AbstractUIAction setHandler(ActionEventHandler h) {
		handler = h;
		return this;
	}

	public String getActionCommand() {
		try {
			return (String) getClass().getDeclaredField(ACTION_COMMAND_FIELD).get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			Log.w("No such field : " + e.getMessage() + " from " + getClass().getName());
		}
		return getClass().getName();
	}
}
