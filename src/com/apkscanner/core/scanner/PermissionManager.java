package com.apkscanner.core.scanner;

import java.util.ArrayList;

import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.XmlPath;

public class PermissionManager
{
	private static XmlPath xmlPermissions = null;
	private static XmlPath xmlPermInfoDefault = null;
	private static XmlPath xmlPermInfoLang = null;
	
	public static UsesPermissionInfo getUsesPermissionInfo(String name, String maxSdkVersion)
	{
		UsesPermissionInfo permInfo = new UsesPermissionInfo();
		permInfo.name = name;
    	if(maxSdkVersion != null && !maxSdkVersion.isEmpty()) {
    		permInfo.maxSdkVersion = Integer.parseInt(maxSdkVersion);
    	}

		loadXmlPath();

		if(xmlPermissions != null) {
			XmlPath permXPath = xmlPermissions.getNode("/manifest/permission[@name='" + name + "']");
			if(permXPath != null) {
				permInfo.permissionGroup = permXPath.getAttributes("android:permissionGroup");
				permInfo.labels = getInfoString(permXPath.getAttributes("android:label"));
				permInfo.descriptions = getInfoString(permXPath.getAttributes("android:description"));
				permInfo.protectionLevel = permXPath.getAttributes("android:protectionLevel");
				if(permInfo.permissionGroup == null) permInfo.permissionGroup = "Unspecified group";
				if(permInfo.protectionLevel == null) permInfo.protectionLevel = "normal";
			}
		}
		//Log.i(permInfo.permission + ", " + permInfo.permGroup + ", " + permInfo.label + ", " + permInfo.desc);
		return permInfo;
	}
	
	private static ResourceInfo[] getInfoString(String value)
	{
		if(value == null || !value.startsWith("@string")) {
			return new ResourceInfo[] { new ResourceInfo(value) };
		}
		String name = value.replace("@string/", "");
		
		ArrayList<ResourceInfo> resList = new ArrayList<ResourceInfo>();  
		
		if(xmlPermInfoDefault != null) {
			XmlPath infoXPath = xmlPermInfoDefault.getNode("/resources/string[@name='" + name + "']");
			if(infoXPath != null) {
				String result = infoXPath.getTextContent();
				if(result != null) result = result.replaceAll("\"", "");
				resList.add(new ResourceInfo(result, "default"));
			}
		}

		if(xmlPermInfoLang != null) {
			XmlPath infoXPath = xmlPermInfoLang.getNode("/resources/string[@name='" + name + "']");
			if(infoXPath != null) {
				String result = infoXPath.getTextContent();
				if(result != null) result = result.replaceAll("\"", "");
				resList.add(new ResourceInfo(result, Resource.getLanguage()));
			}
		}

		return resList.toArray(new ResourceInfo[0]);
	}
	
	private static void loadXmlPath()
	{
		if(xmlPermissions != null) return;
		
		String lang = Resource.getLanguage();

		xmlPermissions = new XmlPath(Resource.class.getResourceAsStream("/values/permissions-info/27/AndroidManifest.xml"));
		xmlPermInfoDefault = new XmlPath(Resource.class.getResourceAsStream("/values/permissions-info/27/strings.xml"));
		if(Resource.class.getResource("/values/permissions-info/27/strings-" + lang + ".xml") != null) {
			xmlPermInfoLang = new XmlPath(Resource.class.getResourceAsStream("/values/permissions-info/27/strings-" + lang + ".xml"));
		}
	}
}
