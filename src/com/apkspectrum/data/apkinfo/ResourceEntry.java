package com.apkspectrum.data.apkinfo;

import java.util.Map.Entry;

public class ResourceEntry implements Entry<String, ResourceInfo[]>
{
	private String key;
	private ResourceInfo[] value;

	public ResourceEntry(String key, ResourceInfo[] value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public ResourceInfo[] getValue() {
		return value;
	}

	@Override
	public ResourceInfo[] setValue(ResourceInfo[] value) {
		return (this.value = value);
	}
}
