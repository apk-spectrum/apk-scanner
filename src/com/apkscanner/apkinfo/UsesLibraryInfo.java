package com.apkscanner.apkinfo;

public class UsesLibraryInfo
{
	public String name = null; //"string"
	public Boolean required = null;

	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("uses feature : ");
		if(name != null) report.append("  name : " + name + "\n");
		if(required != null) report.append("  required : " + required + "\n");

		return report.toString();
	}
}
