package com.apkscanner.gui.messagebox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.SystemUtil;

public class ComboMessageBox extends JOptionPane {
	private static final long serialVersionUID = -6040773996129760139L;

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

		int ret = ArrowTraversalPane.showOptionDialog(parent, panel, title, JOptionPane.DEFAULT_OPTION, messageType, icon,
				new String[] {Resource.STR_BTN_OK.getString(), Resource.STR_BTN_CANCEL.getString()}, Resource.STR_BTN_OK.getString());

		return ret == 0 ? (String) comboBox.getSelectedItem() : null;
	}
}
