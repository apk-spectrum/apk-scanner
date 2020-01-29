package com.apkspectrum.data.apkinfo;

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
		if(glEsVersion != null) {
			if(name != null) report.append(", ");
			report.append("glEsVersion=0x" + Integer.toHexString(glEsVersion));
		}
		if(required != null) report.append(", required=" + required);
		report.append("\n");

		return report.toString();
	}
}
