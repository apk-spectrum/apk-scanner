package com.apkscanner.plugin;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector.CacheScope;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

public class NetworkSetting {
	static {
		Logger.setBackend(new LogBackEnd() {
			@Override
			public void log(Class<?> clzz, LogLevel level, String msg, Object... params) {
				switch(level) {
				case DEBUG: Log.d(clzz.getSimpleName(), MessageFormat.format(msg, params)); break;
				case ERROR: Log.e(clzz.getSimpleName(), MessageFormat.format(msg, params)); break;
				case INFO: Log.i(clzz.getSimpleName(), MessageFormat.format(msg, params)); break;
				case WARNING: Log.w(clzz.getSimpleName(), MessageFormat.format(msg, params)); break;
				case TRACE: Log.v(clzz.getSimpleName(), MessageFormat.format(msg, params)); break;
				}
				if(params != null && params.length > 0 && params[0] instanceof Exception) {
					((Exception)params[0]).printStackTrace();
				}
		}});
	}

	public static void setProxyServer(PlugInPackage pluginPackage, URI uri) {
		boolean useGlobalConfig = "true".equals(pluginPackage.getConfiguration(PlugInConfig.CONFIG_USE_GLOBAL_PROXIES, "true"));
		if(useGlobalConfig) pluginPackage = null; 

		boolean useSystemProxy = "true".equals(PlugInConfig.getConfiguration(pluginPackage,
												PlugInConfig.CONFIG_USE_SYSTEM_PROXIES,
												useGlobalConfig ? "true" : "false"));

		boolean isSetSystemProxy = false;
		if(useSystemProxy && SystemUtil.checkJvmVersion("1.8")) {
			System.setProperty("java.net.useSystemProxies", "true");
            // Use proxy vole to find the default proxy
            ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
            proxySearch.setPacCacheSettings(20, 1000*60*10, CacheScope.CACHE_SCOPE_URL);
            ProxySelector proxySelector = proxySearch.getProxySelector();
            List<Proxy> l = proxySelector.select(uri);

            //... Now just do what the original did ...
            for (Proxy proxy: l) {
                Log.v("proxy hostname : " + proxy.type());
                InetSocketAddress addr = (InetSocketAddress) proxy.address();

                if(addr != null) {
                	isSetSystemProxy = true;
                	Log.v("proxy hostname : " + addr.getHostName());
                	Log.v("proxy port : " + addr.getPort());
                	System.setProperty("http.proxyHost", addr.getHostName());
                	System.setProperty("http.proxyPort", Integer.toString(addr.getPort()));
                	System.setProperty("https.proxyHost", addr.getHostName());
                	System.setProperty("https.proxyPort", Integer.toString(addr.getPort()));
                } else {
                	Log.v("No Proxy");
                }
            }
		} else {
			if(useSystemProxy) Log.w("Can't supported that get system proxy setting under on JVM 1.7 or earlier");
		}

		if(!isSetSystemProxy) {
			boolean noProxy = "true".equals(PlugInConfig.getConfiguration(pluginPackage, PlugInConfig.CONFIG_NO_PROXIES, "false"));
			if(!noProxy) {
	    		System.setProperty("http.proxyHost", PlugInConfig.getConfiguration(pluginPackage, PlugInConfig.CONFIG_HTTP_PROXY_HOST, ""));
	    		System.setProperty("http.proxyPort", PlugInConfig.getConfiguration(pluginPackage, PlugInConfig.CONFIG_HTTP_PROXY_PORT, ""));
	    		System.setProperty("https.proxyHost", PlugInConfig.getConfiguration(pluginPackage, PlugInConfig.CONFIG_HTTPS_PROXY_HOST, ""));
	    		System.setProperty("https.proxyPort", PlugInConfig.getConfiguration(pluginPackage, PlugInConfig.CONFIG_HTTPS_PROXY_PORT, ""));
			} else {
            	Log.v("No Proxy");
			}
		}
	}

	public static boolean isEnabledNetworkInterface() {
		try {
			// https://stackoverflow.com/questions/1402005/how-to-check-if-internet-connection-is-present-in-java
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
			    NetworkInterface interf = interfaces.nextElement();
			    if (interf.isUp() && !interf.isLoopback()) {
				    List<InterfaceAddress> adrs = interf.getInterfaceAddresses();
				    for (Iterator<InterfaceAddress> iter = adrs.iterator(); iter.hasNext();) {
				        InterfaceAddress adr = iter.next();
				        InetAddress inadr = adr.getAddress();
				        if (inadr instanceof Inet4Address) return true;
				    }
			    }
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}
}
