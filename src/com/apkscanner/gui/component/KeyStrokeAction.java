package com.apkscanner.gui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class KeyStrokeAction extends AbstractAction {
	private static final long serialVersionUID = -1213146704610675520L;

	private JComponent component;
	private KeyStroke keyStroke;
	private ActionListener listener;

	public class KeyStrokeCommand {
		KeyStroke keyStroke;
	}

	public KeyStrokeAction(JComponent component, KeyStroke keyStroke, ActionListener listener) {
		this(component, keyStroke, Integer.toString(keyStroke.getKeyCode()), listener);
	}

	public KeyStrokeAction(JComponent component, KeyStroke keyStroke, String actionCommand, ActionListener listener) {
		putValue(ACTION_COMMAND_KEY, actionCommand);
		this.component = component;
		this.keyStroke = keyStroke;
		this.listener = listener;
	}

	public static void registerKeyStrokeAction(int cond, KeyStrokeAction action) {
		String mapKey = action.keyStroke.toString();
		action.component.getInputMap(cond).put(action.keyStroke, mapKey);
		action.component.getActionMap().put(mapKey, action);
	}

	public static void registerKeyStrokeAction(JComponent comp, int cond, KeyStroke key, ActionListener listener) {
		registerKeyStrokeAction(cond, new KeyStrokeAction(comp, key, listener));
	}

	public static void registerKeyStrokeAction(JComponent comp, int cond, KeyStroke key, String actionCommand, ActionListener listener) {
		registerKeyStrokeAction(cond, new KeyStrokeAction(comp, key, actionCommand, listener));
	}

	public static void registerKeyStrokeActions(JComponent comp, int cond, KeyStroke[] keys, ActionListener listener) {
		for(KeyStroke key: keys) {
			registerKeyStrokeAction(comp, cond, key, listener);
		}
	}

	public static void registerKeyStrokeActions(JComponent comp, int cond, KeyStroke[] keys, String[] actionCommands, ActionListener listener) {
		if(keys.length != actionCommands.length) {
			throw new IllegalArgumentException();
		}
		for(int i = 0; i < keys.length; i++) {
			registerKeyStrokeAction(comp, cond, keys[i], actionCommands[i], listener);
		}
	}

	public JComponent getComponent() {
		return component;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

    public int getModifiersEx() {
        return keyStroke.getModifiers() & ~(InputEvent.SHIFT_DOWN_MASK - 1);
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