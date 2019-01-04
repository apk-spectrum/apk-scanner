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
import java.util.Map;
import java.util.Map.Entry;

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
import com.google.common.collect.ObjectArrays;

public class PlugInPackage
{
	private Manifest manifest;
	private URI pluginUri;
	private String fingerprint;
	private IPlugIn[] plugins;
	private PlugInGroup[] pluginGroups;
	private HashMap<String, HashMap<String,String>> resources;
	private HashMap<String, String> configurations;
	private boolean enabled;

	private PlugInConfig pluginConfig;

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
		enabled = manifest.plugin.enabled;
		pluginGroups = readPlugInGroup(pluginFile, manifest);
		plugins = createPlugInInstance(pluginFile, manifest);
		resources = loadResource(pluginFile, manifest);
		configurations = loadConfiguration(manifest);
		readSettings();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
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

	public URL getIconURL() {
		if(manifest.plugin.icon != null) {
			try {
				URI uri = getResourceUri(manifest.plugin.icon);
				return uri != null ? uri.toURL() : null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getLabel() {
		String label = getResourceString(manifest.plugin.label);
		return label != null ? label : getPackageName();
	}

	public String getDescription() {
		String desc = getResourceString(manifest.plugin.description);
		return desc != null ? desc : "";
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

	public IPlugIn getPlugInByActionCommand(String actionCommand) {
		if(actionCommand == null || plugins == null) return null;
		for(IPlugIn p: ObjectArrays.concat(plugins, pluginGroups, IPlugIn.class)) {
			if(actionCommand.equals(p.getActionCommand())) return p;
		}
		return null;
	}

	public PlugInGroup getPlugInGroup(String name) {
		if(pluginGroups == null || name == null || name.trim().isEmpty()) return null;
		if(name.startsWith(".")) name = manifest.packageName + name;
		for(PlugInGroup g: pluginGroups) {
			if(name.equals(g.getName())) return g;
		}
		return null;
	}

	public PlugInGroup[] getPlugInGroups() {
		return pluginGroups;
	}

	public PlugInGroup[] getTopPlugInGroup() {
		ArrayList<PlugInGroup> list = new ArrayList<>();
		for(PlugInGroup g: pluginGroups) {
			if(g.isTopGroup()) list.add(g);
		}
		return list.toArray(new PlugInGroup[list.size()]);
	}

	public IPlugIn[] getPlugInWithoutGroup() {
		if(plugins == null) return null;
		ArrayList<IPlugIn> list = new ArrayList<>();
		for(IPlugIn p: plugins) {
			if(p.getGroupName() == null) list.add(p);
		}
		return list.toArray(new IPlugIn[list.size()]);
	}

	public boolean useNetworkSetting() {
		return manifest.plugin.useNetworkSetting;
	}

	public boolean useConfigurationSetting() {
		return manifest.plugin.useConfigurationSetting;
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

	private PlugInGroup[] readPlugInGroup(File pluginFile, Manifest manifest) {
		if(manifest == null) return null;
		ArrayList<PlugInGroup> pluginGroup = new ArrayList<>();
		for(Component c: manifest.plugin.components) {
			if(c.type != Component.TYPE_PLUGIN_GROUP) continue;
			if(c.name == null || c.name.trim().isEmpty()) {
				Log.e("Must have name to PlugInGroup");
				continue;
			}
			if(getPlugInGroup(c.name) != null) {
				Log.e("Aleady existed plugin group : " + c.name);
				continue;
			}
			pluginGroup.add(new PlugInGroup(this, c));
		}
		return pluginGroup.toArray(new PlugInGroup[pluginGroup.size()]);
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
			case Component.TYPE_PLUGIN_GROUP:
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
				case Component.TYPE_EXTRA_COMPONENT:
					if(!(plugin instanceof IExtraComponent)) {
						Log.e("Class was no matched to IExtraComponent : " + className);
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

	private HashMap<String, String> loadConfiguration(Manifest manifest) {
		HashMap<String, String> configurations = new HashMap<>();
		if(manifest.configuration != null) {
			for(Configuration c: manifest.configuration) {
				configurations.put(c.name, c.value);
			}
		}
		return configurations;
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
		String lang = PlugInManager.getLang();
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

		return value != null ? value.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t") : name;
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

	public PlugInConfig getPlugInConfig() {
		if(pluginConfig == null) pluginConfig = new PlugInConfig(this);
		return pluginConfig;
	}

	public String getConfiguration(String key) {
		return getConfiguration(key, false);
	}

	public String getConfiguration(String key, boolean allowGlobalConfig) {
		if(key == null) return null;
		synchronized(configurations) {
			String value = configurations.get(key);
			if(value == null && allowGlobalConfig) {
				value = PlugInConfig.getGlobalConfiguration(key);
			}
			return value;
		}
	}

	public String getConfiguration(String key, String defaultValue) {
		return getConfiguration(key, defaultValue, false);
	}

	public String getConfiguration(String key, String defaultValue, boolean allowGlobalConfig) {
		String value = getConfiguration(key, allowGlobalConfig);
		return value != null ? value : defaultValue;
	}

	public void setConfiguration(String key, String value) {
		if(key == null) return;
		synchronized(configurations) {
			if(value != null) {
				configurations.put(key, value);
			} else if(configurations.containsKey(key)) {
				configurations.remove(key);
			}
		}
	}

	public void clearConfiguration(String key) {
		if(key == null) return;
		synchronized(configurations) {
			if(configurations.containsKey(key)) {
				configurations.remove(key);
			}
		}
	}

	public HashMap<String, String> getConfigurations() {
		synchronized(configurations) {
			return new HashMap<>(configurations);
		}
	}

	public Map<String, Object> getChangedProperties() {
		HashMap<String, Object> data = new HashMap<>();
		if(manifest.plugin.enabled != isEnabled()) {
			data.put("enabled", isEnabled());
		}
		HashMap<String, String> conf = getConfigurations();
		if(manifest.configuration != null) {
			for(Configuration c: manifest.configuration) {
				if(conf.containsKey(c.name) && conf.get(c.name).equals(c.value)) {
					conf.remove(c.name);
				}
			}
		}
		if(!conf.isEmpty()) {
			data.put("configuration", conf);
		}

		for(IPlugIn p: ObjectArrays.concat(plugins, pluginGroups, IPlugIn.class)) {
			Map<String, Object> prop = p.getChangedProperties();
			if(!prop.isEmpty()) {
				data.put(p.getActionCommand(), prop);
			}
		}
		return data;
	}

	public void restoreProperties(Map<?, ?> data) {
		if(data == null) return;
		if(data.containsKey("enabled")) {
			setEnabled((boolean)data.get("enabled"));
			data.remove("enabled");
		}

		if(data.containsKey("configuration")) {
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) data.get("configuration");
			synchronized(configurations) {
				configurations.putAll(map);
			}
			data.remove("configuration");
		}

		for(Entry<?, ?> entry: data.entrySet()) {
			IPlugIn plugin = getPlugInByActionCommand((String) entry.getKey());
			if(plugin != null) {
				plugin.restoreProperties((Map<?, ?>) entry.getValue());
			} else {
				Log.w("unknown plugin : " + entry.getKey());
			}
		}
	}
}
