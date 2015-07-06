package com.ApkInfo.Resource;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.ImageIcon;

import com.ApkInfo.Core.CoreApkTool;

public enum Resource
{
	STR_APP_NAME				(Type.TEXT, "APK Scanner"),
	STR_APP_VERSION				(Type.TEXT, "Ver. 1.01"),

	IMG_TOOLBAR_OPEN			(Type.IMAGE, "toolbar_open.png"),
	IMG_TOOLBAR_OPEN_HOVER		(Type.IMAGE, "toolbar_open_hover.png"),
	IMG_TOOLBAR_MANIFEST		(Type.IMAGE, "toolbar_manifast.png"),
	IMG_TOOLBAR_MANIFEST_HOVER	(Type.IMAGE, "toolbar_manifast_hover.png"),
	IMG_TOOLBAR_EXPLORER		(Type.IMAGE, "toolbar_explorer.png"),
	IMG_TOOLBAR_EXPLORER_HOVER	(Type.IMAGE, "toolbar_explorer_hover.png"),
	IMG_TOOLBAR_PACK			(Type.IMAGE, "toolbar_pack.png"),
	IMG_TOOLBAR_PACK_HOVER		(Type.IMAGE, "toolbar_pack_hover.png"),
	IMG_TOOLBAR_UNPACK			(Type.IMAGE, "toolbar_unpack.png"),
	IMG_TOOLBAR_UNPACK_HOVER	(Type.IMAGE, "toolbar_unpack_hover.png"),
	IMG_TOOLBAR_INSTALL			(Type.IMAGE, "toolbar_install.png"),
	IMG_TOOLBAR_INSTALL_HOVER	(Type.IMAGE, "toolbar_install_hover.png"),
	IMG_TOOLBAR_ABOUT			(Type.IMAGE, "toolbar_about.png"),
	IMG_TOOLBAR_ABOUT_HOVER		(Type.IMAGE, "toolbar_about_hover.png"),
	
	IMG_APP_ICON		(Type.IMAGE, "AppIcon.png"),
	IMG_QUESTION		(Type.IMAGE, "question.png"),
	IMG_WARNING			(Type.IMAGE, "warning.png"),
	IMG_SUCCESS			(Type.IMAGE, "Succes.png"),
	IMG_INSTALL_WAIT	(Type.IMAGE, "install_wait.gif"),
	IMG_LOADING			(Type.IMAGE, "loading.gif"),

	BIN_ADB_LNX			(Type.BIN, "adb"),
	BIN_ADB_WIN			(Type.BIN, "adb.exe"),
	BIN_APKTOOL_JAR		(Type.BIN, "apktool.jar");
	
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
	
	public ImageIcon getImageIcon(int width, int height)
	{
		if(type != Type.IMAGE) return null;
		ImageIcon tempImg = new ImageIcon(CoreApkTool.getScaledImage(new ImageIcon(getPath()),width,height));
		
		return tempImg;
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
