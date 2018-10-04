package com.apkscanner.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class NetworkErrorPanel extends JPanel {
	private static final long serialVersionUID = -67190848421501316L;

	private JCheckBox naverLook;
	private JTextArea textArea;

	public NetworkErrorPanel() {
		setLayout(new BorderLayout());
		setOpaque(false);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setMargin(new Insets(0,0,20,0));

		naverLook = new JCheckBox();
		naverLook.setText("Do not look again");
		add(textArea,BorderLayout.NORTH);
		add(naverLook,BorderLayout.SOUTH);
	}

	public boolean isNaverLook() {
		return naverLook.isSelected();
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	public Component add(Component component) {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		panel.add(component);
		panel.add(Box.createVerticalGlue());
		add(panel, BorderLayout.CENTER);
		return component;
	}


}