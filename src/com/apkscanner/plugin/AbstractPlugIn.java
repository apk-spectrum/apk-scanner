package com.apkscanner.plugin;

import java.net.MalformedURLException;
import java.net.URL;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPlugIn implements IPlugIn
{
	protected String packageName;
	protected Component component;
	protected boolean enable;

	public AbstractPlugIn(String packageName, Component component) {
		this.packageName = packageName;
		this.component = component;
		this.enable = component.enable;
	}

	public String getPackageName() {
		return packageName;
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
		return component.label;
	}

	public String getDescription() {
		return component.description;
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
