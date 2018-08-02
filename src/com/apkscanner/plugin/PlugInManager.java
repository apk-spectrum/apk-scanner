package com.apkscanner.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONValue;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public final class PlugInManager
{
	private static ArrayList<PlugInPackage> pluginPackages = new ArrayList<>();
	private static ApkInfo apkinfo = null;

	private PlugInManager() { }

	public static PlugInPackage getPlugInPackage(String packageName) {
		if(packageName == null || packageName.trim().isEmpty()) return null;
		for(PlugInPackage pack: pluginPackages) {
			if(packageName.equals(pack.getPackageName())) return pack;
		}
		return null;
	}

	public static IUpdateChecker[] getUpdateChecker() {
		ArrayList<IUpdateChecker> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			IPlugIn[] plugins = pack.getPlugIn(IPlugIn.PLUGIN_TPYE_UPDATE_CHECKER);
			for(IPlugIn p: plugins) {
				if(p instanceof IUpdateChecker) {
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
				if(p instanceof IPackageSearcher) {
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
				if(p instanceof IExtraComponent) {
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

	public static void loadPlugIn() {
		pluginPackages.clear();

		File pluginFolder = new File(Resource.PLUGIN_PATH.getPath());
		if(!pluginFolder.isDirectory()) {
			Log.e("No such plugins: " + Resource.PLUGIN_PATH.getPath());
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
	}

	public static Map<String, Object> getChangedProperties() {
		HashMap<String, Object> data = new HashMap<>();
		for(PlugInPackage pack: pluginPackages) {
			Map<String, Object> prop = pack.getChangedProperties();
			if(!prop.isEmpty()) {
				data.put(pack.getPackageName(), prop);
			}
		}
		return data;
	}

	public static void restoreProperties(Map<String, Object> data) {
		if(data == null) return;
		for(Entry<String, Object> entry: data.entrySet()) {
			PlugInPackage pack = getPlugInPackage(entry.getKey());
			if(pack != null) {
				pack.restoreProperties((Map<?, ?>) entry.getValue());
			} else {
				Log.w("unknown package : " + entry.getKey());
			}
		}
	}

    public static void main(String[] args) throws IOException {
    	loadPlugIn();

    	IPackageSearcher[] searchers = getPackageSearchers();
    	if(searchers != null && searchers.length > 1) {
    		//searchers[1].launch(null, IPackageSearcher.SEARCHER_TYPE_APP_NAME, "멜론");
    		Log.e(searchers[1].getPreferLangForAppName());
    	}

    	IUpdateChecker[] updator = getUpdateChecker();
    	if(updator != null && updator.length > 0) {
    		Log.e(((IUpdateChecker)updator[0]).getNewVersion());
    		//((IUpdateChecker)updator[0]).launch();	
    	}

    	Log.e(JSONValue.toJSONString(getChangedProperties()));
    	restoreProperties(getChangedProperties());
    }
}
