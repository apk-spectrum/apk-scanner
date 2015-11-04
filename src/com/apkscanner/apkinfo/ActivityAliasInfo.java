package com.apkscanner.apkinfo;

public class ActivityAliasInfo
{
	public Boolean enabled =  null;
	public Boolean exported = null;
	public ResourceInfo[] icons = null; // "drawable resource"
	public ResourceInfo[] labels = null; // "string resource"
	public String name = null; // "string"
	public String permission = null; // "string"
	public String targetActivity = null; // "string"
	
	public IntentFilterInfo[] intentFilter = null;
	public MetaDataInfo[] metaData = null;
}
