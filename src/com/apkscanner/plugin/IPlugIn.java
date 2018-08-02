package com.apkscanner.plugin;

import java.net.URL;
import java.util.Map;

public interface IPlugIn {
	/**
	 * Check to latest version and update for APK Scanner. 
	 */
	public static final int PLUGIN_TPYE_UNKNOWN = 0;
	
	/**
	 * Check to latest version and update for APK Scanner. 
	 */
	public static final int PLUGIN_TPYE_UPDATE_CHECKER = 0x01;

	/**
	 * Search package information from app store or external service.
	 */
	public static final int PLUGIN_TPYE_PACKAGE_SEARCHER = 0x02;
	
	/**
	 * Launch extra tools. so possible add to toolbar. 
	 */
	public static final int PLUGIN_TPYE_EXTERNAL_TOOL = 0x04;

	/**
	 * Add extra component to tabs. 
	 */
	public static final int PLUGIN_TPYE_EXTRA_COMPONENT = 0x08;

	/**
	 * Launch extra tools. so possible add to toolbar. 
	 */
	public static final int PLUGIN_TPYE_ALL = 0x0F;
	
	/**
	 * Get the plug-in name
	 * 
	 * @return the name of plug-in
	 */
	public String getName();

	/**
	 * Get the icon URL for plug-in 
	 * 
	 * @return the icon URL
	 */
	public URL getIconURL();

	/**
	 * Get the plug-in type.
	 * 
	 * @return type of plug-in
	 */
	public int getType();

	public String getLabel();
	
	public String getDescription();
	
	public boolean isEnabled();

	public void setEnabled(boolean enable);

	public String getPackageName();

	public PlugInGroup getParantGroup();

	public String getGroupName();

	public String getActionCommand();
	
	public void launch();
	
	public Map<String, Object> getChangedProperties();

	public void restoreProperties(Map<?, ?> tmp);
}
