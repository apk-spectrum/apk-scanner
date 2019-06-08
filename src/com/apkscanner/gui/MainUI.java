package com.apkscanner.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.UIController;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.component.DropTargetChooser;
import com.apkscanner.gui.component.DropTargetChooser.DefaultTargetObject;
import com.apkscanner.gui.component.DropTargetChooserExt;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkSignerWizard;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.SearchDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.installer.ApkInstallWizard;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.WindowSizeMemorizer;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.IPlugInEventListener;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.AdbServerMonitor.IAdbDemonChangeListener;
import com.apkscanner.tool.adb.IPackageStateListener;
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

public class MainUI extends JFrame implements IPlugInEventListener
{
	private static final long serialVersionUID = -623259597186280485L;

	private ApkScanner apkScanner;
	private int infoHashCode;
	private ToolbarManagement toolbarManager;

	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;
	private MessageBoxPool messagePool;
	private DropTargetChooser dropTargetChooser;

	public MainUI(ApkScanner scanner) {
		toolbarManager = new ToolbarManagement();
		messagePool = new MessageBoxPool(this);

		initialize();

		setApkScanner(scanner);

		toolbarManager.setEnabled((boolean)Resource.PROP_ADB_DEVICE_MONITORING.getData(), 1000);

		PlugInManager.addPlugInEventListener(this);
	}

	public void setApkScanner(ApkScanner scanner) {
		apkScanner = scanner;
		if(apkScanner != null) {
			boolean changed = apkScanner.getApkInfo() != null
					&& apkScanner.getApkInfo().hashCode() != infoHashCode;
			apkScanner.setStatusListener(new ApkScannerListener(), changed);
		}
	}

	public void initialize() {
		Log.i("UI Init start");

		Log.i("initialize() setUIFont");
		String propFont = (String) Resource.PROP_BASE_FONT.getData();
		int propFontStyle = (int)Resource.PROP_BASE_FONT_STYLE.getInt();
		int propFontSize = (int) Resource.PROP_BASE_FONT_SIZE.getInt();
		setUIFont(new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));

		Log.i("initialize() set title & icon");
		setTitle(Resource.STR_APP_NAME.getString());
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());

		Log.i("initialize() set bound & size ");
		Dimension minSize = new Dimension(Resource.INT_WINDOW_SIZE_WIDTH_MIN, Resource.INT_WINDOW_SIZE_HEIGHT_MIN);
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(this, minSize);
		} else {
			setSize(minSize);
		}
		//setMinimumSize(minSize);
		setResizable(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		WindowSizeMemorizer.registeComponent(this);

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

		// Drag & Drop event processing panel
		dropTargetChooser = new DropTargetChooserExt(eventHandler);
		setGlassPane(dropTargetChooser);
		dropTargetChooser.setVisible(true);

		// Shortcut key event processing
		KeyStrokeAction.registerKeyStrokeActions(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new KeyStroke[] {
			KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, false),
			KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
			KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
			KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true)
		}, eventHandler);

		Log.i("UI Init end");
	}

	public void uiLoadBooster() {
		tabbedPanel.uiLoadBooster();
	}

	@Override
	public void onPluginLoaded() {
		toolBar.onLoadPlugin(new UIEventHandler());
		tabbedPanel.onLoadPlugin();

		if(apkScanner != null) {
			int state = apkScanner.getStatus();
			if( PlugInManager.getPackageSearchers().length > 0
					&& Status.BASIC_INFO_COMPLETED.isCompleted(state)
					&& Status.CERT_COMPLETED.isCompleted(state) ) {
				tabbedPanel.setData(apkScanner.getApkInfo(), Status.BASIC_INFO_COMPLETED);
			}
		}
	}

	public void setUpdatedBadgeCount(int count) {
		toolBar.setBadgeCount(count);
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
					tabbedPanel.setData(null, null);
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
						tabbedPanel.onProgress(message);
						break;
					default:
						Log.i(message);
					}
				}
			});
		}

		@Override
		public void onStateChanged(final Status status) {
			//Log.v("onStateChanged() "+ status);
			if(status == Status.STANBY) {
				Log.v("STANBY: does not UI update");
				PlugInManager.setApkInfo(apkScanner.getApkInfo());
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
				PlugInManager.setApkInfo(apkScanner.getApkInfo());

				String apkFilePath = apkScanner.getApkInfo().filePath;
				String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + Resource.STR_APP_NAME.getString();
				setTitle(title);

				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);
				dropTargetChooser.setExternalToolsVisible(true);

				infoHashCode = apkScanner.getApkInfo().hashCode();
			default:
				tabbedPanel.setData(apkScanner.getApkInfo(), status);
				break;
			}
			Log.i("onStateChanged() ui sync end " + status);
		}
	}

	class UIEventHandler implements ActionListener, WindowListener, DropTargetChooser.Listener
	{
		private void evtOpenApkFile(boolean newWindow) {
			final String apkFilePath = ApkFileChooser.openApkFilePath(MainUI.this);
			if(apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}
			if(!newWindow) {
				toolBar.setEnabledAt(ButtonSet.OPEN, false);
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
				dropTargetChooser.setExternalToolsVisible(false);
				tabbedPanel.setLodingLabel();

				Thread thread = new Thread(new Runnable() {
					public void run() {
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

		private void evtOpenPackage(boolean newWindow) {
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
				tabbedPanel.onProgress(null);
				toolBar.setEnabledAt(ButtonSet.OPEN, false);
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
				dropTargetChooser.setExternalToolsVisible(false);

				Thread thread = new Thread(new Runnable() {
					public void run() {
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

		private void evtInstallApk(boolean checkPackage) {
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

		private void evtShowManifest(boolean saveAs) {
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowManifest() apkInfo is null");
				return;
			}

			try {
				String manifestPath = null;
				File manifestFile = null;
				if(!saveAs) {
					manifestPath = apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml";
					manifestFile = new File(manifestPath);
				} else {
					JFileChooser jfc = ApkFileChooser.getFileChooser((String)Resource.PROP_LAST_FILE_SAVE_PATH.getData(), JFileChooser.SAVE_DIALOG, new File("AndroidManifest.xml"));
					if(jfc.showSaveDialog(MainUI.this) != JFileChooser.APPROVE_OPTION) return;
					manifestFile = jfc.getSelectedFile();
					if(manifestFile == null) return;
					Resource.PROP_LAST_FILE_SAVE_PATH.setData(manifestFile.getParentFile().getAbsolutePath());
					manifestPath = manifestFile.getAbsolutePath();
				}

				if(saveAs || !manifestFile.exists()) {
					if(!manifestFile.getParentFile().exists()) {
						if(FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
							Log.d("sucess make folder");
						}
					}

					String[] convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] {"AndroidManifest.xml"});
					AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
					a2x.setMultiLinePrint((boolean)Resource.PROP_PRINT_MULTILINE_ATTR.getData());

					FileWriter fw = new FileWriter(new File(manifestPath));
					fw.write(a2x.toString());
					fw.close();
				} else {
					Log.e("already existed file : " + manifestPath);
				}

				if(!saveAs) SystemUtil.openEditor(manifestPath);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		private void evtShowExplorer(ActionEvent e) {
			int actionType = 0;
			if(e == null || ToolBar.ButtonSet.EXPLORER.matchActionEvent(e)) {
				String data = (String)Resource.PROP_DEFAULT_EXPLORER.getData();
				if(Resource.STR_EXPLORER_ARCHIVE.equals(data)) {
					actionType = 1;
				} else if(Resource.STR_EXPLORER_FOLDER.equals(data)) {
					actionType = 2;
				}
			} else if(ToolBar.MenuItemSet.EXPLORER_ARCHIVE.matchActionEvent(e)) {
				actionType = 1;
			} else if(ToolBar.MenuItemSet.EXPLORER_FOLDER.matchActionEvent(e)) {
				actionType = 2;
			}

			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				return;
			}

			switch(actionType) {
			case 1:
				SystemUtil.openArchiveExplorer(apkInfo.filePath);
				break;
			case 2:
				SystemUtil.openFileExplorer(apkInfo.filePath);
				break;
			default:
				Log.e("evtShowExplorer() unknown type : " + actionType);
				break;
			}
		}

		private void evtOpenDecompiler(ActionEvent e) {
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

			int actionType = 0;
			if(e == null || ToolBar.ButtonSet.OPEN_CODE.matchActionEvent(e)) {
				String data = (String)Resource.PROP_DEFAULT_DECORDER.getData();
				Log.v("PROP_DEFAULT_DECORDER : " + data);
				if(data.matches(".*!.*#.*@.*")) {
					if(evtPluginLaunch(data)) return;
					data = (String)Resource.PROP_DEFAULT_DECORDER.getDefValue();
				}
				if(Resource.STR_DECORDER_JD_GUI.equals(data)) {
					actionType = 1;
				} else if(Resource.STR_DECORDER_JADX_GUI.equals(data)) {
					actionType = 2;
				} else if(Resource.STR_DECORDER_BYTECOD.equals(data)) {
					actionType = 3;
				} else {
					actionType = 2;
				}
			}
			if(actionType == 1 || ToolBar.MenuItemSet.DECODER_JD_GUI.matchActionEvent(e)) {
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
			} else if(actionType == 2 || ToolBar.MenuItemSet.DECODER_JADX_GUI.matchActionEvent(e)) {
				JADXLauncher.run(apkInfo.filePath);
			} else if(actionType == 3 || ToolBar.MenuItemSet.DECODER_BYTECODE.matchActionEvent(e)) {
				BytecodeViewerLauncher.run(apkInfo.filePath);
			}
		}

		private void evtOpenSearcher(ActionEvent e) {
			SearchDlg dialog = new SearchDlg();
			dialog.setApkInfo(apkScanner.getApkInfo());

			dialog.setModal(false);
			dialog.setVisible(true);

			Log.d(dialog.sName);
		}

		private void evtSettings() {
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

		private void evtLaunchApp(final AWTEvent e) {
			final IDevice[] devices = getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
				return;
			}

			Thread thread = new Thread(new Runnable() {
				private String errMsg = null;
				public void run() {
					boolean isShiftPressed = false;
					int actionType = 0;
					if(e instanceof ActionEvent) {
						isShiftPressed = (e != null && (((ActionEvent) e).getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0);
						if(e.getSource() instanceof KeyStrokeAction) {
							if(isShiftPressed) actionType = 2;
						} else if(e == null || ToolBar.ButtonSet.LAUNCH.matchActionEvent((ActionEvent) e)) {
							if(isShiftPressed) actionType = 2;
						} else if(ToolBar.ButtonSet.SUB_LAUNCH.matchActionEvent((ActionEvent) e)) {
							String data = (String)Resource.PROP_DEFAULT_LAUNCH_MODE.getData();
							if(Resource.STR_LAUNCH_LAUNCHER.equals(data)) {
								actionType = 1;
							} else if(Resource.STR_LAUNCH_SELECT.equals(data)) {
								actionType = 2;
							}
						} else if(ToolBar.MenuItemSet.LAUNCH_LAUNCHER.matchActionEvent((ActionEvent) e)) {
							actionType = 1;
							isShiftPressed = false;
						} else if(ToolBar.MenuItemSet.LAUNCH_SELECT.matchActionEvent((ActionEvent) e)) {
							actionType = 2;
							isShiftPressed = false;
						}
					} else if(e instanceof InputEvent) {
						isShiftPressed = (e != null && (((InputEvent) e).getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0);
						if(isShiftPressed) actionType = 2;
					}

					int activityOpt = Resource.PROP_LAUNCH_ACTIVITY_OPTION.getInt();
					if(actionType == 2) {
						activityOpt = Resource.INT_LAUNCH_ALWAYS_CONFIRM_ACTIVITY;
					}

					for(IDevice device: devices) {
						Log.v("launch activity on " + device.getSerialNumber());

						PackageInfo packageInfo = getPackageInfo(device);

						if(!packageInfo.isEnabled()) {
							messagePool.show(MessageBoxPool.MSG_DISABLED_PACKAGE, device.getProperty(IDevice.PROP_DEVICE_MODEL));
							continue;
						}

						String selectedActivity = null;
						ComponentInfo[] activities = null;
						if(!isShiftPressed && (activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY
								|| activityOpt == Resource.INT_LAUNCH_ONLY_LAUNCHER_ACTIVITY)) {
							activities = packageInfo.getLauncherActivityList(false);
						}

						if(activities != null && activities.length == 1) {
							selectedActivity = activities[0].name;
						} else {
							activities = packageInfo.getLauncherActivityList(true);
							if(!isShiftPressed && activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY) {
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
				public void run() {
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

		private void evtClearData() {
			final IDevice[] devices = getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
				return;
			}

			Thread thread = new Thread(new Runnable() {
				public void run() {
					for(IDevice device: devices) {
						Log.v("clear data on " + device.getSerialNumber());

						PackageInfo packageInfo = getPackageInfo(device);

						String errMessage = PackageManager.clearData(packageInfo);

						if(errMessage != null && !errMessage.isEmpty()) {
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

		private void evtShowInstalledPackageInfo() {
			final IDevice[] devices = getInstalledDevice();
			if(devices == null || devices.length == 0) {
				Log.i("No such device of a package installed.");
				messagePool.show(MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
				return;
			}

			Thread thread = new Thread(new Runnable() {
				public void run() {
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

		private boolean evtPluginLaunch(String actionCommand) {
			IPlugIn plugin = PlugInManager.getPlugInByActionCommand(actionCommand);
			if(plugin == null) return false;
			plugin.launch();
			return true;
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

		private void setLanguage(String lang) {
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
		public void actionPerformed(ActionEvent e) {
			if (ToolBar.ButtonSet.OPEN.matchActionEvent(e) || ToolBar.MenuItemSet.OPEN_APK.matchActionEvent(e)) {
				evtOpenApkFile((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0);
			} else if(ToolBar.ButtonSet.MANIFEST.matchActionEvent(e)) {
				evtShowManifest((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0);
			} else if(ToolBar.ButtonSet.EXPLORER.matchActionEvent(e)
					|| ToolBar.MenuItemSet.EXPLORER_ARCHIVE.matchActionEvent(e)
					|| ToolBar.MenuItemSet.EXPLORER_FOLDER.matchActionEvent(e)) {
				evtShowExplorer(e);
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
				evtOpenPackage((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0);
			} else if(ToolBar.MenuItemSet.INSTALLED_CHECK.matchActionEvent(e)) {
				evtShowInstalledPackageInfo();
			} else if(ToolBar.ButtonSet.OPEN_CODE.matchActionEvent(e)
					|| ToolBar.MenuItemSet.DECODER_JD_GUI.matchActionEvent(e)
					|| ToolBar.MenuItemSet.DECODER_JADX_GUI.matchActionEvent(e)
					|| ToolBar.MenuItemSet.DECODER_BYTECODE.matchActionEvent(e)) {
				evtOpenDecompiler(e);
			} else if(ToolBar.ButtonSet.SEARCH.matchActionEvent(e)
					|| ToolBar.MenuItemSet.SEARCH_RESOURCE.matchActionEvent(e)) {
				evtOpenSearcher(e);
			} else if(ToolBar.ButtonSet.LAUNCH.matchActionEvent(e) || ToolBar.ButtonSet.SUB_LAUNCH.matchActionEvent(e)
					|| ToolBar.MenuItemSet.LAUNCH_LAUNCHER.matchActionEvent(e)
					|| ToolBar.MenuItemSet.LAUNCH_SELECT.matchActionEvent(e)) {
				evtLaunchApp(e);
			} else if(ToolBar.MenuItemSet.UNINSTALL_APK.matchActionEvent(e)) {
				evtUninstallApp();
			} else if(ToolBar.MenuItemSet.CLEAR_DATA.matchActionEvent(e)) {
				evtClearData();
			} else if(ToolBar.ButtonSet.SIGN.matchActionEvent(e) || ToolBar.ButtonSet.SUB_SIGN.matchActionEvent(e)) {
				evtSignApkFile();
			} else if(e.getActionCommand() != null && e.getActionCommand().startsWith("PLUGIN:")) {
				evtPluginLaunch(e.getActionCommand().replaceAll("PLUGIN:", ""));
			} else if(ToolBar.CMD_VISIBLE_TO_BASEIC.equals(e.getActionCommand())) {
				if(e.getSource() instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem ckBox = ((JCheckBoxMenuItem)e.getSource());
					Resource.PROP_VISIBLE_TO_BASIC.setData(ckBox.isSelected());
					tabbedPanel.reloadResource();
				}
			}  else if(ToolBar.CMD_VISIBLE_TO_BASEIC_CHANGED.equals(e.getActionCommand())) {
				tabbedPanel.reloadResource();
			} else if(e.getSource() instanceof KeyStrokeAction){
				keyStrokeActionPerformed(e);
			} else {
				Log.v("Unkown action : " + e);
			}
		}

		private void keyStrokeActionPerformed(ActionEvent e) {
			KeyStrokeAction action = (KeyStrokeAction) e.getSource();
			int modifier = action.getModifiersEx();
			int keycode = action.getKeyStroke().getKeyCode();

			if(modifier == InputEvent.CTRL_DOWN_MASK) {
				switch(keycode) {
				case KeyEvent.VK_O: evtOpenApkFile(false);	break;
				case KeyEvent.VK_P: evtOpenPackage(false);	break;
				case KeyEvent.VK_N: Launcher.run();			break;
				case KeyEvent.VK_I: evtInstallApk(false);	break;
				case KeyEvent.VK_T: evtShowInstalledPackageInfo();	break;
				case KeyEvent.VK_E: evtShowExplorer(null);	break;
				case KeyEvent.VK_M: evtShowManifest(false);	break;
				case KeyEvent.VK_R: evtLaunchApp(e);		break;
				//case KeyEvent.VK_S: evtSettings();			break;
				}
			} else if(modifier == (InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) {
				switch(keycode) {
				case KeyEvent.VK_O: evtOpenApkFile(true);	break;
				case KeyEvent.VK_P: evtOpenPackage(true);	break;
				case KeyEvent.VK_R: evtLaunchApp(e);	break;
				}
			} else if(modifier == 0) {
				switch(keycode) {
				case KeyEvent.VK_F1 : AboutDlg.showAboutDialog(MainUI.this);break;
				case KeyEvent.VK_F12: LogDlg.showLogDialog(MainUI.this);	break;
				case KeyEvent.VK_ESCAPE:
					switch((int)Resource.PROP_ESC_ACTION.getInt()) {
					case Resource.INT_ESC_ACT_NONE: return;
					case Resource.INT_ESC_ACT_CHANG_UI_MODE: break;
					case Resource.INT_ESC_ACT_EXIT: dispose(); return;
					}
				case KeyEvent.VK_F2: UIController.changeToEasyGui(); break;
				}
			}
		}

		// Drag & Drop event processing
		@Override
		public void filesDropped(Object dropedTarget, final File[] files) {
			final String[] filePaths = new String[files.length];
			for(int i = 0; i< files.length; i++) {
				try {
					filePaths[i] = files[i].getCanonicalPath();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			if(dropedTarget instanceof DefaultTargetObject) {
				switch((DefaultTargetObject)dropedTarget) {
				case DROPED_TARGET_APK_OPEN:
					Log.i("filesDropped()");
					toolBar.setEnabledAt(ButtonSet.OPEN, false);
					toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
					dropTargetChooser.setExternalToolsVisible(false);
					tabbedPanel.setLodingLabel();

					Thread thread = new Thread(new Runnable() {
						public void run()
						{
							apkScanner.clear(false);
							apkScanner.openApk(filePaths[0]);
						}
					});
					thread.setPriority(Thread.NORM_PRIORITY);
					thread.start();
					break;
				case DROPED_TARGET_NEW_WIN:
					Launcher.run(filePaths[0]);
					break;
				}
			} else if(dropedTarget instanceof IExternalTool) {
				String apkPath = apkScanner.getApkInfo().filePath;
				((IExternalTool) dropedTarget).launch(apkPath, filePaths[0]);
			}
		}

		private void finished() {
			Log.v("finished()");

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
						applyToobarPolicy();
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

		public void setEnabled(final boolean enable, final int delayMs) {
	        new Timer().schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	Log.v("isDispatchThread " + EventQueue.isDispatchThread());
	                setEnabled(enable);
	            }
	        }, delayMs);
		}

		public void setApkInfo(ApkInfo info) {
			synchronized(this) {
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
			if(enabled) {
				applyToobarPolicy();
			}
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
			Log.v("deviceChanged() " + device.getName() + ", " + device.getState() + ", changeMask " + changeMask);
			if((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
				applyToobarPolicy();
			}
		}

		@Override
		public void deviceConnected(IDevice device) {
			Log.v("deviceConnected() " + device.getName() + ", " + device.getState());
			if(device.isOnline()) {
				applyToobarPolicy();
			} else {
				Log.v("device connected, but not online: " + device.getSerialNumber() + ", " + device.getState());
			}
		}

		@Override
		public void deviceDisconnected(IDevice device) {
			Log.v("deviceDisconnected() " + device.getSerialNumber());
			PackageManager.removeCache(device);
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
