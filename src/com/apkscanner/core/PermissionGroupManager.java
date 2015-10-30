package com.apkscanner.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.MyXPath;

public class PermissionGroupManager
{
	public class PermissionInfo
	{
		public String name;
		public String icon;
		public String label;
		public String description;
		public String group;
		public String protectionLevel;
		
		public boolean isDangerousLevel()
		{
			return (protectionLevel != null && protectionLevel.indexOf("dangerous") > -1) ? true : false;
		}
		
		public boolean isSignatureLevel()
		{
			return (protectionLevel != null
					&& protectionLevel.indexOf("signature") > -1 && protectionLevel.indexOf("ystem") == -1 && protectionLevel.indexOf("privileged") == -1) ? true : false;
		}
		
		public boolean isSignatureOrSystemLevel()
		{
			return (protectionLevel != null
					&& protectionLevel.indexOf("signature") > -1 && (protectionLevel.indexOf("ystem") > -1 || protectionLevel.indexOf("privileged") > -1)) ? true : false;
		}

		public boolean isSystemLevel()
		{
			return (protectionLevel != null && protectionLevel.indexOf("signature") == -1
					&& (protectionLevel.indexOf("ystem") > -1 || protectionLevel.indexOf("privileged") > -1)) ? true : false;
		}
	}
	
	public class PermissionGroup
	{
		public String name;
		public String label;
		public String desc;
		public String icon;
		public String permSummary;
		public ArrayList<PermissionInfo> permList; 
		public boolean hasDangerous;
	}
	
	private HashMap<String, PermissionGroup> permGroupMap;

	public boolean hasSignatureLevel = false;
	public boolean hasSystemLevel = false;
	public boolean hasSignatureOrSystemLevel = false;

	private MyXPath xmlPermissions;
	private MyXPath xmlPermInfoDefault;
	private MyXPath xmlPermInfoLang;
	
	public PermissionGroupManager(String[] permList)
	{
		String lang = Resource.getLanguage();
		
		//Log.i(getClass().getResource("/values/permissions.xml"));
		//Log.i(getClass().getResource("/values/permissions-info.xml"));
		//Log.i(getClass().getResource("/values/permissions-info-" + lang + ".xml"));

		xmlPermissions = new MyXPath(getClass().getResourceAsStream("/values/AndroidManifest_SDK23.xml"));
		xmlPermInfoDefault = new MyXPath(getClass().getResourceAsStream("/values/permissions-info.xml"));
		if(getClass().getResource("/values/permissions-info-" + lang + ".xml") != null) {
			xmlPermInfoLang = new MyXPath(getClass().getResourceAsStream("/values/permissions-info-" + lang + ".xml"));
		}
		
		permGroupMap = new HashMap<String, PermissionGroup>();
		
		setData(permList);
	}
	
	public HashMap<String, PermissionGroup> getPermGroupMap(){
		return permGroupMap;
	}
	
	public void setData(String[] permList)
	{
		if(permList == null) return;
		for(String perm: permList) {
			PermissionInfo permInfo = getPermissionInfo(perm);
			if(permInfo.group != null) {
				PermissionGroup g = permGroupMap.get(permInfo.group);
				if(g == null) {
					g = getPermissionGroup(permInfo.group);
					permGroupMap.put(permInfo.group, g);
				}
				g.permList.add(permInfo);
				if(permInfo.label != null) {
					g.permSummary += "\n - " + permInfo.label;
				}
				if(permInfo.isDangerousLevel()) {
					g.hasDangerous = true;
				}
				if(permInfo.isSignatureLevel()) {
					Log.i("SignatureLevel : " + permInfo.name);
					hasSignatureLevel = true;
				}
				if(permInfo.isSignatureOrSystemLevel()) {
					Log.i("SignatureOrSystemLevel : " + permInfo.name);
					hasSignatureOrSystemLevel = true;
				}
				if(permInfo.isSystemLevel()) {
					Log.i("SystemLevel : " + permInfo.name);
					hasSystemLevel = true;
				}
			}
		}
	}
	
	public PermissionInfo getPermissionInfo(String name)
	{
		PermissionInfo permInfo = new PermissionInfo();
		permInfo.name = name;

		if(xmlPermissions != null) {
			MyXPath permXPath = xmlPermissions.getNode("/manifest/permission[@name='" + name + "']");
			if(permXPath != null) {
				permInfo.group = permXPath.getAttributes("android:permissionGroup");
				permInfo.label = getInfoString(permXPath.getAttributes("android:label"));
				permInfo.description = getInfoString(permXPath.getAttributes("android:description"));
				permInfo.protectionLevel = permXPath.getAttributes("android:protectionLevel");
				if(permInfo.group == null) permInfo.group = "Unspecified group";
				if(permInfo.protectionLevel == null) permInfo.protectionLevel = "normal";
				if(permInfo.label != null) permInfo.label = permInfo.label.replaceAll("\"", "");
				if(permInfo.description != null) permInfo.description = permInfo.description.replaceAll("\"", "");
			}
		}
		//Log.i(permInfo.permission + ", " + permInfo.permGroup + ", " + permInfo.label + ", " + permInfo.desc);
		return permInfo;
	}
	
	public PermissionGroup getPermissionGroup(String group)
	{
		PermissionGroup permGroup = new PermissionGroup();
		permGroup.name = group;
		permGroup.permList = new ArrayList<PermissionInfo>();

		if(xmlPermissions != null) {
			MyXPath groupXPath = xmlPermissions.getNode("/manifest/permission-group[@name='" +  group + "']");
			if(groupXPath != null) {
				permGroup.icon = getIconPath(groupXPath.getAttributes("android:icon"));
				permGroup.label = getInfoString(groupXPath.getAttributes("android:label"));
				permGroup.desc = getInfoString(groupXPath.getAttributes("android:description"));
				if(permGroup.label != null) permGroup.label = permGroup.label.replaceAll("\"", "");
				if(permGroup.desc != null) permGroup.desc = permGroup.desc.replaceAll("\"", "");
			}
		}
		if(permGroup.label != null) {
			permGroup.permSummary = "[" + permGroup.label + "] : " + permGroup.desc;
		} else {
			permGroup.permSummary = "[" + permGroup.name + "]";
		}
		
		//Log.i(permGroup.icon + ", " + permGroup.permGroup + ", " + permGroup.label + ", " + permGroup.desc);
		return permGroup;
	}
	
	public String getInfoString(String value)
	{
		if(value == null || !value.startsWith("@string")) {
			return value;
		}
		String name = value.replace("@string/", "");
		
		String result = null;
		if(xmlPermInfoLang != null) {
			MyXPath infoXPath = xmlPermInfoLang.getNode("/permission-info/string[@name='" + name + "']");
			if(infoXPath != null) {
				result = infoXPath.getTextContent();
			}
		}

		if(result == null && xmlPermInfoDefault != null) {
			MyXPath infoXPath = xmlPermInfoDefault.getNode("/permission-info/string[@name='" + name + "']");
			if(infoXPath != null) {
				result = infoXPath.getTextContent();
			}
		}

		return result;
	}
	
	public String getIconPath(String value)
	{
		if(value == null || !value.startsWith("@drawable")) {
			value = "@drawable/perm_group_unknown";
		}
		String path = value.replace("@drawable/", "");
		
		if(getClass().getResource("/icons/" + path + ".png") != null) {
			path = getClass().getResource("/icons/" + path + ".png").toString();
		} else {
			//path = getClass().getResource("/icons/perm_group_default.png").toString();
		}
		
		return path;
	}
}
