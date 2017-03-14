package com.apkscanner.gui.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.apkscanner.util.SystemUtil;

public class JTextOptionPane extends JOptionPane {
	private static final long serialVersionUID = -7000242417475447404L;

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
}
