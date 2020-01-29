package com.apkscanner.gui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.IPackageStateListener;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.AdbServerMonitor.IAdbDemonChangeListener;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.util.Log;

class ToolBarManagement implements IDeviceChangeListener, IAdbDemonChangeListener, IPackageStateListener
{
	private ToolBar toolBar;

	private boolean enabled;

	private String packageName;
	private int versionCode;
	private boolean hasSignature;
	private boolean hasMainActivity;

	public ToolBarManagement(ToolBar toolBar) {
		this.toolBar = toolBar;

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