package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPackageSearcher extends AbstractPlugIn implements IPackageSearcher
{
	public AbstractPackageSearcher(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public int getSupportType() {
		int type = 0;
		for(String s: component.target.split("\\|")) {
			if(s.toLowerCase().equals("package")) {
				type |= IPackageSearcher.SEARCHER_TYPE_PACKAGE_NAME;
			} else if(s.toLowerCase().equals("label")) {
				type |= IPackageSearcher.SEARCHER_TYPE_APP_NAME;
			}
		}
		return type;
	}

	@Override
	public String getPreferLangForAppName() {
		return component.preferLang;
	}
}
