package com.apkscanner.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.plugin.manifest.Manifest;
import com.apkscanner.plugin.manifest.ManifestReader;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public final class PlugInManager
{
	private static PakcageSearcherManager psm = new PakcageSearcherManager();
	private static HashMap<String, Manifest> pluginPackages = new HashMap<String, Manifest>();

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
		for(File plugin: pluginFiles) {
			boolean isLinker = false;
			Manifest manifest = null;
			ZipFile zipFile = null;
			try {
				isLinker = plugin.getName().endsWith(".xml");
				if(isLinker) {
					manifest = ManifestReader.readManifest(plugin);
				} else {
					zipFile = new ZipFile(plugin);
					ZipEntry entry = zipFile.getEntry("Manifest.xml");
					manifest = ManifestReader.readManifest(zipFile.getInputStream(entry));
				}
			} catch (InvalidManifestException | IOException e) {
				Log.e("Occurred an InvalidManifestException : " + e.getMessage());
				e.printStackTrace();
			} finally {
				if(zipFile != null) {
					try {
						zipFile.close();
					} catch (IOException e) { }
				}
			}
			createPlugInInstance(manifest, !isLinker ? plugin : null );
		}
	}
	
	public static void createPlugInInstance(Manifest manifest, File jarPath) {
		if(manifest == null) return;
		Log.v(manifest.packageName);
		Log.v(manifest.versionCode + "");
		Log.v(manifest.versionName);
		Log.v(manifest.minScannerVersion);
		if(jarPath == null) {
			pluginPackages.put(manifest.packageName, manifest);
			for(Component c: manifest.plugin.components) {
				switch(c.type) {
				case Component.TYPE_PACAKGE_SEARCHER_LINKER:
					IPackageSearcher plugin = new PackageSearcherLinker(manifest.packageName, c);
					psm.add(plugin);
					break;
				case Component.TYPE_UPDATE_CHECKER_LINKER:
					IUpdateChecker plugin2 = new UpdateCheckerLinker(manifest.packageName, c);
					//plugin2.launch();
					Log.e(plugin2.getNewVersion());
					Log.e(" " + plugin2.checkNewVersion(Resource.STR_APP_VERSION.getString()));
					break;
				case Component.TYPE_EXTERNAL_TOOL_LINKER:
					//IExternalTool plugin3 = new ExternalToolLinker(manifest.packageName, c);
					//plugin3.launch("abc.apk");
					break;
				default: 
					break;
				}
			}
		} else {
			Log.e("" + jarPath.getAbsolutePath());
			for(Component c: manifest.plugin.components) {
				switch(c.type) {
				case Component.TYPE_PACAKGE_SEARCHER:
					break;
				case Component.TYPE_UPDATE_CHECKER_LINKER:
					break;
				case Component.TYPE_EXTERNAL_TOOL_LINKER:
					break;
				}
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
