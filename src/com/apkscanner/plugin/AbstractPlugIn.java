package com.apkscanner.plugin;

import java.net.URL;

public abstract class AbstractPlugIn implements IPlugIn
{
	private String packageName;
	private String pluginName;

	public AbstractPlugIn(String packageName, String pluginName) {
		this.packageName = packageName;
		this.pluginName = pluginName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getName() {
		return pluginName;
	}

	public URL getIconURL() {
		return null;
	}

	public String getLabel() {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public int getType() {
		if(this instanceof IPackageSearcher) {
			return PLUGIN_TPYE_PACKAGE_SEARCHER; 
		}
		if(this instanceof IUpdateChecker) {
			return PLUGIN_TPYE_UPDATE_CHECKER; 
		}
		if(this instanceof IExternalTool) {
			return PLUGIN_TPYE_EXTRA_TOOL; 
		}
		return PLUGIN_TPYE_UNKNOWN;
	}
}
