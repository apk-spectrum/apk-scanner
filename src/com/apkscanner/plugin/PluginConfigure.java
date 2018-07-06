package com.apkscanner.plugin;

public class PluginConfigure {
	private static String lang = "";

	public static void setLang(String newLang) {
		lang = newLang != null ? newLang.trim() : "";
	}

	public static String getLang() {
		return lang != null ? lang : "";
	}
}
