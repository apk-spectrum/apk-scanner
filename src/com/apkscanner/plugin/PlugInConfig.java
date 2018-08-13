package com.apkscanner.plugin;

import java.util.HashMap;

public class PlugInConfig
{
	public static final String CONFIG_NO_PROXIES = "noProxies";
	public static final String CONFIG_USE_GLOBAL_PROXIES = "useGlobalProxies";
	public static final String CONFIG_USE_SYSTEM_PROXIES = "useSystemProxies";
	public static final String CONFIG_HTTP_PROXY_HOST = "http.proxyHost";
	public static final String CONFIG_HTTP_PROXY_PORT = "http.proxyPort";
	public static final String CONFIG_HTTPS_PROXY_HOST = "https.proxyHost";
	public static final String CONFIG_HTTPS_PROXY_PORT = "https.proxyPort";

	static HashMap<String, String> configurations = new HashMap<>();

	public static String getGlobalConfiguration(String key) {
		return configurations.containsKey(key) ? configurations.get(key) : null;
	}

	public static String getGlobalConfiguration(String key, String defaultValue) {
		String value = getGlobalConfiguration(key);
		return value != null ? value : defaultValue;
	}

	public static void setGlobalConfiguration(String key, String value) {
		if(key == null) return;
		if(value == null) value = "";
		configurations.put(key, value);
	}

	public static String getConfiguration(PlugInPackage plugInPackage, String key) {
		if(plugInPackage == null) {
			return getGlobalConfiguration(key);
		} else {
			return plugInPackage.getConfiguration(key);
		}
	}

	public static String getConfiguration(PlugInPackage plugInPackage, String key, String defaultValue) {
		if(plugInPackage == null) {
			return getGlobalConfiguration(key, defaultValue);
		} else {
			return plugInPackage.getConfiguration(key, defaultValue);
		}
	}
}
