package com.apkscanner.plugin;

public interface IUpdateChecker extends IPlugIn {
	public String getNewVersion() throws NetworkException;
	public boolean checkNewVersion() throws NetworkException;
	public void launch() throws NetworkException;
}
