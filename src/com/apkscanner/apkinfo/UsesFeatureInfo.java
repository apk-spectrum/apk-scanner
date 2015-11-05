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
		if(name != null) report.append("  name : " + name + "\n");
		if(required != null) report.append("  required : " + required + "\n");
		if(glEsVersion != null) report.append("  glEsVersion : " + glEsVersion + "\n");

		return report.toString();
	}
}
