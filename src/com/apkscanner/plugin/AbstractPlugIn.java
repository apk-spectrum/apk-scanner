package com.apkscanner.plugin;

import java.net.URL;

public abstract class AbstractPlugIn implements IPlugIn
{
	public String getPackageName() {
		return getClass().getPackage().getName();
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}

	public URL getIconURL() {
		return null;
	}

	public int getType() {
		if(this instanceof AbstractPackageSearcher) {
			return PLUGIN_TPYE_PACKAGE_SEARCHER; 
		}
		if(this instanceof AbstractUpdateChecker) {
			return PLUGIN_TPYE_UPDATE_CHECKER; 
		}
		if(this instanceof AbstractExternalTool) {
			return PLUGIN_TPYE_EXTRA_TOOL; 
		}
		return PLUGIN_TPYE_UNKNOWN;
	}
}
