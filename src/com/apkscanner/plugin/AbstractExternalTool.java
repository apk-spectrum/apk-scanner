package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractExternalTool extends AbstractPlugIn implements IExternalTool {

	public AbstractExternalTool(String packageName, Component component) {
		super(packageName, component);
	}

}
