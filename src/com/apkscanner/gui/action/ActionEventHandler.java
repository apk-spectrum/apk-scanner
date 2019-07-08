package com.apkscanner.gui.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import com.apkscanner.util.Log;

abstract public class ActionEventHandler implements ActionListener
{
	protected Map<String, Action> actionMap = new HashMap<>();
	protected Map<String, Object> dataMap;

	public void addAction(AbstractUIAction action) {
		addAction(action.getActionCommand(), action);
	}

	public void addAction(String actionCommand, Action action) {
		if(action == null) return;
		if(actionMap.containsKey(actionCommand)) {
			Log.v(String.format("addAction() %s was already existed. So change to new : %s", actionCommand, action));
		}
		actionMap.put(actionCommand, action);
	}

	public void removeAction(AbstractUIAction action) {
		if(action == null) return;
		if(actionMap.containsKey(action.getActionCommand())) {
			actionMap.remove(action.getActionCommand(), action);
		}
	}

	public void removeAction(String actionCommand) {
		if(actionCommand == null) return;
		if(actionMap.containsKey(actionCommand)) {
			actionMap.remove(actionCommand);
		}
	}

	public Action getAction(String actionCommand) {
		if(actionCommand == null) return null;
		return actionMap.get(actionCommand);
	}

	public void sendEvent(String actionCommand) {
		sendEvent(getWindow(), actionCommand);
	}

	public void sendEvent(Component c, String actionCommand) {
		Action action = getAction(actionCommand);
		if(action == null) return;

		action.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, actionCommand));
	}

	public Object getData(String key) {
		return dataMap != null ? dataMap.get(key) : null;
	}

	public void putData(String key, Object value) {
		if(dataMap == null) {
			dataMap = new HashMap<>();
		}
		if(value == null) {
			dataMap.remove(key);
		} else {
			dataMap.put(key, value);
		}
	}

	protected static Window getWindow() {
		Object source = null;

		AWTEvent e = EventQueue.getCurrentEvent();
		if(e != null) {
			source = e.getSource();
		}

		if(source instanceof Component) {
			return SwingUtilities.getWindowAncestor((Component) source);
		}

		return null;
	}
}
