package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

	public InstallOptionPanel() {
		setLayout(new BorderLayout());	


		JCheckBox cbLaucnApp = new JCheckBox("Launch after installed");
		cbLaucnApp.setSelected(true);

		JPanel installOptionsPanel = new JPanel();
		installOptionsPanel.setLayout(new BoxLayout(installOptionsPanel, BoxLayout.Y_AXIS));
		
		//installPanel.setBorder(BorderFactory.createTitledBorder("Install options"));

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
		
		JComboBox<String> cbLaunchActivity = new JComboBox<String>(new String[] { "aaa" , "bbb"});
		cbLaunchActivity.setAlignmentY(0);
		cbLaunchActivity.setAlignmentX(0);
		additionalOptionsPanel.add(cbLaunchActivity);
		
		//additionalOptionsPanel
		
		JCheckBox ckReplace = new JCheckBox("Replace existing application");
		JCheckBox ckDowngrade = new JCheckBox("Allow version code downgrade");
		JCheckBox ckToSdCard = new JCheckBox("Install application on sdcard");
		JCheckBox ckGrandPerm = new JCheckBox("Grant all runtime permissions");
		JCheckBox ckLock = new JCheckBox("forward lock application");
		JCheckBox ckTestPack = new JCheckBox("allow test packages");
		
		additionalOptionsPanel.add(ckReplace);
		additionalOptionsPanel.add(ckDowngrade);
		additionalOptionsPanel.add(ckToSdCard);
		additionalOptionsPanel.add(ckGrandPerm);
		additionalOptionsPanel.add(ckLock);
		additionalOptionsPanel.add(ckTestPack);


		JRadioButton RadiosystemPush = new JRadioButton("system");
		JRadioButton RadioprivPush = new JRadioButton("priv-app");
		JRadioButton RadiodataPush = new JRadioButton("data");
		JCheckBox CheckOverwrite = new JCheckBox("overwrite lib");
		JCheckBox CheckWithLib = new JCheckBox("with Lib");
		JCheckBox CheckReboot = new JCheckBox("reboot after push");


		JPanel pushPanel = new JPanel(new GridLayout(0,1));
		pushPanel.setBorder(BorderFactory.createTitledBorder("Push"));
		pushPanel.add(RadiosystemPush);
		pushPanel.add(RadioprivPush);
		pushPanel.add(RadiodataPush);
		pushPanel.add(CheckOverwrite);
		pushPanel.add(CheckWithLib);
		pushPanel.add(CheckReboot);

		//optionPanel.add(installPanel);
		//optionPanel.add(pushPanel);


		//togglePanel.add(InstalltoggleButton);
		//togglePanel.add(pushtoggleButton);
		
		JPanel installMethodPanel = new JPanel();
		installMethodPanel.add(new JLabel(" How to install : "));
		installMethodPanel.add(makeToggleButtonBar(0x555555, true));
		installMethodPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));

		add(makeToggleButtonBar(0x555555, true), BorderLayout.NORTH);
		add(installOptionsPanel, BorderLayout.CENTER);		
		add(new JButton("Apply all models"), BorderLayout.SOUTH);
	}

	private static AbstractButton makeButton(String title) {
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
		return b;
	}

	private static JPanel makeToggleButtonBar(int cc, boolean round) {
		ButtonGroup bg = new ButtonGroup();
		JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));
        p.setBorder(BorderFactory.createTitledBorder("How to install"));
        //p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		Color color = new Color(cc);
		for (AbstractButton b: Arrays.asList(makeButton("Install"), makeButton("Push"))) {
			b.setBackground(color);
			b.setIcon(new ToggleButtonBarCellIcon());

			bg.add(b);
			p.add(b);
		}
		return p;
	}
}
