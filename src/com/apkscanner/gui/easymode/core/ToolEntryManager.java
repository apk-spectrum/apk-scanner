package com.apkscanner.gui.easymode.core;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.permissionmanager.PermissionGroupInfoExt;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.ApkSignerWizard;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.easymode.EasyLightApkScanner;
import com.apkscanner.gui.easymode.dlg.EasyPermissionDlg;
import com.apkscanner.gui.easymode.dlg.EasyToolbarCertDlg;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.resource.Resource;
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
	static public EasyLightApkScanner Apkscanner;
	static public JFrame mainframe = null;

	static ArrayList<ToolEntry> allEntry;

	public static MessageBoxPool messagePool;
	
	public ToolEntryManager() {
		
	}

	public static void initToolEntryManager() {
		allEntry = new ArrayList<ToolEntry>(Arrays.asList(
//				new ToolEntry(Resource.STR_APP_NAME.getString(), "Excute Original Scanner",
//						Resource.IMG_APP_ICON.getImageIcon(100, 100)),
				new ToolEntry(Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString(),
						Resource.IMG_TOOLBAR_OPEN.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.STR_BTN_OPEN_PACKAGE_LAB.getString(),
						Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString(),
						Resource.IMG_TOOLBAR_MANIFEST.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString(),
						Resource.IMG_TOOLBAR_EXPLORER.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString(),
						Resource.IMG_TOOLBAR_INSTALL.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_SIGN.getString(), Resource.STR_BTN_SIGN_LAB.getString(),
						Resource.IMG_TOOLBAR_SIGNNING.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString(),
						Resource.IMG_TOOLBAR_LAUNCH.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_DEL.getString(), Resource.STR_BTN_DEL.getString(),
						Resource.IMG_TOOLBAR_UNINSTALL.getImageIcon()),
				new ToolEntry(Resource.STR_MENU_CLEAR_DATA.getString(), Resource.STR_MENU_CLEAR_DATA.getString(),
						Resource.IMG_TOOLBAR_CLEAR.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_DETAILS_INFO.getString(), Resource.STR_BTN_DETAILS_INFO.getString(),
						Resource.IMG_TOOLBAR_SEARCH.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENING_CODE_LAB.getString(),
						Resource.IMG_TOOLBAR_OPENCODE.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString(),
						Resource.IMG_TOOLBAR_SETTING.getImageIcon()),
				new ToolEntry(Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString(),
						Resource.IMG_TOOLBAR_ABOUT.getImageIcon())));
		messagePool = new MessageBoxPool(mainframe);
		refreshToolManager();
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
		String[] toollist = Resource.PROP_EASY_GUI_TOOLBAR.getData().toString().split(",");
		Log.d(toollist[0].length() + "");
		
		
		
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
		Log.d("Tool Click - " + cmd);
		if (cmd.equals(Resource.STR_BTN_OPEN.getString())) {
			final String apkFilePath = ApkFileChooser.openApkFilePath(null);
			if (apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}
			Launcher.run(apkFilePath);
		} else if (cmd.equals(Resource.STR_APP_NAME.getString())) {
			Launcher.run(Apkscanner.getApkFilePath(), false);
		} else if (cmd.equals(Resource.STR_BTN_OPEN_PACKAGE.getString())) {
			PackageTreeDlg Dlg = new PackageTreeDlg(mainframe);
			if (Dlg.showTreeDlg() != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}
			final String device = Dlg.getSelectedDevice();
			final String apkFilePath = Dlg.getSelectedApkPath();
			final String frameworkRes = Dlg.getSelectedFrameworkRes();
			Launcher.run(device, apkFilePath, frameworkRes);
		} else if (cmd.equals(Resource.STR_BTN_MANIFEST.getString())) {
			openMenifest();
		} else if (cmd.equals(Resource.STR_BTN_EXPLORER.getString())) {
			ApkInfo apkInfo = Apkscanner.getApkInfo();
			if (apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
				
				return;
			}
			SystemUtil.openArchiveExplorer(apkInfo.filePath);
		} else if (cmd.equals(Resource.STR_BTN_INSTALL.getString())) {
			ApkInfo apkInfo = Apkscanner.getApkInfo();
			if (apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
				return;
			}
			ApkInstallWizard wizard = new ApkInstallWizard(apkInfo.filePath, mainframe);
			wizard.start();
		} else if (cmd.equals(Resource.STR_BTN_SIGN.getString())) {
			ToolEntryManager.excuteSinerDlg(mainframe);
		}  else if (cmd.equals(TOOL_SHOW_SIGN_DLG)) {
			ApkInfo apkInfo = Apkscanner.getApkInfo();
			if (apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
				return;
			}

			EasyToolbarCertDlg dlg = new EasyToolbarCertDlg(mainframe, true, apkInfo);
			dlg = null;
		} else if (cmd.equals(Resource.STR_BTN_LAUNCH.getString())) {
			launchApp();
		} else if (cmd.equals(Resource.STR_BTN_DEL.getString())) {
			UninstallApp();
		} else if (cmd.equals(Resource.STR_MENU_CLEAR_DATA.getString())) {
			ClearData();
		} else if (cmd.equals(Resource.STR_BTN_DETAILS_INFO.getString())) {
			ShowInstalledPackageInfo();
		} else if (cmd.equals(Resource.STR_BTN_OPENCODE.getString())) {
			OpenDecompiler();
		} else if (cmd.equals(Resource.STR_BTN_ABOUT.getString())) {
			AboutDlg.showAboutDialog(mainframe);
		} else if (cmd.equals(Resource.STR_BTN_SETTING.getString())) {
			Settings();
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

		String lang = (String)Resource.PROP_LANGUAGE.getData();
		if(lang != null && Resource.getLanguage() != null
				&& !Resource.getLanguage().equals(lang)) {
			restart();
		}
	}
	
	private static void OpenDecompiler() {
		ApkInfo apkInfo = Apkscanner.getApkInfo();
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
		String data = (String) Resource.PROP_DEFAULT_DECORDER.getData();
		if (Resource.STR_DECORDER_JD_GUI.equals(data)) {
			actionType = 1;
		} else if (Resource.STR_DECORDER_JADX_GUI.equals(data)) {
			actionType = 2;
		} else if (Resource.STR_DECORDER_BYTECOD.equals(data)) {
			actionType = 3;
		} else {

			return;
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

	private static void ShowInstalledPackageInfo() {
		final IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
		if (devices == null || devices.length == 0) {
			Log.i("No such device of a package installed.");
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
			return;
		}

		if(Apkscanner.getApkInfo() ==null) {
			messagePool.show(MessageBoxPool.MSG_NO_SUCH_APK_FILE);
		}
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				for (IDevice device : devices) {
					final PackageInfo info = PackageManager.getPackageInfo(device,
							Apkscanner.getApkInfo().manifest.packageName);
					
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

	private static void ClearData() {
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
					Log.v("clear data on " + device.getSerialNumber());

					PackageInfo packageInfo = PackageManager.getPackageInfo(device,
							Apkscanner.getApkInfo().manifest.packageName);

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
		ApkInfo apkInfo = Apkscanner.getApkInfo();
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
			a2x.setMultiLinePrint((boolean) Resource.PROP_PRINT_MULTILINE_ATTR.getData());

			FileWriter fw;
			try {
				fw = new FileWriter(new File(manifestPath));
				fw.write(a2x.toString());
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("already existed file : " + manifestPath);
		}

		SystemUtil.openEditor(manifestPath);
	}

	private static void UninstallApp() {
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
					Log.v("uninstall apk on " + device.getSerialNumber());

					PackageInfo packageInfo = PackageManager.getPackageInfo(device,
							Apkscanner.getApkInfo().manifest.packageName);

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

	private static void launchApp() {		
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
				int actionType = 0;
				int activityOpt = Resource.INT_LAUNCH_ALWAYS_CONFIRM_ACTIVITY;

				for (IDevice device : devices) {
					Log.v("launch activity on " + device.getSerialNumber());

					PackageInfo packageInfo = PackageManager.getPackageInfo(device,
							Apkscanner.getApkInfo().manifest.packageName);
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
					if (!isShiftPressed && (activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY
							|| activityOpt == Resource.INT_LAUNCH_ONLY_LAUNCHER_ACTIVITY)) {
						activities = packageInfo.getLauncherActivityList(false);
					}

					if (activities != null && activities.length == 1) {
						selectedActivity = activities[0].name;
					} else {
						activities = packageInfo.getLauncherActivityList(true);
						if (!isShiftPressed && activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY) {
							if (activities != null && activities.length == 1) {
								selectedActivity = activities[0].name;
							}
						}
						if (selectedActivity == null) {
							ApkInfo apkInfo = Apkscanner.getApkInfo();
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
										Resource.STR_BTN_LAUNCH.getString(), MessageBoxPane.QUESTION_MESSAGE, null,
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
					} else if ((boolean) Resource.PROP_TRY_UNLOCK_AF_LAUNCH.getData()) {
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
		// TODO Auto-generated method stub
		for (int i = 0; i < allEntry.size(); i++) {
			ToolEntry entry = allEntry.get(i);
			if (obj.title.equals(entry.title)) {
				return i;
			}
		}
		return 0;
	}
	
	private static void restart() {
		if(Apkscanner.getApkInfo() != null) {
			Launcher.run(Apkscanner.getApkInfo().filePath);
		} else {
			Launcher.run();
		}
		mainframe.dispose();
	}

	public static void excutePermissionDlg() {
		// TODO Auto-generated method stub
		new EasyPermissionDlg(mainframe, true, Apkscanner.getApkInfo());			
		
	}

	public static void excuteSinerDlg(JFrame frame) {		
		// TODO Auto-generated method stub
		ApkInfo apkInfo = Apkscanner.getApkInfo();
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
	
	public static void showPermDetailDesc(PermissionGroupInfoExt group)
	{
		if(group == null) return;

		StringBuilder body = new StringBuilder("");
		//body.append("<div id=\"perm-detail-desc\">");
		body.append("■ ");
		if(group.label != null) {
			body.append(group.getLabel() + " - ");
		}
		body.append("[" + group.name + "]\n");
		if(group.description != null) {
			body.append(" : " + group.getDescription() + "\n");
		}
		body.append("------------------------------------------------------------------------------------------------------------\n\n");

		for(PermissionInfo info: group.permissions) {
			body.append("▶ ");
			if(info.isDangerousLevel()) {
				body.append("[DANGEROUS] ");	
			}
			if(info.labels != null) {
				String label = info.labels[0].name;
				for(ResourceInfo r: info.labels) {
					if(r.configuration != null && r.configuration.equals(Resource.getLanguage())) {
						label = r.name;
						break;
					}
				}
				if(label != null)
					body.append(label + " ");
			}
			body.append("[" + info.name + "]\n");
			body.append(" - protectionLevel=" + info.protectionLevel + "\n");
			if(info.descriptions != null) {
				String description = info.descriptions[0].name;
				for(ResourceInfo r: info.descriptions) {
					if(r.configuration != null && r.configuration.equals(Resource.getLanguage())) {
						description = r.name;
						break;
					}
				}
				if(description != null) body.append(" : " + description + "\n");
				body.append("\n");
			}
		}
		MessageBoxPane.showTextAreaDialog(mainframe, body.toString(), Resource.STR_BASIC_PERM_DISPLAY_TITLE.getString(), 
				MessageBoxPane.INFORMATION_MESSAGE, new ImageIcon(group.icon.replaceAll("^file:/", "")), new Dimension(600, 200));
		
	}
}
