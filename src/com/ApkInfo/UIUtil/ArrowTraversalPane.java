package com.ApkInfo.UIUtil;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

public class ArrowTraversalPane extends JOptionPane
{
	private static final long serialVersionUID = 4947402878882910721L;
	private static final int Integer = 0;
	
	public ArrowTraversalPane(Object message, int messageType, int optionType)
	{
		super(message, optionType, messageType);
		
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
		
		Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
	}

	static public int showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
	    JOptionPane pane = new ArrowTraversalPane(message, optionType, messageType);

		pane.setMessageType(messageType);
		pane.setIcon(icon);
		pane.setOptionType(optionType);
		pane.setOptions(options);
		pane.setInitialValue(initialValue);
		
		Object old = UIManager.get("Button.defaultButtonFollowsFocus");
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

		JDialog dialog = pane.createDialog(parentComponent, title);
		dialog.setVisible(true);
		dialog.dispose();

		UIManager.put("Button.defaultButtonFollowsFocus", old);
		
		int ret = -1;
		if(pane.getValue() != null && !pane.getValue().equals((Integer)-1)) {
			for(Object o: options) {
				ret++;
				if(o.equals(pane.getValue())) break;
			}
		}
		return ret;
	}

}