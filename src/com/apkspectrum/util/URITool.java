package com.apkspectrum.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class URITool
{
	public static String encodeURI(String uri) {
		try {
			return new URI(null, null, uri, null).toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URI convertURI(String uri) {
		return URI.create(encodeURI(uri));
	}

	public static String convertJarURI(String jarPath, String entryPath) {
		return convertJarURI(new File(jarPath), entryPath);
	}

	public static String convertJarURI(File jarFile, String entryPath) {
		return "jar:" + jarFile.toURI() + "!/" + encodeURI(entryPath);
	}

	public static String getJarPath(URI jarEntryUri) {
		if(jarEntryUri != null) {
			if("jar".equals(jarEntryUri.getScheme())) {
				jarEntryUri = URITool.convertURI(jarEntryUri.getSchemeSpecificPart());
				String jarPath = jarEntryUri.getPath();
				if(jarPath.contains("!/"))
					jarPath = jarPath.split("!/", 2)[0];
				return new File(jarPath).getAbsolutePath();
			}
		}
		return null;
	}

	public static String getJarEntryPath(URI jarEntryUri) {
		if(jarEntryUri != null) {
			if("jar".equals(jarEntryUri.getScheme())) {
				jarEntryUri = URITool.convertURI(jarEntryUri.getSchemeSpecificPart());
				String jarPath = jarEntryUri.getPath();
				if(jarPath.contains("!/"))
					return jarPath.split("!/", 2)[1];
				return jarPath;
			}
		}
		return null;
	}
}
