package com.apkscanner.plugin.manifest;

public class Component
{
	static public final int TYPE_UNKNWON = 0;
	static public final int TYPE_PACAKGE_SEARCHER = 1;
	static public final int TYPE_PACAKGE_SEARCHER_LINKER = 2;
	static public final int TYPE_UPDATE_CHECKER = 3;
	static public final int TYPE_UPDATE_CHECKER_LINKER = 4;
	static public final int TYPE_EXTERNAL_TOOL = 5;
	static public final int TYPE_EXTERNAL_TOOL_LINKER = 6;

	public final int type;
	public final String description;
	public final boolean enable;
	public final String icon;
	public final String label;
	public final String name;
	public final String url;

	//public final Linker[] linkers;
	public final String target;
	public final String preferLang;
	public final String path;
	public final String param;
	public final String updateUrl;

	Component(int type, boolean enable, String label, String icon, String description, String name, String url, /* Linker[] linkers */
			String target, String preferLang, String path, String param, String updateUrl) {
		this.type = type;
		this.enable = enable;
		this.label = label;
		this.icon = icon;
		this.description = description;
		this.name = name;
		this.url = url;

		//this.linkers = linkers;
		this.target = target;
		this.preferLang = preferLang;
		this.path = path;
		this.param = param;
		this.updateUrl = updateUrl;
	}
}
