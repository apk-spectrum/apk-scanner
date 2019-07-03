package com.apkscanner.plugin;

public interface IPackageSearcher extends IPlugIn
{
	public static final String VISIBLE_PROPERTY = "visibleToBasic";

	public static final int SEARCHER_TYPE_PACKAGE_NAME = 1;
	public static final int SEARCHER_TYPE_APP_NAME = 2;

	/**
	 * Get the type of search supported
	 *
	 * @return the bits for the type of search supported.
	 *         It's can be combined. SEARCHER_TYPE_*
	 */
	public int getSupportType();

	public boolean trySearch();

	public String getPreferLangForAppName();

	public boolean isVisibleToBasic();

	public void setVisibleToBasic(boolean visible);
}
