package com.apkscanner.plugin;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractExtraComponent extends AbstractPlugIn implements IExtraComponent
{
	protected java.awt.Component tabbedComponent = null;

	public AbstractExtraComponent(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public java.awt.Component getComponent() {
		return tabbedComponent;
	}

	@Override
	public void initialize() {
		if(tabbedComponent != null) return; 
		tabbedComponent = new javax.swing.JPanel();
	}

	@Override
	public String getTitle() {
		return getLabel();
	}

	@Override
	public String getToolTip() {
		String tooltip = getDescription();
		if(tooltip == null) tooltip = getLabel();
		return tooltip;
	}

	@Override
	public Icon getIcon() {
		URL url = getIconURL();
		if(url == null) return null;
		return new ImageIcon(url);
	}
}
