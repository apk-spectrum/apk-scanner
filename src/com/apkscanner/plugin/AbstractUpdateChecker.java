package com.apkscanner.plugin;

import com.apkscanner.resource.Resource;

public abstract class AbstractUpdateChecker extends AbstractPlugIn implements IUpdateChecker {
	public AbstractUpdateChecker(String packageName, String pluginName) {
		super(packageName, pluginName);
	}
	
	public boolean checkNewVersion() {
		return checkNewVersion(Resource.STR_APP_VERSION.getString());
	}
}
