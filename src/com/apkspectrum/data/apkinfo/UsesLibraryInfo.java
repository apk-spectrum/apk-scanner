package com.apkspectrum.data.apkinfo;

public class UsesLibraryInfo
{
	public String name = null; //"string"
	public Boolean required = null;

	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("uses feature : ");
		if(name != null) report.append(name);
		if(required != null) report.append(", required=" + required);
		report.append("\n");

		return report.toString();
	}
}
