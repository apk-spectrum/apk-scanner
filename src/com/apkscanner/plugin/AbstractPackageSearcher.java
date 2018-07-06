package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPackageSearcher extends AbstractPlugIn implements IPackageSearcher
{
	public AbstractPackageSearcher(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public String getPreferLangForAppName() {
		return null;
	}
}
