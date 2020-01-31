package com.apkspectrum.data.apkinfo;

public class CompactApkInfo {

	public String filePath = null;

	public String packageName = null; // "string"
	public String sharedUserId = null; // "string"
	public Integer versionCode = null; // "integer"
	public String versionName = null; // "string"

	public Integer minSdkVersion = null;
	public Integer targetSdkVersion = null;
	public Integer maxSdkVersion = null;

	public String installLocation = null; // ["auto" | "internalOnly" | "preferExternal"]

	public String[] libraries = null;
	public String[] certificates = null;

	public ComponentInfo[] activityList = null;

	public CompactApkInfo() { }

	public CompactApkInfo(ApkInfo info) {
		filePath = info.filePath;

		packageName = info.manifest.packageName;
		sharedUserId = info.manifest.sharedUserId;
		versionCode = info.manifest.versionCode;
		versionName = info.manifest.versionName;

		minSdkVersion = info.manifest.usesSdk.minSdkVersion;
		targetSdkVersion = info.manifest.usesSdk.targetSdkVersion;
		maxSdkVersion = info.manifest.usesSdk.maxSdkVersion;

		installLocation = info.manifest.installLocation;

		libraries = info.libraries;
		certificates = info.certificates;
		
		activityList = ApkInfoHelper.getLauncherActivityList(info, true);
	}
}
