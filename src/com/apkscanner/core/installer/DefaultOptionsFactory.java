package com.apkscanner.core.installer;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.SimpleOutputReceiver;
import com.apkscanner.util.Log;

public class DefaultOptionsFactory {
	private CompactApkInfo apkInfo;
	private SignatureReport signatureReport;

	private boolean hasApkInfo;
	private boolean wasSigned;
	private int minSdkVersion;

	public DefaultOptionsFactory(CompactApkInfo apkInfo, SignatureReport signatureReport) {
		this.apkInfo = apkInfo;
		this.signatureReport = signatureReport;

		hasApkInfo = (apkInfo != null && apkInfo.packageName != null && !apkInfo.packageName.isEmpty());
		wasSigned = signatureReport != null || (apkInfo.certificates != null && apkInfo.certificates.length > 0);
		minSdkVersion = (apkInfo != null && apkInfo.minSdkVersion != null) ? apkInfo.minSdkVersion : 1;
	}

	public OptionsBundle createOptions(IDevice device) {
		OptionsBundle options = new OptionsBundle();
		int blockedFlags = 0;

		if(hasApkInfo) {
			if(!wasSigned) {
				blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL | OptionsBundle.FLAG_OPT_PUSH;
			} else if(device != null) {
				PackageInfo packageInfo = PackageManager.getPackageInfo(device, apkInfo.packageName);
				int apiLevel = 25;
				if(apiLevel < minSdkVersion) {
					blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL | OptionsBundle.FLAG_OPT_PUSH;
				} else if(signatureReport != null && packageInfo != null) {
					String signature = packageInfo.getSignature();
					if(signature != null) {
						if(!signatureReport.contains("RAWDATA", signature)) {
							blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL;
							if(!AdbDeviceHelper.isRoot(device)) {
								blockedFlags |= OptionsBundle.FLAG_OPT_PUSH;	
							}
						}
					}
					
					if(packageInfo.isSystemApp()) {
						
					}
				}
				
				if(packageInfo != null) {
					String apkPath = null;
					if(packageInfo.isSystemApp()) {
						apkPath = packageInfo.getApkPath();
					} else if(packageInfo.getHiddenSystemPackageValue("pkg") != null) {
						apkPath = packageInfo.getHiddenSystemPackageValue("codePath");
					}
					
					if(apkPath != null && !apkPath.endsWith(".apk")) {
						SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
						try {
							device.executeShellCommand("ls -l " + apkPath, outputReceiver);
						} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
							e.printStackTrace();
						}
						for(String line: outputReceiver.getOutput()) {
							if(line.isEmpty()) continue;
							String tmp = line.replaceAll(".*\\s(\\S*\\.apk)", "/$1");
							if(!line.equals(tmp)) {
								apkPath += tmp;
								break;
							}
						}
					}
					
					if(apkPath == null || apkPath.isEmpty()) {
						apkPath = "/system/app/" + packageInfo.packageName + "-1/base.apk";
					} else if(!apkPath.endsWith(".apk")) {
						if(!apkPath.startsWith("/system/app/") || !apkPath.startsWith("/system/priv-app/")) {
							Log.v("Invalid apk path : " + apkPath);
							apkPath = "/system/app/" + packageInfo.packageName + "-1/base.apk";
						}
						if(apkPath.endsWith("/")) {
							apkPath += "base.apk";
						}
					}
					options.systemPath = apkPath;
				}
			}

			if(apkInfo.activityList == null || apkInfo.activityList.length <= 0) {
				blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL_LAUNCH;
			} else {
				options.launchActivity = apkInfo.activityList[0].name;
			}
		}

		options.setBlockedFlags(blockedFlags);
		
		return options;
	}
}
