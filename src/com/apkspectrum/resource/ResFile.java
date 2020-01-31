package com.apkspectrum.resource;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public interface ResFile<T> extends ResValue<T>
{
	public String getPath();
	public URL getURL();

	public enum Type {
		BIN("tool"),
		DATA("data"),
		LIB("lib"),
		PLUGIN("plugin"),
		SECURITY("security"),
		ETC(""),
		RES_VALUE(null),
		RES_ROOT(null);

		private static String basePath;
		private String value;

		static {
			basePath = ResFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			basePath = (new File(basePath)).getParentFile().getPath();
			try {
				basePath = URLDecoder.decode(basePath, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		Type(String value) {
			this.value = value;
		}

		public String getPath() {
			if(value == null) return null;
			return basePath + (!value.isEmpty() ? File.separator + value : "");
		}
	}
}
