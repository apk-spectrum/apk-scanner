package com.apkscanner.plugin;

public interface IPlugInEventListener {
	public void onPluginLoaded();
	public void onUpdated(IUpdateChecker[] plugins);
	public boolean onUpdateFailed(IUpdateChecker plugin);
}
