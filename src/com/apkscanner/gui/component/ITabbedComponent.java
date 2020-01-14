package com.apkscanner.gui.component;

import java.awt.Component;

import javax.swing.Icon;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;

public abstract interface ITabbedComponent
{
	public Component getComponent();

	public void initialize();

	public boolean isTabbedVisible();
	public boolean isTabbedEnabled();

	public void setTabbedEnabled(boolean enabled);
	public void setTabbedVisible(boolean enabled);

	public String getTitle();
	public String getToolTip();
	public Icon getIcon();
	public int getPriority();

	public void setData(ApkInfo apkInfo, Status status);
	public void clearData();

	public void setPriority(int priority);
	public void setTabbedRequest(ITabbedRequest request);
}