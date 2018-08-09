package com.apkscanner.plugin;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.text.MessageFormat;
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
		System.setProperty("java.net.useSystemProxies","true");
		System.setProperty("proxySet","true");
		System.setProperty("http.protocols", "TLSv1,TLSv1.1,TLSv1.2");

		boolean isSetSystemProxy = false;
		if(SystemUtil.checkJvmVersion("1.8")) {
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
			Log.w("Not supported that get system proxy setting on JVM 1.7");
		}

		if(!isSetSystemProxy) {
    		System.setProperty("http.proxyHost", pluginPackage.getConfiguration("http.proxyHost", ""));
    		System.setProperty("http.proxyPort", pluginPackage.getConfiguration("http.proxyPort", ""));
    		System.setProperty("https.proxyHost", pluginPackage.getConfiguration("https.proxyHost", ""));
    		System.setProperty("https.proxyPort", pluginPackage.getConfiguration("https.proxyPort", ""));
		}
	}
}
