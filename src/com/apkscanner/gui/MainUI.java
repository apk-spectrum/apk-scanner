package com.apkscanner.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.SearchDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.gui.messagebox.ComboMessageBox;
import com.apkscanner.gui.messagebox.JTextOptionPane;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.AdbPackageManager;
import com.apkscanner.tool.adb.AdbPackageManager.PackageInfo;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.AdbServerMonitor.IAdbDemonChangeListener;
import com.apkscanner.tool.dex2jar.Dex2JarWrapper;
import com.apkscanner.tool.jd_gui.JDGuiLauncher;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

public class MainUI extends JFrame
{
	private static final long serialVersionUID = -623259597186280485L;

	private ApkScanner apkScanner;
	private DeviceMonitor deviceMonitor;

	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;

	public MainUI(ApkScanner scanner)
	{
		apkScanner = scanner;
		if(apkScanner != null) {
			apkScanner.setStatusListener(new ApkScannerListener());
		}
		deviceMonitor = new DeviceMonitor();
	}

	public void initialize()
	{
		if(!EventQueue.isDispatchThread()) {
			Log.i("createAndShowGUI() - This task is not EDT. Invoke to EDT.");
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						initialize();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}

		Log.i("UI Init start");

		Log.i("initialize() setLookAndFeel");
		try {
			UIManager.setLookAndFeel((String)Resource.PROP_CURRENT_THEME.getData());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		Log.i("initialize() setUIFont");
		String propFont = (String) Resource.PROP_BASE_FONT.getData();
		int propFontStyle = (int)Resource.PROP_BASE_FONT_STYLE.getInt();
		int propFontSize = (int) Resource.PROP_BASE_FONT_SIZE.getInt();
		setUIFont(new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));

		Log.i("initialize() set title & icon");
		setTitle(Resource.STR_APP_NAME.getString());
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());

		Log.i("initialize() set bound & size");

		int width = Resource.INT_WINDOW_SIZE_WIDTH_MIN;
		int height = Resource.INT_WINDOW_SIZE_HEIGHT_MIN;
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			width = Resource.PROP_WINDOW_WIDTH.getInt();
			height = Resource.PROP_WINDOW_HEIGHT.getInt();
		}
		
		setBounds(0, 0, width, height);
		setMinimumSize(new Dimension(Resource.INT_WINDOW_SIZE_WIDTH_MIN, Resource.INT_WINDOW_SIZE_HEIGHT_MIN));
		setResizable(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		UIEventHandler eventHandler = new UIEventHandler();

		Log.i("initialize() toolbar init");
		// ToolBar initialize and add
		toolBar = new ToolBar(eventHandler);
		toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
		toolBar.setEnabledAt(ButtonSet.NEED_DEVICE, false);
		add(toolBar, BorderLayout.NORTH);

		Log.i("initialize() tabbedpanel init");
		// TabPanel initialize and add
		String tabbedStyle = (String) Resource.PROP_TABBED_UI_THEME.getData(com.apkscanner.gui.theme.tabbedpane.PlasticTabbedPaneUI.class.getName());
		tabbedPanel = new TabbedPanel(tabbedStyle);
		add(tabbedPanel, BorderLayout.CENTER);

		Log.i("initialize() register event handler");
		// Closing event of window be delete tempFile
		addWindowListener(eventHandler);

		// Drag & Drop event processing
		new FileDrop(this, /*dragBorder,*/ eventHandler); // end FileDrop.Listener

		// Shortcut key event processing
		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(eventHandler);

		Log.i("UI Init end");

		deviceMonitor.start();
	}

	private static void setUIFont(javax.swing.plaf.FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				if(!"InternalFrame.titleFont".equals(key)) {
					UIManager.put(key, f);
				}
			}
		}
	}

	private class ApkScannerListener implements ApkScanner.StatusListener
	{
		@Override
		public void onStart(final long estimatedTime) {
			Log.i("onStart()");
			deviceMonitor.setApkInfo(null);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					tabbedPanel.setLodingLabel();
				}
			});
		}

		@Override
		public void onSuccess() {
			Log.v("ApkCore.onSuccess()");
		}

		@Override
		public void onError(int error) {
			Log.e("ApkCore.onError() " + error);
			deviceMonitor.setApkInfo(null);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					setTitle(Resource.STR_APP_NAME.getString());
					tabbedPanel.setData(null);

					final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
					//JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
					JOptionPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_OPEN_APK.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, Appicon,
							new String[] {Resource.STR_BTN_CLOSE.getString()}, Resource.STR_BTN_CLOSE.getString());
				}
			});
		}

		@Override
		public void onCompleted() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					Log.v("ApkCore.onComplete()");
					toolBar.setEnabledAt(ButtonSet.OPEN, true);
				}
			});
		}

		@Override
		public void onProgress(final int step, final String msg) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					switch(step) {
					case 0:
						tabbedPanel.setProgress(Integer.valueOf(msg));
						break;
					default:
						Log.i(msg);
					}
				}
			});
		}

		@Override
		public void onStateChanged(final Status status)
		{
			//Log.v("onStateChanged() "+ status);
			if(status == Status.STANBY) {
				Log.v("STANBY: does not UI update");
				return;
			}

			if(!EventQueue.isDispatchThread()) {
				Log.v("onStateChanged() This task is not EDT. Invoke to EDT for " + status);
				switch(status) {
				case ACTIVITY_COMPLETED: case CERT_COMPLETED:
					deviceMonitor.setApkInfo(apkScanner.getApkInfo());
				default: break;
				}
				try {
					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							onStateChanged(status);
						}
					});
					return;
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}
			}

			Log.i("onStateChanged() ui sync start for " + status);
			switch(status) {
			case BASIC_INFO_COMPLETED:
				String apkFilePath = apkScanner.getApkInfo().filePath;
				String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + Resource.STR_APP_NAME.getString();
				setTitle(title);

				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);
				tabbedPanel.setData(apkScanner.getApkInfo(), 0);
				break;
			case WIDGET_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 1);
				break;
			case LIB_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 2);
				break;
			case RESOURCE_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 3);
				break;
			case RES_DUMP_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 3 + TabbedPanel.CMD_EXTRA_DATA);
				break;
			case ACTIVITY_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 4);
				break;
			case CERT_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 0 + TabbedPanel.CMD_EXTRA_DATA);
				tabbedPanel.setData(apkScanner.getApkInfo(), 5);
				break;
			default:
				break;
			}
			Log.i("onStateChanged() ui sync end " + status);
		}
	}

	class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener, FileDrop.Listener
	{
		private void evtOpenApkFile(boolean newWindow)
		{
			final String apkFilePath = ApkFileChooser.openApkFilePath(MainUI.this);
			if(apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}
			if(!newWindow) {
				toolBar.setEnabledAt(ButtonSet.OPEN, false);
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
				tabbedPanel.setLodingLabel();

				new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
						apkScanner.openApk(apkFilePath);
					}
				}).start();
			} else {
				Launcher.run(apkFilePath);
			}
		}

		private void evtOpenPackage(boolean newWindow)
		{
			PackageTreeDlg Dlg = new PackageTreeDlg();
			if(Dlg.showTreeDlg(MainUI.this) != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}

			final String device = Dlg.getSelectedDevice();
			final String apkFilePath = Dlg.getSelectedApkPath();
			final String frameworkRes = Dlg.getSelectedFrameworkRes();

			if(!newWindow) {
				tabbedPanel.setLodingLabel();
				tabbedPanel.setProgress(-1);
				toolBar.setEnabledAt(ButtonSet.OPEN, false);
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);

				new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
						apkScanner.openPackage(device, apkFilePath, frameworkRes);
					}
				}).start();
			} else {
				Launcher.run(device, apkFilePath, frameworkRes);
			}
		}

		private void evtInstallApk(boolean checkPackage)
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				return;
			}

			toolBar.setEnabledAt(ButtonSet.INSTALL, false);

			ApkInstallWizard wizard = new ApkInstallWizard(MainUI.this);
			wizard.setApk(apkInfo);
			wizard.start();

			toolBar.setEnabledAt(ButtonSet.INSTALL, true);
		}

		private void evtShowManifest()
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowManifest() apkInfo is null");
				return;
			}

			try {
				String manifestPath = apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml";
				File manifestFile = new File(manifestPath); 
				if(!manifestFile.exists()) {
					if(!manifestFile.getParentFile().exists()) {
						if(FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
							Log.d("sucess make folder");
						}
					}

					String[] convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] {"AndroidManifest.xml"});
					AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
					a2x.setMultiLinePrint(true);

					FileWriter fw = new FileWriter(new File(manifestPath));
					fw.write(a2x.toString());
					fw.close();
				}

				SystemUtil.openEditor(manifestPath);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		private void evtShowExplorer()
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				return;
			}

			SystemUtil.openArchiveExplorer(apkInfo.filePath);
		}

		private void evtOpenJDGUI()
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null || apkInfo.filePath == null
					|| !new File(apkInfo.filePath).exists()) {
				Log.e("evtOpenJDGUI() apkInfo is null");
				return;
			}

			if(!ZipFileUtil.exists(apkInfo.filePath, "classes.dex")) {
				Log.e("No such file : classes.dex");
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_NO_SUCH_CLASSES_DEX.getString(),
						Resource.STR_LABEL_WARNING.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.WARNING_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				return;
			}

			toolBar.setEnabledAt(ButtonSet.OPEN_CODE, false);

			String jarfileName = apkInfo.tempWorkPath + File.separator + (new File(apkInfo.filePath)).getName().replaceAll("\\.apk$", ".jar");
			Dex2JarWrapper.convert(apkInfo.filePath, jarfileName, new Dex2JarWrapper.DexWrapperListener() {
				@Override
				public void onCompleted() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							toolBar.setEnabledAt(ButtonSet.OPEN_CODE, true);
						}
					});
				}

				@Override
				public void onError(final String message) {
					Log.e("Failure: Fail Dex2Jar : " + message);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							JTextOptionPane.showTextDialog(null, Resource.STR_MSG_FAILURE_DEX2JAR.getString() + "\n\nerror message", message,  Resource.STR_LABEL_ERROR.getString(), JTextOptionPane.ERROR_MESSAGE,
									null, new Dimension(300, 120));
						}
					});
				}

				@Override
				public void onSuccess(String jarFilePath) {
					JDGuiLauncher.run(jarFilePath);
				}
			});
		}

		private void evtOpenSearchPopup() {
			SearchDlg dialog = new SearchDlg();
			dialog.setApkInfo(apkScanner.getApkInfo());

			dialog.setModal(false);
			dialog.setVisible(true);

			Log.d(dialog.sName);

			// (?i) <- "찾을 문자열"에 대소문자 구분을 없애고
			// .*   <- 문자열이 행의 어디에 있든지 찾을 수 있게

			//String findStr = "(?i).*" + dialog.sName + ".*";
		}

		private void evtSettings()
		{
			SettingDlg dlg = new SettingDlg(MainUI.this);
			dlg.setVisible(true);
			
			//changed theme
			if(dlg.isNeedRestart()) {
				restart();
			}

			String lang = (String)Resource.PROP_LANGUAGE.getData();
			if(lang != null && Resource.getLanguage() != null
					&& !Resource.getLanguage().equals(lang)) {
				setLanguage(lang);
			}
		}

		private void evtLaunchApp()
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtLaunchApp() apkInfo is null");
				return;
			}

			String selectedActivity = null;
			ComponentInfo[] activities = ApkInfoHelper.getLauncherActivityList(apkInfo, false);
			if(activities != null && activities.length == 1) {
				selectedActivity = activities[0].name;
			} else {
				activities = ApkInfoHelper.getLauncherActivityList(apkInfo, true);
				if(activities != null) {
					String[] items = new String[activities.length];
					for(int i = 0; i < activities.length; i++) {
						boolean isLauncher = ((activities[i].featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
						items[i] = (isLauncher ? "[LAUNCHER]":"[MAIN]") + " " + activities[i].name.replaceAll(apkInfo.manifest.packageName, "");
					}
					String selected = ComboMessageBox.show(MainUI.this, "Select Activity", items,  Resource.STR_BTN_LAUNCH.getString(), JTextOptionPane.QUESTION_MESSAGE,
							null, new Dimension(400, 0));
					if(selected == null) {
						return;
					}
					selectedActivity = selected.split(" ")[1];
				}
			}

			if(selectedActivity == null) {
				Log.w("No such activity of launcher or main");
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_NO_SUCH_LAUNCHER.getString(),
						Resource.STR_LABEL_WARNING.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				return;
			}

			final String launcherActivity = apkInfo.manifest.packageName + "/" + selectedActivity;
			Log.i("launcherActivity : " + launcherActivity);

			final IDevice[] devices = deviceMonitor.getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_NO_SUCH_PACKAGE_DEVICE.getString(),
						Resource.STR_LABEL_INFO.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				return;
			}

			new Thread(new Runnable() {
				private String errMsg = null;
				public void run()
				{
					for(IDevice device: devices) {
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
						} else {
							AdbDeviceHelper.tryDismissKeyguard(device);
						}
					}
				}
			}).start();

		}

		private void setLanguage(String lang)
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			Resource.setLanguage(lang);
			String title = Resource.STR_APP_NAME.getString();
			if(apkInfo != null) {
				title += " - " + apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)+1);
			}
			setTitle(title);
			toolBar.reloadResource();
			tabbedPanel.reloadResource();
		}

		private void restart() {
			if(apkScanner.getApkInfo() != null) {
				Launcher.run(apkScanner.getApkInfo().filePath);
			} else {
				Launcher.run();
			}
			dispose();
		}

		// ToolBar event processing
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (ToolBar.ButtonSet.OPEN.matchActionEvent(e) || ToolBar.MenuItemSet.OPEN_APK.matchActionEvent(e)) {
				evtOpenApkFile(false);
			} else if(ToolBar.ButtonSet.MANIFEST.matchActionEvent(e)) {
				evtShowManifest();
			} else if(ToolBar.ButtonSet.EXPLORER.matchActionEvent(e)) {
				evtShowExplorer();
			} else if(ToolBar.ButtonSet.INSTALL.matchActionEvent(e)
					|| ToolBar.ButtonSet.INSTALL_UPDATE.matchActionEvent(e)
					|| ToolBar.ButtonSet.INSTALL_DOWNGRADE.matchActionEvent(e) 
					|| ToolBar.ButtonSet.SUB_INSTALL.matchActionEvent(e) 
					|| ToolBar.ButtonSet.SUB_INSTALL_UPDATE.matchActionEvent(e)
					|| ToolBar.ButtonSet.SUB_INSTALL_DOWNGRADE.matchActionEvent(e)
					|| ToolBar.MenuItemSet.INSTALL_APK.matchActionEvent(e) ) {
				evtInstallApk(false);
			} else if(ToolBar.ButtonSet.SETTING.matchActionEvent(e)) {
				evtSettings();
			} else if(ToolBar.ButtonSet.ABOUT.matchActionEvent(e)) {
				AboutDlg.showAboutDialog(MainUI.this);
			} else if(ToolBar.MenuItemSet.NEW_EMPTY.matchActionEvent(e)) {
				Launcher.run();
			} else if(ToolBar.MenuItemSet.NEW_APK.matchActionEvent(e)) {
				evtOpenApkFile(true);
			} else if(ToolBar.MenuItemSet.NEW_PACKAGE.matchActionEvent(e)) {
				evtOpenPackage(true);
			} else if(ToolBar.ButtonSet.OPEN_PACKAGE.matchActionEvent(e) || ToolBar.MenuItemSet.OPEN_PACKAGE.matchActionEvent(e)) {
				evtOpenPackage(false);
			} else if(ToolBar.MenuItemSet.INSTALLED_CHECK.matchActionEvent(e)) {
				evtInstallApk(true);
			} else if(ToolBar.ButtonSet.OPEN_CODE.matchActionEvent(e)) {
				evtOpenJDGUI();
			} else if(ToolBar.ButtonSet.SEARCH.matchActionEvent(e)) {
				evtOpenSearchPopup();
			} else if(ToolBar.ButtonSet.LAUNCH.matchActionEvent(e) || ToolBar.ButtonSet.SUB_LAUNCH.matchActionEvent(e)) {
				evtLaunchApp();
			} else {
				Log.w("Unkown action : " + e);
			}
		}

		// Shortcut key event processing
		@Override
		public boolean dispatchKeyEvent(KeyEvent e)
		{
			if(!isFocused()) return false;
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				if(e.getModifiers() == KeyEvent.CTRL_MASK) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_O: evtOpenApkFile(false);	break;
					case KeyEvent.VK_P: evtOpenPackage(false);	break;
					case KeyEvent.VK_N: Launcher.run();			break;
					case KeyEvent.VK_I: evtInstallApk(false);	break;
					case KeyEvent.VK_T: evtInstallApk(true);	break;
					case KeyEvent.VK_E: evtShowExplorer();		break;
					case KeyEvent.VK_M: evtShowManifest();		break;
					case KeyEvent.VK_R: evtLaunchApp();			break;
					//case KeyEvent.VK_S: evtSettings();			break;
					default: return false;
					}
					return true;
				} else if(e.getModifiers() == (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_O: evtOpenApkFile(true);	break;
					case KeyEvent.VK_P: evtOpenPackage(true);	break;
					default: return false;
					}
					return true;
				} else if(e.getModifiers() == 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_F1 : AboutDlg.showAboutDialog(MainUI.this);break;
					case KeyEvent.VK_F12: LogDlg.showLogDialog(MainUI.this);	break;
					default: return false;
					}
					return true;
				}
			}
			return false;
		}

		// Drag & Drop event processing
		@Override
		public void filesDropped(File[] files)
		{
			Log.i("filesDropped()");
			try {
				toolBar.setEnabledAt(ButtonSet.OPEN, false);
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
				tabbedPanel.setLodingLabel();

				apkScanner.clear(false);
				apkScanner.openApk(files[0].getCanonicalPath());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		private void finished()
		{
			Log.v("finished()");

			if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData(false)) {
				int width = (int)getSize().getWidth();
				int height = (int)getSize().getHeight();
				if(Resource.PROP_WINDOW_WIDTH.getInt() != width
						|| Resource.PROP_WINDOW_HEIGHT.getInt() != (int)getSize().getHeight()) {
					Resource.PROP_WINDOW_WIDTH.setData(width);
					Resource.PROP_WINDOW_HEIGHT.setData(height);
				}
			}

			setVisible(false);
			apkScanner.clear(true);

			System.exit(0);
		}

		// Closing event of window be delete tempFile
		@Override public void windowClosing(WindowEvent e) { finished(); }
		@Override public void windowClosed(WindowEvent e) { finished(); }

		@Override public void windowOpened(WindowEvent e) { }
		@Override public void windowIconified(WindowEvent e) { }
		@Override public void windowDeiconified(WindowEvent e) { }
		@Override public void windowActivated(WindowEvent e) { }
		@Override public void windowDeactivated(WindowEvent e) { }
	}

	class DeviceMonitor implements IDeviceChangeListener, IAdbDemonChangeListener
	{
		private AdbServerMonitor demonMonitor = null;
		private String packageName = null;
		private int versionCode = 0;
		private boolean hasSignature = false;
		private boolean hasMainActivity = false; 

		private HashMap<IDevice, PackageInfo> devices = new HashMap<IDevice, PackageInfo>(); 

		public void start() {
			Log.v("DeviceMonitor.start() s");
			if(demonMonitor != null) {
				Log.v("aleady started");
				return;
			}
			demonMonitor = AdbServerMonitor.startServerAndCreateBridge(Resource.BIN_ADB.getPath(), false, true);
			AdbServerMonitor.addAdbDemonChangeListener(this);

			AndroidDebugBridge.addDeviceChangeListener(this);
			Log.v("DeviceMonitor.start() e");			
		}

		public void stop() {
			if(demonMonitor != null) {
				demonMonitor.stop();
				demonMonitor = null;
			}
			AdbServerMonitor.removeAdbDemonChangeListener(this);

			AndroidDebugBridge.removeDeviceChangeListener(this);
		}

		public IDevice[] getInstalledDevice() {
			synchronized (devices) {
				return devices.keySet().toArray(new IDevice[devices.size()]);
			}
		}



		public void setApkInfo(ApkInfo info) {
			synchronized(this) {
				if(info != null) {
					packageName = info.manifest.packageName;
					versionCode = info.manifest.versionCode;
					hasSignature = (info.certificates != null && info.certificates.length > 0);
					hasMainActivity = ApkInfoHelper.getLauncherActivityList(info, true).length > 0; 
				} else {
					packageName = null;
					versionCode = 0;
					hasSignature = false;
					hasMainActivity = false; 
				}
				synchronized (devices) {
					devices.clear();
				}
			}
			applyToobarPolicy();
		}

		private void applyToobarPolicy() {
			Log.v("applyToobarPolicy()");

			if(EventQueue.isDispatchThread()) {
				Log.w("applyToobarPolicy() This task is EDT. Invoke to Nomal thread");
				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						applyToobarPolicy();
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
				return;
			}

			synchronized(this) {
				AndroidDebugBridge adb = AndroidDebugBridge.getBridge();
				if (adb == null) {
					Log.i("DeviceMonitor is not ready");
					return;
				}
				final boolean hasDevice = (adb.getDevices().length > 0);

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if(hasDevice) {
							toolBar.setFlag(ToolBar.FLAG_LAYOUT_DEVICE_CONNECTED);
							toolBar.setEnabledAt(ButtonSet.NEED_DEVICE, true);
						} else {
							toolBar.clearFlag();
							toolBar.setEnabledAt(ButtonSet.NEED_DEVICE, false);
						}
					}
				});

				if(hasDevice && packageName != null) {
					boolean hasInstalled = false;
					boolean hasLower = false;
					boolean hasUpper = false;
					for(IDevice device: adb.getDevices()) {
						PackageInfo pkg = null;
						synchronized(devices) {
							if(devices.containsKey(device)) {
								pkg = devices.get(device);
							} else {
								pkg = AdbPackageManager.getPackageInfo(device.getSerialNumber(), packageName);
								if(pkg != null) {
									devices.put(device, pkg);
								}
								try {
									device.getApiLevel(); // dummy
								} catch (Exception e) { }
							}
						}
						if(pkg != null) {
							hasInstalled = true;
							if(versionCode < pkg.versionCode) {
								hasUpper = true;									
							} else if(versionCode > pkg.versionCode) {
								hasLower = true;
							}
						}
					}

					int toolbarFlag = ToolBar.FLAG_LAYOUT_NONE;
					if(!hasSignature && !hasInstalled) {
						toolbarFlag = ToolBar.FLAG_LAYOUT_UNSIGNED;
					}
					if(hasInstalled) {
						if(hasLower) {
							toolbarFlag = ToolBar.FLAG_LAYOUT_INSTALLED_LOWER;
						} else if(hasUpper) {
							toolbarFlag = ToolBar.FLAG_LAYOUT_INSTALLED_UPPER;
						} else if(hasMainActivity) {
							toolbarFlag = ToolBar.FLAG_LAYOUT_LAUNCHER;
						} else {
							toolbarFlag = ToolBar.FLAG_LAYOUT_INSTALLED;
						}
					}

					sendFlag(toolbarFlag);
				} else {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							toolBar.clearFlag();
						}
					});
				}


			}
		}

		private void sendFlag(final int flag) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					Log.v("sendFlag " + flag);
					if(flag != ToolBar.FLAG_LAYOUT_UNSIGNED) {
						toolBar.unsetFlag(ToolBar.FLAG_LAYOUT_UNSIGNED);	
					}
					toolBar.setFlag(flag);
				}
			});
			return;
		}

		@Override
		public void deviceChanged(IDevice device, int changeMask) { }

		@Override
		public void deviceConnected(IDevice device) {
			applyToobarPolicy();
		}

		@Override
		public void deviceDisconnected(IDevice device) {
			synchronized(devices) {
				if(devices.containsKey(device.getSerialNumber())) {
					devices.remove(device.getSerialNumber());
				}
			}
			applyToobarPolicy();
		}

		@Override
		public void adbDemonConnected(String adbPath, AdbVersion version) {
			Log.e("adbDemon Connected() " + adbPath + ", version " + version);
		}

		@Override
		public void adbDemonDisconnected() {
			Log.e("adbDemon Disconnected() ");
		}
	}
}
