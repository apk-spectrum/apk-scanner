package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.resource.Resource;

public abstract class AbstractUpdateChecker extends AbstractPlugIn implements IUpdateChecker {
	public AbstractUpdateChecker(String packageName, Component component) {
		super(packageName, component);
	}
	
	public boolean checkNewVersion() {
		return checkNewVersion(Resource.STR_APP_VERSION.getString());
	}
}
