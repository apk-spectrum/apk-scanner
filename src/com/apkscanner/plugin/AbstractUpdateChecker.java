package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractUpdateChecker extends AbstractPlugIn implements IUpdateChecker
{
	public AbstractUpdateChecker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}
}
