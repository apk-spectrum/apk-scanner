package com.apkscanner.plugin;

import java.util.ArrayList;

import com.apkscanner.plugin.manifest.Component;

public class PlugInGroup extends AbstractPlugIn
{
	public PlugInGroup(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	public boolean isTopGroup() {
		String gname = component.pluginGroup;
		return gname == null || gname.trim().isEmpty();
	}

	public PlugInGroup[] getChildrenGroup() {
		if(getName() == null) return null;
		ArrayList<PlugInGroup> list = new ArrayList<>();
		for(PlugInGroup g: pluginPackage.getPlugInGroups()) {
			if(this.equals(g.getParantGroup())) {
				list.add(g);
			}
		}
		return list.toArray(new PlugInGroup[list.size()]);
	}

	public IPlugIn[] getPlugIn() {
		if(getName() == null) return null;
		ArrayList<IPlugIn> list = new ArrayList<>();
		for(IPlugIn g: pluginPackage.getPlugIn(PLUGIN_TPYE_ALL)) {
			if(this.equals(g.getParantGroup())) {
				list.add(g);
			}
		}
		return list.toArray(new IPlugIn[list.size()]);
	}
}
