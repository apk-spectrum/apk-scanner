package com.apkscanner.gui.installer;

import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.data.apkinfo.CompactApkInfo;

public class SimpleOptionPanel extends JPanel {
	private static final long serialVersionUID = -1856410346702035872L;

	public static final String ACT_CMD_SIMPLE_INSTALL = "ACT_CMD_SIMPLE_INSTALL";
	public static final String ACT_CMD_SIMPLE_PUSH = "ACT_CMD_SIMPLE_PUSH";
	public static final String ACT_CMD_SET_ADVANCED_OPT = "ACT_CMD_SET_ADVANCED";

	public SimpleOptionPanel(ActionListener listener) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JButton btn = new JButton("install");
		btn.addActionListener(listener);
		btn.setActionCommand(ACT_CMD_SIMPLE_INSTALL);
		add(btn);

		btn = new JButton("push");
		btn.addActionListener(listener);
		btn.setActionCommand(ACT_CMD_SIMPLE_PUSH);
		add(btn);

		btn = new JButton("Advanced settings");
		btn.addActionListener(listener);
		btn.setActionCommand(ACT_CMD_SET_ADVANCED_OPT);
		add(btn);
	}

	public void setApkInfo(CompactApkInfo apkinfo) {

	}

	public void setOptions(OptionsBundle bundle) {

	}
}
