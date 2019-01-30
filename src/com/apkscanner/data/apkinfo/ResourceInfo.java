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

	@Override
	public boolean equals(Object target) {
		if(!(target instanceof ResourceInfo)) return false;
		ResourceInfo other = (ResourceInfo) target;
		return stringEquals(name, other.name)
				&& stringEquals(configuration, other.configuration);
	}

	protected boolean stringEquals(String a, String b) {
		return ((a == null && b == null) || (a != null && a.equals(b)));
	}
}
