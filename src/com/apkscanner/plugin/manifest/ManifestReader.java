package com.apkscanner.plugin.manifest;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

		if(manifest.getNodeList("/manifest").getLength() != 1) {
			throw new InvalidManifestException("Must have only one <manifest> tag on root");
		}

		if(manifest.getNodeList("/manifest/plugin").getLength() != 1) {
			throw new InvalidManifestException("Must have only one <plugin> tag on manifest");
		}

		if(manifest.getNode("/manifest/plugin").getChildNodes().getLength() == 0) {
			throw new InvalidManifestException("No have plugin");
		}

		XmlPath node = manifest.getNode("/manifest");
		String packageName = node.getAttributes("package");
		String versionName = node.getAttributes("versionName");
		int versionCode = node.getAttributes("versionCode") != null ? Integer.valueOf(node.getAttributes("versionCode")) : 0;
		String minScannerVersion = node.getAttributes("minScannerVersion");

		PlugIn plugin = makePlugin(manifest);
		Resources[] resources = makeResources(manifest);

		return new Manifest(packageName, versionName, versionCode, minScannerVersion, plugin, resources);
	}

	static private PlugIn makePlugin(@NonNull XmlPath manifest) {
		XmlPath node = manifest.getNode("/manifest/plugin");
		boolean enable = !"false".equals(node.getAttributes("enable"));
		String label = node.getAttributes("label");
		String icon = node.getAttributes("icon");
		String description = node.getAttributes("description");
		int pluginSize = manifest.getNode("/manifest/plugin").getChildNodes().getLength();

		Component[] components =  makeComponents(manifest);
		return new PlugIn(enable, label, icon, description, pluginSize, components);
	}

	static private Component[] makeComponents(@NonNull XmlPath manifest) {
		ArrayList<Component> components = new ArrayList<Component>();
		NodeList list = manifest.getNode("/manifest/plugin").getChildNodes().getNodeList();
		for(int i = 0; i < list.getLength(); i++) {
			Node element = list.item(i);
			if(element.getNodeType() == Node.ELEMENT_NODE) {
				int type;
				switch(element.getNodeName()) {
					case "package-searcher": type = Component.TYPE_PACAKGE_SEARCHER; break;
					case "package-searcher-linker": type = Component.TYPE_PACAKGE_SEARCHER_LINKER; break;
					case "update-checker": type = Component.TYPE_UPDATE_CHECKER; break;
					case "update-checker-linker": type = Component.TYPE_UPDATE_CHECKER_LINKER; break;
					case "external-tool": type = Component.TYPE_EXTERNAL_TOOL; break;
					case "external-tool-linker": type = Component.TYPE_EXTERNAL_TOOL_LINKER; break;
					default: type = Component.TYPE_UNKNWON;
				}
				XmlPath node = new XmlPath(element);
				boolean enable = !"false".equals(node.getAttributes("enable"));
				String label = node.getAttributes("label");
				String icon = node.getAttributes("icon");
				String description = node.getAttributes("description");
				String name = node.getAttributes("name");
				String url = node.getAttributes("url");

				//Linker[] linkers = makeLinker(node);
				String target = null;
				String preferLang = null;
				String path = null;
				String param = null;
				String updateUrl = null;
				switch(type) {
				case Component.TYPE_PACAKGE_SEARCHER_LINKER:
					target = node.getAttributes("target");
					preferLang = node.getAttributes("preferLanguage");
					break;
				case Component.TYPE_UPDATE_CHECKER_LINKER:
					updateUrl = node.getAttributes("updateUrl");
					break;
				case Component.TYPE_EXTERNAL_TOOL_LINKER:
					path = node.getAttributes("path");
					param = node.getAttributes("param");
					break;
				}
				components.add(new Component(type, enable, label, icon, description, name, url, /* linkers */
						target, preferLang, path, param, updateUrl));
			}
		}
		return components.toArray(new Component[components.size()]);
	}

	@SuppressWarnings("unused")
	static private Linker[] makeLinker(@NonNull XmlPath component) {
		//Log.d("makeLinker() " + component.toString());
		ArrayList<Linker> linkers = new ArrayList<Linker>();
		XmlPath node = component.getChildNodes();
		for(int i = 0; i < node.getLength(); i++) {
			if(node.getNodeList().item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			if(!"linker".equals(node.getNodeList().item(i).getNodeName())) {
				Log.w("this is no linker node : " + node.getNodeList().item(i).getNodeName());
				continue;
			}
			String url = node.getAttributes(i, "url");
			String target = node.getAttributes(i, "target");
			String preferLang = node.getAttributes(i, "preferLang");
			linkers.add(new Linker(url, target, preferLang));
		}
		return !linkers.isEmpty() ? linkers.toArray(new Linker[linkers.size()]) : null;
	}

	private static Resources[] makeResources(XmlPath manifest) {
		ArrayList<Resources> resources = new ArrayList<Resources>();
		XmlPath node = manifest.getNodeList("/manifest/resources");
		for(int i = 0; i < node.getLength(); i++) {
			String src = node.getAttributes(i, "src");
			String lang = node.getAttributes(i, "lang");
			ArrayList<StringData> datas = new ArrayList<StringData>();
			XmlPath child = node.getChildNodes(i);
			
			for(int j=0; j<child.getLength(); j++) {
				if(child.getNodeList().item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				if(!"string".equals(child.getNodeList().item(j).getNodeName())) {
					Log.w("this is no string node : " + child.getNodeList().item(j).getNodeName());
					continue;
				}
				String name = child.getAttributes(j, "name");
				String data = child.getTextContent(j);
				datas.add(new StringData(name, data));	
			}
			resources.add(new Resources(src, lang, datas.toArray(new StringData[datas.size()])));
		}
		return resources.toArray(new Resources[resources.size()]);
	}
}
