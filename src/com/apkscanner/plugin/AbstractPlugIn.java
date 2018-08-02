package com.apkscanner.plugin;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractPlugIn implements IPlugIn
{
	protected PlugInPackage pluginPackage;
	protected Component component;
	protected boolean enabled;

	public AbstractPlugIn(PlugInPackage pluginPackage, Component component) {
		this.pluginPackage = pluginPackage;
		this.component = component;
		this.enabled = component != null ? component.enabled : false;
	}

	@Override
	public String getPackageName() {
		return pluginPackage.getPackageName();
	}

	@Override
	public String getName() {
		String name = (component.name != null && !component.name.trim().isEmpty()) ? component.name : null;
		if(name != null && name.startsWith(".")) {
			name = getPackageName() + name;
		}
		return name;
	}

	@Override
	public String getGroupName() {
		String name = (component.pluginGroup != null && !component.pluginGroup.trim().isEmpty()) ? component.pluginGroup : null;
		if(name != null && name.startsWith(".")) {
			name = getPackageName() + name;
		}
		return name;
	}

	@Override
	public URL getIconURL() {
		if(component.icon != null) { 
			try {
				URI uri = pluginPackage.getResourceUri(component.icon); 
				return uri != null ? uri.toURL() : null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getLabel() {
		return pluginPackage.getResourceString(component.label);
	}

	@Override
	public String getDescription() {
		return pluginPackage.getResourceString(component.description);
	}

	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public int getType() {
		if(this instanceof IPackageSearcher) {
			return PLUGIN_TPYE_PACKAGE_SEARCHER; 
		}
		if(this instanceof IUpdateChecker) {
			return PLUGIN_TPYE_UPDATE_CHECKER; 
		}
		if(this instanceof IExternalTool) {
			return PLUGIN_TPYE_EXTERNAL_TOOL; 
		}
		if(this instanceof IExtraComponent) {
			return PLUGIN_TPYE_EXTRA_COMPONENT; 
		}
		return PLUGIN_TPYE_UNKNOWN;
	}

	@Override
	public PlugInGroup getParantGroup() {
		if(pluginPackage == null) return null;
		return pluginPackage.getPlugInGroup(component.pluginGroup);
	}

	@Override
	public String getActionCommand() {
		String typeName = null;
		switch(getType()) {
		case PLUGIN_TPYE_PACKAGE_SEARCHER:
			typeName = IPackageSearcher.class.getSimpleName() + "#" + ((IPackageSearcher)this).getSupportType();
			break;
		case PLUGIN_TPYE_UPDATE_CHECKER:
			typeName = IUpdateChecker.class.getSimpleName();
			break;
		case PLUGIN_TPYE_EXTERNAL_TOOL:
			typeName = IExternalTool.class.getSimpleName() + "#" + ((IExternalTool)this).getToolType();
			break;
		default:
			typeName = IPlugIn.class.getName();
			break;
		}
		return getPackageName() + "!" + typeName + "@" + getName() + "[0x" + Integer.toHexString(component.hashCode()) + "]";
	}

	@Override
	public void launch() { }

	@Override
	public Map<String, Object> getChangedProperties() {
		HashMap<String, Object> data = new HashMap<>();
		if(component.enabled != isEnabled()) {
			if(!(this instanceof IExternalTool) || ((IExternalTool)this).isSupoortedOS()) {
				data.put("enabled", isEnabled());
			}
		}
		return data;
	}

	@Override
	public void restoreProperties(Map<?, ?> data) {
		if(data == null) return;
		if(data.containsKey("enabled")) {
			setEnabled((boolean)data.get("enabled"));
		}
	}

	@Override
	public int hashCode() {
		return component.hashCode();
	}
}
