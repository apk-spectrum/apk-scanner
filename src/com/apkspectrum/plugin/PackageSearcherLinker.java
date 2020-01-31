package com.apkspectrum.plugin;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.plugin.manifest.Component;

public class PackageSearcherLinker extends AbstractPackageSearcher
{
	public PackageSearcherLinker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public boolean trySearch() {
		return true;
	}

	@Override
	public void launch() {
		String name = null;
		switch(getSupportType()) {
		case SEARCHER_TYPE_PACKAGE_NAME:
			name = PlugInManager.getApkInfo().manifest.packageName; 
			break;
		case SEARCHER_TYPE_APP_NAME:
			name = ApkInfoHelper.getResourceValue(PlugInManager.getApkInfo().manifest.application.labels, getPreferLangForAppName());
			break;
		}

		String filter = null;
		try {
			filter = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			filter = name;
		}
		String url = component.url.replaceAll("%[tT][aA][rR][gG][eE][tT]%", filter);
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
