package com.apkscanner.plugin;

import java.net.MalformedURLException;
import java.net.URL;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPlugIn implements IPlugIn
{
	protected PlugInPackage pluginPackage;
	protected Component component;
	protected boolean enable;

	public AbstractPlugIn(PlugInPackage pluginPackage, Component component) {
		this.pluginPackage = pluginPackage;
		this.component = component;
		this.enable = component != null ? component.enable : false;
	}

	public String getPackageName() {
		return pluginPackage.getPackageName();
	}

	public String getName() {
		return component.name;
	}

	public URL getIconURL() {
		try {
			return new URL(component.icon);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getLabel() {
		return pluginPackage.getResourceString(component.label);
	}

	public String getDescription() {
		return pluginPackage.getResourceString(component.description);
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isEnabled() {
		return enable;
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
