package com.apkscanner.plugin;

public class PluginConfiguration {
	private static String lang = "";

	public static void setLang(String newLang) {
		lang = newLang != null ? newLang.trim() : "";
	}

	public static String getLang() {
		return lang != null ? lang : "";
	}

	public static String getConfiguration(String key) {
		return null;
	}
}
