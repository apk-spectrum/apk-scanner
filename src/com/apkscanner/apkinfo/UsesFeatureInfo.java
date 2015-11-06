package com.apkscanner.apkinfo;

public class UsesFeatureInfo
{
	public String name = null; //"string"
	public Boolean required = null;
	public Integer glEsVersion = null;
	
	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("uses feature : ");
		if(name != null) report.append(name);
		if(required != null) report.append(", required=" + required);
		if(glEsVersion != null) report.append(", glEsVersion=" + glEsVersion);
		report.append("\n");

		return report.toString();
	}
}
