package com.apkscanner.apkinfo;

public class CompatibleScreensInfo
{
	public static class Screen {
		public String screenSize = null; //["small" | "normal" | "large" | "xlarge"]
		public String screenDensity = null; /* ["ldpi" | "mdpi" | "hdpi" | "xhdpi"
	                                   | "280" | "360" | "420" | "480" | "560" ] */
	}
	public Screen[] screen = null;
	
	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("compatible screens :\n");
		for(Screen s: screen) {
			if(s.screenSize != null) {
				report.append("  screenSize : " + s.screenSize);
				if(s.screenDensity != null) {
					report.append(", screenDensity : " + s.screenDensity +"\n");
				}
			} else if(s.screenDensity != null) {
				report.append("   screenDensity : " + s.screenDensity +"\n");
			}
		}

		return report.toString();
	}
}
