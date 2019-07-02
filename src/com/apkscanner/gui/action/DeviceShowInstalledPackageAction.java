package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;

import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public class DeviceShowInstalledPackageAction extends AbstractDeviceAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_INSTALLED_PACKAGE_INFO";

	public DeviceShowInstalledPackageAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtShowInstalledPackageInfo(getWindow(e));
	}

	private void evtShowInstalledPackageInfo(final Window owner) {
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
					final PackageInfo info = getPackageInfo(device, packagName);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							PackageInfoPanel packageInfoPanel = new PackageInfoPanel();
							packageInfoPanel.setPackageInfo(info);
							packageInfoPanel.showDialog(owner);
						}
					});
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}
}
