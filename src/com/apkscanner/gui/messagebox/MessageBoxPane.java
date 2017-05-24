package com.apkscanner.gui.messagebox;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.SystemUtil;

public class MessageBoxPane extends JOptionPane
{
	private static final long serialVersionUID = 4947402878882910721L;
	private static final int Integer = 0;
	
	public MessageBoxPane(Object message, int messageType, int optionType, Icon icon, Object[] options, Object initialValue)
	{
		super(message, messageType, optionType, icon, options, initialValue);
		
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
		
		Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
	}

	static public void showMessageDialog(Component parentComponent, Object message, String title, int messageType, Icon icon) {
		
	}

	static public int showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
	    JOptionPane pane = new MessageBoxPane(message, messageType, optionType, icon, options, initialValue);

		JDialog dialog = pane.createDialog(parentComponent, title);
		dialog.setVisible(true);
		dialog.dispose();
		

		
		int ret = -1;
		if(pane.getValue() != null && !pane.getValue().equals((Integer)-1)) {
			for(Object o: options) {
				ret++;
				if(o.equals(pane.getValue())) break;
			}
		}
		return ret;
	}
	

	public static void showTextDialog(Component parent, String content, String title, int messageType, Icon icon, Dimension size)
	{
		showTextDialog(parent, null, content, title, messageType, icon, size);
	}

	public static void showTextDialog(Component parent, String message, String content, String title, int messageType, Icon icon, Dimension size)
	{
		JTextPane messagePane = new JTextPane();
		if(message != null) {
			messagePane.setText(message);
			messagePane.setOpaque(false);
			messagePane.setEditable(false);
			messagePane.setFocusable(false);
		} else {
			messagePane.setVisible(false);
		}

		JTextArea taskOutput = new JTextArea();
		taskOutput.setText(content);
		taskOutput.setEditable(false);
		taskOutput.setCaretPosition(0);

		final JScrollPane scrollPane = new JScrollPane(taskOutput);
		scrollPane.setPreferredSize(size);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);
		panel.add(messagePane,BorderLayout.NORTH);
		panel.add(scrollPane,BorderLayout.CENTER);

		if(SystemUtil.isWindows()) {
			panel.addHierarchyListener(new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					Window window = SwingUtilities.getWindowAncestor(panel);
					if (window instanceof Dialog) {
						Dialog dialog = (Dialog)window;
						if (!dialog.isResizable()) {
							dialog.setResizable(true);
							}
						}
					}
			});
		}

		JOptionPane.showMessageDialog(parent, panel, title, messageType, icon);
	}
	
	

	public static String show(Component parent, String[] items, String title, int messageType, Icon icon, Dimension size)
	{
		return show(parent, null, items, title, messageType, icon, size);
	}

	public static String show(Component parent, String message, String[] items, String title, int messageType, Icon icon, Dimension size)
	{
		JTextPane messagePane = new JTextPane();
		if(message != null) {
			messagePane.setText(message);
			messagePane.setOpaque(false);
			messagePane.setEditable(false);
			messagePane.setFocusable(false);
		} else {
			messagePane.setVisible(false);
		}

		JComboBox<String> comboBox = new JComboBox<String>(items);
		comboBox.setEditable(false);
		if(size.getHeight() == 0) {
			Dimension _size = comboBox.getPreferredSize();
			size.setSize(size.getWidth(), _size.getHeight());
		}
		comboBox.setPreferredSize(size);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);
		panel.add(messagePane,BorderLayout.CENTER);
		panel.add(comboBox,BorderLayout.SOUTH);

		if(SystemUtil.isWindows()) {
			panel.addHierarchyListener(new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					Window window = SwingUtilities.getWindowAncestor(panel);
					if (window instanceof Dialog) {
						Dialog dialog = (Dialog)window;
						if (!dialog.isResizable()) {
							dialog.setResizable(true);
						}
					}
				}
			});
		}

		int ret = MessageBoxPane.showOptionDialog(parent, panel, title, JOptionPane.DEFAULT_OPTION, messageType, icon,
				new String[] {Resource.STR_BTN_OK.getString(), Resource.STR_BTN_CANCEL.getString()}, Resource.STR_BTN_OK.getString());

		return ret == 0 ? (String) comboBox.getSelectedItem() : null;
	}
}