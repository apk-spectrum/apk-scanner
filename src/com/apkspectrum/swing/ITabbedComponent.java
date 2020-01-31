package com.apkspectrum.swing;

import java.awt.Component;

import javax.swing.Icon;

public abstract interface ITabbedComponent<T>
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

	public void setData(T apkInfo, int status);
	public void clearData();

	public void setPriority(int priority);
	public void setTabbedRequest(ITabbedRequest request);
}