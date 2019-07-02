package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public class DeviceUninstallAppAction extends AbstractDeviceAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_UNINSTALL_APP";

	public DeviceUninstallAppAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtUninstallApp(getWindow(e));
	}

	private void evtUninstallApp(final Window owner) {
		final ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		final ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null) return;

		final String packagName = apkInfo.manifest.packageName;

		final IDevice[] devices = getInstalledDevice(packagName);
		if(devices == null || devices.length == 0) {
			Log.i("No such device of a package installed.");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
			return;
		}

		Thread thread = new Thread(new Runnable() {
			public void run() {
				for(IDevice device: devices) {
					Log.v("uninstall apk on " + device.getSerialNumber());

					PackageInfo packageInfo = getPackageInfo(device, packagName);

					String errMessage = null;
					if(!packageInfo.isSystemApp()) {
						errMessage = PackageManager.uninstallPackage(packageInfo);
					} else {
						int n = MessageBoxPool.show(owner, MessageBoxPool.QUESTION_REMOVE_SYSTEM_APK);
						if(n == MessageBoxPane.NO_OPTION) {
							return;
						}

						errMessage = PackageManager.removePackage(packageInfo);
						if(errMessage == null || errMessage.isEmpty()) {
							n = MessageBoxPool.show(owner, MessageBoxPool.QUESTION_REBOOT_SYSTEM);
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
								MessageBoxPool.show(owner, MessageBoxPool.MSG_FAILURE_UNINSTALLED, errMsg);
							}
						});
					} else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								MessageBoxPool.show(owner, MessageBoxPool.MSG_SUCCESS_REMOVED);
							}
						});
					}
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}
}
