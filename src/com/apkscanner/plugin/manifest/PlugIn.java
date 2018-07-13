package com.apkscanner.plugin.manifest;

public class PlugIn
{
	public final boolean enable;
	public final String label;
	public final String icon;
	public final String description;

	public final Component[] components;

	PlugIn(boolean enable, String label, String icon, String description, Component[] components) {
		this.enable = enable;
		this.label = label;
		this.icon = icon;
		this.description = description;
		this.components = components;
	}
}
