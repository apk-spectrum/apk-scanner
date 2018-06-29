package com.apkscanner.plugin;

public abstract class AbstractPackageSearcher extends AbstractPlugIn implements IPackageSearcher {
	public AbstractPackageSearcher(String packageName, String pluginName) {
		super(packageName, pluginName);
	}

	@Override
	public String getPreferLangForAppName() {
		return null;
	}
}
