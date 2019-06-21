package com.apkscanner.gui.easymode.core;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.UIController;
import com.apkscanner.gui.component.ApkFileChooser;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkSignerWizard;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.PermissionHistoryPanel;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.easymode.dlg.EasyToolbarCertDlg;
import com.apkscanner.gui.installer.ApkInstallWizard;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.external.BytecodeViewerLauncher;
import com.apkscanner.tool.external.Dex2JarWrapper;
import com.apkscanner.tool.external.JADXLauncher;
import com.apkscanner.tool.external.JDGuiLauncher;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

public class ToolEntryManager {

	static public String TOOL_SHOW_SIGN_DLG = "tool_show_sign_dlg";
	
	static ArrayList<ToolEntry> ShowEntry = new ArrayList<ToolEntry>();
	static ArrayList<ToolEntry> hideEntry = new ArrayList<ToolEntry>();
	static public ApkScanner apkScanner;
	static public JFrame mainframe = null;

	static ArrayList<ToolEntry> allEntry;

	static ArrayList<ToolEntry> deviceEntry;

	
	public static MessageBoxPool messagePool;
	
	public ToolEntryManager() {
		
	}

	public static void initToolEntryManager() {
		Log.i("start toolentryManager");
		allEntry = new ArrayList<ToolEntry>(Arrays.asList(
//				new ToolEntry(RStr.APP_NAME.get(), "Excute Original Scanner",
//						RImg.APP_ICON.getImageIcon(100, 100)),
				new ToolEntry(RStr.BTN_OPEN.get(), RStr.BTN_OPEN_LAB.get(),
						RImg.TOOLBAR_OPEN.getImageIcon()),
				new ToolEntry(RStr.BTN_OPEN_PACKAGE.get(), RStr.BTN_OPEN_PACKAGE_LAB.get(),
						RImg.TOOLBAR_PACKAGETREE.getImageIcon()),
				new ToolEntry(RStr.BTN_MANIFEST.get(), RStr.BTN_MANIFEST_LAB.get(),
						RImg.TOOLBAR_MANIFEST.getImageIcon()),
				new ToolEntry(RStr.BTN_EXPLORER.get(), RStr.BTN_EXPLORER_LAB.get(),
						RImg.TOOLBAR_EXPLORER.getImageIcon()),
				new ToolEntry(RStr.BTN_INSTALL.get(), RStr.BTN_INSTALL_LAB.get(),
						RImg.TOOLBAR_INSTALL.getImageIcon()),
				new ToolEntry(RStr.BTN_SIGN.get(), RStr.BTN_SIGN_LAB.get(),
						RImg.TOOLBAR_SIGNNING.getImageIcon()),
//				new ToolEntry(RStr.BTN_LAUNCH.get(), RStr.BTN_LAUNCH_LAB.get(),
//						RImg.TOOLBAR_LAUNCH.getImageIcon()),
//				new ToolEntry(RStr.BTN_DEL.get(), RStr.BTN_DEL.get(),
//						RImg.TOOLBAR_UNINSTALL.getImageIcon()),
//				new ToolEntry(RStr.MENU_CLEAR_DATA.get(), RStr.MENU_CLEAR_DATA.get(),
//						RImg.TOOLBAR_CLEAR.getImageIcon()),
//				new ToolEntry(RStr.BTN_DETAILS_INFO.get(), RStr.BTN_DETAILS_INFO.get(),
//						RImg.TOOLBAR_SEARCH.getImageIcon()),
				new ToolEntry(RStr.BTN_OPENCODE.get(), RStr.BTN_OPENING_CODE_LAB.get(),
						RImg.TOOLBAR_OPENCODE.getImageIcon()),
				new ToolEntry(RStr.BTN_SETTING.get(), RStr.BTN_SETTING_LAB.get(),
						RImg.TOOLBAR_SETTING.getImageIcon()),
				new ToolEntry(RStr.BTN_ABOUT.get(), RStr.BTN_ABOUT_LAB.get(),
						RImg.TOOLBAR_ABOUT.getImageIcon())));
		Log.i("start deviceEntry toolEntry");
		deviceEntry = new ArrayList<ToolEntry>(Arrays.asList(
//				new ToolEntry(RStr.APP_NAME.get(), "Excute Original Scanner",
//				RImg.APP_ICON.getImageIcon(100, 100)),
		new ToolEntry(RStr.BTN_LAUNCH.get(), RStr.BTN_LAUNCH_LAB.get(),
				RImg.TOOLBAR_LAUNCH.getImageIcon()),
		new ToolEntry(RStr.BTN_DEL.get(), RStr.BTN_DEL.get(),
				RImg.TOOLBAR_UNINSTALL.getImageIcon()),
		new ToolEntry(RStr.MENU_CLEAR_DATA.get(), RStr.MENU_CLEAR_DATA.get(),
				RImg.TOOLBAR_CLEAR.getImageIcon()),
		new ToolEntry(RStr.BTN_DETAILS_INFO.get(), RStr.BTN_DETAILS_INFO.get(),
				RImg.TOOLBAR_SEARCH.getImageIcon())));
		
		Log.i("start MessageBoxPool");
		messagePool = new MessageBoxPool(mainframe);
		
		Log.i("start refreshToolManager");
		refreshToolManager();
	}

	public static ArrayList<ToolEntry> getDeviceToolbarList() {
		return deviceEntry;
	}
	
	public static ArrayList<ToolEntry> getAllToolbarList() {
		return allEntry;
	}

	public static ArrayList<ToolEntry> getShowToolbarList() {
		refreshToolManager();
		return ShowEntry;
	}

	public static ArrayList<ToolEntry> getHideToolbarList() {
		refreshToolManager();
		return hideEntry;
	}

	public static void refreshToolManager() {
		ShowEntry.clear();
		String[] toollist = RProp.S.EASY_GUI_TOOLBAR.get().split(",");
		
		for (String str : toollist) {
			if(str.length() !=0)
			ShowEntry.add(allEntry.get(Integer.parseInt(str)));
		}

		hideEntry.clear();
		for (ToolEntry entry : allEntry) {
			if (ShowEntry.indexOf(entry) == -1) {
				hideEntry.add(entry);
			}
		}
	}
	public static void excuteEntry(String cmd) {
		excuteEntry(cmd, null);
	}
	
	public static void excuteEntry(String cmd, Object obj) {
		Log.d("Tool Click - " + cmd);
		if (cmd.equals(RStr.BTN_OPEN.get())) {
			final String apkFilePath = ApkFileChooser.openApkFilePath(null);
			if (apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}
			if(apkScanner.getApkInfo() != null) {
				Launcher.run(apkFilePath);
			} else {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						apkScanner.clear(false);
						apkScanner.openApk(apkFilePath);
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
			}			
		} else if (cmd.equals(RStr.APP_NAME.get())) {
			UIController.changeToMainGui();
		} else if (cmd.equals(RStr.BTN_OPEN_PACKAGE.get())) {
			PackageTreeDlg Dlg = new PackageTreeDlg(mainframe);
			if (Dlg.showTreeDlg() != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}
			final String device = Dlg.getSelectedDevice();
			final String apkFilePath = Dlg.getSelectedApkPath();
			final String frameworkRes = Dlg.getSelectedFrameworkRes();
			if(apkScanner.getApkInfo() != null) {
				Launcher.run(device, apkFilePath, frameworkRes);
			} else {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						apkScanner.clear(false);
						//Apkscanner.getApkScanner().openApk(apkFilePath);
						apkScanner.openPackage(device, apkFilePath, frameworkRes);
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
			}
			
		} else if (cmd.equals(RStr.BTN_MANIFEST.get())) {
			openMenifest();
		} else if (cmd.equals(RStr.BTN_EXPLORER.get())) {
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if (apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
				
				return;
			}
			SystemUtil.openArchiveExplorer(apkInfo.filePath);
		} else if (cmd.equals(RStr.BTN_INSTALL.get())) {
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if (apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
				return;
			}
			ApkInstallWizard wizard = new ApkInstallWizard(apkInfo.filePath, mainframe);
			wizard.start();
		} else if (cmd.equals(RStr.BTN_SIGN.get())) {
			ToolEntryManager.excuteSinerDlg(mainframe);
		}  else if (cmd.equals(TOOL_SHOW_SIGN_DLG)) {
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if (apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
				return;
			}
			new EasyToolbarCertDlg(mainframe, true, apkInfo);
		} else if (cmd.equals(RStr.BTN_OPENCODE.get())) {
			OpenDecompiler();
		} else if (cmd.equals(RStr.BTN_ABOUT.get())) {
			AboutDlg.showAboutDialog(mainframe);
		} else if (cmd.equals(RStr.BTN_SETTING.get())) {
			Settings();
		} // use device tool menu
	
		else if (cmd.equals(RStr.BTN_LAUNCH.get())) {
			launchApp(obj);
		} else if (cmd.equals(RStr.BTN_DEL.get())) {
			UninstallApp(obj);
		} else if (cmd.equals(RStr.MENU_CLEAR_DATA.get())) {
			ClearData(obj);
		} else if (cmd.equals(RStr.BTN_DETAILS_INFO.get())) {
			ShowInstalledPackageInfo(obj);
		}
	}

	private static void Settings()
	{
		SettingDlg dlg = new SettingDlg(mainframe);
		dlg.setVisible(true);

		//changed theme
		if(dlg.isNeedRestart()) {
			restart();
		}

		String lang = RProp.S.LANGUAGE.get();
		if(lang != null && RStr.getLanguage() != null
				&& !RStr.getLanguage().equals(lang)) {
			restart();
		}
	}
	
	private static void OpenDecompiler() {
		ApkInfo apkInfo = apkScanner.getApkInfo();
		if (apkInfo == null || apkInfo.filePath == null || !new File(apkInfo.filePath).exists()) {
			Log.e("evtOpenJDGUI() apkInfo is null");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}

		if (!ZipFileUtil.exists(apkInfo.filePath, "classes.dex")) {
			Log.e("No such file : classes.dex");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_CLASSES_DEX);
			return;
		}

		int actionType = 0;
		String data = RProp.S.DEFAULT_DECORDER.get();
		Log.v("PROP_DEFAULT_DECORDER : " + data);
		if(data.matches(".*!.*#.*@.*")) {
			//if(evtPluginLaunch(data)) return;
			data = (String)RProp.DEFAULT_DECORDER.getDefaultValue();
		}
		if (RConst.STR_DECORDER_JD_GUI.equals(data)) {
			actionType = 1;
		} else if (RConst.STR_DECORDER_JADX_GUI.equals(data)) {
			actionType = 2;
		} else if (RConst.STR_DECORDER_BYTECOD.equals(data)) {
			actionType = 3;
		} else {
			actionType = 2;
		}
		if (actionType == 1) {
			String jarfileName = apkInfo.tempWorkPath + File.separator
					+ (new File(apkInfo.filePath)).getName().replaceAll("\\.apk$", ".jar");
			Dex2JarWrapper.convert(apkInfo.filePath, jarfileName, new Dex2JarWrapper.DexWrapperListener() {
				@Override
				public void onCompleted() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							
						}
					});
				}

				@Override
				public void onError(final String message) {
					Log.e("Failure: Fail Dex2Jar : " + message);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							MessageBoxPool.show(mainframe, MessageBoxPool.MSG_FAILURE_DEX2JAR, message);
						}
					});
				}

				@Override
				public void onSuccess(String jarFilePath) {
					JDGuiLauncher.run(jarFilePath);
				}
			});
		} else if (actionType == 2) {
			JADXLauncher.run(apkInfo.filePath);
		} else if (actionType == 3) {
			BytecodeViewerLauncher.run(apkInfo.filePath);
		}
	}

	private static void ShowInstalledPackageInfo(final Object obj) {
		final IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
		if (devices == null || devices.length == 0) {
			Log.i("No such device of a package installed.");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
			return;
		}

		if(apkScanner.getApkInfo() ==null) {
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
		}
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				for (IDevice device : devices) {
					if(!(obj.equals(device))) {
						continue;
					}
					Log.v("InstalledPackageInfo" + device.getSerialNumber());
					
					final PackageInfo info = PackageManager.getPackageInfo(device,
							apkScanner.getApkInfo().manifest.packageName);
					
					if(info ==null) {
						messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
						continue;
					}
					
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							PackageInfoPanel packageInfoPanel = new PackageInfoPanel();
							packageInfoPanel.setPackageInfo(info);
							packageInfoPanel.showDialog(mainframe);
						}
					});
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	private static void ClearData(final Object obj) {
		messagePool = new MessageBoxPool(mainframe);
		final IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
		if (devices == null || devices.length == 0) {
			Log.i("No such device of a package installed.");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
			return;
		}

		Thread thread = new Thread(new Runnable() {
			public void run() {
				for (IDevice device : devices) {
					
					if(!(obj.equals(device))) {
						continue;
					}
					Log.v("clear data on " + device.getSerialNumber());
					PackageInfo packageInfo = PackageManager.getPackageInfo(device,
							apkScanner.getApkInfo().manifest.packageName);

					if(packageInfo ==null) {
						messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
						continue;
					}
					
					String errMessage = PackageManager.clearData(packageInfo);

					if (errMessage != null && !errMessage.isEmpty()) {
						final String errMsg = errMessage;
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								Log.e(errMsg);
								messagePool.show(MessageBoxPool.MSG_FAILURE_CLEAR_DATA, errMsg);
							}
						});
					} else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								messagePool.show(MessageBoxPool.MSG_SUCCESS_CLEAR_DATA);
							}
						});
					}
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	private static void openMenifest() {
		ApkInfo apkInfo = apkScanner.getApkInfo();
		if (apkInfo == null) {
			Log.e("evtShowManifest() apkInfo is null");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);			
			return;
		}
		String manifestPath = null;
		File manifestFile = null;

		manifestPath = apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml";
		manifestFile = new File(manifestPath);

		if (!manifestFile.exists()) {
			if (!manifestFile.getParentFile().exists()) {
				if (FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
					Log.d("sucess make folder");
				}
			}
			String[] convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath,
					new String[] { "AndroidManifest.xml" });
			AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
			a2x.setMultiLinePrint(RProp.B.PRINT_MULTILINE_ATTR.get());

			FileWriter fw;
			try {
				fw = new FileWriter(new File(manifestPath));
				fw.write(a2x.toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.e("already existed file : " + manifestPath);
		}

		SystemUtil.openEditor(manifestPath);
	}

	private static void UninstallApp(final Object obj) {
		messagePool = new MessageBoxPool(mainframe);
		final IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
		if (devices == null || devices.length == 0) {
			Log.i("No such device of a package installed.");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
			return;
		}

		Thread thread = new Thread(new Runnable() {
			public void run() {
				for (IDevice device : devices) {
					if(!(obj.equals(device))) {
						continue;
					}
					Log.v("uninstall apk on " + device.getSerialNumber());
					
					PackageInfo packageInfo = PackageManager.getPackageInfo(device,
							apkScanner.getApkInfo().manifest.packageName);

					if (packageInfo == null) {
						messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);			
						continue;
					}
					
					String errMessage = null;
					if (!packageInfo.isSystemApp()) {
						errMessage = PackageManager.uninstallPackage(packageInfo);
					} else {
						int n = messagePool.show(MessageBoxPool.QUESTION_REMOVE_SYSTEM_APK);
						if (n == MessageBoxPane.NO_OPTION) {
							return;
						}

						errMessage = PackageManager.removePackage(packageInfo);
						if (errMessage == null || errMessage.isEmpty()) {
							n = messagePool.show(MessageBoxPool.QUESTION_REBOOT_SYSTEM);
							if (n == MessageBoxPane.YES_OPTION) {
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

					if (errMessage != null && !errMessage.isEmpty()) {
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

	private static void launchApp(final Object obj) {		
		final IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
		if (devices == null || devices.length == 0) {
			Log.i("No such device of a package installed.");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
			return;
		}

		Thread thread = new Thread(new Runnable() {
			private String errMsg = null;

			public void run() {
				boolean isShiftPressed = false;
				int activityOpt = RConst.INT_LAUNCH_ALWAYS_CONFIRM_ACTIVITY;

				for (IDevice device : devices) {
					if(!(obj.equals(device))) {
						continue;
					}
					Log.v("launch activity on " + device.getSerialNumber());

					PackageInfo packageInfo = PackageManager.getPackageInfo(device,
							apkScanner.getApkInfo().manifest.packageName);
					if(packageInfo == null) {
						messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
						continue;
					}
					
					if (!packageInfo.isEnabled()) {
						messagePool.show(MessageBoxPool.MSG_DISABLED_PACKAGE,
								device.getProperty(IDevice.PROP_DEVICE_MODEL));
						continue;
					}

					String selectedActivity = null;
					ComponentInfo[] activities = null;
					if (!isShiftPressed && (activityOpt == RConst.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY
							|| activityOpt == RConst.INT_LAUNCH_ONLY_LAUNCHER_ACTIVITY)) {
						activities = packageInfo.getLauncherActivityList(false);
					}

					if (activities != null && activities.length == 1) {
						selectedActivity = activities[0].name;
					} else {
						activities = packageInfo.getLauncherActivityList(true);
						if (!isShiftPressed && activityOpt == RConst.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY) {
							if (activities != null && activities.length == 1) {
								selectedActivity = activities[0].name;
							}
						}
						if (selectedActivity == null) {
							ApkInfo apkInfo = apkScanner.getApkInfo();
							ComponentInfo[] apkActivities = ApkInfoHelper.getLauncherActivityList(apkInfo, true);

							int mergeLength = (activities != null ? activities.length : 0)
									+ (apkActivities != null ? apkActivities.length : 0);
							ArrayList<String> mergeList = new ArrayList<String>(mergeLength);

							if (activities != null && activities.length > 0) {
								for (int i = 0; i < activities.length; i++) {
									boolean isLauncher = ((activities[i].featureFlag
											& ApkInfo.APP_FEATURE_LAUNCHER) != 0);
									boolean isMain = ((activities[i].featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
									mergeList.add((isLauncher ? "[LAUNCHER]" : (isMain ? "[MAIN]" : "")) + " "
											+ activities[i].name.replaceAll("^" + packageInfo.packageName, ""));
								}
							}

							if (apkActivities != null && apkActivities.length > 0) {
								for (ComponentInfo comp : apkActivities) {
									boolean isLauncher = ((comp.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
									boolean isMain = ((comp.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
									mergeList.add((isLauncher ? "[APK_LAUNCHER]" : (isMain ? "[APK_MAIN]" : "[APK]"))
											+ " " + comp.name.replaceAll("^" + apkInfo.manifest.packageName, ""));
								}
							}

							if (!mergeList.isEmpty()) {
								String selected = (String) MessageBoxPane.showInputDialog(mainframe,
										"Select Activity for " + device.getProperty(IDevice.PROP_DEVICE_MODEL),
										RStr.BTN_LAUNCH.get(), MessageBoxPane.QUESTION_MESSAGE, null,
										mergeList.toArray(new String[mergeList.size()]), mergeList.get(0));
								if (selected == null) {
									return;
								}
								selectedActivity = selected.split(" ")[1];
							}
						}
					}

					if (selectedActivity == null) {
						Log.w("No such activity of launcher or main");
						messagePool.show(MessageBoxPool.MSG_NO_SUCH_LAUNCHER);
						return;
					}

					final String launcherActivity = packageInfo.packageName + "/" + selectedActivity;
					Log.i("launcherActivity : " + launcherActivity);

					String[] cmdResult = AdbDeviceHelper.launchActivity(device, launcherActivity);
					if (cmdResult == null || (cmdResult.length >= 2 && cmdResult[1].startsWith("Error"))
							|| (cmdResult.length >= 1 && cmdResult[0].startsWith("error"))) {
						Log.e("activity start faile : " + launcherActivity);

						if (cmdResult != null) {
							StringBuilder sb = new StringBuilder(
									"cmd: adb shell start -n " + launcherActivity + "\n\n");
							for (String s : cmdResult)
								sb.append(s + "\n");
							errMsg = sb.toString();
							Log.e(errMsg);
						}

						EventQueue.invokeLater(new Runnable() {
							public void run() {
								messagePool.show(MessageBoxPool.MSG_FAILURE_LAUNCH_APP, errMsg);
							}
						});
					} else if (RProp.B.TRY_UNLOCK_AF_LAUNCH.get()) {
						AdbDeviceHelper.tryDismissKeyguard(device);
					}
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	public void excuteEntry(ToolEntry entry) {
		excuteEntry(entry.getTitle());
	}

	public static int findIndexFromAllEntry(ToolEntry obj) {
		for (int i = 0; i < allEntry.size(); i++) {
			ToolEntry entry = allEntry.get(i);
			if (obj.title.equals(entry.title)) {
				return i;
			}
		}
		return 0;
	}
	
	private static void restart() {
		if(apkScanner.getApkInfo() != null) {
			Launcher.run(apkScanner.getApkInfo().filePath);
		} else {
			Launcher.run();
		}
		mainframe.dispose();
	}

	public static void excutePermissionDlg(PermissionManager manager) {
		PermissionHistoryPanel historyView = new PermissionHistoryPanel();
		historyView.setPermissionManager(manager);
		historyView.showDialog(mainframe);
	}

	public static void excuteSinerDlg(JFrame frame) {		
		ApkInfo apkInfo = apkScanner.getApkInfo();
		if(apkInfo == null || apkInfo.filePath == null
				|| !new File(apkInfo.filePath).exists()) {
			Log.e("evtSignApkFile() apkInfo is null");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}
		ApkSignerWizard wizard = new ApkSignerWizard(frame);
		wizard.setApk(apkInfo.filePath);
		wizard.setVisible(true);
	}
	
	public static void showPermDetailDesc(PermissionManager manager, String groupName)
	{
		PermissionHistoryPanel historyView = new PermissionHistoryPanel();
		historyView.setPermissionManager(manager);
		historyView.setFilterText(groupName);
		historyView.showDialog(mainframe);
	}
}
