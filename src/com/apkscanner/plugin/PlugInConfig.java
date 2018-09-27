package com.apkscanner.plugin;

import java.util.HashMap;

public class PlugInConfig
{
	public static final String CONFIG_NO_PROXIES = "noProxies";
	public static final String CONFIG_USE_GLOBAL_PROXIES = "useGlobalProxies";
	public static final String CONFIG_USE_SYSTEM_PROXIES = "useSystemProxies";
	public static final String CONFIG_USE_PAC_PROXIES = "usePacProxies";
	public static final String CONFIG_PAC_URL = "pacUrl";

	public static final String CONFIG_HTTP_PROXY_HOST = "http.proxyHost";
	public static final String CONFIG_HTTP_PROXY_PORT = "http.proxyPort";
	public static final String CONFIG_HTTP_PROXY_USER = "http.proxyUser";
	public static final String CONFIG_HTTP_PROXY_PASS = "http.proxyPassword";
	public static final String CONFIG_HTTPS_PROXY_HOST = "https.proxyHost";
	public static final String CONFIG_HTTPS_PROXY_PORT = "https.proxyPort";
	public static final String CONFIG_HTTPS_PROXY_USER = "https.proxyUser";
	public static final String CONFIG_HTTPS_PROXY_PASS = "https.proxyPassword";
	public static final String CONFIG_HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

	public static final String[] CONFIG_PROXY_PROPERTIES = {
			CONFIG_HTTP_PROXY_HOST,
			CONFIG_HTTP_PROXY_PORT,
			CONFIG_HTTP_PROXY_USER,
			CONFIG_HTTP_PROXY_PASS,
			CONFIG_HTTPS_PROXY_HOST,
			CONFIG_HTTPS_PROXY_PORT,
			CONFIG_HTTPS_PROXY_USER,
			CONFIG_HTTPS_PROXY_PASS,
			CONFIG_HTTP_NON_PROXY_HOSTS
	};

	// Ignoring certificate errors opens the connection to potential MITM attacks.
	public static final String CONFIG_IGNORE_SSL_CERT = "ignoreSSLCert";

	public static final String CONFIG_SSL_TRUSTSTORE = "javax.net.ssl.trustStore";
	public static final String CONFIG_SSL_TRUSTSTORE_PWD = "javax.net.ssl.trustStorePassword";

	public static final String CONFIG_IGNORE_NETWORK_ERR_NO_SUCHE_INTERFACE = "ingnore.err-no-such-interface";
	public static final String CONFIG_IGNORE_NETWORK_ERR_CONNECTION_TIMEOUT = "ingnore.err-connection-time-out";
	public static final String CONFIG_IGNORE_NETWORK_ERR_SSL_HANDSHAKE = "ingnore.err-ssl-handshake";

	static HashMap<String, String> configurations = new HashMap<>();

	private PlugInPackage plugInPackage;
	private boolean allowGlobalConfig;

	public PlugInConfig(PlugInPackage plugInPackage) {
		this(plugInPackage, false);
	}

	public PlugInConfig(PlugInPackage plugInPackage, boolean allowGlobalConfig) {
		this.plugInPackage = plugInPackage;
		this.allowGlobalConfig = allowGlobalConfig;
	}
	
	public void setPlugInPackage(PlugInPackage plugInPackage) {
		this.plugInPackage = plugInPackage;
	}

	public void setPlugInPackage(boolean allowGlobalConfig) {
		this.allowGlobalConfig = allowGlobalConfig;
	}

	public static String getGlobalConfiguration(String key) {
		return configurations.containsKey(key) ? configurations.get(key) : null;
	}

	public static String getGlobalConfiguration(String key, String defaultValue) {
		String value = getGlobalConfiguration(key);
		return value != null ? value : defaultValue;
	}

	public static void setGlobalConfiguration(String key, String value) {
		if(key == null) return;
		if(value != null) {
			configurations.put(key, value);
		} else if(configurations.containsKey(key)) {
			configurations.remove(key);
		}
	}

	public static void clearGlobalConfiguration(String key) {
		if(key == null) return;
		configurations.remove(key);
	}

	public String getConfiguration(String key) {
		String value = getConfiguration(plugInPackage, key);
		if(value == null && plugInPackage != null && allowGlobalConfig) {
			value = getConfiguration((PlugInPackage)null, key);
		}
		return value; 
	}

	public String getConfiguration(String key, String defaultValue) {
		String value = getConfiguration(plugInPackage, key, defaultValue);
		if(value == null && plugInPackage != null && allowGlobalConfig) {
			value = getConfiguration((PlugInPackage)null, key, defaultValue);
		}
		return value; 
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

	public void setConfiguration(String key, String value) {
		setConfiguration(plugInPackage, key, value);
	}

	public static void setConfiguration(PlugInPackage plugInPackage, String key, String value) {
		if(plugInPackage == null) {
			setGlobalConfiguration(key, value);
		} else {
			plugInPackage.setConfiguration(key, value);
		}
	}

	public void clearConfiguration(String key) {
		clearConfiguration(plugInPackage, key);
	}

	public static void clearConfiguration(PlugInPackage plugInPackage, String key) {
		if(plugInPackage == null) {
			clearGlobalConfiguration(key);
		} else {
			plugInPackage.clearConfiguration(key);
		}
	}
}
