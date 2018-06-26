package com.apkscanner.plugin.manifest;

public class PlugIn
{
	public final boolean enable;
	public final String label;
	public final String icon;
	public final String description;
	public final int size;

	public final Component[] components;

	PlugIn(boolean enable, String label, String icon, String description, int size, Component[] components) {
		this.enable = enable;
		this.label = label;
		this.icon = icon;
		this.description = description;
		this.size = size;
		this.components = components;
	}
}
