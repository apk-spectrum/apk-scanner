package com.apkscanner.plugin;

public interface IUpdateChecker extends IPlugIn {
	public String getNewVersion();
	public boolean checkNewVersion();
	public void launch();
}
