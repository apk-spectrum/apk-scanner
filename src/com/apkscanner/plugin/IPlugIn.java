package com.apkscanner.plugin;

import java.net.URL;

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
	public static final int PLUGIN_TPYE_EXTRA_TOOL = 0x04;

	/**
	 * Launch extra tools. so possible add to toolbar. 
	 */
	public static final int PLUGIN_TPYE_ALL = 0x07;
	
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
	
	public void setEnable(boolean enable);
	
	public String getPackageName();
}
