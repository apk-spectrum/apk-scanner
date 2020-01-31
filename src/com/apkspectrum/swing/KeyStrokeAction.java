package com.apkspectrum.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class KeyStrokeAction extends AbstractAction {
	private static final long serialVersionUID = -1213146704610675520L;

	private JComponent component;
	private KeyStroke keyStroke;
	private ActionListener listener;

	public KeyStrokeAction(JComponent component, KeyStroke keyStroke, ActionListener listener) {
		this(component, keyStroke, null, listener);
	}

	public KeyStrokeAction(JComponent component, KeyStroke keyStroke, String actionCommand, ActionListener listener) {
		if(actionCommand == null) {
			actionCommand = keyStroke.toString();
		}
		putValue(ACTION_COMMAND_KEY, actionCommand);
		this.component = component;
		this.keyStroke = keyStroke;
		this.listener = listener;
	}

	public JComponent getComponent() {
		return component;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

    public int getModifiers() {
        return keyStroke.getModifiers() & (InputEvent.SHIFT_DOWN_MASK - 1);
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

	public static void registerKeyStrokeAction(JComponent comp, KeyStroke keyStroke, Action action) {
		registerKeyStrokeAction(comp, JComponent.WHEN_FOCUSED, keyStroke, action);
	}

	public static void registerKeyStrokeAction(JComponent comp, int cond, KeyStroke keyStroke, Action action) {
		String mapKey = keyStroke.toString();
		comp.getInputMap(cond).put(keyStroke, mapKey);
		comp.getActionMap().put(mapKey, action);
	}

	public static void registerKeyStrokeAction(KeyStrokeAction action) {
		registerKeyStrokeAction(action.component, JComponent.WHEN_FOCUSED, action.keyStroke, action);
	}

	public static void registerKeyStrokeAction(int cond, KeyStrokeAction action) {
		registerKeyStrokeAction(action.component, cond, action.keyStroke, action);
	}

	public static void registerKeyStrokeAction(JComponent comp, KeyStroke keyStroke, ActionListener listener) {
		registerKeyStrokeAction(comp, JComponent.WHEN_FOCUSED, keyStroke, new KeyStrokeAction(comp, keyStroke, listener));
	}

	public static void registerKeyStrokeAction(JComponent comp, int cond, KeyStroke keyStroke, ActionListener listener) {
		registerKeyStrokeAction(comp, cond, keyStroke, new KeyStrokeAction(comp, keyStroke, listener));
	}

	public static void registerKeyStrokeAction(JComponent comp, KeyStroke keyStroke, String actionCommand, ActionListener listener) {
		registerKeyStrokeAction(comp, JComponent.WHEN_FOCUSED, keyStroke, new KeyStrokeAction(comp, keyStroke, actionCommand, listener));
	}

	public static void registerKeyStrokeAction(JComponent comp, int cond, KeyStroke keyStroke, String actionCommand, ActionListener listener) {
		registerKeyStrokeAction(comp, cond, keyStroke, new KeyStrokeAction(comp, keyStroke, actionCommand, listener));
	}

	public static void registerKeyStrokeActions(JComponent comp, KeyStroke[] keyStrokes, ActionListener listener) {
		registerKeyStrokeActions(comp, JComponent.WHEN_FOCUSED, keyStrokes, listener);
	}

	public static void registerKeyStrokeActions(JComponent comp, int cond, KeyStroke[] keyStrokes, ActionListener listener) {
		for(KeyStroke keyStroke: keyStrokes) {
			registerKeyStrokeAction(comp, cond, keyStroke, new KeyStrokeAction(comp, keyStroke, listener));
		}
	}

	public static void registerKeyStrokeActions(JComponent comp, KeyStroke[] keyStrokes, String[] actionCommands, ActionListener listener) {
		registerKeyStrokeActions(comp, JComponent.WHEN_FOCUSED, keyStrokes, actionCommands, listener);
	}

	public static void registerKeyStrokeActions(JComponent comp, int cond, KeyStroke[] keyStrokes, String[] actionCommands, ActionListener listener) {
		if(keyStrokes.length != actionCommands.length) {
			throw new IllegalArgumentException();
		}
		for(int i = 0; i < keyStrokes.length; i++) {
			registerKeyStrokeAction(comp, cond, keyStrokes[i], new KeyStrokeAction(comp, keyStrokes[i], actionCommands[i], listener));
		}
	}
}