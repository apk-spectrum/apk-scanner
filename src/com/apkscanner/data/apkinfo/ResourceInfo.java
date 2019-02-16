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
		return objEquals(name, other.name)
				&& objEquals(configuration, other.configuration);
	}

	protected boolean objEquals(Object a, Object b) {
		return ((a == null && b == null) || (a != null && a.equals(b)));
	}
}
