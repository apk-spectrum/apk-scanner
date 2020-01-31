package com.apkspectrum.plugin.manifest;

public final class Manifest
{
	public final String packageName;
	public final String versionName;
	public final Integer versionCode;
	public final String minScannerVersion;

	public final PlugIn plugin;
	public final Resources[] resources;
	public final Configuration[] configuration;

	Manifest(String packageName, String versionName, int versionCode, String minScannerVersion, PlugIn plugin,
			Resources[] resources, Configuration[] configuration) {
		this.packageName = packageName;
		this.versionName = versionName;
		this.versionCode = versionCode;
		this.minScannerVersion = minScannerVersion;
		this.plugin = plugin;
		this.resources = resources;
		this.configuration = configuration;
	}
}
