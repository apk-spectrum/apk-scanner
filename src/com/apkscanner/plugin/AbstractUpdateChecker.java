package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractUpdateChecker extends AbstractPlugIn implements IUpdateChecker
{
	NetworkException lastNetworkException;

	public AbstractUpdateChecker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	NetworkException makeNetworkException(Exception e) {
		return lastNetworkException = new NetworkException(e);
	}

	public NetworkException getLastNetworkException() {
		return lastNetworkException;
	}
}
