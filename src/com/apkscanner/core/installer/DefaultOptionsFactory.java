package com.apkscanner.core.installer;

import com.android.ddmlib.IDevice;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;

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
				int apiLevel = device.getApiLevel();
				if(apiLevel < minSdkVersion) {
					blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL | OptionsBundle.FLAG_OPT_PUSH;
				} else if(signatureReport != null) {
					PackageInfo packageInfo = PackageManager.getPackageInfo(device, apkInfo.packageName);
					if(packageInfo != null) {
						String signature = packageInfo.getSignature();
						if(signature != null) {
							if(!signatureReport.contains("RAWDATA", signature)) {
								blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL;
							}
						}
					}
				}
			}
		}

		options.setBlockedFlags(blockedFlags);
		return options;
	}
}
