package com.apkscanner.plugin;

public interface IUpdateChecker extends IPlugIn {
	public String getNewVersion();
	public void launch();
}
