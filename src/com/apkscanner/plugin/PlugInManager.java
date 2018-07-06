package com.apkscanner.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public final class PlugInManager
{
	private static PakcageSearcherManager psm = new PakcageSearcherManager();
	private static ArrayList<PlugInPackage> pluginPackages = new ArrayList<PlugInPackage>();

	private PlugInManager() { }

	public static IUpdateChecker getUpdateChecker() {
		return null;
	}

	public static IPackageSearcher[] getPackageSearchers() {
		return psm.getList();
	}

	public static IPackageSearcher[] getPackageSearchers(int type) {
		return psm.getList(type);
	}

	public static IPackageSearcher[] getPackageSearchers(int type, String name) {
		return psm.getList(type, name);
	}

	public static void loadPlugIn() {
    	//psm.add(new SimpleSearcher());
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
			PlugInPackage pack;
			try {
				pack = new PlugInPackage(pluginFile);
				pluginPackages.add(pack);
			} catch (InvalidManifestException e) {
				e.printStackTrace();
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
    }
}
