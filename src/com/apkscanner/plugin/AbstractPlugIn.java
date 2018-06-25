package com.apkscanner.plugin;

import java.net.URL;

public abstract class AbstractPlugIn implements IPlugIn {
	/**
	 * Get the plug-in name
	 * 
	 * @return the name of plug-in
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Get the icon URL for plug-in 
	 * 
	 * @return the icon URL
	 */
	public URL getIconURL() {
		return null;
	}

	/**
	 * Get the plug-in type.
	 * 
	 * @return an type of plug-in
	 */
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
