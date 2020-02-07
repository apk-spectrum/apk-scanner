package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.easymode.contents.EasyGuiDeviceToolPanel;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.MessageBoxPane;
import com.apkspectrum.tool.adb.PackageInfo;
import com.apkspectrum.tool.adb.PackageManager;
import com.apkspectrum.util.Log;

@SuppressWarnings("serial")
public class DeviceUninstallAppAction extends AbstractDeviceAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_UNINSTALL_APP";

	public DeviceUninstallAppAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		IDevice device = null;
		if(e.getSource() instanceof EasyGuiDeviceToolPanel) {
			device = ((EasyGuiDeviceToolPanel) e.getSource()).getSelecteddevice();
		}
		evtUninstallApp(getWindow(e), device);
	}

	private void evtUninstallApp(final Window owner, final IDevice target) {
		final ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) return;

		final String packageName = apkInfo.manifest.packageName;

		Thread thread = new Thread(new Runnable() {
			public void run() {
				IDevice[] devices = null;
				if(target == null) {
					devices = getInstalledDevice(packageName);
				} else {
					if(getPackageInfo(target, packageName) != null) {
						devices =  new IDevice[] { target };
					}
				}

				if(devices == null || devices.length == 0) {
					Log.i("No such device of a package installed.");
					MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
					return;
				}

				for(IDevice device: devices) {
					Log.v("uninstall apk on " + device.getSerialNumber());

					PackageInfo packageInfo = getPackageInfo(device, packageName);

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
