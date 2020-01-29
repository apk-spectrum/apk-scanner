package com.apkspectrum.data.apkinfo;

public class UsesConfigurationInfo
{
	public Boolean reqFiveWayNav = null;
	public Boolean reqHardKeyboard = null;
	public String reqKeyboardType = null; //["undefined" | "nokeys" | "qwerty" | "twelvekey"]
	public String reqNavigation = null; //["undefined" | "nonav" | "dpad" | "trackball" | "wheel"]
	public String reqTouchScreen = null; //["undefined" | "notouch" | "stylus" | "finger"]

	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("uses configuration : \n");
		if(reqFiveWayNav != null) report.append("  reqFiveWayNav : " + reqFiveWayNav + "\n");
		if(reqHardKeyboard != null) report.append("  reqHardKeyboard : " + reqHardKeyboard + "\n");
		if(reqKeyboardType != null) report.append("  reqKeyboardType : " + reqKeyboardType + "\n");
		if(reqNavigation != null) report.append("  reqNavigation : " + reqNavigation + "\n");
		if(reqTouchScreen != null) report.append("  reqTouchScreen : " + reqTouchScreen + "\n");

		return report.toString();
	}
}
