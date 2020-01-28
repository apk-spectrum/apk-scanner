package com.apkspectrum.plugin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.apkspectrum.plugin.manifest.Component;

public abstract class AbstractPlugIn implements IPlugIn
{
	protected PlugInPackage pluginPackage;
	protected Component component;
	protected boolean enabled;

	private PropertyChangeSupport pcs;

	public AbstractPlugIn(PlugInPackage pluginPackage, Component component) {
		this.pluginPackage = pluginPackage;
		this.component = component;
		this.enabled = component != null ? component.enabled : false;
	}

	@Override
	public PlugInPackage getPlugInPackage() {
		return pluginPackage;
	}

	@Override
	public PlugInConfig getPlugInConfig() {
		return new PlugInConfig(pluginPackage);
	}

	@Override
	public String getPackageName() {
		return pluginPackage.getPackageName();
	}

	@Override
	public String getName() {
		String name = (component.name != null && !component.name.trim().isEmpty()) ? component.name.trim() : null;
		if(name != null && name.startsWith(".")) {
			name = getPackageName() + name;
		}
		return name;
	}

	@Override
	public String getGroupName() {
		String name = (component.pluginGroup != null && !component.pluginGroup.trim().isEmpty()) ? component.pluginGroup.trim() : null;
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
				return uri != null ? uri.toURL() : pluginPackage.getIconURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getLabel() {
		String label = pluginPackage.getResourceString(component.label);
		return label != null ? label : pluginPackage.getLabel();
	}

	@Override
	public String getDescription() {
		String desc = pluginPackage.getResourceString(component.description);
		return desc != null ? desc : pluginPackage.getDescription();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if(this.enabled == enabled) return;
		this.enabled = enabled;
		firePropertyChange(ENABLED_PROPERTY, !enabled, enabled);
	}

	@Override
	public boolean isEnabled() {
		return isEnabled(true);
	}

	@Override
	public boolean isEnabled(boolean inheritance) {
		boolean enabled = this.enabled;
		if(enabled && inheritance) {
			enabled = pluginPackage.isEnabled();
			if(enabled) {
				PlugInGroup parent = getParantGroup();
				enabled = (parent == null) || parent.isEnabled();
			}
		}
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
		if(component.enabled != isEnabled(false)) {
			if(!(this instanceof IExternalTool) || ((IExternalTool)this).isSupoortedOS()) {
				data.put("enabled", isEnabled(false));
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

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if(pcs == null) return;
		pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

	@Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    	if(listener == null) return;
    	if(pcs == null) pcs = new PropertyChangeSupport(this);
    	else pcs.removePropertyChangeListener(listener);
    	pcs.addPropertyChangeListener(listener);
    }

	@Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    	if(pcs == null) return;
    	pcs.removePropertyChangeListener(listener);
    }

	@Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
    	if(prop == null || listener == null) return;
    	if(pcs == null) pcs = new PropertyChangeSupport(this);
    	pcs.addPropertyChangeListener(prop, listener);
    }

	@Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
    	if(pcs == null) return;
    	pcs.removePropertyChangeListener(prop, listener);
    	if(pcs.getPropertyChangeListeners().length == 0) pcs = null;
    }
}
