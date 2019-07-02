package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;

import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public class DeviceClearAppDataAction extends AbstractDeviceAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_CLEAR_APP_DATA";

	public DeviceClearAppDataAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtClearData(getWindow(e));
	}

	private void evtClearData(final Window owner) {
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
					Log.v("clear data on " + device.getSerialNumber());

					PackageInfo packageInfo = getPackageInfo(device, packagName);

					String errMessage = PackageManager.clearData(packageInfo);

					if(errMessage != null && !errMessage.isEmpty()) {
						final String errMsg = errMessage;
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								Log.e(errMsg);
								MessageBoxPool.show(owner, MessageBoxPool.MSG_FAILURE_CLEAR_DATA, errMsg);
							}
						});
					} else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								MessageBoxPool.show(owner, MessageBoxPool.MSG_SUCCESS_CLEAR_DATA);
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
