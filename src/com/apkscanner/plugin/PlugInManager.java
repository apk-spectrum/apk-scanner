package com.apkscanner.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
	private static ArrayList<IPlugIn> plugins = new ArrayList<IPlugIn>();
	
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
		for(File pluginFile: pluginFiles) {
			boolean isLinker = false;
			Manifest manifest = null;
			ZipFile zipFile = null;
			try {
				isLinker = pluginFile.getName().endsWith(".xml");
				if(isLinker) {
					manifest = ManifestReader.readManifest(pluginFile);
				} else {
					zipFile = new ZipFile(pluginFile);
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
			
			if(createPlugInInstance(manifest, !isLinker ? pluginFile : null)) {
				pluginPackages.put(manifest.packageName, manifest);	
			}
		}
	}
	
	public static boolean createPlugInInstance(Manifest manifest, File jarFile) {
		if(manifest == null) return false;
		Log.v(manifest.packageName);
		Log.v(manifest.versionCode + "");
		Log.v(manifest.versionName);
		Log.v(manifest.minScannerVersion);

		boolean hasPlugins = false;
		URLClassLoader loader = null;
		if(jarFile != null) {
			try {
				URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
	            loader = new URLClassLoader(new URL [] {classURL});
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
	        if(loader == null) {
	        	Log.e("URLClassLoader is null : " + jarFile.getPath());
	        	return false;
	        }
		}
		
		for(Component c: manifest.plugin.components) {
			IPlugIn plugin = null;
			switch(c.type) {
			case Component.TYPE_PACAKGE_SEARCHER_LINKER:
				plugin = new PackageSearcherLinker(manifest.packageName, c);
				psm.add((IPackageSearcher)plugin);
				break;
			case Component.TYPE_UPDATE_CHECKER_LINKER:
				plugin = new UpdateCheckerLinker(manifest.packageName, c);
				break;
			case Component.TYPE_EXTERNAL_TOOL_LINKER:
				plugin = new ExternalToolLinker(manifest.packageName, c);
				break;
			default:
				if(jarFile == null) {
					Log.w("XML plug-ins need only the LINKER plug-in. This type is not supported : " +  c.type);
					break;
				}

				if(c.name == null || c.name.isEmpty() || c.name.endsWith(".")) {
					Log.w("error: Illegal class name : \"" + c.name + "\"");
					continue;
				}
			    String className = (c.name.startsWith(".") ? manifest.packageName : "") + c.name;

		        try {
		            Class<?> clazz = loader.loadClass(className);
		            plugin = (IPlugIn)clazz.getDeclaredConstructor(String.class, Component.class).newInstance(manifest.packageName, c);
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
					if(plugin instanceof IPackageSearcher) {
						psm.add((IPackageSearcher)plugin);
					} else {
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
				hasPlugins = true;
			}
		}

		return hasPlugins;
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
