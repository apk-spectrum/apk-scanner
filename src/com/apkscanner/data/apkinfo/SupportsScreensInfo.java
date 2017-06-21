package com.apkscanner.data.apkinfo;

public class SupportsScreensInfo
{
	public Boolean resizeable = null;
	public Boolean smallScreens = null;
	public Boolean normalScreens = null;
	public Boolean largeScreens = null;
	public Boolean xlargeScreens = null;
	public Boolean anyDensity = null;
	public Integer requiresSmallestWidthDp = null;
	public Integer compatibleWidthLimitDp = null;
	public Integer largestWidthLimitDp = null;
	
	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("supports screens :\n");
		if(resizeable != null) report.append("  resizeable : " + resizeable + "\n");
		if(smallScreens != null) report.append("  smallScreens : " + smallScreens + "\n");
		if(normalScreens != null) report.append("  normalScreens : " + normalScreens + "\n");
		if(largeScreens != null) report.append("  largeScreens : " + largeScreens + "\n");
		if(xlargeScreens != null) report.append("  xlargeScreens : " + xlargeScreens + "\n");
		if(anyDensity != null) report.append("  anyDensity : " + anyDensity + "\n");
		if(requiresSmallestWidthDp != null) report.append("  requiresSmallestWidthDp : " + requiresSmallestWidthDp + "\n");
		if(compatibleWidthLimitDp != null) report.append("  compatibleWidthLimitDp : " + compatibleWidthLimitDp + "\n");
		if(largestWidthLimitDp != null) report.append("  largestWidthLimitDp : " + largestWidthLimitDp + "\n");

		return report.toString();
	}
}
