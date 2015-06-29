package com.ApkInfo.Resource;

import java.io.File;

import javax.swing.ImageIcon;

import com.ApkInfo.Core.CoreApkTool;

public enum Resource
{
	IMG_APP_ICON		(Type.IMAGE, "AppIcon.png"),
	IMG_QUESTION		(Type.IMAGE, "question.png"),
	IMG_WARNING			(Type.IMAGE, "warning.png"),
	IMG_SUCCESS			(Type.IMAGE, "Succes.png"),
	IMG_INSTALL_WAIT	(Type.IMAGE, "install_wait.gif"),
	IMG_LOADING			(Type.IMAGE, "loading.gif");
	
	private enum Type {
		IMAGE,
		BIN,
		ETC
	}

	private String name;
	private Type type;
	private Resource(Type type, String name)
	{
		this.type = type;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPath()
	{
		String subPath;
		switch(type){
		case IMAGE:
			subPath = "";
			break;
		case BIN:
			subPath = "";
			break;
		case ETC: default:
			subPath = "";
			break;
		}
		return CoreApkTool.GetUTF8Path() + File.separator + subPath + name;
	}
	
	public ImageIcon getImageIcon()
	{
		if(type != Type.IMAGE) return null;
		return new ImageIcon(getPath());
	}
}
