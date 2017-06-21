package com.apkscanner.data.apkinfo;

public class ResourceInfo
{
	public String name;
	public String configuration;
	
	public ResourceInfo(String name)
	{
		this(name, null);
	}
	
	public ResourceInfo(String name, String configuration)
	{
		this.name = name;
		this.configuration = configuration;
	}
	
}
