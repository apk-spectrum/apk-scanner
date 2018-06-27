package com.apkscanner.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.apkscanner.plugin.manifest.InvalidManifestException;
import com.apkscanner.plugin.manifest.Manifest;
import com.apkscanner.plugin.manifest.ManifestReader;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public final class PlugInManager
{
	private static PakcageSearcherManager psm = new PakcageSearcherManager();

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
		if(manifest != null) {
			Log.e(manifest.packageName);
			Log.e(manifest.plugin.icon);
			Log.e("enable " + manifest.plugin.enable);
			Log.e(manifest.plugin.components[0].name);
			Log.e(manifest.plugin.components[1].linkers[0].url);
			Log.e(manifest.plugin.components[1].linkers[1].target);
		}
	}

    public static void main(String[] args) throws IOException {
    	loadPlugIn();
    }
}
