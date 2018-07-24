package com.apkscanner.plugin;

public interface IExternalTool extends IPlugIn {
	public void launch();
	public boolean isDecorderTool();
	public boolean isDiffTool();
	public boolean isNormalTool();
}
