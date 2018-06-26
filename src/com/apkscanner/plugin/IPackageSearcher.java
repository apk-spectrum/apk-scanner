package com.apkscanner.plugin;

import java.awt.Event;

public interface IPackageSearcher extends IPlugIn {
	public static final int SEARCHER_TYPE_PACKAGE_NAME = 0x01;
	public static final int SEARCHER_TYPE_APP_NAME = 0x02;

	/**
	 * Get the type of search supported
	 * 
	 * @return the bits for the type of search supported.
	 *         It's can be combined. SEARCHER_TYPE_* 
	 */
	public int getSupportType();

	public boolean trySearch(int type, String name);
	
	public void launch(Event event, int type, String name);
	
	public String getPreferLangForAppName();
}
