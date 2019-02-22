package com.apkscanner.plugin.manifest;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Node;

import com.apkscanner.annotations.NonNull;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class ManifestReader
{
	private ManifestReader() { }

	static public Manifest readManifest(@NonNull File path)  throws InvalidManifestException  {
		if(path == null || !path.canRead()) {
			Log.w("path is null or can not read");
			return null;
		}
		return makeManifest(new XmlPath(path));
	}

	static public Manifest readManifest(@NonNull InputStream input)  throws InvalidManifestException  {
		if(input == null) {
			Log.w("input is null");
			return null;
		}
		return makeManifest(new XmlPath(input));
	}

	static private Manifest makeManifest(@NonNull XmlPath manifest) throws InvalidManifestException {
		if(manifest == null) {
			throw new InvalidManifestException("XmlPath is null", new NullPointerException());
		}

		if(manifest.getLastException() != null) {
			throw new InvalidManifestException("Fail to parse xml", manifest.getLastException());
		}

		if(manifest.getCount("/manifest") != 1) {
			throw new InvalidManifestException("Must have only one <manifest> tag on root");
		}

		if(manifest.getCount("/manifest/plugin") != 1) {
			throw new InvalidManifestException("Must have only one <plugin> tag on manifest");
		}

		if(manifest.getNode("/manifest/plugin").getChildCount() == 0) {
			throw new InvalidManifestException("No have plugin");
		}

		XmlPath node = manifest.getNode("/manifest");
		String packageName = node.getAttribute("package");
		String versionName = node.getAttribute("versionName");
		int versionCode = node.getAttribute("versionCode") != null ? Integer.valueOf(node.getAttribute("versionCode")) : 0;
		String minScannerVersion = node.getAttribute("minScannerVersion");

		PlugIn plugin = makePlugin(manifest);
		Resources[] resources = makeResources(manifest);
		Configuration[] configuration = makeConfigurations(manifest);

		return new Manifest(packageName, versionName, versionCode, minScannerVersion, plugin, resources, configuration);
	}

	static private PlugIn makePlugin(@NonNull XmlPath manifest) {
		XmlPath node = manifest.getNode("/manifest/plugin");
		boolean enabled = !"false".equals(node.getAttribute("enabled"));
		String label = node.getAttribute("label");
		String icon = node.getAttribute("icon");
		String description = node.getAttribute("description");
		boolean useNetworkSetting = "true".equals(node.getAttribute("useNetworkSetting"));
		boolean useConfigurationSetting = "true".equals(node.getAttribute("useConfigurationSetting"));

		Component[] components =  makeComponents(manifest);
		return new PlugIn(enabled, label, icon, description, components, useNetworkSetting, useConfigurationSetting);
	}

	static private Component[] makeComponents(@NonNull XmlPath manifest) {
		ArrayList<Component> components = new ArrayList<>();
		XmlPath list = manifest.getNodeList("/manifest/plugin/*");
		for(int i = 0; i < list.getCount(); i++) {
			XmlPath node = list.getNode(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				int type;
				switch(node.getNodeName()) {
					case "package-searcher": type = Component.TYPE_PACAKGE_SEARCHER; break;
					case "package-searcher-linker": type = Component.TYPE_PACAKGE_SEARCHER_LINKER; break;
					case "update-checker": type = Component.TYPE_UPDATE_CHECKER; break;
					case "update-checker-linker": type = Component.TYPE_UPDATE_CHECKER_LINKER; break;
					case "external-tool": type = Component.TYPE_EXTERNAL_TOOL; break;
					case "external-tool-linker": type = Component.TYPE_EXTERNAL_TOOL_LINKER; break;
					case "extra-component": type = Component.TYPE_EXTRA_COMPONENT; break;
					case "plugin-group" : type = Component.TYPE_PLUGIN_GROUP; break;
					default: type = Component.TYPE_UNKNWON;
				}
				boolean enabled = !"false".equals(node.getAttribute("enabled"));
				String label = node.getAttribute("label");
				String icon = node.getAttribute("icon");
				String description = node.getAttribute("description");
				String name = node.getAttribute("name");
				String url = node.getAttribute("url");
				String pluginGroup = node.getAttribute("pluginGroup");

				//Linker[] linkers = makeLinker(node);
				String target = null;
				String preferLang = null;
				String path = null;
				String param = null;
				String updateUrl = null;
				String like = null;
				String supportedOS = null;
				Boolean visibleToBasic = null;
				String periodDay = null;
				String targetPackageName = null;

				switch(type) {
				case Component.TYPE_PACAKGE_SEARCHER_LINKER:
					target = node.getAttribute("target");
					preferLang = node.getAttribute("preferLanguage");
				case Component.TYPE_PACAKGE_SEARCHER:
					visibleToBasic = !"false".equals(node.getAttribute("visibleToBasic"));
					break;
				case Component.TYPE_UPDATE_CHECKER_LINKER:
					updateUrl = node.getAttribute("updateUrl");
					periodDay = node.getAttribute("periodDay");
					targetPackageName = node.getAttribute("targetPackageName");
					break;
				case Component.TYPE_EXTERNAL_TOOL_LINKER:
					path = node.getAttribute("path");
					param = node.getAttribute("param");
					supportedOS = node.getAttribute("supportedOS");
				case Component.TYPE_EXTERNAL_TOOL:
					like = node.getAttribute("like");
					break;
				}
				components.add(new Component(type, enabled, label, icon, description, name, url, /* linkers */
						target, preferLang, path, param, updateUrl, pluginGroup, like, supportedOS, visibleToBasic, periodDay, targetPackageName));
			}
		}
		return components.toArray(new Component[components.size()]);
	}

	@SuppressWarnings("unused")
	static private Linker[] makeLinker(@NonNull XmlPath component) {
		//Log.d("makeLinker() " + component.toString());
		ArrayList<Linker> linkers = new ArrayList<>();
		XmlPath node = component.getChildNodes();
		for(int i = 0; i < node.getCount(); i++) {
			XmlPath comp = node.getNode(i);
			if(comp.getNodeType() != Node.ELEMENT_NODE) continue;
			if(!"linker".equals(comp.getNodeName())) {
				Log.w("this is no linker node : " + comp.getNodeName());
				continue;
			}
			String url = node.getAttribute(i, "url");
			String target = node.getAttribute(i, "target");
			String preferLang = node.getAttribute(i, "preferLang");
			linkers.add(new Linker(url, target, preferLang));
		}
		return !linkers.isEmpty() ? linkers.toArray(new Linker[linkers.size()]) : null;
	}

	private static Resources[] makeResources(XmlPath manifest) {
		ArrayList<Resources> resources = new ArrayList<>();
		XmlPath node = manifest.getNodeList("/manifest/resources");
		for(int i = 0; i < node.getCount(); i++) {
			String src = node.getAttribute(i, "src");
			String lang = node.getAttribute(i, "lang");
			ArrayList<StringData> datas = new ArrayList<>();
			XmlPath child = node.getChildNodes(i);

			for(int j=0; j<child.getCount(); j++) {
				XmlPath res = child.getNode(j);
				if(res.getNodeType() != Node.ELEMENT_NODE) continue;
				if(!"string".equals(res.getNodeName())) {
					Log.w("this is no string node : " + res.getNodeName());
					continue;
				}
				String name = child.getAttribute(j, "name");
				String data = child.getTextContent(j);
				datas.add(new StringData(name, data));	
			}
			resources.add(new Resources(src, lang, datas.toArray(new StringData[datas.size()])));
		}
		return resources.toArray(new Resources[resources.size()]);
	}

	private static Configuration[] makeConfigurations(XmlPath manifest) {
		ArrayList<Configuration> configurations = new ArrayList<>();
		XmlPath node = manifest.getNodeList("/manifest/configuration");
		for(int i = 0; i < node.getCount(); i++) {
			String name = node.getAttribute(i, "name");
			String value = node.getAttribute(i, "value");
			configurations.add(new Configuration(name, value));
		}
		return configurations.toArray(new Configuration[configurations.size()]);
	}
}
