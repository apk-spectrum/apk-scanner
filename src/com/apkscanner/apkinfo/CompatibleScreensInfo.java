package com.apkscanner.apkinfo;

public class CompatibleScreensInfo
{
	public class Screen {
		public String screenSize = null; //["small" | "normal" | "large" | "xlarge"]
		public String screenDensity = null; /* ["ldpi" | "mdpi" | "hdpi" | "xhdpi"
	                                   | "280" | "360" | "420" | "480" | "560" ] */
	}
	public Screen[] screen = null;
}
