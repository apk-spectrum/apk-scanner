package com.ApkInfo.Resource;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.ImageIcon;

public enum Resource
{
	STR_APP_VERSION		(Type.TEXT, "Ver. 1.01"),

	IMG_APP_ICON		(Type.IMAGE, "AppIcon.png"),
	IMG_QUESTION		(Type.IMAGE, "question.png"),
	IMG_WARNING			(Type.IMAGE, "warning.png"),
	IMG_SUCCESS			(Type.IMAGE, "Succes.png"),
	IMG_INSTALL_WAIT	(Type.IMAGE, "install_wait.gif"),
	IMG_LOADING			(Type.IMAGE, "loading.gif"),

	BIN_ADB_LNX			(Type.BIN, "adb"),
	BIN_ADB_WIN			(Type.BIN, "adb.exe"),

	BIN_APKTOOL_JAR		(Type.ETC, "apktool.jar");
	
	private enum Type {
		IMAGE,
		TEXT,
		BIN,
		ETC
	}

	private String value;
	private Type type;
	private Resource(Type type, String value)
	{
		this.type = type;
		this.value = value;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String getPath()
	{
		if(type == Type.TEXT) return null;

		String subPath;
		switch(type){
		case IMAGE:
			subPath = File.separator + "res";
			break;
		case BIN:
			subPath = File.separator + "tool";
			break;
		case ETC: default:
			subPath = "";
			break;
		}
		return getUTF8Path() + subPath + File.separator + value;
	}
	
	public ImageIcon getImageIcon()
	{
		if(type != Type.IMAGE) return null;
		return new ImageIcon(getPath());
	}

	private String getUTF8Path()
	{
		String resourcePath = Resource.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		resourcePath = (new File(resourcePath)).getParentFile().getPath();
		
		try {
			resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return resourcePath;
	}
}
