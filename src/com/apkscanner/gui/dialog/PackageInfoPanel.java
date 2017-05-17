package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.ApkInstaller.ApkInstallerListener;
import com.apkscanner.core.signer.Signature;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.gui.messagebox.ComboMessageBox;
import com.apkscanner.gui.messagebox.JTextOptionPane;
import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickListener;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.SimpleOutputReceiver;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.FileUtil.FSStyle;

public class PackageInfoPanel extends JPanel implements ActionListener, HyperlinkClickListener, ChangeListener{

	private static final String ACT_CMD_OPEN_PACKAGE = "ACT_CMD_OPEN_PACKAGE";
	private static final String ACT_CMD_SAVE_PACKAGE = "ACT_CMD_SAVE_PACKAGE";
	private static final String ACT_CMD_LAUCH_PACKAGE = "ACT_CMD_LAUCH_PACKAGE";
	private static final String ACT_CMD_UNINSTALL_PACKAGE = "ACT_CMD_UNINSTALL_PACKAGE";
	
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

		add(toolBar, BorderLayout.NORTH);

		String tabbedStyle = (String) Resource.PROP_TABBED_UI_THEME.getData();
		tabbedPane = new JTabbedPane();
		TabbedPaneUIManager.setUI(tabbedPane, tabbedStyle);
		tabbedPane.addChangeListener(this);

		infoPanel = new JHtmlEditorPane();
		infoPanel.setEditable(false);
		infoPanel.setOpaque(true);
		infoPanel.setBackground(Color.white);
		infoPanel.setHyperlinkClickListener(this);

		sysPackInfoPanel = new JHtmlEditorPane();
		sysPackInfoPanel.setEditable(false);
		sysPackInfoPanel.setOpaque(true);
		sysPackInfoPanel.setBackground(Color.white);
		sysPackInfoPanel.setHyperlinkClickListener(this);

		//Font font = new Font("helvitica", Font.BOLD, 15);
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#basic-info, #perm-group {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#basic-info a {text-decoration:none; color:black;}");
		style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";}");
		style.append(".danger-perm {text-decoration:none; color:red;}");
		style.append("#about {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");
		style.append("#div-button { background-color: #e7e7e7; border: none; color: white; margin:1px; padding: 5px; text-align: center; text-decoration: none; display: inline-block;");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#div-button a {text-decoration:none; color:black;}");

		infoPanel.setStyle(style.toString());
		sysPackInfoPanel.setStyle(style.toString());

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
	
	
	private void alignTabbedPanel(boolean hasSysPackPanel) {
		tabbedPane.removeAll();
		tabbedPane.addTab(Resource.STR_TAB_PACAKGE_INFO.getString(), null, infoPanel, null);
		if(hasSysPackPanel) {
			tabbedPane.addTab(Resource.STR_TAB_SYS_PACAKGE_INFO.getString(), null, sysPackInfoPanel, null);
		}
		tabbedPane.addTab(Resource.STR_TAB_DUMPSYS.getString(), null, new JScrollPane(dumpsysTextArea), null);
		tabbedPane.addTab(Resource.STR_TAB_CERT.getString(), null, new JScrollPane(signatureTextArea), null);
	}

	public void setPackageInfo(PackageInfo info) {
		packageInfo = info;

		hasSysPack = info.getHiddenSystemPackageValue("pkg") != null;
		alignTabbedPanel(hasSysPack);

		info.getLauncherActivityList(true);

		txtApkPath.setText(info.getApkPath());

		infoPanel.setBody(getSummaryText(info, false));
		if(hasSysPack) {
			sysPackInfoPanel.setBody(getSummaryText(info, true));
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

	private String getSummaryText(PackageInfo info, boolean isSystemPackage)
	{
		//String appName = "App Name";
		String infoBlock = !isSystemPackage ? "Packages" : "Hidden system packages";
		String suffix = isSystemPackage ? "@system" : "@active";

		String apkPath = !isSystemPackage ? info.getApkPath() : info.getHiddenSystemPackageValue("codePath");

		long apkSize = 0;
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		try {
			info.device.executeShellCommand("ls -l " + apkPath, outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
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
		if(!isSystemPackage) {
			this.apkPath = apkPath;
		} else {
			this.hiddenApkPath = apkPath;
		}

		String versionName = info.getValue(infoBlock, "versionName");
		String versionCode = info.getValue(infoBlock, "versionCode");

		String temp = null;
		String sdkVersion = "";
		temp = info.getValue(infoBlock, "minSdk");
		if(temp != null) {
			sdkVersion += makeHyperLink("@event", temp +" (Min)", "Min SDK version", "minSdk" + suffix, null);
		}
		temp = info.getValue(infoBlock, "targetSdk");
		if(temp != null) {
			if(!sdkVersion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += makeHyperLink("@event", temp + " (Target)", "Targer SDK version", "targetSdk" + suffix, null);
		}
		temp = info.getValue(infoBlock, "maxSdk");
		if(temp != null) {
			if(!sdkVersion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += makeHyperLink("@event", temp + " (Max)", "Max SDK version", "maxSdk" + suffix, null);
		}
		if(sdkVersion.isEmpty()) {
			sdkVersion += "Unspecified";
		}

		//String feature = "LAUNCHER, FLAGS<br/>";

		StringBuilder feature = new StringBuilder();

		int state = -1;
		String value = info.getValue(infoBlock, "enabled");
		if(value != null && value.matches("\\d+")) {
			state = Integer.parseInt(value);			
		}
		if(state < 0 || state > 1) {
			feature.append("<font style=\"color:#ED7E31; font-weight:bold\">");
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_DISABLED_PACK_LAB.getString(), Resource.STR_FEATURE_DISABLED_PACK_DESC.getString(), "feature-disabled-pack" + suffix, null));
			feature.append("</font>, ");
		}

		if(isSystemPackage) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_SYS_PACK_LAB.getString(), Resource.STR_FEATURE_HIDDEN_SYS_PACK_DESC.getString(), "feature-hidden-pack" + suffix, null));
		} else if(info.hasLauncher()) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher" + suffix, null));
		} else {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden" + suffix, null));
		}
		feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_FLAG_LAB.getString(), Resource.STR_FEATURE_FLAG_DESC.getString(), "feature-flags" + suffix, null));

		String installer = info.getValue(infoBlock, "installerPackageName");
		if(installer == null || installer.isEmpty()) {
			installer = "N/A";
		}
		installer += ", " + info.getValue(infoBlock, "timeStamp");
		installer = makeHyperLink("@event", installer, "TimeStamp", "feature-timeStamp" + suffix, null);

		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td height=100>");
		strTabInfo.append("      <div id=\"basic-info\">");
		//strTabInfo.append("        <font style=\"font-size:20px; color:#548235; font-weight:bold\">");
		//strTabInfo.append("          " + appName);
		//strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <font style=\"font-size:15px; color:#4472C4\">");
		strTabInfo.append("          [" + info.packageName +"]");
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <font style=\"font-size:15px; color:#ED7E31\">");
		strTabInfo.append("          " + makeHyperLink("@event", "Ver. " + versionName +" / " + versionCode, "VersionName : " + versionName + "\n" + "VersionCode : " + versionCode, "app-version" + suffix, null));
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          @SDK Ver. " + sdkVersion + "<br/>");
		strTabInfo.append("          " + FileUtil.getFileSize(apkSize, FSStyle.FULL));
		strTabInfo.append("        </font>");
		strTabInfo.append("        <br/><br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          [" + Resource.STR_FEATURE_LAB.getString() + "] ");
		strTabInfo.append("          " + feature.toString() + "<br/>");
		strTabInfo.append("          [" + Resource.STR_FEATURE_INSTALLER_LAB.getString() + "] ");
		strTabInfo.append("          " + installer);
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("      </div>");
		strTabInfo.append("    </td>");
		strTabInfo.append("  </tr>");
		strTabInfo.append("</table>");
		strTabInfo.append("<div id=\"perm-group\" style=\"text-align:left; width:300px; padding-top:5px; border-top:1px; border-left:0px; border-right:0px; border-bottom:0px; border-style:solid;\">");
		strTabInfo.append("  <font style=\"font-size:12px;color:black;\">");
		/*
		if(allPermissionsList != null && !allPermissionsList.isEmpty()) {
			strTabInfo.append("    [" + Resource.STR_BASIC_PERMISSIONS.getString() + "] - ");
			strTabInfo.append("    " + makeHyperLink("@event","<u>" + Resource.STR_BASIC_PERMLAB_DISPLAY.getString() + "</u>",Resource.STR_BASIC_PERMDESC_DISPLAY.getString(),"display-list", null));
		} else {
			strTabInfo.append("    " + Resource.STR_LABEL_NO_PERMISSION.getString());
		}
		 */
		strTabInfo.append("  </font><br/>");
		strTabInfo.append("  <font style=\"font-size:5px\"><br/></font>");
		//strTabInfo.append("  " + permGorupImg);
		strTabInfo.append("</div>");
		strTabInfo.append("<div height=10000 width=10000></div>");

		return strTabInfo.toString();
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

	private String makeHyperLink(String href, String text, String title, String id, String style)
	{
		return JHtmlEditorPane.makeHyperLink(href, text, title, id, style);
	}

	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		JTextOptionPane.showTextDialog(null, content, title, JOptionPane.INFORMATION_MESSAGE, icon, size);
	}

	public void showFeatureInfo(String id)
	{
		String feature = null;

		String[] tmp = id.split("@");
		String infoBlock = id.indexOf("@system") > -1 ? "Hidden system packages" : "Packages";

		Dimension size = new Dimension(400, 100);

		if("feature-hidden".equals(tmp[0])) {
			feature = Resource.STR_FEATURE_HIDDEN_DESC.getString();
		} else if("feature-launcher".equals(tmp[0])) {
			feature = Resource.STR_FEATURE_LAUNCHER_DESC.getString();
		} else if("feature-flags".equals(tmp[0])) {
			StringBuilder sb = new StringBuilder();
			String temp = packageInfo.getValue(infoBlock, "flags"); 
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
		} else if("feature-timeStamp".equals(tmp[0])) {
			StringBuilder sb = new StringBuilder();

			String temp = packageInfo.getValue(infoBlock, "installerPackageName");
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
		} else if("feature-disabled-pack".equals(tmp[0])) {
			StringBuilder sb = new StringBuilder();
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
		}

		showDialog(feature, "Feature info", size, null);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if(!hasSysPack) return;

		JTabbedPane tabSource = (JTabbedPane) arg0.getSource();
		if(tabSource.getSelectedComponent().equals(sysPackInfoPanel)) {
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

			ApkInstaller apkInstaller = new ApkInstaller(packageInfo.device.getSerialNumber(), new ApkInstallerListener() {
				StringBuilder sb = new StringBuilder();
				@Override
				public void OnError(int cmdType, String device) {
					JTextOptionPane.showTextDialog(null, Resource.STR_MSG_FAILURE_PULLED.getString() + "\n\nConsol output", sb.toString(),  Resource.STR_LABEL_ERROR.getString(), JTextOptionPane.ERROR_MESSAGE,
							null, new Dimension(400, 100));
				}

				@Override
				public void OnSuccess(int cmdType, String device) {
					int n = ArrowTraversalPane.showOptionDialog(null,
							Resource.STR_MSG_SUCCESS_PULL_APK.getString() + "\n" + destFile.getAbsolutePath(),
							Resource.STR_LABEL_QUESTION.getString(),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.INFORMATION_MESSAGE,
							null,
							new String[] {Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OK.getString()},
							Resource.STR_BTN_OK.getString());
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
				}

				@Override public void OnCompleted(int cmdType, String device) { }
				@Override public void OnMessage(String msg) { sb.append(msg); }
			});		
			apkInstaller.pullApk(apkPath, destFile.getAbsolutePath());
		} else if(ACT_CMD_LAUCH_PACKAGE.equals(actCmd)) {
			final boolean selectActivity = (arg0.getModifiers() & InputEvent.SHIFT_MASK) != 0;
			final IDevice device = packageInfo.device;

			if(!packageInfo.isEnabled()) {
				ArrowTraversalPane.showOptionDialog(null,
						device.getName() + "\n : " + Resource.STR_MSG_DISABLED_PACKAGE.getString(),
						Resource.STR_LABEL_WARNING.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
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
								items[i] = (isLauncher ? "[LAUNCHER]": (isMain ? "[MAIN]": "")) + " " + activities[i].name.replaceAll(packageInfo.packageName, "");
							}
							String selected = ComboMessageBox.show(PackageInfoPanel.this, "Select Activity for " + device.getProperty(IDevice.PROP_DEVICE_MODEL), items,  Resource.STR_BTN_LAUNCH.getString(), JTextOptionPane.QUESTION_MESSAGE,
									null, new Dimension(400, 0));
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
								ArrowTraversalPane.showOptionDialog(null,
										Resource.STR_MSG_NO_SUCH_LAUNCHER.getString(),
										Resource.STR_LABEL_WARNING.getString(),
										JOptionPane.OK_OPTION, 
										JOptionPane.INFORMATION_MESSAGE,
										null,
										new String[] {Resource.STR_BTN_OK.getString()},
										Resource.STR_BTN_OK.getString());
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
								JTextOptionPane.showTextDialog(null, Resource.STR_MSG_FAILURE_LAUNCH_APP.getString() + "\n\nConsol output", errMsg,  Resource.STR_LABEL_ERROR.getString(), JTextOptionPane.ERROR_MESSAGE,
										null, new Dimension(500, 120));
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
				errMessage = PackageManager.removePackage(packageInfo);
				if(errMessage == null || errMessage.isEmpty()) {
					try {
						device.reboot(null);
					} catch (TimeoutException | AdbCommandRejectedException | IOException e) {
						e.printStackTrace();
					}
				}
			}

			if(errMessage != null && !errMessage.isEmpty()) {
				final String errMsg = errMessage;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						JTextOptionPane.showTextDialog(null, Resource.STR_MSG_FAILURE_UNINSTALLED.getString() + "\nConsol output:", errMsg,  Resource.STR_LABEL_ERROR.getString(), JTextOptionPane.ERROR_MESSAGE,
								null, new Dimension(300, 50));
					}
				});
			} else {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if(hasSysPack) {
							int n = ArrowTraversalPane.showOptionDialog(null,
									Resource.STR_QUESTION_PACK_INFO_REFRESH.getString(),
									Resource.STR_LABEL_QUESTION.getString(),
									JOptionPane.YES_NO_OPTION, 
									JOptionPane.QUESTION_MESSAGE,
									null,
									new String[] {Resource.STR_BTN_CLOSE.getString(), Resource.STR_BTN_NO.getString(), Resource.STR_BTN_YES.getString()},
									Resource.STR_BTN_YES.getString());
							if(n == 0) {
								//dispose();
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
							int n = ArrowTraversalPane.showOptionDialog(null,
									Resource.STR_QUESTION_PACK_INFO_CLOSE.getString(),
									Resource.STR_LABEL_QUESTION.getString(),
									JOptionPane.YES_NO_OPTION, 
									JOptionPane.QUESTION_MESSAGE,
									null,
									new String[] {Resource.STR_BTN_NO.getString(), Resource.STR_BTN_YES.getString()},
									Resource.STR_BTN_YES.getString());
							if(n == 1) {
								//dispose();
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
		}
	}

	@Override
	public void hyperlinkClick(String id) {
		if(id.startsWith("feature-")) {
			showFeatureInfo(id);
			return;
		}

		String[] tmp = id.split("@");
		String infoBlock = id.indexOf("@system") > -1 ? "Hidden system packages" : "Packages";

		if(tmp[0].endsWith("Sdk")){
			String sdkVer = packageInfo.getValue(infoBlock, tmp[0]);
			SdkVersionInfoDlg sdkDlg = new SdkVersionInfoDlg(null, Resource.STR_SDK_INFO_FILE_PATH.getString(), Integer.parseInt(sdkVer));
			sdkDlg.setLocationRelativeTo(this);
			sdkDlg.setVisible(true);
		} else if("app-version".equals(tmp[0])) {
			int versionCode = 0;
			String verCodeTmp = packageInfo.getValue(infoBlock, "versionCode");
			if(tmp != null && verCodeTmp.matches("\\d+")) {
				versionCode = Integer.parseInt(verCodeTmp);
			}
			String ver = "versionName : " + packageInfo.getValue(infoBlock, "versionName") + "\n" + "versionCode : " + versionCode;
			showDialog(ver, "App version info", new Dimension(300, 50), null);
		}
	}

}
