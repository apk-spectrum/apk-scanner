package com.apkscanner.plugin;

import java.util.Map;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPackageSearcher extends AbstractPlugIn implements IPackageSearcher
{
	protected boolean visibleToBasic;

	public AbstractPackageSearcher(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
		visibleToBasic = component.visibleToBasic == null ? true : component.visibleToBasic;
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

	@Override
	public boolean isVisibleToBasic() {
		return visibleToBasic;
	}

	@Override
	public void setVisibleToBasic(boolean visible) {
		visibleToBasic = visible;
	}

	@Override
	public Map<String, Object> getChangedProperties() {
		Map<String, Object> data = super.getChangedProperties();
		if(component.visibleToBasic != isVisibleToBasic()) {
			data.put("visibleToBasic", isVisibleToBasic());
		}
		return data;
	}

	@Override
	public void restoreProperties(Map<?, ?> data) {
		super.restoreProperties(data);
		if(data == null) return;
		if(data.containsKey("visibleToBasic")) {
			setVisibleToBasic((boolean)data.get("visibleToBasic"));
		}
	}
}
