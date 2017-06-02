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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.util.ToggleButtonBarCellIcon;
import com.apkscanner.util.Log;

public class InstallOptionPanel extends JPanel {
	private static final long serialVersionUID = 2307623568442307145L;

	public static final String ACT_CMD_INSTALL = "ACT_CMD_INSTALL";
	public static final String ACT_CMD_PUSH = "ACT_CMD_PUSH";
	public static final String ACT_CMD_NOT_INSTALL = "ACT_CMD_NOT_INSTALL";

	private OptionsBundle bundle;

	private JPanel optionsPanel;
	private ButtonGroup bgInstallMethod;
	
	private JCheckBox ckLaucnApp;
	private JComboBox<String> cbLaunchActivity;
	private JCheckBox ckReplace;
	private JCheckBox ckDowngrade;
	private JCheckBox ckOnSdCard;
	private JCheckBox ckGrandPerm;
	private JCheckBox ckLock;
	private JCheckBox ckTestPack;

	private JRadioButton rbSystemPush;
	private JRadioButton rbPrivPush;
	private JCheckBox ckReboot;
	private Box lib32Box;
	private JCheckBox ckLib32;
	private JComboBox<String> cbLib32Src;
	private JComboBox<String> cbLib32Dest;
	private Box lib64Box;
	private JCheckBox ckLib64;
	private JComboBox<String> cbLib64Src;
	private JComboBox<String> cbLib64Dest;

	public InstallOptionPanel() {
		setLayout(new BorderLayout());	

		optionsPanel = new JPanel(new CardLayout());
		optionsPanel.add(makeInstallOptionsPanel(), ACT_CMD_INSTALL);
		optionsPanel.add(makePushOptionPanel(), ACT_CMD_PUSH);
		optionsPanel.add(new JPanel(), ACT_CMD_NOT_INSTALL);

		JPanel installMethodPanel = makeToggleButtonBar(0x555555, true, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String actionCommand = arg0.getActionCommand();
				((CardLayout)optionsPanel.getLayout()).show(optionsPanel, actionCommand);
				
				if(bundle != null) {
					if(ACT_CMD_INSTALL.equals(actionCommand)) {
						bundle.set(OptionsBundle.FLAG_OPT_INSTALL);	
					} else if(ACT_CMD_PUSH.equals(actionCommand)) {
						bundle.set(OptionsBundle.FLAG_OPT_PUSH);
					} else {
						bundle.set(OptionsBundle.FLAG_OPT_NOT_INSTALL);
					}
				}
			}
		});

		add(installMethodPanel, BorderLayout.NORTH);
		add(optionsPanel, BorderLayout.CENTER);		
		add(new JButton("Apply all models"), BorderLayout.SOUTH);
	}

	private JPanel makeInstallOptionsPanel() {
		JPanel installOptionsPanel = new JPanel();
		installOptionsPanel.setLayout(new BoxLayout(installOptionsPanel, BoxLayout.Y_AXIS));
		installOptionsPanel.setBorder(BorderFactory.createTitledBorder("Install options"));

		ckLaucnApp = new JCheckBox("Launch after installed");
		installOptionsPanel.add(ckLaucnApp);

		installOptionsPanel.add(Box.createVerticalStrut(5));

		final JPanel additionalOptionsPanel = new JPanel();
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

		cbLaunchActivity = new JComboBox<String>();
		cbLaunchActivity.setAlignmentY(0);
		cbLaunchActivity.setAlignmentX(0);
		Dimension maxSize = cbLaunchActivity.getMaximumSize();
		maxSize.height = cbLaunchActivity.getMinimumSize().height;
		cbLaunchActivity.setMaximumSize(maxSize);
		additionalOptionsPanel.add(cbLaunchActivity);

		//additionalOptionsPanel

		ckReplace = new JCheckBox("Replace existing application");
		ckDowngrade = new JCheckBox("Allow version code downgrade");
		ckOnSdCard = new JCheckBox("Install application on sdcard");
		ckGrandPerm = new JCheckBox("Grant all runtime permissions");
		ckLock = new JCheckBox("Forward lock application");
		ckTestPack = new JCheckBox("Allow test packages");

		additionalOptionsPanel.add(ckReplace);
		additionalOptionsPanel.add(ckDowngrade);
		additionalOptionsPanel.add(ckOnSdCard);
		additionalOptionsPanel.add(ckGrandPerm);
		additionalOptionsPanel.add(ckLock);
		additionalOptionsPanel.add(ckTestPack);

		installOptionsPanel.add(additionalOptionsPanel);
		installOptionsPanel.add(Box.createVerticalGlue());

		return installOptionsPanel;
	}

	private JPanel makePushOptionPanel() {
		JPanel pushOptionsPanel = new JPanel();
		pushOptionsPanel.setLayout(new BoxLayout(pushOptionsPanel, BoxLayout.Y_AXIS));
		pushOptionsPanel.setBorder(BorderFactory.createTitledBorder("Push options"));
		
		rbSystemPush = new JRadioButton("/system/app");
		rbPrivPush = new JRadioButton("/system/priv-app");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbSystemPush);
		bg.add(rbPrivPush);

		Box installLocationBox = Box.createHorizontalBox();
		installLocationBox.setAlignmentX(0);
		installLocationBox.setAlignmentY(0);
		installLocationBox.add(new JLabel("Path:"));
		installLocationBox.add(Box.createHorizontalStrut(5));
		installLocationBox.add(rbSystemPush);
		installLocationBox.add(Box.createHorizontalStrut(10));
		installLocationBox.add(rbPrivPush);
		installLocationBox.setMaximumSize(installLocationBox.getMinimumSize());

		pushOptionsPanel.add(installLocationBox);
		JTextField txtTargetPath = new JTextField("/system/app/apkscanner/apk.apk");
		txtTargetPath.setEditable(false);
		txtTargetPath.setCaretPosition(0);
		pushOptionsPanel.add(txtTargetPath);

		ckReboot = new JCheckBox("Reboot after pushed");
		pushOptionsPanel.add(ckReboot);
		pushOptionsPanel.add(Box.createVerticalStrut(5));
		pushOptionsPanel.add(new JLabel("With Libraries"));
		//pushOptionsPanel.add(CheckWithLib32);
		//pushOptionsPanel.add(CheckWithLib64);

		lib32Box = Box.createHorizontalBox();
		lib32Box.setAlignmentX(0);
		lib32Box.setAlignmentY(0);

		ckLib32 = new JCheckBox("32Bit");
		lib32Box.add(ckLib32);
		cbLib32Src = new JComboBox<String>();
		cbLib32Src.setEditable(false);
		Dimension maxSize = cbLib32Src.getMaximumSize();
		maxSize.height = cbLib32Src.getMinimumSize().height;
		Dimension prefSize = new Dimension(maxSize);
		//Dimension minSize = new Dimension(maxSize);
		prefSize.width = 110;
		//minSize.width = 150;

		cbLib32Src.setSize(prefSize);
		cbLib32Src.setPreferredSize(prefSize);
		cbLib32Src.setMaximumSize(prefSize);
		cbLib32Src.setMinimumSize(prefSize);
		lib32Box.add(cbLib32Src);
		lib32Box.add(new JLabel(">"));
		cbLib32Dest = new JComboBox<String>(new String[]{"/system/lib", "/system/vendor/lib", "/system/app/{package}/lib"});
		cbLib32Dest.setEditable(false);
		cbLib32Dest.setMaximumSize(maxSize);
		cbLib32Dest.setMinimumSize(prefSize);
		//lib32Dest.setPreferredSize(maxSize);
		lib32Box.add(cbLib32Dest);
		//lib32Box.add(Box.createHorizontalGlue());
		pushOptionsPanel.add(lib32Box);

		lib64Box = Box.createHorizontalBox();
		lib64Box.setAlignmentX(0);
		lib64Box.setAlignmentY(0);

		ckLib64 = new JCheckBox("64Bit");
		lib64Box.add(ckLib64);
		cbLib64Src = new JComboBox<String>();
		cbLib64Src.setEditable(false);
		cbLib64Src.setSize(prefSize);
		cbLib64Src.setPreferredSize(prefSize);
		cbLib64Src.setMaximumSize(prefSize);
		cbLib64Src.setMinimumSize(prefSize);
		lib64Box.add(cbLib64Src);
		lib64Box.add(new JLabel(">"));
		cbLib64Dest = new JComboBox<String>(new String[]{"/system/lib64", "/system/vendor/lib64", "/system/app/{package}/lib64"});
		cbLib64Dest.setEditable(false);
		cbLib64Dest.setMaximumSize(maxSize);
		cbLib64Dest.setMinimumSize(prefSize);
		//lib64Dest.setPreferredSize(maxSize);
		lib64Box.add(cbLib64Dest);
		//lib64Box.add(Box.createHorizontalGlue());
		pushOptionsPanel.add(lib64Box);

		//pushOptionsPanel.add(new JLabel("▶ Show libray list"));

		DefaultTableModel model = new DefaultTableModel(new String[][] { 
			{"1", "APK/lib/armeabi64-v7a/fjklds.so" , "/system/lib/fjklds.so"},
			{"2", "APK/lib/armeabi64-v7a/fjklds.so" , "/system/lib/fjklds.so"},
			{"3", "APK/lib/armeabi64-v7a/fjklds.so" , "/system/lib/fjklds.so"},
			{"4", "APK/lib/armeabi64-v7a/fjklds.so" , "/system/lib/fjklds.so"},
			{"5", "APK/lib/armeabi64-v7a/fjklds.so" , "/system/lib/fjklds.so"}
		}, new String[] {"No." , "Srouce" , "Destination"});
		JTable list = new JTable(model);
		JScrollPane listPanel = new JScrollPane(list);
		listPanel.setAlignmentX(0);
		pushOptionsPanel.add(listPanel);

		pushOptionsPanel.add(Box.createVerticalGlue());

		return pushOptionsPanel;
	}

	private AbstractButton makeButton(String title, String actionCommand) {
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

	private JPanel makeToggleButtonBar(int cc, boolean round, ActionListener listener) {
		bgInstallMethod = new ButtonGroup();
		JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));
		p.setBorder(BorderFactory.createTitledBorder("How to install"));
		//p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		Color color = new Color(cc);
		for (AbstractButton b: Arrays.asList(makeButton("Install", ACT_CMD_INSTALL), makeButton("Push", ACT_CMD_PUSH), makeButton("Not install", ACT_CMD_NOT_INSTALL))) {
			b.setBackground(color);
			b.setIcon(new ToggleButtonBarCellIcon());
			b.addActionListener(listener);

			bgInstallMethod.add(b);
			p.add(b);
		}

		return p;
	}

	public void setApkInfo(CompactApkInfo apkinfo) {
		synchronized (this) {
			cbLaunchActivity.removeAllItems();
			if(apkinfo.activityList != null && apkinfo.activityList.length > 0) {
				for(ComponentInfo c: apkinfo.activityList) {
					cbLaunchActivity.addItem(c.name);
				}
				cbLaunchActivity.setEnabled(true);

				if(bundle != null) {
					String selActivity = bundle.getLaunchActivity(); 
					if(selActivity != null && !selActivity.isEmpty()) {
						cbLaunchActivity.setSelectedItem(selActivity);
					}
				}
			} else {
				cbLaunchActivity.addItem("No Such Activity");
				cbLaunchActivity.setEnabled(false);
			}
			
			cbLib32Src.removeAllItems();
			//cbLib32Dest.removeAllItems();
			cbLib64Src.removeAllItems();
			//cbLib64Dest.removeAllItems();
			if(apkinfo.libraries != null && apkinfo.libraries.length > 0) {
				ArrayList<String> archList = new ArrayList<String>();
				for(String lib: apkinfo.libraries) {
					if(!lib.startsWith("lib/")) {
						Log.v("Unknown lib path : " + lib);
						continue;
					}
					String arch = lib.replaceAll("lib/([^/]*)/.*", "$1");
					if(!archList.contains(arch)) {
						archList.add(arch);
						if(!arch.contains("64")) {
							cbLib32Src.addItem(arch);
						} else {
							cbLib64Src.addItem(arch);
						}
					}
				}

				lib32Box.setVisible(cbLib32Src.getItemCount() > 0);
				lib64Box.setVisible(cbLib64Src.getItemCount() > 0);

				ckLib32.setSelected(true);
				ckLib64.setSelected(true);
				cbLib32Src.setEnabled(true);
				cbLib32Dest.setEnabled(true);
				cbLib64Src.setEnabled(true);
				cbLib64Dest.setEnabled(true);
			} else {
				ckLib32.setSelected(false);
				ckLib64.setSelected(false);
				cbLib32Src.setEnabled(false);
				cbLib32Dest.setEnabled(false);
				cbLib64Src.setEnabled(false);
				cbLib64Dest.setEnabled(false);

				lib32Box.setVisible(false);
				lib64Box.setVisible(false);
			}
		}
	}

	public void setOptions(OptionsBundle bundle) {
		synchronized (this) {
			this.bundle = bundle;

			String selectAct = null;
			if(bundle.isInstallOptions()) {
				selectAct = ACT_CMD_INSTALL;
			} else if(bundle.isPushOptions()) {
				selectAct = ACT_CMD_PUSH;
			} else {
				selectAct = ACT_CMD_NOT_INSTALL;
			}
			((CardLayout)optionsPanel.getLayout()).show(optionsPanel, selectAct);

			Enumeration<AbstractButton> btnGroup = bgInstallMethod.getElements();
			while(btnGroup.hasMoreElements()) {
				AbstractButton btn = btnGroup.nextElement();
				if(selectAct.equals(btn.getActionCommand())) {
					bgInstallMethod.setSelected(btn.getModel(), true);
					break;
				}
			}
			
			String selActivity = bundle.getLaunchActivity(); 
			if(selActivity != null && !selActivity.isEmpty()) {
				cbLaunchActivity.setSelectedItem(selActivity);
			}
			ckLaucnApp.setSelected(bundle.isSetLaunch());
			ckLaucnApp.setEnabled(!bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_INSTALL_LAUNCH));
			ckReplace.setSelected(bundle.isSetReplace());
			ckDowngrade.setSelected(bundle.isSetDowngrade());
			ckOnSdCard.setSelected(bundle.isSetOnSdcard());
			ckGrandPerm.setSelected(bundle.isSetGrantPermissions());
			ckLock.setSelected(bundle.isSetForwardLock());
			ckTestPack.setSelected(bundle.isSetAllowTestPackage());
			
			ckReboot.setSelected(bundle.isSetReboot());
			
		}
	}

}
