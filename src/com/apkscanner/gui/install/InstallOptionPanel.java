package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import com.apkscanner.gui.util.ToggleButtonBarCellIcon;

public class InstallOptionPanel extends JPanel {
	private static final long serialVersionUID = 2307623568442307145L;
	
	public static final String ACT_CMD_INSTALL = "ACT_CMD_INSTALL";
	public static final String ACT_CMD_PUSH = "ACT_CMD_PUSH";

	public InstallOptionPanel() {
		setLayout(new BorderLayout());	


		JCheckBox cbLaucnApp = new JCheckBox("Launch after installed");
		cbLaucnApp.setSelected(true);
		
		JPanel installOptionsPanel = new JPanel();
		installOptionsPanel.setLayout(new BoxLayout(installOptionsPanel, BoxLayout.Y_AXIS));
		
		installOptionsPanel.setBorder(BorderFactory.createTitledBorder("Install options"));

		installOptionsPanel.add(cbLaucnApp);
		installOptionsPanel.add(Box.createVerticalStrut(5));
		
		JPanel additionalOptionsPanel = new JPanel();
		additionalOptionsPanel.setAlignmentX(0);
		additionalOptionsPanel.setVisible(false);
		additionalOptionsPanel.setLayout(new BoxLayout(additionalOptionsPanel, BoxLayout.Y_AXIS));

		JLabel additionalOptionsLabel = new JLabel("▶ Choose additional options:");
		additionalOptionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				super.mouseClicked(arg0);
				if(!(arg0.getSource() instanceof JLabel)) {
					return;
				}
				JLabel label = (JLabel)arg0.getSource();
				String text = label.getText();
				if(text.startsWith("▶")) {
					label.setText(text.replaceFirst("▶", "▼"));
					additionalOptionsPanel.setVisible(true);
				} else {
					label.setText(text.replaceFirst("▼", "▶"));
					additionalOptionsPanel.setVisible(false);
				}
			}
		});
		installOptionsPanel.add(additionalOptionsLabel);
		installOptionsPanel.add(additionalOptionsPanel);
		installOptionsPanel.add(Box.createVerticalGlue());
		
		JComboBox<String> cbLaunchActivity = new JComboBox<String>(new String[] { "aaa" , "bbb"});
		cbLaunchActivity.setAlignmentY(0);
		cbLaunchActivity.setAlignmentX(0);
		Dimension maxSize = cbLaunchActivity.getMaximumSize();
		maxSize.setSize(maxSize.getWidth(), cbLaunchActivity.getMinimumSize().getHeight());
		cbLaunchActivity.setMaximumSize(maxSize);
		additionalOptionsPanel.add(cbLaunchActivity);
		
		//additionalOptionsPanel
		
		JCheckBox ckReplace = new JCheckBox("Replace existing application");
		JCheckBox ckDowngrade = new JCheckBox("Allow version code downgrade");
		JCheckBox ckToSdCard = new JCheckBox("Install application on sdcard");
		JCheckBox ckGrandPerm = new JCheckBox("Grant all runtime permissions");
		JCheckBox ckLock = new JCheckBox("Forward lock application");
		JCheckBox ckTestPack = new JCheckBox("Allow test packages");
		
		additionalOptionsPanel.add(ckReplace);
		additionalOptionsPanel.add(ckDowngrade);
		additionalOptionsPanel.add(ckToSdCard);
		additionalOptionsPanel.add(ckGrandPerm);
		additionalOptionsPanel.add(ckLock);
		additionalOptionsPanel.add(ckTestPack);


		JRadioButton RadiosystemPush = new JRadioButton("/system/app");
		JRadioButton RadioprivPush = new JRadioButton("/system/priv-app");
		JCheckBox CheckWithLib32 = new JCheckBox("With Lib32");
		JCheckBox CheckWithLib64 = new JCheckBox("With Lib64");
		JCheckBox CheckReboot = new JCheckBox("Reboot after pushed");


		JPanel pushOptionsPanel = new JPanel();
		pushOptionsPanel.setLayout(new BoxLayout(pushOptionsPanel, BoxLayout.Y_AXIS));
		pushOptionsPanel.setBorder(BorderFactory.createTitledBorder("Push options"));
		Box installLocationBox = Box.createHorizontalBox();
		installLocationBox.setAlignmentX(0);
		installLocationBox.setAlignmentY(0);
		installLocationBox.add(new JLabel("Path:"));
		installLocationBox.add(Box.createHorizontalStrut(5));
		installLocationBox.add(RadiosystemPush);
		installLocationBox.add(Box.createHorizontalStrut(10));
		installLocationBox.add(RadioprivPush);
		installLocationBox.setMaximumSize(installLocationBox.getMinimumSize());
		
		pushOptionsPanel.add(installLocationBox);
		pushOptionsPanel.add(CheckReboot);
		pushOptionsPanel.add(CheckWithLib32);
		pushOptionsPanel.add(CheckWithLib64);
		pushOptionsPanel.add(Box.createVerticalGlue());

		//optionPanel.add(installPanel);
		//optionPanel.add(pushPanel);


		//togglePanel.add(InstalltoggleButton);
		//togglePanel.add(pushtoggleButton);
		
		final JPanel installOrPushOptionsPanel = new JPanel(new CardLayout());
		installOrPushOptionsPanel.add(installOptionsPanel, ACT_CMD_INSTALL);
		installOrPushOptionsPanel.add(pushOptionsPanel, ACT_CMD_PUSH);
		
		JPanel installMethodPanel = makeToggleButtonBar(0x555555, true, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String actionCommand = arg0.getActionCommand();
				((CardLayout)installOrPushOptionsPanel.getLayout()).show(installOrPushOptionsPanel, actionCommand);
			}
		});

		add(installMethodPanel, BorderLayout.NORTH);
		add(installOrPushOptionsPanel, BorderLayout.CENTER);		
		add(new JButton("Apply all models"), BorderLayout.SOUTH);
	}

	private static AbstractButton makeButton(String title, String actionCommand) {
		AbstractButton b = new JRadioButton(title);
		//b.setVerticalAlignment(SwingConstants.CENTER);
		//b.setVerticalTextPosition(SwingConstants.CENTER);
		//b.setHorizontalAlignment(SwingConstants.CENTER);
		b.setHorizontalTextPosition(SwingConstants.CENTER);
		b.setBorder(BorderFactory.createEmptyBorder());
		b.setContentAreaFilled(false);
		b.setFocusPainted(false);
		//b.setBackground(new Color(cc));
		b.setForeground(Color.WHITE);
		b.setActionCommand(actionCommand);
		return b;
	}

	private static JPanel makeToggleButtonBar(int cc, boolean round, ActionListener listener) {
		ButtonGroup bg = new ButtonGroup();
		JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));
        p.setBorder(BorderFactory.createTitledBorder("How to install"));
        //p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		Color color = new Color(cc);
		for (AbstractButton b: Arrays.asList(makeButton("Install", ACT_CMD_INSTALL), makeButton("Push", ACT_CMD_PUSH))) {
			b.setBackground(color);
			b.setIcon(new ToggleButtonBarCellIcon());
			b.addActionListener(listener);

			bg.add(b);
			p.add(b);
		}
		return p;
	}
}
