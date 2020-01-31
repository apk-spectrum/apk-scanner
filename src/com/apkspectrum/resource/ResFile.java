package com.apkspectrum.resource;

import java.io.File;
import java.net.URL;

public interface ResFile<T> extends ResValue<T>
{
	public String getPath();
	public URL getURL();

	enum Type {
		BIN("tool"), DATA("data"), LIB("lib"), PLUGIN("plugin"), SECURITY("security"), ETC(""),
		RES_VALUE(null), RES_ROOT(null);

		private String path;

		Type(String name) {
			if(name == null) return;
			path = _RFile.getUTF8Path() + (!name.isEmpty() ? File.separator + name : "");
		}

		public String getPath() { return path; }
	}
}
