package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractExternalTool extends AbstractPlugIn implements IExternalTool
{
	public AbstractExternalTool(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}
}
