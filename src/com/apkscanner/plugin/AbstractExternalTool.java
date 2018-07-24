package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractExternalTool extends AbstractPlugIn implements IExternalTool
{
	public AbstractExternalTool(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public boolean isDecorderTool() {
		return "decorder".equals(component.like);
	}

	@Override
	public boolean isDiffTool() {
		return "difftool".equals(component.like);
	}

	@Override
	public boolean isNormalTool() {
		return component.like == null || (!isDecorderTool() && !isDiffTool());
	}
}
