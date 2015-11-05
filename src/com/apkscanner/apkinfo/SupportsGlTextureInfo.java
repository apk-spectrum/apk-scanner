package com.apkscanner.apkinfo;

public class SupportsGlTextureInfo
{
	public String name = null; // "string"
	
	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		
		report.append("supports gl texture : ");
		report.append(name + "\n");

		return report.toString();
	}
}
