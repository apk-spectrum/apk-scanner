package com.ApkInfo.Resource;

import java.io.File;

import javax.swing.ImageIcon;

import com.ApkInfo.Core.CoreApkTool;

public enum Resource
{
	APP_ICON("AppIcon.png"),
	QUESTION("question.png"),
	WARNING("warning.png"),
	SUCCESS("Succes.png"),
	INSTALL_WAIT("install_wait.gif"),
	LOADING("loading.gif");
	

	private String name;
	private Resource(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPath()
	{
		return CoreApkTool.GetUTF8Path() + File.separator + name;
	}
	
	public ImageIcon getImageIcon()
	{
		return new ImageIcon(getPath());
	}
}
