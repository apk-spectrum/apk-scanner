package com.apkscanner.data.apkinfo;

public class ComponentInfo {
	public Boolean enabled =  null;
	public Boolean exported = null;
	public ResourceInfo[] icons = null; // "drawable resource"
	public ResourceInfo[] labels = null; // "string resource"
	public String name = null; // "string"
	public String permission = null; // "string"

	public MetaDataInfo[] metaData = null;

	public IntentFilterInfo[] intentFilter = null;

	public Integer featureFlag = 0;

	public String xmlString;
}
