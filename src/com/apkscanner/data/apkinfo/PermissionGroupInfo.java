package com.apkscanner.data.apkinfo;

import java.util.Arrays;

import com.apkscanner.resource.Resource;

public class PermissionGroupInfo
{
	public ResourceInfo[] descriptions = null; // "string resource"
	public ResourceInfo[] icons = null; // "drawable resource"
	public ResourceInfo[] labels = null; // "string resource"
    public String name = null; // "string"
    public int priority;

	public PermissionGroupInfo() { }

	public PermissionGroupInfo(PermissionGroupInfo info) {
		descriptions = info.descriptions;
		icons = info.icons;
		labels = info.labels;
		name = info.name;
	}

	@Override
	public boolean equals(Object target) {
		if(!(target instanceof PermissionGroupInfo)) return false;
		PermissionGroupInfo other = (PermissionGroupInfo) target;
		return stringEquals(name, other.name)
				&& Arrays.deepEquals(labels, other.labels)
				&& Arrays.deepEquals(descriptions, other.descriptions)
				&& Arrays.deepEquals(icons, other.icons);
	}

	protected boolean stringEquals(String a, String b) {
		return ((a == null && b == null) || (a != null && a.equals(b)));
	}

    public String getLabel() {
    	return ApkInfoHelper.getResourceValue(labels, (String)Resource.PROP_PREFERRED_LANGUAGE.getData(""));
    }

    public String getDescription() {
    	return ApkInfoHelper.getResourceValue(descriptions, (String)Resource.PROP_PREFERRED_LANGUAGE.getData(""));
    }
}
