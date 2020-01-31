package com.apkspectrum.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.apkspectrum.resource._RFile;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;
import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector.CacheScope;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyUtil;

public class NetworkSetting
{
	public static final String DEFAULT_TRUSTSTORE_PASSWORD = "changeit";

	public static final String APK_SCANNER_SSL_TRUSTSTORE = "%APK_SCANNER_SSL_TRUSTSTORE%";
	public static final String JVM_SSL_TRUSTSTORE = "%JVM_SSL_TRUSTSTORE%";

	// Ignoring certificate errors opens the connection to potential MITM attacks.
	public static final String IGNORE_TRUSTSTORE = "%IGNORE_TRUSTSTORE%";

	private static boolean isIgnoreTrustStore = false;
	private static String trustStore;

	private PlugInPackage pluginPackage;
	private boolean isSetTruststore;

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

	public NetworkSetting(PlugInPackage pluginPackage) {
		this.pluginPackage = pluginPackage;
	}

	public void setProxyServer(URI uri) {
		setProxyServer(pluginPackage, uri);
	}

	public static void setProxyServer(PlugInPackage pluginPackage, URI uri) {
		PlugInConfig config = new PlugInConfig(pluginPackage);

		boolean useGlobalConfig = "true".equals(config.getConfiguration(PlugInConfig.CONFIG_USE_GLOBAL_PROXIES, "true"));
		if(useGlobalConfig) config.setPlugInPackage(null);

		boolean useSystemProxy = "true".equals(config.getConfiguration(PlugInConfig.CONFIG_USE_SYSTEM_PROXIES,
												useGlobalConfig ? "true" : "false"));
		boolean usePacProxy = "true".equals(config.getConfiguration(PlugInConfig.CONFIG_USE_PAC_PROXIES, "false"));

		boolean wasSetProxy = false;
		if((useSystemProxy || usePacProxy) && SystemUtil.checkJvmVersion("1.8")) {
			ProxySelector proxySelector = null;
			List<Proxy> l = null;
			if(useSystemProxy) {
				System.setProperty("java.net.useSystemProxies", "true");
	            // Use proxy vole to find the default proxy
	            ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
	            proxySearch.setPacCacheSettings(20, 1000*60*10, CacheScope.CACHE_SCOPE_URL);
	            proxySelector = proxySearch.getProxySelector();
	            if(proxySelector != null) {
	            	l = proxySelector.select(uri);
	            }
			} else {
    			String pacUrl = config.getConfiguration(PlugInConfig.CONFIG_PAC_URL, "");
    			if (pacUrl.startsWith("file://") && !pacUrl.startsWith("file:///")) {
    				pacUrl = "file:///" + pacUrl.substring(7);
    			}
    			proxySelector = ProxyUtil.buildPacSelectorForUrl(pacUrl);
    			l = proxySelector.select(uri);
    		}

            //... Now just do what the original did ...
            if(l != null) for (Proxy proxy: l) {
                Log.v("proxy hostname : " + proxy.type());
                InetSocketAddress addr = (InetSocketAddress) proxy.address();

                if(addr != null) {
                	wasSetProxy = true;
                	Log.v("proxy hostname : " + addr.getHostName());
                	Log.v("proxy port : " + addr.getPort());
                	Log.v("scheme " + uri.getScheme());
                	System.setProperty(uri.getScheme() + ".proxyHost", addr.getHostName());
                	System.setProperty(uri.getScheme() + ".proxyPort", Integer.toString(addr.getPort()));
                } else {
                	Log.v("No Proxy");
                }
            }
		} else {
			if(useSystemProxy || usePacProxy) Log.w("Can't supported that get system proxy setting under on JVM 1.7 or earlier");
		}

		if(!wasSetProxy) {
			boolean noProxy = "true".equals(config.getConfiguration(PlugInConfig.CONFIG_NO_PROXIES, "false"));
			if(!noProxy) {
				for(String proerty: PlugInConfig.CONFIG_PROXY_PROPERTIES) {
					String val = config.getConfiguration(proerty);
					if(val != null) System.setProperty(proerty, val);
					else System.clearProperty(proerty);
				}
				// Java ignores http.proxyUser. Here come's the workaround.
				Authenticator.setDefault(new Authenticator() {
				    @Override
				    protected PasswordAuthentication getPasswordAuthentication() {
				        if (getRequestorType() == RequestorType.PROXY) {
				            String prot = getRequestingProtocol().toLowerCase();
				            String host = System.getProperty(prot + ".proxyHost", "");
				            String port = System.getProperty(prot + ".proxyPort", "80");
				            String user = System.getProperty(prot + ".proxyUser", "");
				            String password = System.getProperty(prot + ".proxyPassword", "");

				            if (getRequestingHost().equalsIgnoreCase(host)) {
				                if (Integer.parseInt(port) == getRequestingPort()) {
				                    // Seems to be OK.
				                    return new PasswordAuthentication(user, password.toCharArray());  
				                }
				            }
				        }
				        return null;
				    }  
				});
			} else {
            	Log.v("No Proxy");
				for(String proerty: PlugInConfig.CONFIG_PROXY_PROPERTIES) {
					System.clearProperty(proerty);
				}
				Authenticator.setDefault(null);
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

	public boolean setSSLTrustStore() throws NetworkException {
		PlugInConfig config = new PlugInConfig(pluginPackage, true);

		if(isIgnoreSSLCert()) {
			Log.w("Fail to set truststore because was set ignore ssl cert.");
			return false;
		}
		if(isSetTrustStore()) {
			Log.w("Fail to set truststore because was set others.");
			return false;
		}

		trustStore = config.getConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE, APK_SCANNER_SSL_TRUSTSTORE);
		Log.v("trustStore: " + trustStore);
		if(APK_SCANNER_SSL_TRUSTSTORE.equals(trustStore)) {
			trustStore = _RFile.SSL_TRUSTSTORE_PATH.get();
		} else if(JVM_SSL_TRUSTSTORE.equals(trustStore)) {
			trustStore = "";
		} else if(IGNORE_TRUSTSTORE.equals(trustStore)) {
			trustStore = null;
		}
		Log.v("trustStore convert path : " + trustStore);

		try {
			TrustManager[] trustManager = null;
			if(trustStore == null) {
				trustManager = new TrustManager[] {
				    new X509TrustManager() {
				        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				        	Log.w("getAcceptedIssuers() Dangerous: Ignoring certificate errors opens the connection to potential MITM attacks.");
				            return null;
				        }
				        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				        	Log.w("checkClientTrusted() Dangerous: Ignoring certificate errors opens the connection to potential MITM attacks.");
				        }
				        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				        	Log.w("checkServerTrusted() Dangerous: Ignoring certificate errors opens the connection to potential MITM attacks.");
				        	//Log.w(new SignatureReport(certs[0]).toString());
				        }
				    }
				};
			} else if(!trustStore.isEmpty() && new File(trustStore).canRead()) {
			    KeyStore ts = KeyStore.getInstance("JKS");
			    ts.load(new FileInputStream(new File(trustStore)), config.getConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE_PWD, DEFAULT_TRUSTSTORE_PASSWORD).toCharArray());
			    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			    tmf.init(ts);
			    trustManager = tmf.getTrustManagers();
			} else {
				Log.v("use truststore of jre");
			}
		    SSLContext sslContext = SSLContext.getInstance("SSL");
		    sslContext.init(null, trustManager, null);

		    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		} catch(NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException e) {
			throw new NetworkException(e);
		}

		isSetTruststore = true;
		isIgnoreTrustStore = (trustStore == null);

		return true;
	}

	public void restoreSSLTrustStore() {
		if(isSetTruststore) {
			trustStore = null;
			isSetTruststore = false;
			isIgnoreTrustStore = false;
		}
	}

	public static boolean isIgnoreSSLCert() {
		return isIgnoreTrustStore;
	}

	public boolean isSetTrustStore() {
		return isSetTruststore; // System.getProperty("javax.net.ssl.trustStore") != null
	}
}
