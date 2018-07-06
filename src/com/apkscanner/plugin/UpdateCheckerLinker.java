package com.apkscanner.plugin;

import java.awt.Desktop;
import java.net.URI;

import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.util.ApkScannerVersion;
import com.apkscanner.util.Log;

public class UpdateCheckerLinker extends AbstractUpdateChecker
{
	public UpdateCheckerLinker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public String getNewVersion() {
		if(component.url == null) return "0.0.0";
		return "2.3.6";
	}

	@Override
	public boolean checkNewVersion(String oldVersion) {
		String version = getNewVersion();
		if(version == null || version.trim().isEmpty()) {
			Log.w("No such new version");
			return false;
		}
		ApkScannerVersion newVer = ApkScannerVersion.parseFrom(version);
		ApkScannerVersion oldVer = ApkScannerVersion.parseFrom(oldVersion);
		return newVer.compareTo(oldVer) > 0;
	}

	@Override
	public void launch() {
		if(!checkNewVersion()) {
			Log.w("Current version is latest");
			return;
		}

		String url = component.updateUrl != null ? component.updateUrl : component.url;
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI(url));
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        }
	    }
	}

}
