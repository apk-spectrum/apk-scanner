package com.apkscanner.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;

import com.apkscanner.annotations.NonNull;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public final class Manifest {

	String packageName;
	String versionName;
	Integer versionCode;
	
	String pluginLabel;
	String pluginIcon;
	int pluginType;
	
	
	@SuppressWarnings("unused")
	private LinkedHashMap<String, String> plugins = new LinkedHashMap<String, String>();

	public Manifest() {
		
	}
	
	public Manifest(@NonNull File path) throws InvalidManifestException {
		if(path == null || !path.canRead()) {
			Log.w("path is null or can not read");
			return;
		}
		setXmlPath(new XmlPath(path));
	}

	public Manifest(@NonNull InputStream input) throws InvalidManifestException {
		if(input == null) {
			Log.w("input is null");
			return;
		}
		setXmlPath(new XmlPath(input));
	}

	public boolean setXmlPath(@NonNull XmlPath manifest) throws InvalidManifestException {
		if(manifest == null) {
			new InvalidManifestException("XmlPath is null", new NullPointerException());
		}

		if(manifest.getNodeList("/manifest").getLength() != 1) {
			throw new InvalidManifestException("Must have only one <manifest> tag on root");
		}

		if(manifest.getNodeList("/manifest/plugin").getLength() != 1) {
			throw new InvalidManifestException("Must have only one <plugin> tag on manifest");
		}

		if(manifest.getNode("/manifest/application").getChildNodes().getLength() == 0) {
			throw new InvalidManifestException("No have plugin");
		}

		XmlPath topManifest = manifest.getNode("/manifest");
		packageName = topManifest.getAttributes("package");
		
		

		return true;
	}
	
}
