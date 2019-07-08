package com.apkscanner.resource;

import java.awt.Image;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

public interface Res<T>
{
	public T get();
}

interface ResValue<T> extends Res<T>
{
	public String getValue();
}

interface ResString<T> extends ResValue<T>
{
	public String getString();
}

interface ResFile<T> extends ResValue<T>
{
	public String getPath();
	public URL getURL();

	enum Type {
		BIN("tool"), DATA("data"), LIB("lib"), PLUGIN("plugin"), SECURITY("security"), ETC(""),
		RES_VALUE(null), RES_ROOT(null);

		private String path;

		Type(String name) {
			if(name == null) return;
			path = RFile.getUTF8Path() + (!name.isEmpty() ? File.separator + name : "");
		}

		String getPath() { return path; }
	}
}

interface ResImage<T> extends ResFile<T>
{
	public Image getImage();
	public ImageIcon getImageIcon();
}

interface ResProp<T> extends Res<T>
{
	public void set(T data);
}
