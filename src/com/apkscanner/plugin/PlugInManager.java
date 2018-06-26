package com.apkscanner.plugin;

import java.io.File;
import java.io.IOException;

import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.plugin.manifest.ManifestReader;
import com.apkscanner.plugin.sample.SimpleSearcher;
import com.apkscanner.util.ClassFinder;
import com.apkscanner.util.Log;

public final class PlugInManager
{
	static private PakcageSearcherManager psm = new PakcageSearcherManager();

	private PlugInManager() { }

	static public IUpdateChecker getUpdateChecker() {
		return null;
	}

	static public IPackageSearcher[] getPackageSearchers() {
		return psm.getList();
	}

	static public IPackageSearcher[] getPackageSearchers(int type) {
		return psm.getList(type);
	}

	static public IPackageSearcher[] getPackageSearchers(int type, String name) {
		return psm.getList(type, name);
	}

	static public void loadPlugIn() {
    	psm.add(new SimpleSearcher());
		try {
			for(Class<?> cls : ClassFinder.getClasses("com.apkscanner.plugin")) {
				if(cls.isMemberClass() || cls.isInterface()) continue;
				Log.d("cls " + cls.getName());
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

    public static void main(String[] args) throws IOException {
    	loadPlugIn();
    	for(IPackageSearcher plugin: getPackageSearchers(IPackageSearcher.SEARCHER_TYPE_PACKAGE_NAME)) {
    		Log.d("plugin name: " + plugin.getName());
    	}
    	try {
			ManifestReader.readManifest(new File("C:\\strings.xml"));
		} catch (InvalidManifestException e) {
			e.printStackTrace();
		}
    }
}
