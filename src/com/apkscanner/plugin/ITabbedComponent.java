package com.apkscanner.plugin;

import java.awt.Component;

import javax.swing.Icon;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;

public abstract interface ITabbedComponent
{
	public Component getComponent();

	public void initialize();
	public void reloadResource();

	public String getTitle();
	public String getToolTip();
	public Icon getIcon();

	public void setData(ApkInfo apkInfo, Status status, ITabbedRequest request);
	public void clearData();
}