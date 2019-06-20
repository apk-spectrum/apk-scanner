package com.apkscanner.resource;

import java.awt.Image;
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

	enum Type { BIN, LIB, PLUGIN, SECURITY, RAW, ETC }
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
