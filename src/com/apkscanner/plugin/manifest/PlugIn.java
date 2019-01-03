package com.apkscanner.plugin.manifest;

public class PlugIn
{
	public final boolean enabled;
	public final String label;
	public final String icon;
	public final String description;
	public final boolean useNetworkSetting;
	public final boolean useConfigurationSetting;

	public final Component[] components;

	PlugIn(boolean enable, String label, String icon, String description, Component[] components, boolean useNetworkSetting, boolean useConfigurationSetting) {
		this.enabled = enable;
		this.label = label;
		this.icon = icon;
		this.description = description;
		this.components = components;
		this.useNetworkSetting = useNetworkSetting;
		this.useConfigurationSetting = useConfigurationSetting;
	}
}
