package com.apkscanner.data.apkinfo;

public class ProviderInfo extends ComponentInfo
{
	public String[] authorities = null; // "list"
	public Boolean grantUriPermissions = null;
	public Integer initOrder = null;
	public Boolean multiprocess = null;
	public String process = null; // "string"
	public String readPermission = null; // "string"
	public Boolean syncable = null;
	public String writePermission = null; // "string"

	public GrantUriPermissionInfo[] grantUriPermission = null;
	public PathPermissionInfo[] pathPermission = null;

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
