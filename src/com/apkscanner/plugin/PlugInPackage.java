package com.apkscanner.plugin;

import java.util.ArrayList;

import com.apkscanner.annotations.NonNull;
import com.apkscanner.plugin.manifest.Manifest;

public class PlugInPackage
{
	private Manifest manifest;
	private ArrayList<IPlugIn> plugins = new ArrayList<IPlugIn>();

	public PlugInPackage(@NonNull Manifest manifest) {
		this.manifest = manifest;
	}

	public String getPackageName() {
		return manifest.packageName;
	}

	public int getVersionCode() {
		return manifest.versionCode;
	}

	public String getVersionName() {
		return manifest.versionName;
	}

	public String getMinScannerName() {
		return manifest.minScannerVersion;
	}

	public boolean hasPlugIn(int type) {
		for(IPlugIn p: plugins) {
			if((p.getType() & type) == type) return true;
		}
		return false;
	}
}
