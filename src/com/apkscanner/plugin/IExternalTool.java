package com.apkscanner.plugin;

public interface IExternalTool extends IPlugIn {
	public static final String TYPE_NORMAL_TOOL = "normal";
	public static final String TYPE_DECORDER_TOOL = "decorder";
	public static final String TYPE_DIFF_TOOL = "difftool";
	public static final String TYPE_UNKNOWN = "unknown";

	public String getToolType();
	public boolean isDecorderTool();
	public boolean isDiffTool();
	public boolean isNormalTool();
	public boolean isSupoortedOS();
}
