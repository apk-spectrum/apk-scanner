package com.apkscanner.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.resource.RFile;
import com.apkscanner.util.Log;

public final class PlugInManager
{
	private static List<PlugInPackage> pluginPackages = new ArrayList<>();
	private static ApkInfo apkinfo = null;
	private static String lang = "";

	private static List<IPlugInEventListener> eventListeners = new ArrayList<>();
	private static final Object sLock = eventListeners;

	private PlugInManager() { }

	public static void addPlugInEventListener(IPlugInEventListener listener) {
		synchronized (sLock) {
			if (!eventListeners.contains(listener)) {
				eventListeners.add(listener);
			}
			if(!pluginPackages.isEmpty()) {
				listener.onPluginLoaded();
			}
		}
	}

	public static void removePlugInEventListener(IPlugInEventListener listener) {
		synchronized (sLock) {
			eventListeners.remove(listener);
		}
	}

	public static PlugInPackage getPlugInPackage(String packageName) {
		if(packageName == null || packageName.trim().isEmpty()) return null;
		for(PlugInPackage pack: pluginPackages) {
			if(packageName.equals(pack.getPackageName())) return pack;
		}
		return null;
	}

	public static PlugInPackage[] getPlugInPackages() {
		return pluginPackages.toArray(new PlugInPackage[pluginPackages.size()]);
	}

	public static IUpdateChecker[] getUpdateChecker() {
		ArrayList<IUpdateChecker> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_UPDATE_CHECKER);
			for(IPlugIn p: plugins) {
				if(p instanceof IUpdateChecker && p.isEnabled()) {
					list.add((IUpdateChecker)p);
				}
			}
		}
		return list.toArray(new IUpdateChecker[list.size()]);
	}

	public static IExternalTool[] getExternalTool() {
		ArrayList<IExternalTool> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_EXTERNAL_TOOL);
			for(IPlugIn p: plugins) {
				if( p instanceof IExternalTool
						&& p.isEnabled()
						&& ((IExternalTool) p).isSupoortedOS()
						&& !((IExternalTool) p).isDecorderTool()) {
					list.add((IExternalTool)p);
				}
			}
		}
		return list.toArray(new IExternalTool[list.size()]);
	}

	public static IExternalTool[] getDecorderTool() {
		ArrayList<IExternalTool> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_EXTERNAL_TOOL);
			for(IPlugIn p: plugins) {
				if( p instanceof IExternalTool
						&& p.isEnabled()
						&& ((IExternalTool) p).isSupoortedOS()
						&& ((IExternalTool) p).isDecorderTool() ) {
					list.add((IExternalTool)p);
				}
			}
		}
		return list.toArray(new IExternalTool[list.size()]);
	}

	public static IPackageSearcher[] getPackageSearchers() {
		ArrayList<IPackageSearcher> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_PACKAGE_SEARCHER);
			for(IPlugIn p: plugins) {
				if(p instanceof IPackageSearcher && p.isEnabled()) {
					list.add((IPackageSearcher)p);
				}
			}
		}
		return list.toArray(new IPackageSearcher[list.size()]);
	}

	public static IPackageSearcher[] getPackageSearchers(int type) {
		ArrayList<IPackageSearcher> list = new ArrayList<>();
		for(IPackageSearcher searcher: getPackageSearchers()) {
			if((searcher.getSupportType() & type) == type) {
				list.add(searcher);
			}
		}
		return list.toArray(new IPackageSearcher[list.size()]);
	}

	public static IExtraComponent[] getExtraComponenet() {
		ArrayList<IExtraComponent> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_EXTRA_COMPONENT);
			for(IPlugIn p: plugins) {
				if(p instanceof IExtraComponent && p.isEnabled()) {
					list.add((IExtraComponent)p);
				}
			}
		}
		return list.toArray(new IExtraComponent[list.size()]);
	}

	public static IPlugIn[] getPlugInAll() {
		ArrayList<IPlugIn> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_ALL);
			for(IPlugIn p: plugins) {
				list.add(p);
			}
		}
		return list.toArray(new IPlugIn[list.size()]);
	}

	public static IPlugIn getPlugInByActionCommand(String actionCommand) {
		if(actionCommand == null) return null;
		String packageName = actionCommand.replaceAll("!.*", "");
		PlugInPackage pack = getPlugInPackage(packageName);
		return pack != null ? pack.getPlugInByActionCommand(actionCommand) : null;
	}

	public static void setApkInfo(ApkInfo info) {
		apkinfo = info;
	}

	public static ApkInfo getApkInfo() {
		return apkinfo;
	}

	public static void setLang(String newLang) {
		lang = newLang != null ? newLang.trim() : "";
	}

	public static String getLang() {
		return lang != null ? lang : "";
	}

	public static void loadPlugIn() {
		synchronized (sLock) {
			pluginPackages.clear();

			File pluginFolder = new File(RFile.PLUGIN_PATH.get());
			if(!pluginFolder.isDirectory()) {
				Log.v("No such plugins: " + RFile.PLUGIN_PATH.get());
				return;
			}

			File[] pluginFiles = pluginFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml") || name.endsWith(".jar");
				}
			});

			for(File pluginFile: pluginFiles) {
				PlugInPackage pack = null;
				try {
					pack = new PlugInPackage(pluginFile);
				} catch (InvalidManifestException e) {
					e.printStackTrace();
				}
				if(pack != null) {
					String packageName = pack.getPackageName();
					PlugInPackage oldPack = getPlugInPackage(packageName);
					if(oldPack != null) {
						Log.i(packageName + " was already existed a same package : " + oldPack.getPlugInUri());
						if(oldPack.getVersionCode() < pack.getVersionCode()) {
							Log.i("This is new version, so remove old version " + oldPack.getPlugInUri());
							pluginPackages.remove(oldPack);
							pluginPackages.add(pack);
						} else {
							Log.i("This is old version or same, so do not add package : " + pack.getPlugInUri());
						}
					} else {
						pluginPackages.add(pack);
					}
				}
			}

			loadProperty();

			IPlugInEventListener[] listenersCopy = null;
			synchronized (sLock) {
				listenersCopy = eventListeners.toArray(
						new IPlugInEventListener[eventListeners.size()]);
			}

			for (IPlugInEventListener listener : listenersCopy) {
				listener.onPluginLoaded();
			}
		}
	}

	public static Map<String, Object> getChangedProperties() {
		HashMap<String, Object> data = new HashMap<>();
		for(PlugInPackage pack: pluginPackages) {
			Map<String, Object> prop = pack.getChangedProperties();
			if(!prop.isEmpty()) {
				data.put(pack.getPackageName(), prop);
			}
		}
		if(!PlugInConfig.configurations.isEmpty()) {
			data.put("globalConfiguration", PlugInConfig.configurations);
		}
		return data;
	}

	public static void restoreProperties(Map<?, ?> data) {
		if(data == null) return;

		if(data.containsKey("globalConfiguration")) {
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) data.get("globalConfiguration");
			PlugInConfig.configurations.putAll(map);
			data.remove("globalConfiguration");
		}

		for(Entry<?, ?> entry: data.entrySet()) {
			PlugInPackage pack = getPlugInPackage((String) entry.getKey());
			if(pack != null) {
				pack.restoreProperties((Map<?, ?>) entry.getValue());
			} else {
				Log.w("unknown package : " + entry.getKey());
			}
		}
	}

	public static void loadProperty()
	{
		File file = new File(RFile.PLUGIN_CONF_PATH.get());
		if(!file.exists() || file.length() == 0) return;
		try(FileReader fileReader = new FileReader(file)) {
			JSONParser parser = new JSONParser();
			restoreProperties((JSONObject)parser.parse(fileReader));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveProperty()
	{
		File file = new File(RFile.PLUGIN_CONF_PATH.get());
		try {
			if(!file.exists() && !file.createNewFile()) {
				Log.w("Cann't create file : " + RFile.PLUGIN_CONF_PATH.get());
			}
		} catch (IOException e1) { }

		if(!file.canWrite()) {
			Log.v("Cann't write file : " + RFile.PLUGIN_CONF_PATH.get());
			return;
		}

		String transMultiLine = JSONValue.toJSONString(getChangedProperties())
				.replaceAll("^\\{(.*)\\}$", "{\n$1\n}")
				.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",]*)?,)", "$1\n");
		//.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",\\[]*(\\[[^\\]]\\])?)?,)", "$1\n");

		try( FileWriter fw = new FileWriter(RFile.PLUGIN_CONF_PATH.get());
			 BufferedWriter writer = new BufferedWriter(fw) ) {
			writer.write(transMultiLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
