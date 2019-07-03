package com.apkscanner.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.gui.component.WindowSizeMemorizer;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.plugin.IPlugInEventListener;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.LanguageChangeListener;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.AdbServerMonitor.IAdbDemonChangeListener;
import com.apkscanner.tool.adb.IPackageStateListener;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;

public class MainUI extends JFrame implements IPlugInEventListener, LanguageChangeListener
{
	private static final long serialVersionUID = -623259597186280485L;

	private ApkScanner apkScanner;
	private int infoHashCode;
	private ToolbarManagement toolbarManager;

	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;
	private MessageBoxPool messagePool;
	private PlugInDropTargetChooser dropTargetChooser;

	public MainUI(ApkScanner scanner, UiEventHandler eventHandler) {
		toolbarManager = new ToolbarManagement();
		messagePool = new MessageBoxPool(this);

		initialize(eventHandler);

		setApkScanner(scanner);

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

	public void initialize(UiEventHandler eventHandler) {
		Log.i("UI Init start");

		Log.i("initialize() setUIFont");
		String propFont = RProp.S.BASE_FONT.get();
		int propFontStyle = RProp.I.BASE_FONT_STYLE.get();
		int propFontSize = RProp.I.BASE_FONT_SIZE.get();
		setUIFont(new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));

		Log.i("initialize() set title & icon");
		setTitle(RStr.APP_NAME.get());
		setIconImage(RImg.APP_ICON.getImage());

		Log.i("initialize() set bound & size ");
		Dimension minSize = new Dimension(RConst.INT_WINDOW_SIZE_WIDTH_MIN, RConst.INT_WINDOW_SIZE_HEIGHT_MIN);
		if(RProp.B.SAVE_WINDOW_SIZE.get()) {
			WindowSizeMemorizer.resizeCompoent(this, minSize);
		} else {
			setSize(minSize);
		}
		//setMinimumSize(minSize);
		setResizable(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		WindowSizeMemorizer.registeComponent(this);

		//UIEventHandler eventHandler = new UIEventHandler();

		Log.i("initialize() toolbar init");
		// ToolBar initialize and add
		toolBar = new ToolBar(this, eventHandler);
		toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
		toolBar.setEnabledAt(ButtonSet.NEED_DEVICE, false);
		add(toolBar, BorderLayout.NORTH);

		Log.i("initialize() tabbedpanel init");
		// TabPanel initialize and add
		String tabbedStyle = RProp.S.TABBED_UI_THEME.get();
		tabbedPanel = new TabbedPanel(tabbedStyle);
		add(tabbedPanel, BorderLayout.CENTER);

		Log.i("initialize() register event handler");
		// Closing event of window be delete tempFile
		addWindowListener(eventHandler);

		// Drag & Drop event processing panel
		dropTargetChooser = new PlugInDropTargetChooser(eventHandler);
		setGlassPane(dropTargetChooser);
		dropTargetChooser.setVisible(true);

		RStr.addLanguageChangeListener(this);
		Log.i("UI Init end");
	}

	public void uiLoadBooster() {
		tabbedPanel.uiLoadBooster();
	}

	@Override
	public void onPluginLoaded() {
		toolBar.onLoadPlugin();
		tabbedPanel.onLoadPlugin();
	}

	@Override
	public void languageChange(String oldLang, String newLang) {
		ApkInfo apkInfo = apkScanner.getApkInfo();
		String title = RStr.APP_NAME.get();
		if(apkInfo != null) {
			title += " - " + apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)+1);
		}
		setTitle(title);
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
					setTitle(RStr.APP_NAME.get());
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
				String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + RStr.APP_NAME.get();
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

	class ToolbarManagement implements IDeviceChangeListener, IAdbDemonChangeListener, IPackageStateListener
	{
		private boolean enabled;

		private String packageName;
		private int versionCode;
		private boolean hasSignature;
		private boolean hasMainActivity;

		public ToolbarManagement() {
			if(RProp.B.ADB_DEVICE_MONITORING.get()) {
				setEnabled(RProp.B.ADB_DEVICE_MONITORING.get(), 1000);
			}
			RProp.ADB_DEVICE_MONITORING.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					Log.v("Change property : " + evt);
					setEnabled((boolean) evt.getNewValue());
				}
			});
		}

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
