package com.apkspectrum.tool.adb;

public interface IPackageStateListener {
	public void packageInstalled(PackageInfo packageInfo);
	public void packageUninstalled(PackageInfo packageInfo);
	public void enableStateChanged(PackageInfo packageInfo);
}
