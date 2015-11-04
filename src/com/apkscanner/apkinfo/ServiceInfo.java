package com.apkscanner.apkinfo;

public class ServiceInfo
{
	public Boolean enabled =  null;
	public Boolean exported = null;
	public ResourceInfo[] icons = null; // "drawable resource"
	public Boolean isolatedProcess = null;
	public ResourceInfo[] labels = null; // "string resource"
	public String name = null; // "string"
	public String permission = null; // "string"
	public String process = null; // "string"

	public IntentFilterInfo[] intentFilter = null;
	public MetaDataInfo[] metaData = null;
}
