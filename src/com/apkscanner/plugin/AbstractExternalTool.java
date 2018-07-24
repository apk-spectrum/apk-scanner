package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractExternalTool extends AbstractPlugIn implements IExternalTool
{
	public AbstractExternalTool(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public String getToolType() {
		if(isNormalTool()) return TYPE_NORMAL_TOOL;
		return component.like;
	}

	@Override
	public boolean isDecorderTool() {
		return TYPE_DECORDER_TOOL.equals(component.like);
	}

	@Override
	public boolean isDiffTool() {
		return TYPE_DIFF_TOOL.equals(component.like);
	}

	@Override
	public boolean isNormalTool() {
		return component.like == null || (!isDecorderTool() && !isDiffTool());
	}
}
