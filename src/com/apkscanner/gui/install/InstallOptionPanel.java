package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.util.ToggleButtonBarCellIcon;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class InstallOptionPanel extends JPanel implements ItemListener {
	private static final long serialVersionUID = 2307623568442307145L;

	public static final String ACT_CMD_INSTALL = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL);
	public static final String ACT_CMD_PUSH = Integer.toString(OptionsBundle.FLAG_OPT_PUSH);
	public static final String ACT_CMD_NO_INSTALL = Integer.toString(OptionsBundle.FLAG_OPT_NO_INSTALL);

	public static final String ACT_OPT_LAUNCH = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_LAUNCH);
	public static final String ACT_OPT_REPLACE = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_REPLACE);
	public static final String ACT_OPT_DOWNGRADE = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_DOWNGRADE);
	public static final String ACT_OPT_ON_SDCARD = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_ON_SDCARD);
	public static final String ACT_OPT_GRANT_PERM = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_GRANT_PERM);
	public static final String ACT_OPT_FORWARD_LOCK = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_FORWARD_LOCK);
	public static final String ACT_OPT_ALLOW_TEST = Integer.toString(OptionsBundle.FLAG_OPT_INSTALL_TEST_PACKAGE); 

	public static final String ACT_OPT_PUSH_SYSTEM = Integer.toString(OptionsBundle.FLAG_OPT_PUSH_SYSTEM);
	public static final String ACT_OPT_PUSH_PRIVAPP = Integer.toString(OptionsBundle.FLAG_OPT_PUSH_PRIVAPP);
	public static final String ACT_OPT_PUSH_REBOOT = Integer.toString(OptionsBundle.FLAG_OPT_PUSH_REBOOT);
	public static final String ACT_OPT_PUSH_LIB32 = Integer.toString(OptionsBundle.FLAG_OPT_PUSH_LIB32);
	public static final String ACT_OPT_PUSH_LIB64 = Integer.toString(OptionsBundle.FLAG_OPT_PUSH_LIB64);

	private OptionsBundle bundle;
	private String[] libraries;

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

	//private ButtonGroup bgPushDest;
	private JRadioButton rbSystemPush;
	private JRadioButton rbPrivPush;
	private JTextField txtTargetPath; 
	private JCheckBox ckReboot;
	private JLabel lbWithLibs;
	private Box lib32Box;
	private JCheckBox ckLib32;
	private JComboBox<String> cbLib32Src;
	private JComboBox<String> cbLib32Dest;
	private Box lib64Box;
	private JCheckBox ckLib64;
	private JComboBox<String> cbLib64Src;
	private JComboBox<String> cbLib64Dest;
	private JTable libPreviewList;
	private JScrollPane libPreviewPanel;

	public InstallOptionPanel() {
		setLayout(new BorderLayout());

		JPanel installMethodPanel = makeToggleButtonBar(0x555555, true, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String actionCommand = arg0.getActionCommand();
				int blockedCause = 0;

				if(bundle != null) {
					if(ACT_CMD_INSTALL.equals(actionCommand)) {
						if(bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_INSTALL)) {
							blockedCause = bundle.getBlockedCause(OptionsBundle.FLAG_OPT_INSTALL);
						} else {
							bundle.set(OptionsBundle.FLAG_OPT_INSTALL);
						}
					} else if(ACT_CMD_PUSH.equals(actionCommand)) {
						if(bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_PUSH)) {
							blockedCause = bundle.getBlockedCause(OptionsBundle.FLAG_OPT_PUSH);
						} else {
							bundle.set(OptionsBundle.FLAG_OPT_PUSH);	
						}
					} else if(!bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_NO_INSTALL)) {
						bundle.set(OptionsBundle.FLAG_OPT_NO_INSTALL);
					}
				}

				if(blockedCause == 0) {
					((CardLayout)optionsPanel.getLayout()).show(optionsPanel, actionCommand);
				} else {
					setOptions(bundle);
					int messageId = 0;
					switch(blockedCause) {
					case OptionsBundle.BLOACKED_COMMON_CAUSE_UNSIGNED:
						messageId = MessageBoxPool.MSG_BLOCKED_CAUSE_UNSIGNED; break;
					case OptionsBundle.BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL:
						messageId = MessageBoxPool.MSG_BLOCKED_UNSUPPORTED_SDK_LEVEL; break;
					case OptionsBundle.BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED:
						messageId = MessageBoxPool.MSG_BLOCKED_MISMATCH_SIGNED; break;
					case OptionsBundle.BLOACKED_PUSH_CAUSE_NO_ROOT:
						messageId = MessageBoxPool.MSG_BLOCKED_NO_ROOT; break;
					case OptionsBundle.BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM:
						messageId = MessageBoxPool.MSG_BLOCKED_MISMATCH_SIGNED_NOT_SYSTEM; break;
					case OptionsBundle.BLOACKED_CAUSE_UNKNWON:
						messageId = MessageBoxPool.MSG_BLOCKED_UNKNOWN; break;
					}
					MessageBoxPool.show(InstallOptionPanel.this, messageId);
				}
			}
		});

		optionsPanel = new JPanel(new CardLayout());
		optionsPanel.add(makeInstallOptionsPanel(), ACT_CMD_INSTALL);
		optionsPanel.add(makePushOptionPanel(), ACT_CMD_PUSH);
		optionsPanel.add(new JPanel(), ACT_CMD_NO_INSTALL);

		JButton disseminate = new JButton(Resource.STR_BTN_APPLY_ALL_MODELS.getString());
		disseminate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(bundle != null) {
					bundle.disseminate();
				}
			}
		});

		add(installMethodPanel, BorderLayout.NORTH);
		add(optionsPanel, BorderLayout.CENTER);		
		add(disseminate, BorderLayout.SOUTH);
	}

	private JPanel makeInstallOptionsPanel() {
		JPanel installOptionsPanel = new JPanel();
		installOptionsPanel.setLayout(new BoxLayout(installOptionsPanel, BoxLayout.Y_AXIS));
		installOptionsPanel.setBorder(BorderFactory.createTitledBorder(Resource.STR_LABEL_INSTALL_OPTIONS.getString()));

		ckLaucnApp = new JCheckBox(Resource.STR_BTN_LAUNCH_AF_INSTALLED.getString());
		ckLaucnApp.setActionCommand(ACT_OPT_LAUNCH);
		ckLaucnApp.addItemListener(this);

		installOptionsPanel.add(ckLaucnApp);

		installOptionsPanel.add(Box.createVerticalStrut(5));

		final JPanel additionalOptionsPanel = new JPanel();
		additionalOptionsPanel.setAlignmentX(0);
		additionalOptionsPanel.setVisible(false);
		additionalOptionsPanel.setLayout(new BoxLayout(additionalOptionsPanel, BoxLayout.Y_AXIS));

		JLabel additionalOptionsLabel = new JLabel("▶ " + Resource.STR_LABEL_ADDITIONAL_OPTIONS.getString());
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
		cbLaunchActivity.setActionCommand(ACT_OPT_LAUNCH);
		cbLaunchActivity.addItemListener(this);
		cbLaunchActivity.setAlignmentY(0);
		cbLaunchActivity.setAlignmentX(0);
		Dimension maxSize = cbLaunchActivity.getMaximumSize();
		Dimension minSize = cbLaunchActivity.getMinimumSize();
		maxSize.height = minSize.height;
		cbLaunchActivity.setMaximumSize(maxSize);
		minSize.width = 100;
		cbLaunchActivity.setMinimumSize(minSize);

		Box launcherBox = Box.createHorizontalBox();
		launcherBox.setAlignmentX(0);
		launcherBox.setAlignmentY(0);
		launcherBox.setMaximumSize(maxSize);
		JLabel launcherLabel = new JLabel(Resource.STR_LABEL_LAUNCHER_AF_INSTALLED.getString());
		launcherLabel.setAlignmentX(0);
		launcherLabel.setAlignmentY(0);
		launcherLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
		launcherBox.add(launcherLabel);
		launcherBox.add(Box.createHorizontalStrut(5));
		launcherBox.add(cbLaunchActivity);
		additionalOptionsPanel.add(launcherBox);

		//additionalOptionsPanel

		ckReplace = new JCheckBox(Resource.STR_BTN_REPLACE_EXISTING_APP.getString());
		ckReplace.setActionCommand(ACT_OPT_REPLACE);
		ckReplace.addItemListener(this);

		ckDowngrade = new JCheckBox(Resource.STR_BTN_ALLOW_DOWNGRADE.getString());
		ckDowngrade.setActionCommand(ACT_OPT_DOWNGRADE);
		ckDowngrade.addItemListener(this);

		ckOnSdCard = new JCheckBox(Resource.STR_BTN_INSTALL_ON_SDCARD.getString());
		ckOnSdCard.setActionCommand(ACT_OPT_ON_SDCARD);
		ckOnSdCard.addItemListener(this);

		ckGrandPerm = new JCheckBox(Resource.STR_BTN_GRANT_RUNTIME_PERM.getString());
		ckGrandPerm.setActionCommand(ACT_OPT_GRANT_PERM);
		ckGrandPerm.addItemListener(this);

		ckTestPack = new JCheckBox(Resource.STR_BTN_ALLOW_TEST_PACKAGE.getString());
		ckTestPack.setActionCommand(ACT_OPT_ALLOW_TEST);
		ckTestPack.addItemListener(this);

		ckLock = new JCheckBox(Resource.STR_BTN_FORWARD_LOCK.getString());
		ckLock.setActionCommand(ACT_OPT_FORWARD_LOCK);
		ckLock.addItemListener(this);

		JPanel addCheckOptionsPanel = new JPanel();
		addCheckOptionsPanel.setLayout(new BoxLayout(addCheckOptionsPanel, BoxLayout.Y_AXIS));
		addCheckOptionsPanel.add(ckReplace);
		addCheckOptionsPanel.add(ckDowngrade);
		addCheckOptionsPanel.add(ckOnSdCard);
		addCheckOptionsPanel.add(ckGrandPerm);
		addCheckOptionsPanel.add(ckTestPack);
		addCheckOptionsPanel.add(ckLock);

		JScrollPane emptyBorderScrollPanel = new JScrollPane(addCheckOptionsPanel);
		emptyBorderScrollPanel.setBorder(new EmptyBorder(0,0,0,0));
		emptyBorderScrollPanel.setAlignmentX(0);
		additionalOptionsPanel.add(emptyBorderScrollPanel);

		installOptionsPanel.add(additionalOptionsPanel);
		installOptionsPanel.add(Box.createVerticalGlue());

		return installOptionsPanel;
	}

	private JPanel makePushOptionPanel() {
		JPanel pushOptionsPanel = new JPanel();
		pushOptionsPanel.setLayout(new BoxLayout(pushOptionsPanel, BoxLayout.Y_AXIS));
		pushOptionsPanel.setBorder(BorderFactory.createTitledBorder(Resource.STR_LABEL_PUSH_OPTIONS.getString()));

		rbSystemPush = new JRadioButton("/system/app");
		rbSystemPush.setActionCommand(ACT_OPT_PUSH_SYSTEM);
		rbSystemPush.addItemListener(this);
		rbPrivPush = new JRadioButton("/system/priv-app");
		rbPrivPush.setActionCommand(ACT_OPT_PUSH_PRIVAPP);
		rbPrivPush.addItemListener(this);
		//JRadioButton rbDirect = new JRadioButton("Direct");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbSystemPush);
		bg.add(rbPrivPush);
		//bg.add(rbDirect);

		Box installLocationBox = Box.createHorizontalBox();
		installLocationBox.setAlignmentX(0);
		installLocationBox.setAlignmentY(0);
		installLocationBox.add(new JLabel(Resource.STR_LABEL_PATH.getString() + ":"));
		installLocationBox.add(Box.createHorizontalStrut(5));
		installLocationBox.add(rbSystemPush);
		installLocationBox.add(Box.createHorizontalStrut(10));
		installLocationBox.add(rbPrivPush);
		//installLocationBox.add(Box.createHorizontalStrut(10));
		//installLocationBox.add(rbDirect);
		installLocationBox.setMaximumSize(installLocationBox.getMinimumSize());

		pushOptionsPanel.add(installLocationBox);
		txtTargetPath = new JTextField();
		txtTargetPath.setEditable(false);
		txtTargetPath.setCaretPosition(0);
		Dimension szTxtField = txtTargetPath.getMaximumSize();
		szTxtField.height = txtTargetPath.getMinimumSize().height;
		txtTargetPath.setMaximumSize(szTxtField);
		pushOptionsPanel.add(txtTargetPath);

		ckReboot = new JCheckBox(Resource.STR_BTN_REBOOT_AF_PUSHED.getString());
		ckReboot.setActionCommand(ACT_OPT_PUSH_REBOOT);
		ckReboot.addItemListener(this);
		pushOptionsPanel.add(ckReboot);
		pushOptionsPanel.add(Box.createVerticalStrut(5));
		lbWithLibs = new JLabel(Resource.STR_LABEL_WITH_LIBRARIES.getString());
		pushOptionsPanel.add(lbWithLibs);
		//pushOptionsPanel.add(CheckWithLib32);
		//pushOptionsPanel.add(CheckWithLib64);

		lib32Box = Box.createHorizontalBox();
		lib32Box.setAlignmentX(0);
		lib32Box.setAlignmentY(0);

		ckLib32 = new JCheckBox("32Bit");
		ckLib32.setActionCommand(ACT_OPT_PUSH_LIB32);
		ckLib32.addItemListener(this);
		lib32Box.add(ckLib32);
		cbLib32Src = new JComboBox<String>();
		cbLib32Src.setActionCommand(ACT_OPT_PUSH_LIB32);
		cbLib32Src.addItemListener(this);
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
		cbLib32Dest = new JComboBox<String>(new String[]{"/system/lib/", "/system/vendor/lib/", "{package}/lib/arm/"});
		cbLib32Dest.setActionCommand(ACT_OPT_PUSH_LIB32);
		cbLib32Dest.addItemListener(this);
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
		ckLib64.setActionCommand(ACT_OPT_PUSH_LIB64);
		ckLib64.addItemListener(this);
		lib64Box.add(ckLib64);
		cbLib64Src = new JComboBox<String>();
		cbLib64Src.setActionCommand(ACT_OPT_PUSH_LIB64);
		cbLib64Src.addItemListener(this);
		cbLib64Src.setEditable(false);
		cbLib64Src.setSize(prefSize);
		cbLib64Src.setPreferredSize(prefSize);
		cbLib64Src.setMaximumSize(prefSize);
		cbLib64Src.setMinimumSize(prefSize);
		lib64Box.add(cbLib64Src);
		lib64Box.add(new JLabel(">"));
		cbLib64Dest = new JComboBox<String>(new String[]{"/system/lib64/", "/system/vendor/lib64/", "{package}/lib/arm64"});
		cbLib64Dest.setActionCommand(ACT_OPT_PUSH_LIB64);
		cbLib64Dest.addItemListener(this);
		cbLib64Dest.setEditable(false);
		cbLib64Dest.setMaximumSize(maxSize);
		cbLib64Dest.setMinimumSize(prefSize);
		//lib64Dest.setPreferredSize(maxSize);
		lib64Box.add(cbLib64Dest);
		//lib64Box.add(Box.createHorizontalGlue());
		pushOptionsPanel.add(lib64Box);

		//pushOptionsPanel.add(new JLabel("▶ Show libray list"));

		DefaultTableModel model = new DefaultTableModel(new String[][] { },
				new String[] {Resource.STR_LABEL_NUM.getString() , Resource.STR_LABEL_ABI.getString() , Resource.STR_LABEL_DESTINATION.getString()});
		libPreviewList = new JTable(model) {
			private static final long serialVersionUID = -6116478445588059120L;
			@Override
			public void doLayout() {
				if (tableHeader != null) {
					TableColumn resizingColumn = tableHeader.getResizingColumn();
					//  Viewport size changed. Increase last columns width
					if (resizingColumn == null) {
						TableColumnModel tcm = getColumnModel();
						int lastColumn = tcm.getColumnCount() - 1;
						tableHeader.setResizingColumn( tcm.getColumn( lastColumn ) ) ;
					}
				}
				super.doLayout();
			}
		};
		libPreviewList.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		libPreviewPanel = new JScrollPane(libPreviewList);
		libPreviewPanel.setAlignmentX(0);
		pushOptionsPanel.add(libPreviewPanel);

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
		p.setBorder(BorderFactory.createTitledBorder(Resource.STR_LABEL_HOW_TO_INSTALL.getString()));
		//p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		Color color = new Color(cc);
		for (AbstractButton b: Arrays.asList(
				makeButton(Resource.STR_BTN_TO_INSTALL.getString(), ACT_CMD_INSTALL),
				makeButton(Resource.STR_BTN_TO_PUSH.getString(), ACT_CMD_PUSH),
				makeButton(Resource.STR_BTN_NO_INSTALL.getString(), ACT_CMD_NO_INSTALL))) {
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
				ckLaucnApp.setEnabled(true);

				if(bundle != null) {
					String selActivity = bundle.getLaunchActivity(); 
					if(selActivity != null && !selActivity.isEmpty()) {
						cbLaunchActivity.setSelectedItem(selActivity);
					}
					ckLaucnApp.setSelected(bundle.isSetLaunch());
				}
			} else {
				cbLaunchActivity.addItem(Resource.STR_MSG_NO_SUCH_LAUNCHER.getString());
				cbLaunchActivity.setEnabled(false);
				ckLaucnApp.setSelected(false);
				ckLaucnApp.setEnabled(false);
			}

			cbLib32Src.removeAllItems();
			//cbLib32Dest.removeAllItems();
			cbLib64Src.removeAllItems();
			//cbLib64Dest.removeAllItems();
			libraries = apkinfo.libraries;
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

				lbWithLibs.setVisible((cbLib32Src.getItemCount() + cbLib64Src.getItemCount()) > 0);
				libPreviewPanel.setVisible((cbLib32Src.getItemCount() + cbLib64Src.getItemCount()) > 0);

				lib32Box.setVisible(cbLib32Src.getItemCount() > 0);
				lib64Box.setVisible(cbLib64Src.getItemCount() > 0);

				ckLib32.setSelected(cbLib32Src.getItemCount() > 0);
				ckLib64.setSelected(cbLib64Src.getItemCount() > 0);

				refreshLibPreview();
			} else {
				lbWithLibs.setVisible(false);
				libPreviewPanel.setVisible(false);
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
				selectAct = ACT_CMD_NO_INSTALL;
			}
			((CardLayout)optionsPanel.getLayout()).show(optionsPanel, selectAct);

			Enumeration<AbstractButton> btnGroup = bgInstallMethod.getElements();
			while(btnGroup.hasMoreElements()) {
				AbstractButton btn = btnGroup.nextElement();
				String actCmd = btn.getActionCommand();
				if(selectAct.equals(actCmd)) {
					bgInstallMethod.setSelected(btn.getModel(), true);
					if(ACT_CMD_NO_INSTALL.equals(actCmd)) {
						if(bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_INSTALL | OptionsBundle.FLAG_OPT_PUSH)) {
							btn.setText(Resource.STR_BTN_IMPOSSIBLE_INSTALL.getString());
						} else {
							btn.setText(Resource.STR_BTN_NO_INSTALL.getString());
						}
					}
				} else if(ACT_CMD_INSTALL.equals(actCmd)) {
					if(bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_INSTALL)) {
						//btn.setEnabled(false);
						btn.setText(Resource.STR_BTN_TO_CANNOT_INSTALL.getString());
					} else {
						//btn.setEnabled(true);
						btn.setText(Resource.STR_BTN_TO_INSTALL.getString());
					}
				} else if(ACT_CMD_PUSH.equals(actCmd)) {
					if(bundle.isBlockedFlags(OptionsBundle.FLAG_OPT_PUSH)) {
						//btn.setEnabled(false);
						btn.setText(Resource.STR_BTN_TO_CANNOT_PUSH.getString());
					} else {
						//btn.setEnabled(true);
						btn.setText(Resource.STR_BTN_TO_PUSH.getString());
					}
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

			rbSystemPush.setSelected(bundle.isSetPushToSystem());
			rbPrivPush.setSelected(bundle.isSetPushToPriv());

			if(cbLib32Dest.getItemCount() > 2) {
				cbLib32Dest.removeItemAt(2);
			}
			if(cbLib64Dest.getItemCount() > 2) {
				cbLib64Dest.removeItemAt(2);
			}

			String systemPath = bundle.getTargetSystemPath();
			if(systemPath != null) {
				txtTargetPath.setText(systemPath);
				if(systemPath.matches("/system/(priv-)?app/[^/]*/[^/]*\\.apk")) {
					cbLib32Dest.addItem(systemPath.replaceAll("[^/]*.apk$", "lib/arm/"));
					cbLib64Dest.addItem(systemPath.replaceAll("[^/]*.apk$", "lib/arm64/"));
				}
			} else if(bundle.isSetPushToPriv()) {
				txtTargetPath.setText("/system/priv-app/unknown-1/unknown-1.apk");
			} else {
				txtTargetPath.setText("/system/app/unknown-1/unknown-1.apk");
			}
			ckReboot.setSelected(bundle.isSetReboot());
			ckLib32.setSelected(bundle.isSetWithLib32());
			ckLib64.setSelected(bundle.isSetWithLib64());

			if(bundle.isSetWithLib32()) {
				cbLib32Dest.setSelectedItem(bundle.getWithLib32ToPath());
				cbLib32Src.setSelectedItem(bundle.getWithLib32Arch());
			}
			if(bundle.isSetWithLib64()) {
				cbLib64Dest.setSelectedItem(bundle.getWithLib64ToPath());
				cbLib64Src.setSelectedItem(bundle.getWithLib64Arch());
			}
			refreshLibPreview();
		}
	}

	private void refreshLibPreview() {
		/*
		private JCheckBox ckLib32;
		private JComboBox<String> cbLib32Src;
		private JComboBox<String> cbLib32Dest;
		private Box lib64Box;
		private JCheckBox ckLib64;
		private JComboBox<String> cbLib64Src;
		private JComboBox<String> cbLib64Dest;
		private JTable libPreviewList;
		 */

		DefaultTableModel tableModel = (DefaultTableModel)libPreviewList.getModel();
		while(tableModel.getRowCount() > 0) {
			tableModel.removeRow(0);
		}

		int idx = 1;
		if(ckLib32.isSelected()) {
			String selArch = (String) cbLib32Src.getSelectedItem();
			String selDest = (String) cbLib32Dest.getSelectedItem();
			String filter = "lib/" + selArch + "/";
			for(String lib: libraries) {
				if(lib.startsWith(filter)) {
					tableModel.addRow(new String[] {Integer.toString(idx++) , selArch , lib.replace(filter, selDest)});
				}
			}
		}

		if(ckLib64.isSelected()) {
			String selArch = (String) cbLib64Src.getSelectedItem();
			String selDest = (String) cbLib64Dest.getSelectedItem();
			String filter = "lib/" + selArch + "/";
			for(String lib: libraries) {
				if(lib.startsWith(filter)) {
					tableModel.addRow(new String[] {Integer.toString(idx++) , selArch , lib.replace(filter, selDest)});
				}
			}
		}

	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		String actCmd = null;
		String[] extraData = null;
		int flag = 0;

		if(arg0.getSource() instanceof AbstractButton) {
			actCmd = ((AbstractButton) arg0.getSource()).getActionCommand();
			flag = Integer.parseInt(actCmd);
			boolean isSelected = arg0.getStateChange() == ItemEvent.SELECTED;
			switch(flag) {
			case OptionsBundle.FLAG_OPT_INSTALL_LAUNCH:
				extraData = new String[] { (String)cbLaunchActivity.getSelectedItem() };
				break;
			case OptionsBundle.FLAG_OPT_PUSH_SYSTEM:
			case OptionsBundle.FLAG_OPT_PUSH_PRIVAPP:
				String path = (flag == OptionsBundle.FLAG_OPT_PUSH_SYSTEM) ? "/system/app/" : "/system/priv-app/";
				String convPath = txtTargetPath.getText().replaceAll("/system/(priv-)?app/", path);
				txtTargetPath.setText(convPath);

				boolean needRefresh32 = cbLib32Dest.getSelectedIndex() == 2;
				boolean needRefresh64 = cbLib64Dest.getSelectedIndex() == 2;

				if(cbLib32Dest.getItemCount() > 2) {
					cbLib32Dest.removeItemAt(2);
				}
				if(cbLib64Dest.getItemCount() > 2) {
					cbLib64Dest.removeItemAt(2);
				}
				if(convPath.matches("/system/(priv-)?app/[^/]*/[^/]*\\.apk")) {
					cbLib32Dest.addItem(convPath.replaceAll("[^/]*.apk$", "lib/arm/"));
					if(needRefresh32) cbLib32Dest.setSelectedIndex(2);
					cbLib64Dest.addItem(convPath.replaceAll("[^/]*.apk$", "lib/arm64/"));
					if(needRefresh64) cbLib64Dest.setSelectedIndex(2);
				}

				if(bundle != null && isSelected) {
					String installedPath = bundle.getInstalledPath();
					if(installedPath != null && !installedPath.equals(convPath)) {
						if(installedPath.startsWith("/system/")) {
							Log.w("need remove installed app");
						} else {
							Log.v("Existed installed apk from /data/");
						}
					}
				}
				break;
			case OptionsBundle.FLAG_OPT_PUSH_LIB32:
				cbLib32Src.setEnabled(isSelected);
				cbLib32Dest.setEnabled(isSelected);
				if(isSelected) extraData = new String[] { (String)cbLib32Src.getSelectedItem(), (String)cbLib32Dest.getSelectedItem() };
				break;
			case OptionsBundle.FLAG_OPT_PUSH_LIB64:
				cbLib64Src.setEnabled(isSelected);
				cbLib64Dest.setEnabled(isSelected);
				if(isSelected) extraData = new String[] { (String)cbLib64Src.getSelectedItem(), (String)cbLib64Dest.getSelectedItem() };
				break;
			}
		} else if(arg0.getSource() instanceof JComboBox &&
				arg0.getStateChange() == ItemEvent.SELECTED) {
			actCmd = ((JComboBox<?>) arg0.getSource()).getActionCommand();
			flag = Integer.parseInt(actCmd);
			switch(flag) {
			case OptionsBundle.FLAG_OPT_INSTALL_LAUNCH:
				extraData = new String[] { (String)arg0.getItem() };
				if(!ckLaucnApp.isSelected()) {
					ckLaucnApp.setSelected(true);
					flag = 0;
				}
				break;
			case OptionsBundle.FLAG_OPT_PUSH_LIB32:
				extraData = new String[] { (String)cbLib32Src.getSelectedItem(), (String)cbLib32Dest.getSelectedItem() };
				break;
			case OptionsBundle.FLAG_OPT_PUSH_LIB64:
				extraData = new String[] { (String)cbLib64Src.getSelectedItem(), (String)cbLib64Dest.getSelectedItem() };
				break;
			}
		}

		switch(flag) {
		case OptionsBundle.FLAG_OPT_PUSH_LIB32:
		case OptionsBundle.FLAG_OPT_PUSH_LIB64:
			refreshLibPreview();
			break;
		}

		if(bundle != null && flag != 0) {
			Log.v("flag 0x" + Integer.toHexString(flag) + ", extraData " + extraData + ", getStateChange " + arg0.getStateChange());
			if(arg0.getStateChange() == ItemEvent.SELECTED) {
				bundle.set(flag, extraData);
			} else {
				bundle.unset(flag);
			}
		}
	}

}
