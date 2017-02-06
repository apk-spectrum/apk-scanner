package com.apkscanner.apkinfo;

public class ProviderInfo
{
	public String[] authorities = null; // "list"
	public Boolean enabled =  null;
	public Boolean exported = null;
	public Boolean grantUriPermissions = null;
	public ResourceInfo[] icons = null; // "drawable resource"
	public Integer initOrder = null;
	public ResourceInfo[] labels = null; // "string resource"
	public Boolean multiprocess = null;
	public String name = null; // "string"
	public String permission = null; // "string"
	public String process = null; // "string"
	public String readPermission = null; // "string"
	public Boolean syncable = null;
	public String writePermission = null; // "string"

	public MetaDataInfo[] metaData = null;
	public GrantUriPermissionInfo[] grantUriPermission = null;
	public PathPermissionInfo[] pathPermission = null;
	
	public Integer featureFlag = 0;
	
	public String getReport()
	{
		StringBuilder report = new StringBuilder();
		report.append("name : " + name + "\n");
		if(enabled != null) report.append("enabled : " + enabled + "\n");
		if(exported != null) report.append("exported : " + exported + "\n");
		if(permission != null) report.append("permission : " + permission + "\n");
		if(readPermission != null) report.append("readPermission : " + readPermission + "\n");
		if(writePermission != null) report.append("writePermission : " + writePermission + "\n");
		
		return report.toString();
	}
}
