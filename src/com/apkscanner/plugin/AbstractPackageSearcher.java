package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPackageSearcher extends AbstractPlugIn implements IPackageSearcher {
	public AbstractPackageSearcher(String packageName, Component component) {
		super(packageName, component);
	}

	@Override
	public String getPreferLangForAppName() {
		return null;
	}
}
