package com.apkscanner.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.ApkSignerWizard;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.SearchDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.AdbServerMonitor.IAdbDemonChangeListener;
import com.apkscanner.tool.adb.IPackageStateListener;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
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
	private ToolbarManagement toolbarManager = new ToolbarManagement();

	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;
	private MessageBoxPool messagePool;

	public MainUI(ApkScanner scanner)
	{
		messagePool = new MessageBoxPool(this);

		initialize();

		apkScanner = scanner;
		if(apkScanner != null) {
			apkScanner.setStatusListener(new ApkScannerListener());
		}
	}

	public void initialize()
	{
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
		String tabbedStyle = (String) Resource.PROP_TABBED_UI_THEME.getData();
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
        new Timer().schedule(new TimerTask() {
            @Override
            public void run()
            {
            	Log.e("isDispatchThread " + EventQueue.isDispatchThread());
                toolbarManager.setEnabled((boolean)Resource.PROP_ADB_DEVICE_MONITORING.getData());
            }
        }, 1000);
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
			toolbarManager.setApkInfo(null);

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
			toolbarManager.setApkInfo(null);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					setTitle(Resource.STR_APP_NAME.getString());
					tabbedPanel.setData(null);
					messagePool.show(MessageBoxPool.MSG_FAILURE_OPEN_APK);
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
		public void onProgress(final int step, final String message) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					switch(step) {
					case 0:
						tabbedPanel.setProgress(message);
						break;
					default:
						Log.i(message);
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

			switch(status) {
			case ACTIVITY_COMPLETED: case CERT_COMPLETED:
				toolbarManager.setApkInfo(apkScanner.getApkInfo());
			default: break;
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

				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
						apkScanner.openApk(apkFilePath);
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
			} else {
				Launcher.run(apkFilePath);
			}
		}

		private void evtOpenPackage(boolean newWindow)
		{
			PackageTreeDlg Dlg = new PackageTreeDlg(MainUI.this);
			if(Dlg.showTreeDlg() != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}

			final String device = Dlg.getSelectedDevice();
			final String apkFilePath = Dlg.getSelectedApkPath();
			final String frameworkRes = Dlg.getSelectedFrameworkRes();

			if(!newWindow) {
				tabbedPanel.setLodingLabel();
				tabbedPanel.setProgress(null);
				toolBar.setEnabledAt(ButtonSet.OPEN, false);
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);

				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
						apkScanner.openPackage(device, apkFilePath, frameworkRes);
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
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

			ApkInstallWizard wizard = new ApkInstallWizard(apkInfo.filePath, MainUI.this);			
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
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_CLASSES_DEX);
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
							MessageBoxPool.show(MainUI.this, MessageBoxPool.MSG_FAILURE_DEX2JAR, message);
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

			toolbarManager.setEnabled((boolean)Resource.PROP_ADB_DEVICE_MONITORING.getData());
		}

		private void evtLaunchApp(final boolean selectActivity)
		{
			final IDevice[] devices = getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
				return;
			}

			Thread thread = new Thread(new Runnable() {
				private String errMsg = null;
				public void run()
				{
					for(IDevice device: devices) {
						Log.v("launch activity on " + device.getSerialNumber());

						PackageInfo packageInfo = getPackageInfo(device);

						if(!packageInfo.isEnabled()) {
							messagePool.show(MessageBoxPool.MSG_DISABLED_PACKAGE, device.getProperty(IDevice.PROP_DEVICE_MODEL));
							continue;
						}

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
							if(selectedActivity == null) {
								ApkInfo apkInfo = apkScanner.getApkInfo();
								ComponentInfo[] apkActivities = ApkInfoHelper.getLauncherActivityList(apkInfo, true);

								int mergeLength = (activities != null ? activities.length : 0) + (apkActivities != null ? apkActivities.length : 0);
								ArrayList<String> mergeList = new ArrayList<String>(mergeLength);

								if(activities != null && activities.length > 0) {
									for(int i = 0; i < activities.length; i++) {
										boolean isLauncher = ((activities[i].featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
										boolean isMain = ((activities[i].featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
										mergeList.add((isLauncher ? "[LAUNCHER]": (isMain ? "[MAIN]": "")) + " " + activities[i].name.replaceAll("^"+packageInfo.packageName, ""));
									}
								}

								if(apkActivities != null && apkActivities.length > 0) {
									for(ComponentInfo comp: apkActivities) {
										boolean isLauncher = ((comp.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
										boolean isMain = ((comp.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
										mergeList.add((isLauncher ? "[APK_LAUNCHER]": (isMain ? "[APK_MAIN]": "[APK]")) + " " + comp.name.replaceAll("^"+apkInfo.manifest.packageName, ""));										
									}
								}

								if(!mergeList.isEmpty()) { 
									String selected = (String)MessageBoxPane.showInputDialog(MainUI.this, "Select Activity for " + device.getProperty(IDevice.PROP_DEVICE_MODEL),
											Resource.STR_BTN_LAUNCH.getString(), MessageBoxPane.QUESTION_MESSAGE, null, mergeList.toArray(new String[mergeList.size()]), mergeList.get(0));
									if(selected == null) {
										return;
									}
									selectedActivity = selected.split(" ")[1];
								}
							}
						}

						if(selectedActivity == null) {
							Log.w("No such activity of launcher or main");
							messagePool.show(MessageBoxPool.MSG_NO_SUCH_LAUNCHER);
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
									messagePool.show(MessageBoxPool.MSG_FAILURE_LAUNCH_APP, errMsg);
								}
							});
						} else if((boolean)Resource.PROP_TRY_UNLOCK_AF_LAUNCH.getData()) {
							AdbDeviceHelper.tryDismissKeyguard(device);
						}
					}
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}

		private void evtUninstallApp() {
			final IDevice[] devices = getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
				return;
			}

			Thread thread = new Thread(new Runnable() {
				public void run()
				{
					for(IDevice device: devices) {
						Log.v("uninstall apk on " + device.getSerialNumber());

						PackageInfo packageInfo = getPackageInfo(device);

						String errMessage = null;
						if(!packageInfo.isSystemApp()) {
							errMessage = PackageManager.uninstallPackage(packageInfo);
						} else {
							int n = messagePool.show(MessageBoxPool.QUESTION_REMOVE_SYSTEM_APK);
							if(n == MessageBoxPane.NO_OPTION) {
								return;
							}

							errMessage = PackageManager.removePackage(packageInfo);
							if(errMessage == null || errMessage.isEmpty()) {
								n = messagePool.show(MessageBoxPool.QUESTION_REBOOT_SYSTEM);
								if(n == MessageBoxPane.YES_OPTION) {
									try {
										device.reboot(null);
									} catch (TimeoutException | AdbCommandRejectedException | IOException e) {
										e.printStackTrace();
									}
								}
							}
						}

						if(errMessage != null && !errMessage.isEmpty()) {
							final String errMsg = errMessage;
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									messagePool.show(MessageBoxPool.MSG_FAILURE_UNINSTALLED, errMsg);
								}
							});
						} else {
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									messagePool.show(MessageBoxPool.MSG_SUCCESS_REMOVED);
								}
							});
						}
					}
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}

		private void evtSignApkFile() {
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null || apkInfo.filePath == null
					|| !new File(apkInfo.filePath).exists()) {
				Log.e("evtSignApkFile() apkInfo is null");
				return;
			}
			ApkSignerWizard wizard = new ApkSignerWizard(MainUI.this);
			wizard.setApk(apkInfo.filePath);
			wizard.setVisible(true);
		}

		private void evtShowInstalledPackageInfo()
		{
			final IDevice[] devices = getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
				return;
			}

			Thread thread = new Thread(new Runnable() {
				public void run()
				{
					for(IDevice device: devices) {
						final PackageInfo info = getPackageInfo(device);
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								PackageInfoPanel packageInfoPanel = new PackageInfoPanel();
								packageInfoPanel.setPackageInfo(info);
								packageInfoPanel.showDialog(MainUI.this);
							}
						});
					}
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}

		private IDevice[] getInstalledDevice() {
			IDevice[] devices = null;
			if(toolbarManager.isEnabled()) {
				devices = PackageManager.getInstalledDevices(apkScanner.getApkInfo().manifest.packageName);
			} else {
				AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				devices = adb.getDevices();
				Log.i("devices size : " + devices.length);

				ArrayList<IDevice> deviceList = new ArrayList<IDevice>();
				for(IDevice dev: devices) {
					PackageInfo info = getPackageInfo(dev);
					if(info != null) {
						deviceList.add(dev);
					}
				}
				devices = deviceList.toArray(new IDevice[deviceList.size()]);
			}
			return devices;
		}

		private PackageInfo getPackageInfo(IDevice device) {
			return PackageManager.getPackageInfo(device, apkScanner.getApkInfo().manifest.packageName);
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
				evtOpenApkFile((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
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
				evtOpenPackage((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
			} else if(ToolBar.MenuItemSet.INSTALLED_CHECK.matchActionEvent(e)) {
				evtShowInstalledPackageInfo();
			} else if(ToolBar.ButtonSet.OPEN_CODE.matchActionEvent(e)) {
				evtOpenJDGUI();
			} else if(ToolBar.ButtonSet.SEARCH.matchActionEvent(e)) {
				evtOpenSearchPopup();
			} else if(ToolBar.ButtonSet.LAUNCH.matchActionEvent(e) || ToolBar.ButtonSet.SUB_LAUNCH.matchActionEvent(e)) {
				evtLaunchApp((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
			} else if(ToolBar.MenuItemSet.UNINSTALL_APK.matchActionEvent(e)) {
				evtUninstallApp();
			} else if(ToolBar.ButtonSet.SIGN.matchActionEvent(e) || ToolBar.ButtonSet.SUB_SIGN.matchActionEvent(e)) { 
				evtSignApkFile();
			} else {
				Log.v("Unkown action : " + e);
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
					case KeyEvent.VK_T: evtShowInstalledPackageInfo();	break;
					case KeyEvent.VK_E: evtShowExplorer();		break;
					case KeyEvent.VK_M: evtShowManifest();		break;
					case KeyEvent.VK_R: evtLaunchApp(false);	break;
					//case KeyEvent.VK_S: evtSettings();			break;
					default: return false;
					}
					return true;
				} else if(e.getModifiers() == (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_O: evtOpenApkFile(true);	break;
					case KeyEvent.VK_P: evtOpenPackage(true);	break;
					case KeyEvent.VK_R: evtLaunchApp(true);	break;
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
		public void filesDropped(final File[] files)
		{
			Log.i("filesDropped()");
			toolBar.setEnabledAt(ButtonSet.OPEN, false);
			toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
			tabbedPanel.setLodingLabel();

			Thread thread = new Thread(new Runnable() {
				public void run()
				{
					try {
						apkScanner.clear(false);
						apkScanner.openApk(files[0].getCanonicalPath());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
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

	class ToolbarManagement implements IDeviceChangeListener, IAdbDemonChangeListener, IPackageStateListener
	{
		private boolean enabled;

		private String packageName;
		private int versionCode;
		private boolean hasSignature;
		private boolean hasMainActivity; 

		public void registerEventListeners() {
			AdbServerMonitor.addAdbDemonChangeListener(this);
			AndroidDebugBridge.addDeviceChangeListener(this);
			PackageManager.addPackageStateListener(this);
		}

		public void unregisterEventListeners() {
			AdbServerMonitor.removeAdbDemonChangeListener(this);
			AndroidDebugBridge.removeDeviceChangeListener(this);
			PackageManager.removePackageStateListener(this);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enable) {
			synchronized(this) {
				if(enabled != enable) {
					enabled = enable;
					if(enable) {
						AdbServerMonitor.startServerAndCreateBridgeAsync();
						registerEventListeners();
					} else {
						unregisterEventListeners();
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								toolBar.clearFlag();
							}
						});
					}
				}
			}
		}

		public void setApkInfo(ApkInfo info) {
			synchronized(this) {
				if(!enabled) {
					return;
				}
				if(info != null) {
					packageName = info.manifest.packageName;
					versionCode = info.manifest.versionCode != null ? info.manifest.versionCode : 0;
					hasSignature = ApkInfoHelper.isSigned(info);
					hasMainActivity = ApkInfoHelper.getLauncherActivityList(info, true).length > 0; 
				} else {
					packageName = null;
					versionCode = 0;
					hasSignature = false;
					hasMainActivity = false; 
				}
			}
			applyToobarPolicy();
		}

		private void applyToobarPolicy() {
			Log.v("applyToobarPolicy()");

			if(EventQueue.isDispatchThread()) {
				Log.v("applyToobarPolicy() This task is EDT. Invoke to Nomal thread");
				Thread thread = new Thread(new Runnable() {
					public void run() {
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
					Log.v("DeviceMonitor is not ready");
				}

				final boolean hasDevice = adb != null ? (adb.getDevices().length > 0) : false;

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
						if(device.isOnline()) {
							pkg = PackageManager.getPackageInfo(device, packageName);
						}
						if(pkg != null) {
							hasInstalled = pkg.getApkPath() != null;
							int packVerCode = pkg.getVersionCode();
							if(versionCode < packVerCode) {
								hasUpper = true;									
							} else if(versionCode > packVerCode) {
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

					final int flag = toolbarFlag;
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							Log.v("sendFlag " + flag);
							if(flag != ToolBar.FLAG_LAYOUT_UNSIGNED) {
								toolBar.unsetFlag(ToolBar.FLAG_LAYOUT_UNSIGNED);
							}
							toolBar.setFlag(flag);
						}
					});
				} else {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							toolBar.clearFlag();
							if(hasDevice) {
								toolBar.setFlag(ToolBar.FLAG_LAYOUT_DEVICE_CONNECTED);
							}
						}
					});
				}
			}
		}

		@Override
		public void deviceChanged(IDevice device, int changeMask) { 
			Log.v("deviceChanged() " + device.getSerialNumber() + ", " + device.getState() + ", changeMask " + changeMask);
			if((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
				applyToobarPolicy();
			}
		}

		@Override
		public void deviceConnected(IDevice device) {
			Log.v("deviceConnected() " + device.getSerialNumber() + ", " + device.getState());
			if(device.isOnline()) {
				applyToobarPolicy();
			} else {
				Log.v("device connected, but not online: " + device.getSerialNumber() + ", " + device.getState());
			}
		}

		@Override
		public void deviceDisconnected(IDevice device) {
			Log.v("deviceDisconnected() " + device.getSerialNumber());
			PackageManager.removeCash(device);
			applyToobarPolicy();
		}

		@Override
		public void adbDemonConnected(String adbPath, AdbVersion version) {
			Log.v("adbDemon Connected() " + adbPath + ", version " + version);
		}

		@Override
		public void adbDemonDisconnected() {
			Log.v("adbDemon Disconnected() ");
		}

		@Override
		public void packageInstalled(PackageInfo packageInfo) {
			if(packageName != null && packageInfo != null 
					&& packageName.equals(packageInfo.packageName)) {
				applyToobarPolicy();
			}
		}

		@Override
		public void packageUninstalled(PackageInfo packageInfo) {
			if(packageName != null && packageInfo != null 
					&& packageName.equals(packageInfo.packageName)) {
				applyToobarPolicy();
			}
		}

		@Override
		public void enableStateChanged(PackageInfo packageInfo) {

		}
	}
}
