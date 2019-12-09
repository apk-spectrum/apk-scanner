package com.apkscanner.gui.tabpanels;

import java.io.File;

public class ResourceObject {
	public static final int ATTR_AXML = 1;
	public static final int ATTR_XML = 2;
	public static final int ATTR_IMG = 3;
	public static final int ATTR_QMG = 4;
	public static final int ATTR_TXT = 5;
	public static final int ATTR_CERT = 6;
	public static final int ATTR_FS_IMG = 7;
	public static final int ATTR_ETC = 8;

	public String label;
	public Boolean isFolder;
	public String path;
	public String config;
	public ResourceType type;
	public int attr;
	public int childCount;
	public Boolean isLoading;

	public ResourceObject(File file) {
		label = file.getName();
		path = file.getAbsolutePath();
		isFolder = file.isDirectory();

		type = ResourceType.LOCAL;
		isLoading = false;
		childCount = 0;

		setAttr();
	}

	public ResourceObject(String path, boolean isFolder) {
		this.path = path;
		this.isFolder = isFolder;
		this.isLoading = false;
		if (path.startsWith("res/animator")) {
			type = ResourceType.ANIMATOR;
		} else if (path.startsWith("res/anim")) {
			type = ResourceType.ANIM;
		} else if (path.startsWith("res/color")) {
			type = ResourceType.COLOR;
		} else if (path.startsWith("res/drawable")) {
			type = ResourceType.DRAWABLE;
		} else if (path.startsWith("res/mipmap")) {
			type = ResourceType.MIPMAP;
		} else if (path.startsWith("res/layout")) {
			type = ResourceType.LAYOUT;
		} else if (path.startsWith("res/menu")) {
			type = ResourceType.MENU;
		} else if (path.startsWith("res/raw")) {
			type = ResourceType.RAW;
		} else if (path.startsWith("res/values")) {
			type = ResourceType.VALUES;
		} else if (path.startsWith("res/xml")) {
			type = ResourceType.XML;
		} else if (path.startsWith("assets")) {
			type = ResourceType.ASSET;
		} else if(path.startsWith("META-INF")) {
			type = ResourceType.METAINF;
		} else {
			type = ResourceType.ETC;
		}

		if (type.getInt() <= ResourceType.XML.getInt()) {
			if (path.startsWith("res/" + type.toString() + "-"))
				config = path.replaceAll("res/" + type.toString() + "-([^/]*)/.*", "$1");
		}

		setAttr();

		if (isFolder) {
			label = Resources.getOnlyFoldername(path);
		} else {
			label = Resources.getOnlyFilename(path);
		}

		childCount = 0;
	}

	private void setAttr() {
		String extension = path.replaceAll(".*/", "").replaceAll(".*\\.", ".").toLowerCase();

		if (extension.endsWith(".xml")) {
			if (path.startsWith("res/") || path.equals("AndroidManifest.xml"))
				attr = ATTR_AXML;
			else
				attr = ATTR_XML;
		} else if (extension.endsWith(".png") || extension.endsWith(".jpg") || extension.endsWith(".gif")
				|| extension.endsWith(".bmp") || extension.endsWith(".webp")) {
			attr = ATTR_IMG;
		} else if (extension.endsWith(".qmg")) {
			attr = ATTR_QMG;
		} else if (extension.endsWith(".txt") || extension.endsWith(".mk") || extension.endsWith(".html")
				|| extension.endsWith(".js") || extension.endsWith(".css") || extension.endsWith(".json")
				|| extension.endsWith(".props") || extension.endsWith(".properties") || extension.endsWith(".policy")
				|| extension.endsWith(".mf") || extension.endsWith(".sf") || extension.endsWith(".rc")
				|| extension.endsWith(".version") || extension.endsWith(".default")) {
			attr = ATTR_TXT;
		} else if(extension.endsWith(".rsa") || extension.endsWith(".dsa") || extension.endsWith(".ec")) {
			attr = ATTR_CERT;
		} else if(extension.endsWith(".img")) {
			attr = ATTR_FS_IMG;
		} else {
			attr = ATTR_ETC;
		}
	}

	@Override
	public String toString() {
		String str = null;
		if (childCount > 0) {
			str = label + " (" + childCount + ")";
		} else if (config != null && !config.isEmpty()) {
			str = label + " (" + config + ")";
		} else {
			str = label;
		}
		return str;
	}

	public void setLoadingState(Boolean state) {
		isLoading = state;
	}

	public Boolean getLoadingState() {
		return isLoading;
	}
}