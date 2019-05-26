package com.apkscanner.gui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class KeyStrokeAction extends AbstractAction {
	private static final long serialVersionUID = -1213146704610675520L;

	private JComponent component;
	private KeyStroke keyStroke;
	private ActionListener listener;

	public KeyStrokeAction(JComponent component, KeyStroke keyStroke, ActionListener listener) {
		putValue(ACTION_COMMAND_KEY, Integer.toString(keyStroke.getKeyCode()));
		this.component = component;
		this.keyStroke = keyStroke;
		this.listener = listener;
	}

	public static void registerKeyStrokeAction(JComponent comp, int cond, KeyStroke key, ActionListener listener) {
		String mapKey = "KEY_VK_" + Integer.toString(key.getKeyCode());
		comp.getInputMap(cond).put(key, mapKey);
		comp.getActionMap().put(mapKey, new KeyStrokeAction(comp, key, listener));
	}

	public static void registerKeyStrokeActions(JComponent comp, int cond, KeyStroke[] keys, ActionListener listener) {
		for(KeyStroke key: keys) {
			String mapKey = "KEY_VK_" + Integer.toString(key.getKeyCode());
			comp.getInputMap(cond).put(key, mapKey);
			comp.getActionMap().put(mapKey, new KeyStrokeAction(comp, key, listener));
		}
	}

	public JComponent getComponent() {
		return component;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

	public String getActionCommand() {
		return (String) getValue(ACTION_COMMAND_KEY);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if(listener == null) return;
		evt.setSource(this);
		listener.actionPerformed(evt);
	}
}