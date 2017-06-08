package com.apkscanner.core.installer;

import java.io.IOException;
import java.util.ArrayList;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.ApkInfo;
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

	private ArrayList<String> archList;

	public DefaultOptionsFactory(CompactApkInfo apkInfo, SignatureReport signatureReport) {
		this.apkInfo = apkInfo;
		this.signatureReport = signatureReport;

		hasApkInfo = (apkInfo != null && apkInfo.packageName != null && !apkInfo.packageName.isEmpty());
		wasSigned = signatureReport != null || (apkInfo != null && apkInfo.certificates != null && apkInfo.certificates.length > 0);
		minSdkVersion = (apkInfo != null && apkInfo.minSdkVersion != null) ? apkInfo.minSdkVersion : 1;

		archList = new ArrayList<String>();
		for(String lib: apkInfo.libraries) {
			if(!lib.startsWith("lib/")) {
				Log.v("Unknown lib path : " + lib);
				continue;
			}
			String arch = lib.replaceAll("lib/([^/]*)/.*", "$1");
			if(!archList.contains(arch)) {
				archList.add(arch);
			}
		}
	}

	public OptionsBundle createOptions(IDevice device) {
		OptionsBundle options = new OptionsBundle();
		int blockedFlags = 0;
		int blockedCause = 0;

		if(hasApkInfo) {
			if(!wasSigned) {
				blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL | OptionsBundle.FLAG_OPT_PUSH;
				blockedCause |= OptionsBundle.BLOACKED_COMMON_CAUSE_UNSIGNED;
			} else {
				if(device != null) {
					Log.v("create options for " + device.getName());
					PackageInfo packageInfo = PackageManager.getPackageInfo(device, apkInfo.packageName);
					int apiLevel = device.getApiLevel();
					if(apiLevel < minSdkVersion) {
						blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL | OptionsBundle.FLAG_OPT_PUSH;
						blockedCause |= OptionsBundle.BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL;
					} else if(signatureReport != null && packageInfo != null) {
						String signature = packageInfo.getSignature();
						if(signature != null) {
							if(!signatureReport.contains("RAWDATA", signature)) {
								blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL;
								blockedCause |= OptionsBundle.BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED;
								if(!packageInfo.isSystemApp()) {
									blockedFlags |= OptionsBundle.FLAG_OPT_PUSH;
									blockedCause |= OptionsBundle.BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM;
								}
							}
						}
					}

					if(!AdbDeviceHelper.isRoot(device)) {
						blockedFlags |= OptionsBundle.FLAG_OPT_PUSH;
						blockedCause |= OptionsBundle.BLOACKED_PUSH_CAUSE_NO_ROOT;
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

						if(apkPath != null && apkPath.endsWith(".apk")) {
							options.installedPath = apkPath;
							if(apkPath.startsWith("/system/")) {
								options.systemPath = apkPath;
							}
						}
					}

					if(options.systemPath == null) {
						SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
						try {
							device.executeShellCommand("ls /system/app/*/*.apk; ls /system/app/*.apk", outputReceiver);
						} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
							e.printStackTrace();
						}
						String systemPathSample = null; 
						for(String line: outputReceiver.getOutput()) {
							if(line.isEmpty() || !line.endsWith(".apk")) continue;
							systemPathSample = line;
							break;
						}
						String makeName = apkInfo.packageName + "-1";  
						if(systemPathSample != null) {
							if(systemPathSample.matches("/system/app/[^/]*/[^/]*\\.apk")) {
								options.systemPath = "/system/app/" + makeName + "/" + makeName + ".apk"; 
							} else if(systemPathSample.matches("/system/app/[^/]*\\.apk")) {
								options.systemPath = "/system/app/" + makeName + ".apk";
							} else {
								Log.w("Unknown system path type : " + systemPathSample);
							}
						} else {
							Log.w("Unknown system path type : " + systemPathSample);
						}
						if(options.systemPath == null) {
							options.systemPath = "/system/app/" + makeName + "/" + makeName + ".apk";
						}
					}

					if(options.systemPath.startsWith("/system/app/")) {
						options.set(OptionsBundle.FLAG_OPT_PUSH_SYSTEM);
					} else if(options.systemPath.startsWith("/system/priv-app/")) {
						options.set(OptionsBundle.FLAG_OPT_PUSH_PRIVAPP);
					} else {
						options.set(OptionsBundle.FLAG_OPT_PUSH_SYSTEM);
						Log.w("Unknown path : " + options.systemPath);
					}

					if(archList != null && !archList.isEmpty()) {
						StringBuilder deviceAbiList = new StringBuilder(); 
						deviceAbiList.append(device.getProperty("ro.product.cpu.abi")).append(",");
						deviceAbiList.append(device.getProperty("ro.product.cpu.abi2")).append(",");
						deviceAbiList.append(device.getProperty("ro.product.cpu.abilist32")).append(",");
						deviceAbiList.append(device.getProperty("ro.product.cpu.abilist64")).append(",");
						deviceAbiList.append(device.getProperty("ro.product.cpu.abilist"));
						Log.v("deviceAbiList:" + deviceAbiList.toString());

						String abi32 = null;
						String abi64 = null;
						for(String abi: deviceAbiList.toString().split(",")) {
							if(abi.isEmpty() || abi.equalsIgnoreCase("null")) continue;
							if(abi32 != null && abi64 != null) break;
							if(abi.contains("64")) {
								if(archList.contains(abi)) {
									if(abi64 == null) {
										abi64 = abi;
										continue;
									}
								}
							} else {
								if(archList.contains(abi)) {
									if(abi32 == null) {
										abi32 = abi;
										continue;
									}
								}
							}
						}
						if(abi32 != null) {
							Log.e("FLAG_OPT_PUSH_LIB32 " + abi32);
							options.set(OptionsBundle.FLAG_OPT_PUSH_LIB32, abi32, "");
						}
						if(abi64 != null) {
							Log.e("FLAG_OPT_PUSH_LIB64 " + abi64);
							options.set(OptionsBundle.FLAG_OPT_PUSH_LIB64, abi64, "");
						}
					}
				}

				if(apkInfo.activityList == null || apkInfo.activityList.length <= 0) {
					blockedFlags |= OptionsBundle.FLAG_OPT_INSTALL_LAUNCH;
				} else {
					int activityFlag = apkInfo.activityList[0].featureFlag;
					if((activityFlag & ApkInfo.APP_FEATURE_LAUNCHER) != ApkInfo.APP_FEATURE_LAUNCHER) {
						options.unset(OptionsBundle.FLAG_OPT_INSTALL_LAUNCH);
					}
				}
				/*
				int activityOpt = Resource.PROP_LAUNCH_ACTIVITY_OPTION.getInt();
				if(activityOpt == Resource.INT_LAUNCH_ONLY_LAUNCHER_ACTIVITY 
						&& (activityFlag & ApkInfo.APP_FEATURE_LAUNCHER) == ApkInfo.APP_FEATURE_LAUNCHER) {
					options.set(OptionsBundle.FLAG_OPT_INSTALL_LAUNCH, apkInfo.activityList[0].name);
				} else if(activityOpt == Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY
						&& (activityFlag & (ApkInfo.APP_FEATURE_LAUNCHER | ApkInfo.APP_FEATURE_MAIN)) != 0) {
					options.set(OptionsBundle.FLAG_OPT_INSTALL_LAUNCH, apkInfo.activityList[0].name);
				} else {
					options.unset(OptionsBundle.FLAG_OPT_INSTALL_LAUNCH);
				}
				 */
			}
		}

		options.setBlockedFlags(blockedFlags, blockedCause);

		return options;
	}
}
