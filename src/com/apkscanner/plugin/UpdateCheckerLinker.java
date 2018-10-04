package com.apkscanner.plugin;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ApkScannerVersion;
import com.apkscanner.util.Log;

public class UpdateCheckerLinker extends AbstractUpdateChecker
{
	private boolean isIgnoreSSLCert;

	public UpdateCheckerLinker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	public boolean getNewVersion() throws NetworkException {
		//if(version != null || component.url == null) return version;

		if(!NetworkSetting.isEnabledNetworkInterface()) {
			Log.w("No such network interface");
			throw makeNetworkException(new NetworkNotFoundException("No such network interface"));
		}

		System.setProperty("http.protocols", "TLSv1,TLSv1.1,TLSv1.2");

		NetworkSetting networkSetting = new NetworkSetting(pluginPackage);
		HttpURLConnection request = null;
		boolean ignoreSSLCert = false;
		boolean isSetTruststore = false;
		try {
			URL targetURL = new URL(component.url);
			networkSetting.setProxyServer(targetURL.toURI());
			isSetTruststore = networkSetting.setSSLTrustStore();
			ignoreSSLCert = NetworkSetting.isIgnoreSSLCert();
			request = (HttpURLConnection) targetURL.openConnection();
			request.setRequestMethod("GET");
		} catch (MalformedURLException | URISyntaxException e) {
			throw makeNetworkException(e);
		} catch (IOException e) {
			throw makeNetworkException(e);
		} finally {
			if(isSetTruststore) networkSetting.restoreSSLTrustStore();
		}

		request.setUseCaches(false);
		request.setDefaultUseCaches(false);
		request.setDoOutput(false);
		request.setDoInput(true);
		request.setInstanceFollowRedirects(false);
		request.setConnectTimeout(5000);
		request.setReadTimeout(5000);

		// customizing information
		request.setRequestProperty("User-Agent","");
		request.setRequestProperty("Referer","");
		request.setRequestProperty("Cookie","");
		request.setRequestProperty("Origin","");
		request.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
		request.setRequestProperty("Pragma", "no-cache");
		request.setRequestProperty("Expires", "0");

		//request.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		//request.setRequestProperty("Content-length",String.valueOf(param.length()));

		//request.setRequestMethod("POST");
		//OutputStream opstrm = request.getOutputStream();
		//opstrm.write("".getBytes());
		//opstrm.flush();
		//opstrm.close();

		String jsonData = null;
		try ( InputStream is = request.getInputStream();
			  InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			  BufferedReader br = new BufferedReader(isr) ) {

			String buffer = null;
			StringBuffer sb = new StringBuffer();
			while ((buffer = br.readLine()) != null) {
				sb.append(buffer);
			}
			jsonData = sb.toString();
			Log.v(jsonData);
		} catch (IOException e) {
			throw makeNetworkException(e);
		} finally {
			request.disconnect();
		}

		JSONParser parser = new JSONParser();
		JSONObject data = null;
		try {
			data = (JSONObject)parser.parse(jsonData);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if(data != null && data.containsKey("version")) {
			setLatestVersionInfo(data);
			setLastUpdateDate(new Date().getTime());
			isIgnoreSSLCert = ignoreSSLCert;
		} else {
			return false;
		}

		return true;
	}

	@Override
	public boolean checkNewVersion() throws NetworkException {
		if(!getNewVersion()) {
			Log.i("No such new version");
			return false;
		}
		String version = (String)latestVersionInfo.get("version");
		if("com.apkscanner.plugin.ApkScannerUpdater".equals(getName())) {
			ApkScannerVersion newVer = ApkScannerVersion.parseFrom(version);
			ApkScannerVersion oldVer = ApkScannerVersion.parseFrom(Resource.STR_APP_VERSION.getString());
			return newVer.compareTo(oldVer) > 0;
		} else if ("com.apkscanner.plugin.SdkPermissionUpdater".equals(getName())) {
			return false;
		} else {
			int curVer = pluginPackage.getVersionCode();
			int newVer = Integer.parseInt(version);
			return newVer > curVer;
		}
	}

	@Override
	public void launch() {
		try {
			if(latestVersionInfo == null && !checkNewVersion()) {
				Log.i("Current version is latest or cann't get latest version");
				return;
			}
		} catch (NetworkException e) {
			e.printStackTrace();
			return;
		}

		String url = !isIgnoreSSLCert ? (String)latestVersionInfo.get("url") : null;
		if(url == null) url = component.updateUrl != null ? component.updateUrl : component.url;

		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI(url));
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        }
	    }
	}
}
