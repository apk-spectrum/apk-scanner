package com.apkscanner.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.apkscanner.apkinfo.PermissionGroup;
import com.apkscanner.apkinfo.PermissionInfo;
import com.apkscanner.apkinfo.ResourceInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.MyXPath;

public class PermissionGroupManager
{
	private HashMap<String, PermissionGroup> permGroupMap;

	public boolean hasSignatureLevel = false;
	public boolean hasSystemLevel = false;
	public boolean hasSignatureOrSystemLevel = false;

	private MyXPath xmlPermissions;
	private MyXPath xmlPermInfoDefault;
	private MyXPath xmlPermInfoLang;
	
	public PermissionGroupManager(PermissionInfo[] permList)
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
	
	public void setData(PermissionInfo[] permList)
	{
		if(permList == null) return;
		for(PermissionInfo permInfo: permList) {
			if(permInfo.permissionGroup != null) {
				PermissionGroup g = permGroupMap.get(permInfo.permissionGroup);
				if(g == null) {
					g = getPermissionGroup(permInfo.permissionGroup);
					permGroupMap.put(permInfo.permissionGroup, g);
				}
				g.permList.add(permInfo);

				if(permInfo.labels != null) {
					String description = permInfo.labels[0].name;
					for(ResourceInfo r: permInfo.labels) {
						if(r.configuration != null && r.configuration.equals(Resource.getLanguage())) {
							description = r.name;
							break;
						}
					}
					if(description != null)  {
						g.permSummary += "\n - " + description;
					}
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
