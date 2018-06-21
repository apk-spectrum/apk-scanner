package com.apkscanner.plugin;

import java.net.URL;

public interface IPlugIn {
	/**
	 * Check to latest version and update for APK Scanner. 
	 */
	public static final int PLUGIN_TPYE_UPDATE_CHECKER = 0;

	/**
	 * Search package information from app store or external service.
	 */
	public static final int PLUGIN_TPYE_STORE_SEARCHER = 1;
	
	/**
	 * Launch extra tools. so possible add to toolbar. 
	 */
	public static final int PLUGIN_TPYE_EXTRA_TOOL = 2;

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
	 * @return an type of plug-in
	 */
	public int getType();
}
