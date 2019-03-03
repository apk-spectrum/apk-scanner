package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.signer.Signature;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickEvent;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickListener;
import com.apkscanner.plugin.IPackageSearcher;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.gui.util.WindowSizeMemorizer;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.SimpleOutputReceiver;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.FileUtil.FSStyle;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class PackageInfoPanel extends JPanel implements ActionListener, HyperlinkClickListener, ChangeListener{
	private static final long serialVersionUID = -2600940167326680123L;

	private static final String ACT_CMD_OPEN_PACKAGE = "ACT_CMD_OPEN_PACKAGE";
	private static final String ACT_CMD_SAVE_PACKAGE = "ACT_CMD_SAVE_PACKAGE";
	private static final String ACT_CMD_LAUCH_PACKAGE = "ACT_CMD_LAUCH_PACKAGE";
	private static final String ACT_CMD_UNINSTALL_PACKAGE = "ACT_CMD_UNINSTALL_PACKAGE";
	private static final String ACT_CMD_CLEAR_DATA = "ACT_CMD_CLEAR_DATA";

	private JDialog dialog;

	private JToolBar toolBar;
	private JTabbedPane tabbedPane;
	private JHtmlEditorPane infoPanel;
	private JHtmlEditorPane sysPackInfoPanel;
	private JTextArea dumpsysTextArea;
	private JTextArea signatureTextArea;
	private JTextField txtApkPath;

	private PackageInfo packageInfo;
	private boolean hasSysPack;
	private String apkPath;
	private String hiddenApkPath;

	public PackageInfoPanel() {
		setLayout(new BorderLayout());
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		toolBar.setBorder(new MatteBorder(0,0,1,0,Color.LIGHT_GRAY));

		JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		separator.setPreferredSize(new Dimension(1,30));

		toolBar.add(getToolbarButton("open", Resource.IMG_APP_ICON.getImageIcon(24, 24), null, ACT_CMD_OPEN_PACKAGE));
		toolBar.add(getToolbarButton("save", Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_SAVE.getImageIcon(24, 24), null, ACT_CMD_SAVE_PACKAGE));
		toolBar.add(separator);
		toolBar.add(getToolbarButton(null, Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(24, 24), null, ACT_CMD_LAUCH_PACKAGE));
		toolBar.add(getToolbarButton("uninstall", Resource.IMG_TOOLBAR_UNINSTALL.getImageIcon(24, 24), null, ACT_CMD_UNINSTALL_PACKAGE));
		toolBar.add(getToolbarButton("clear", Resource.IMG_TOOLBAR_CLEAR.getImageIcon(24, 24), null, ACT_CMD_CLEAR_DATA));

		add(toolBar, BorderLayout.NORTH);

		String tabbedStyle = (String) Resource.PROP_TABBED_UI_THEME.getData();
		tabbedPane = new JTabbedPane();
		TabbedPaneUIManager.setUI(tabbedPane, tabbedStyle);
		tabbedPane.addChangeListener(this);

		infoPanel = new JHtmlEditorPane();
		infoPanel.setEditable(false);
		infoPanel.setOpaque(true);
		infoPanel.setBackground(Color.white);
		infoPanel.addHyperlinkClickListener(this);

		sysPackInfoPanel = new JHtmlEditorPane();
		sysPackInfoPanel.setEditable(false);
		sysPackInfoPanel.setOpaque(true);
		sysPackInfoPanel.setBackground(Color.white);
		sysPackInfoPanel.addHyperlinkClickListener(this);

		dumpsysTextArea = new JTextArea();
		dumpsysTextArea.setEditable(false);

		signatureTextArea = new JTextArea();
		signatureTextArea.setEditable(false);

		add(tabbedPane, BorderLayout.CENTER);

		txtApkPath = new JTextField();
		txtApkPath.setOpaque(true);
		txtApkPath.setEditable(false);

		add(txtApkPath, BorderLayout.SOUTH);
	}

	private JButton getToolbarButton(String text, Icon icon, String tooltip, String actCommand) {
		JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.addActionListener(this);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setFocusable(false);
		button.setActionCommand(actCommand);
		button.setVerticalTextPosition(JLabel.BOTTOM);
		button.setHorizontalTextPosition(JLabel.CENTER);
		//button.setPreferredSize(new Dimension(43,45));
		return button;
	}

	public void setPackageInfo(PackageInfo info) {
		packageInfo = info;

		hasSysPack = info.getHiddenSystemPackageValue("pkg") != null;
		alignTabbedPanel(hasSysPack);

		txtApkPath.setText(info.getApkPath());

		setPacakgeData(info, false);
		if(hasSysPack) {
			setPacakgeData(info, true);
		}

		setDumpsysText(info.getDumpsys());

		String publicKey = null;
		String sig = info.getSignature();
		if(sig != null && !sig.isEmpty()
				&& !sig.startsWith("Permission denied")) {
			SignatureReport sr = new SignatureReport(new Signature[] {new Signature(sig)});
			publicKey = sr.toString();
		} else {
			publicKey = sig;
		}
		signatureTextArea.setText(publicKey);
		signatureTextArea.setCaretPosition(0);
	}

	private void alignTabbedPanel(boolean hasSysPackPanel) {
		tabbedPane.removeAll();
		tabbedPane.addTab(Resource.STR_TAB_PACAKGE_INFO.getString(), null, infoPanel, null);
		if(hasSysPackPanel) {
			tabbedPane.addTab(Resource.STR_TAB_SYS_PACAKGE_INFO.getString(), null, sysPackInfoPanel, null);
		}
		tabbedPane.addTab(Resource.STR_TAB_DUMPSYS.getString(), null, new JScrollPane(dumpsysTextArea), null);
		tabbedPane.addTab(Resource.STR_TAB_SIGNATURES.getString(), null, new JScrollPane(signatureTextArea), null);
	}

	private void setPacakgeData(PackageInfo info, boolean isSystemPackage)
	{
		JHtmlEditorPane panel = !isSystemPackage ? infoPanel : sysPackInfoPanel;
		panel.setText(Resource.RAW_PACKAGE_INFO_LAYOUT_HTML.getString());

		String infoBlock = !isSystemPackage ? "Packages" : "Hidden system packages";
		String apkPath = !isSystemPackage ? info.getApkPath() : info.getHiddenSystemPackageValue("codePath");

		if(!isSystemPackage) {
			this.apkPath = apkPath;
		} else {
			this.hiddenApkPath = apkPath;
		}

		setPackageName(panel, info.packageName);
		setVersion(panel, info.getValue(infoBlock, "versionName"), info.getValue(infoBlock, "versionCode"));
		setSdkVersion(panel, info.getValue(infoBlock, "minSdk"), info.getValue(infoBlock, "targetSdk"), info.getValue(infoBlock, "maxSdk"));
		setFileSize(panel, info.device, apkPath);
		setFeatures(panel, info, isSystemPackage);
		setInstaller(panel, info.getValue(infoBlock, "installerPackageName"), info.getValue(infoBlock, "timeStamp"), infoBlock);
		setPluginSearcher(panel);
	}

	private void setPackageName(JHtmlEditorPane panel, String packageName) {
		panel.setOuterHTMLById("package", packageName);
	}

	private void setVersion(JHtmlEditorPane panel, String versionName, String versionCode) {
		if(versionName == null) versionName = "";
		StringBuilder text = new StringBuilder("Ver. ").append(versionName)
				.append(" / ").append((versionCode != null ? versionCode : "0"));
		StringBuilder descripton = new StringBuilder("VersionName : ").append(versionName).append("\n")
				.append("VersionCode : ").append((versionCode != null ? versionCode : "Unspecified"));
		String versionDesc = descripton.toString();

		panel.setInnerHTMLById("version", makeHyperEvent(panel, "app-version", text.toString(), versionDesc, versionDesc));
	}

	private void setSdkVersion(JHtmlEditorPane panel, String minSdkVersion, String targetSdkVersion, String maxSdkVersion) {
		StringBuilder sdkVersion = new StringBuilder();
		if(minSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperEvent(panel, "min-sdk-info", minSdkVersion +" (Min)", "Min SDK version", minSdkVersion));
		}
		if(targetSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperEvent(panel, "target-sdk-info", targetSdkVersion + " (Target)", "Targer SDK version", targetSdkVersion));
		}
		if(maxSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperEvent(panel, "max-sdk-info", maxSdkVersion + " (Max)", "Max SDK version", maxSdkVersion));
		}
		if(sdkVersion.length() == 0) {
			sdkVersion.append(", Unspecified");
		}
		panel.setOuterHTMLById("sdk-version", sdkVersion.substring(2));
	}

	private void setFileSize(JHtmlEditorPane panel, IDevice device, String apkPath) {
		long apkSize = 0;
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		try {
			device.executeShellCommand("ls -l " + apkPath, outputReceiver);
		} catch (TimeoutException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			Log.w(e1.getMessage());
		}
		for(String line: outputReceiver.getOutput()) {
			if(line.isEmpty()) continue;
			String size = line.replaceAll(".*\\s+(\\d+)\\s+\\d+.*", "$1");
			if(!line.equals(size)) {
				apkSize = Integer.parseInt(size);
				if(!apkPath.endsWith(".apk")) {
					String tmp = line.replaceAll(".*\\s(\\S*\\.apk)", "/$1");
					if(!line.equals(tmp)) {
						apkPath += tmp;
					}
				}
			}
		}
		String text = FileUtil.getFileSize(apkSize, FSStyle.FULL);
		//String description = "MD5: " + FileUtil.getMessageDigest(apkFile, "MD5");
		panel.setInnerHTMLById("file-size", makeHyperEvent(panel, "file-checksum", text, text, apkPath));
	}

	private void setFeatures(JHtmlEditorPane panel, PackageInfo info, boolean isSystemPackage) {
		StringBuilder feature = new StringBuilder("[" + Resource.STR_FEATURE_LAB.getString() + "] ");
		String infoBlock = !isSystemPackage ? "Packages" : "Hidden system packages";

		int state = -1;
		String value = info.getValue(infoBlock, "enabled");
		if(value != null && value.matches("\\d+")) {
			state = Integer.parseInt(value);
		}
		if(state < 0 || state > 1) {
			feature.append("<font style=\"color:#ED7E31; font-weight:bold\">");
			feature.append(makeHyperEvent(panel, "feature-disabled-pack", Resource.STR_FEATURE_DISABLED_PACK_LAB.getString(), Resource.STR_FEATURE_DISABLED_PACK_DESC.getString(), infoBlock));
			feature.append("</font>, ");
		}

		if(isSystemPackage) {
			feature.append(makeHyperEvent(panel, "feature-hidden-pack", Resource.STR_FEATURE_HIDDEN_SYS_PACK_LAB.getString(), Resource.STR_FEATURE_HIDDEN_SYS_PACK_DESC.getString(), infoBlock));
		} else if(info.hasLauncher()) {
			feature.append(makeHyperEvent(panel, "feature-launcher", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), infoBlock));
		} else {
			feature.append(makeHyperEvent(panel, "feature-hidden", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), infoBlock));
		}
		feature.append(", " + makeHyperEvent(panel, "feature-flags", Resource.STR_FEATURE_FLAG_LAB.getString(), Resource.STR_FEATURE_FLAG_DESC.getString(), infoBlock));

		panel.setInnerHTMLById("features", feature.toString());
	}

	private void setInstaller(JHtmlEditorPane panel, String installer, String timestamp, String infoBlock) {
		StringBuilder installerInfo = new StringBuilder("[" + Resource.STR_FEATURE_INSTALLER_LAB.getString() + "] ");
		if(installer == null || installer.isEmpty())
			installer = "N/A";
		installer += ", " + timestamp;
		installerInfo.append(makeHyperEvent(panel, "feature-timeStamp", installer, "TimeStamp", infoBlock));

		panel.setInnerHTMLById("installer", installerInfo.toString());
	}

	private void setPluginSearcher(JHtmlEditorPane panel) {
		String packageSearchers = "";
		//String appLabelSearchers = "";
		if((boolean)Resource.PROP_VISIBLE_TO_BASIC.getData()) {
			IPackageSearcher[] searchers = PlugInManager.getPackageSearchers();
			if(searchers.length > 0) {
				String defaultSearchIcon = Resource.IMG_TOOLBAR_SEARCH.getPath();
				for(IPackageSearcher searcher: searchers) {
					if(!searcher.isVisibleToBasic()) continue;
					URL icon = searcher.getIconURL();
					String iconPath = icon != null ? icon.toString() : defaultSearchIcon;
					String tag = makeHyperEvent(panel, "PLUGIN:"+searcher.hashCode(), String.format("<img src=\"%s\" width=\"16\" height=\"16\">", iconPath), null, searcher.getActionCommand());
					switch(searcher.getSupportType() ) {
					case IPackageSearcher.SEARCHER_TYPE_PACKAGE_NAME:
						packageSearchers += tag;
						break;
					case IPackageSearcher.SEARCHER_TYPE_APP_NAME:
						//appLabelSearchers += tag;
						break;
					};
				}
			}
		}

		//panel.insertElementLast("label", appLabelSearchers);
		if(!packageSearchers.isEmpty()) {
			panel.setOuterHTMLById("package-searcher", packageSearchers);
		} else {
			panel.removeElementById("package-searcher");
		}
	}

	private void setDumpsysText(String[] dumpsys) {
		StringBuilder dumpSys = new StringBuilder();
		StringBuilder packageInfo = new StringBuilder();

		String blockRegex = "^(\\s*)(Hidden system )?[pP]ackages:\\s*$";
		String blockEndRegex = "";
		boolean startInfoBlock = false;
		for(String line: dumpsys) {
			if(startInfoBlock && line.matches(blockEndRegex)) {
				startInfoBlock = false;
			}

			if(!startInfoBlock) {
				if(line.matches(blockRegex)) {
					startInfoBlock = true;
					blockEndRegex = "^" + line.replaceAll(blockRegex, "$1") + "\\S.*";
				}
			}

			if(startInfoBlock) {
				packageInfo.append(line);
				packageInfo.append("\n");
			} else {
				dumpSys.append(line);
				dumpSys.append("\n");
			}
		}
		packageInfo.append("\n");
		packageInfo.append(dumpSys.toString());

		dumpsysTextArea.setText(packageInfo.toString());
		dumpsysTextArea.setCaretPosition(0);
	}

	private String makeHyperEvent(JHtmlEditorPane panel, String id, String text, String title, Object userData) {
		String style = null;
		if(id != null && (id.startsWith("PLUGIN:") || id.contains("-perm-setting"))) style = "color:white";
		return panel.makeHyperLink("@event", text, title, id, style, userData);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if(!hasSysPack) return;

		JTabbedPane tabSource = (JTabbedPane) arg0.getSource();
		if(tabSource != null && tabSource.getSelectedComponent() != null &&
				tabSource.getSelectedComponent().equals(sysPackInfoPanel)) {
			txtApkPath.setText(hiddenApkPath);
		} else {
			txtApkPath.setText(apkPath);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actCmd = arg0.getActionCommand();

		if(ACT_CMD_OPEN_PACKAGE.equals(actCmd)) {
			Launcher.run(packageInfo.device.getSerialNumber(), txtApkPath.getText(), null);
		} else if(ACT_CMD_SAVE_PACKAGE.equals(actCmd)) {
			final String apkPath = txtApkPath.getText();
			if(apkPath == null) return;

			String saveFileName;
			if(apkPath.endsWith("base.apk")) {
				saveFileName = apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
			} else {
				saveFileName = apkPath.replaceAll(".*/", "");
			}

			final File destFile = ApkFileChooser.saveApkFile(this, saveFileName);
			if(destFile == null) return;


			new SwingWorker<String, Object> () {
				@Override
				protected String doInBackground() throws Exception {
					return PackageManager.pullApk(packageInfo.device, apkPath, destFile.getAbsolutePath());
				}

				@Override
				protected void done() {
					String errMessage = null;
					try {
						errMessage = get();
					} catch (InterruptedException | ExecutionException e) {
						errMessage = e.getMessage();
						e.printStackTrace();
					}
					if(errMessage == null) {
						int n = MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.QUESTION_SUCCESS_PULL_APK, destFile.getAbsolutePath());
						switch(n) {
						case 0: // explorer
							SystemUtil.openFileExplorer(destFile);
							break;
						case 1: // open
							Launcher.run(destFile.getAbsolutePath());
							break;
						default:
							break;
						}
					} else {
						MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.MSG_FAILURE_PULLED, errMessage);
					}
				};
			}.execute();
		} else if(ACT_CMD_LAUCH_PACKAGE.equals(actCmd)) {
			final boolean selectActivity = (arg0.getModifiers() & InputEvent.SHIFT_MASK) != 0;
			final IDevice device = packageInfo.device;

			if(!packageInfo.isEnabled()) {
				MessageBoxPool.show(this, MessageBoxPool.MSG_DISABLED_PACKAGE, device.getProperty(IDevice.PROP_DEVICE_MODEL));
				return;
			}

			Thread thread = new Thread(new Runnable() {
				private String errMsg = null;
				public void run()
				{
					String selectedActivity = null;
					ComponentInfo[] activities = null;
					int activityOpt = Resource.PROP_LAUNCH_ACTIVITY_OPTION.getInt();
					if(!selectActivity && (activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY
							|| activityOpt == Resource.INT_LAUNCH_ONLY_LAUNCHER_ACTIVITY)) {
						activities = packageInfo.getLauncherActivityList(false);
					}

					if(activities != null && activities.length == 1) {
						selectedActivity = activities[0].name;
					} else {
						activities = packageInfo.getLauncherActivityList(true);
						if(!selectActivity && activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY) {
							if(activities != null && activities.length == 1) {
								selectedActivity = activities[0].name;
							}
						}
						if(selectedActivity == null && activities != null && activities.length > 0) {
							String[] items = new String[activities.length];
							for(int i = 0; i < activities.length; i++) {
								boolean isLauncher = ((activities[i].featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
								boolean isMain = ((activities[i].featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
								items[i] = (isLauncher ? "[LAUNCHER]": (isMain ? "[MAIN]": "")) + " " + activities[i].name.replaceAll("^"+packageInfo.packageName, "");
							}
							String selected = (String)MessageBoxPane.showInputDialog(PackageInfoPanel.this, "Select Activity for " + device.getProperty(IDevice.PROP_DEVICE_MODEL),
									Resource.STR_BTN_LAUNCH.getString(), MessageBoxPane.QUESTION_MESSAGE, null, items, items[0]);
							if(selected == null) {
								return;
							}
							selectedActivity = selected.split(" ")[1];
						}
					}

					if(selectedActivity == null) {
						Log.w("No such activity of launcher or main");
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.MSG_NO_SUCH_LAUNCHER);
							}
						});
						return;
					}

					final String launcherActivity = packageInfo.packageName + "/" + selectedActivity;
					Log.i("launcherActivity : " + launcherActivity);

					String[] cmdResult = AdbDeviceHelper.launchActivity(device, launcherActivity);
					if(cmdResult == null || (cmdResult.length >= 2 && cmdResult[1].startsWith("Error")) ||
							(cmdResult.length >= 1 && cmdResult[0].startsWith("error"))) {
						Log.e("activity start faile : " + launcherActivity);

						if(cmdResult != null) {
							StringBuilder sb = new StringBuilder("cmd: adb shell start -n " + launcherActivity + "\n\n");
							for(String s : cmdResult) sb.append(s+"\n");
							errMsg = sb.toString();
							Log.e(errMsg);
						}

						EventQueue.invokeLater(new Runnable() {
							public void run() {
								MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.MSG_FAILURE_LAUNCH_APP, errMsg);
							}
						});
					} else if((boolean)Resource.PROP_TRY_UNLOCK_AF_LAUNCH.getData()) {
						AdbDeviceHelper.tryDismissKeyguard(device);
					}
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		} else if(ACT_CMD_UNINSTALL_PACKAGE.equals(actCmd)) {
			final IDevice device = packageInfo.device;

			String errMessage = null;
			if(!packageInfo.isSystemApp()) {
				errMessage = PackageManager.uninstallPackage(packageInfo);
			} else {
				int n = MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.QUESTION_REMOVE_SYSTEM_APK);
				if(n == MessageBoxPane.NO_OPTION) {
					return;
				}

				errMessage = PackageManager.removePackage(packageInfo);
				if(errMessage == null || errMessage.isEmpty()) {
					n = MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.QUESTION_REBOOT_SYSTEM);
					if(n == MessageBoxPane.YES_OPTION) {
						try {
							device.reboot(null);
						} catch (TimeoutException | IOException e) {
							e.printStackTrace();
						} catch (AdbCommandRejectedException e1) {
							Log.w(e1.getMessage());
						}
					}
				}
			}

			if(errMessage != null && !errMessage.isEmpty()) {
				final String errMsg = errMessage;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.MSG_FAILURE_UNINSTALLED, errMsg);
					}
				});
			} else {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if(hasSysPack) {
							int n = 2;
							if(dialog != null) {
								n = MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.QUESTION_PACK_INFO_REFRESH);
							}
							if(n == 0) {
								dialog.dispose();
							} else if(n == 2) {
								setPackageInfo(packageInfo);
							} else {
								for(Component c: toolBar.getComponents()) {
									if(c instanceof JButton) {
										((JButton)c).setEnabled(false);
									}
								}
								apkPath += " - [REMOVED]";
								txtApkPath.setText(apkPath);
							}
						} else {
							int n = 0;
							if(dialog != null) {
								n = MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.QUESTION_PACK_INFO_CLOSE);
							}
							if(n == 1) {
								dialog.dispose();
							} else {
								for(Component c: toolBar.getComponents()) {
									if(c instanceof JButton) {
										((JButton)c).setEnabled(false);
									}
								}
								apkPath += " - [REMOVED]";
								txtApkPath.setText(apkPath);
							}
						}
					}
				});
			}
		} else if(ACT_CMD_CLEAR_DATA.endsWith(actCmd)) {
			new SwingWorker<String, Object> () {
				@Override
				protected String doInBackground() throws Exception {
					return PackageManager.clearData(packageInfo);
				}

				@Override
				protected void done() {
					String errMessage = null;
					try {
						errMessage = get();
					} catch (InterruptedException | ExecutionException e) {
						errMessage = e.getMessage();
						e.printStackTrace();
					}
					if(errMessage == null) {
						MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.MSG_SUCCESS_CLEAR_DATA);
					} else {
						Log.e(errMessage);
						MessageBoxPool.show(PackageInfoPanel.this, MessageBoxPool.MSG_FAILURE_CLEAR_DATA, errMessage);
					}
				};
			}.execute();
		}
	}

	@Override
	public void hyperlinkClick(HyperlinkClickEvent evt) {
		String id = evt.getId();
		switch(id) {
		case "app-version":
			String versionDesc = (String) evt.getUserData();
			showPopupDialog(versionDesc, "App version info", new Dimension(300, 50), null);
			break;
		case "min-sdk-info": case "target-sdk-info": case "max-sdk-info":
			int sdkVersion = Integer.parseInt((String)evt.getUserData());
			SdkVersionInfoDlg sdkDlg = new SdkVersionInfoDlg(null, Resource.STR_SDK_INFO_FILE_PATH.getString(), sdkVersion);
			sdkDlg.setLocationRelativeTo(this);
			sdkDlg.setVisible(true);
			break;
		default:
			if(id.startsWith("feature-")) {
				showFeatureInfo(id, evt.getUserData());
			} else if(id.startsWith("PLUGIN:")) {
				IPlugIn plugin = PlugInManager.getPlugInByActionCommand((String)evt.getUserData());
				if(plugin != null) {
					plugin.launch();
				}
			} else {
				Log.w("Unknown id " + id);
			}
			break;
		}
	}

	private void showFeatureInfo(String id, Object userData) {
		String feature = null;
		Dimension size = new Dimension(400, 100);
		String infoBlock = (String) userData;
		StringBuilder sb;
		String temp;
		switch(id) {
		case "feature-hidden":
			feature = Resource.STR_FEATURE_HIDDEN_DESC.getString();
			break;
		case "feature-launcher":
			feature = Resource.STR_FEATURE_LAUNCHER_DESC.getString();
			break;
		case "feature-flags":
			sb = new StringBuilder();
			temp = packageInfo.getValue(infoBlock, "flags");
			if(temp != null && !temp.isEmpty()) {
				sb.append("flags: ");
				sb.append(temp);
			}
			temp = packageInfo.getValue(infoBlock, "privateFlags");
			if(temp != null && !temp.isEmpty()) {
				sb.append("\nprivateFlags: ");
				sb.append(temp);
			}
			temp = packageInfo.getValue(infoBlock, "pkgFlags");
			if(temp != null && !temp.isEmpty()) {
				sb.append("\npkgFlags: ");
				sb.append(temp);
			}
			feature = sb.toString();
			break;
		case "feature-timeStamp":
			sb = new StringBuilder();

			temp = packageInfo.getValue(infoBlock, "installerPackageName");
			if(temp == null || temp.isEmpty()) {
				temp = "N/A";
			}
			sb.append("Installer: ");
			sb.append(temp);
			sb.append("\n");
			temp = packageInfo.getValue(infoBlock, "timeStamp");
			if(temp != null && !temp.isEmpty()) {
				sb.append("\ntimeStamp: ");
				sb.append(temp);
			}
			temp = packageInfo.getValue(infoBlock, "firstInstallTime");
			if(temp != null && !temp.isEmpty()) {
				sb.append("\nfirstInstallTime: ");
				sb.append(temp);
			}
			temp = packageInfo.getValue(infoBlock, "lastUpdateTime");
			if(temp != null && !temp.isEmpty()) {
				sb.append("\nlastUpdateTime: ");
				sb.append(temp);
			}
			feature = sb.toString();
			break;
		case "feature-disabled-pack":
			sb = new StringBuilder();
			int state = -1;
			String value = packageInfo.getValue(infoBlock, "enabled");
			if(value != null && value.matches("\\d+")) {
				state = Integer.parseInt(value);
			}
			sb.append("EnableState: ");
			sb.append(PackageInfo.getEnabledStateToString(state));
			String caller = packageInfo.getValue(infoBlock, "lastDisabledCaller");
			if(caller != null) {
				sb.append("\n\nlastDisabledCaller: ");
				sb.append(caller);
			}
			feature = sb.toString();
			break;
		}

		showPopupDialog(feature, "Feature info", size, null);
	}

	private void showPopupDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}

	public void showDialog(Window owner) {
		dialog = new JDialog(owner);

		dialog.setTitle("Package Info");
		dialog.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(true);
		dialog.setModal(false);
		dialog.setLayout(new BorderLayout());

		Dimension minSize = new Dimension(500, 400);
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(dialog, minSize);
		} else {
			dialog.setSize(minSize);
		}
		//dialog.setMinimumSize(minSize);
		WindowSizeMemorizer.registeComponent(dialog);

		dialog.setLocationRelativeTo(owner);

		dialog.add(this, BorderLayout.CENTER);

		dialog.setVisible(true);
	}
}
