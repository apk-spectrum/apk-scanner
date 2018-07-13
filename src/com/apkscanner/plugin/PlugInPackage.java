package com.apkscanner.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.NodeList;

import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.plugin.manifest.Configuration;
import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.plugin.manifest.Manifest;
import com.apkscanner.plugin.manifest.ManifestReader;
import com.apkscanner.plugin.manifest.Resources;
import com.apkscanner.plugin.manifest.StringData;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class PlugInPackage
{
	private Manifest manifest;
	private URI pluginUri;
	private String fingerprint;
	private IPlugIn[] plugins;
	private HashMap<String, HashMap<String,String>> resources;

	public PlugInPackage(File pluginFile) throws InvalidManifestException {
		if(pluginFile == null) {
			throw new NullPointerException();
		}
		pluginUri = pluginFile.toURI();
		if(!isJarPackage() && !isXmlPackage()) {
			throw new IllegalArgumentException("Unsupported extension of file.");
		}
		fingerprint = FileUtil.getMessageDigest(pluginFile, "SHA-1");
		manifest = readManifest(pluginUri);
		plugins = createPlugInInstance(pluginFile, manifest);
		resources = loadResource(pluginFile, manifest);
		readSettings();
	}

	public URI getPlugInUri() {
		return pluginUri;
	}

	public Manifest getManifest() {
		return manifest;
	}

	public String getPackageName() {
		return manifest.packageName;
	}

	public int getVersionCode() {
		return manifest.versionCode;
	}

	public String getVersionName() {
		return manifest.versionName;
	}

	public String getMinScannerVersionName() {
		return manifest.minScannerVersion;
	}

	public String getFingerPrint() {
		return fingerprint;
	}

	public boolean hasPlugIn(int plugInType) {
		if(plugins == null) return false;
		for(IPlugIn p: plugins) {
			if((p.getType() & plugInType) != 0) return true;
		}
		return false;
	}

	public IPlugIn[] getPlugIn(int plugInType) {
		if(plugins == null) return null;
		ArrayList<IPlugIn> list = new ArrayList<>();
		for(IPlugIn p: plugins) {
			if((p.getType() & plugInType) != 0) list.add(p);
		}
		return list.toArray(new IPlugIn[list.size()]);
	}

	public boolean isJarPackage() {
		return isJarPackage(pluginUri);
	}

	public boolean isXmlPackage() {
		return isXmlPackage(pluginUri);
	}

	private boolean isJarPackage(URI pluginUri) {
		return pluginUri.toString().endsWith(".jar");
	}

	private boolean isXmlPackage(URI pluginUri) {
		return pluginUri.toString().endsWith(".xml");
	}

	private Manifest readManifest(URI pluginUri) throws InvalidManifestException {
		Manifest manifest = null;
		if(isJarPackage(pluginUri)) {
			pluginUri = getResourceUri(pluginUri, "Manifest.xml");
		}

		URLConnection conn = null;
		try {
			conn = pluginUri.toURL().openConnection();
	        conn.connect();
		} catch(IOException | NullPointerException e) {
			Log.e(e.getMessage());
			throw new InvalidManifestException(e.getMessage());
		}

		try(InputStream is = conn.getInputStream()) {
	        manifest = ManifestReader.readManifest(is);	
		} catch (IOException | NullPointerException e) {
			Log.e(e.getMessage());
			throw new InvalidManifestException(e.getMessage());
		}

		return manifest;
	}

	private IPlugIn[] createPlugInInstance(File pluginFile, Manifest manifest) {
		if(manifest == null) return null;
		Log.v(manifest.packageName + " : " + fingerprint);
		Log.v(manifest.versionCode + "");
		Log.v(manifest.versionName);

		ArrayList<IPlugIn> plugins = new ArrayList<>();
		URLClassLoader loader = null;
		boolean isJarPackage = isJarPackage(pluginFile.toURI());
		if(isJarPackage) {
			try {
				URL classURL = new URL("jar:" + pluginFile.toURI().toString() + "!/");
				loader = new URLClassLoader(new URL [] {classURL});
			} catch (MalformedURLException e) {
				Log.e(e.getMessage());
			}
		}

		for(Component c: manifest.plugin.components) {
			IPlugIn plugin = null;
			switch(c.type) {
			case Component.TYPE_PACAKGE_SEARCHER_LINKER:
				plugin = new PackageSearcherLinker(this, c);
				break;
			case Component.TYPE_UPDATE_CHECKER_LINKER:
				plugin = new UpdateCheckerLinker(this, c);
				break;
			case Component.TYPE_EXTERNAL_TOOL_LINKER:
				plugin = new ExternalToolLinker(this, c);
				break;
			default:
				if(!isJarPackage) {
					Log.w("XML plug-ins need only the LINKER plug-in. This type is not supported : " +  c.type);
					break;
				}
				if(loader == null) {
					Log.w("URLClassLoader is null");
					break;
				}

				if(c.name == null || c.name.isEmpty() || c.name.endsWith(".")) {
					Log.w("error: Illegal class name : \"" + c.name + "\"");
					continue;
				}
			    String className = (c.name.startsWith(".") ? manifest.packageName : "") + c.name;

		        try {
		            Class<?> clazz = loader.loadClass(className);
		            plugin = (IPlugIn)clazz.getDeclaredConstructor(PlugInPackage.class, Component.class).newInstance(this, c);
		        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
		        	Log.e("Fail loadClass : " + className + ", " + e.getMessage());
		        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
		        	Log.e("Fail newInstance : " + className + ", " + e.getMessage());
				}
		        if(plugin == null) {
		        	Log.e("plugin is null : " + className);
		        	continue;
		        }

				switch(c.type) {
				case Component.TYPE_PACAKGE_SEARCHER:
					if(!(plugin instanceof IPackageSearcher)) {
						Log.e("Class was no matched to IPackageSearcher : " + className);
						plugin = null;
					}
					break;
				case Component.TYPE_UPDATE_CHECKER:
					if(!(plugin instanceof IUpdateChecker)) {
						Log.e("Class was no matched to IUpdateChecker : " + className);
						plugin = null;
					}
					break;
				case Component.TYPE_EXTERNAL_TOOL:
					if(!(plugin instanceof IExternalTool)) {
						Log.e("Class was no matched to IExternalTool : " + className);
						plugin = null;
					}
					break;
				default:
					Log.e("Unknown type : " + c.type);
					plugin = null;
					break;
				}
				break;
			}

			if(plugin != null) {
				plugins.add(plugin);
			}
		}
		return plugins.toArray(new IPlugIn[plugins.size()]);
	}

	private HashMap<String, HashMap<String,String>> loadResource(File pluginFile, Manifest manifest) {
		if(manifest == null || manifest.resources == null) return null;
		HashMap<String, HashMap<String,String>> resources = new HashMap<>();

		for(Resources res: manifest.resources) {
			String lang = res.lang != null ? res.lang.trim() : "";
			HashMap<String,String> map = null;
			if(resources.containsKey(lang)) {
				map = resources.get(lang);
			} else {
				map = new HashMap<>();
				resources.put(lang, map);
			}
			if(res.src != null && !res.src.trim().isEmpty()) {
				URI uri = getResourceUri(res.src);
				InputStream is = null;
				XmlPath resXPath = null;
				try {
					URLConnection conn = uri.toURL().openConnection();
			        conn.connect();
			        is = conn.getInputStream();
			        resXPath = new XmlPath(is);
				} catch(IOException e) {
					Log.e(e.getMessage());
				} finally {
					try {
						if(is != null) is.close();
					} catch (IOException e) { }
				}
				NodeList list = resXPath.getNodeList("/resources/string").getNodeList();
				for(int i=0; i < list.getLength(); i++) {
					map.put(list.item(i).getAttributes().getNamedItem("name").getNodeValue(), list.item(i).getTextContent());
				}
			}
			for(StringData data: res.strings) {
				if(data.name == null || data.name.trim().isEmpty()) {
					Log.w("String name is null or empty : " + data.name);
					continue;
				}
				map.put(data.name, data.data);
			}
		}
		return resources;
	}

	private void readSettings() {

	}

	public String getResourceString(String name) {
		if(resources == null || name == null || !name.startsWith("@")) return name;
		String lang = PluginConfiguration.getLang();
		String id = name.substring(1);
		String value = null;
		if(resources.containsKey(lang) && resources.get(lang).containsKey(id)) {
			value = resources.get(lang).get(id);
		}
		if(value == null && !lang.isEmpty()) {
			if(resources.containsKey("") && resources.get("").containsKey(id)) {
				value = resources.get("").get(id);
			}
		}
		if(value == null) {
			for(String l: resources.keySet()) {
				if(l.isEmpty() || l.equals(lang)) continue;
				if(resources.get(l).containsKey(id)) {
					value = resources.get(l).get(id);
					Log.w("this value in " + l);
					break;
				}
			}
		}
		if(value == null) {
			value = name;
		}
		return value;
	}

	public URI getResourceUri(String resPath) {
		return getResourceUri(pluginUri, resPath);
	}

	public URI getResourceUri(URI pluginUri, String resPath) {
		URI uri = null;
		try {
			uri = new URI(resPath);
			if(uri.isAbsolute()) {
				if(!"file".equals(uri.getScheme())) {
					uri = new URI("file:/" + uri.toString());
				}
			} else {
				if(isJarPackage(pluginUri)) {
					String temp = uri.toString();
					uri = new URI("jar:" + pluginUri.toString() + "!/" + (temp.startsWith("/") ? temp.substring(1) : temp));
				} else {
					uri = pluginUri.resolve(uri);
				}
			}
		} catch (URISyntaxException e) {
			Log.e(e.getMessage());
		}
		return uri;
	}
	
	public String getConfiguration(String key) {
		if(key == null) return null;
		String value = null;
		for(Configuration c: manifest.configuration) {
			if(key.equals(c.name)) {
				value = c.value;				
			}
		}
		if(value == null) {
			value = PluginConfiguration.getConfiguration(key);
		}
		return value;
	}
}
