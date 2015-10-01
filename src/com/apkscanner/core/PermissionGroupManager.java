package com.apkscanner.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.MyXPath;

public class PermissionGroupManager
{
	private String[] dangerousPermissions = new String[] {
			"android.permission.READ_CALENDAR",
			"android.permission.WRITE_CALENDAR",
			"android.permission.CAMERA",
			"android.permission.READ_CONTACTS",
			"android.permission.WRITE_CONTACTS",
			"android.permission.GET_ACCOUNT",
			"android.permission.ACCESS_FINE_LOCATION",
			"android.permission.ACCESS_COARSE_LOCATION",
			"android.permission.RECORD_AUDIO",
			"android.permission.READ_PHONE_STATE",
			"android.permission.CALL_PHONE",
			"android.permission.READ_CALL_LOG",
			"android.permission.WRITE_CALL_LOG",
			"com.android.voicemail.permission.ADD_VOICEMAIL",
			"android.permission.USE_SIP",
			"android.permission.PROCESS_OUTGOING_CALLS",
			"android.permission.BODY_SENSORS",
			"android.permission.SEND_SMS",
			"android.permission.RECEIVE_SMS",
			"android.permission.READ_SMS",
			"android.permission.RECEIVE_WAP_PUSH",
			"android.permission.RECEIVE_MMS",
			"android.permission.READ_CELL_BROADCASTS",
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE"
	};
	
	public class PermissionInfo
	{
		public String permission;
		public String permGroup;
		public String label;
		public String desc;
		public boolean isDangerous;
	}
	
	public class PermissionGroup
	{
		public String permGroup;
		public String label;
		public String desc;
		public String icon;
		public String permSummary;
		public ArrayList<PermissionInfo> permList; 
		public boolean hasDangerous;
	}
	
	private HashMap<String, PermissionGroup> permGroupMap;

	private MyXPath xmlPermissions;
	private MyXPath xmlPermInfoDefault;
	private MyXPath xmlPermInfoLang;
	
	public PermissionGroupManager(String[] permList)
	{
		String lang = Resource.getLanguage();
		
		//Log.i(getClass().getResource("/values/permissions.xml"));
		//Log.i(getClass().getResource("/values/permissions-info.xml"));
		//Log.i(getClass().getResource("/values/permissions-info-" + lang + ".xml"));

		xmlPermissions = new MyXPath(getClass().getResourceAsStream("/values/permissions.xml"));
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
		for(String perm: permList) {
			PermissionInfo permInfo = getPermissionInfo(perm);
			if(permInfo.permGroup != null) {
				PermissionGroup g = permGroupMap.get(permInfo.permGroup);
				if(g == null) {
					g = getPermissionGroup(permInfo.permGroup);
					permGroupMap.put(permInfo.permGroup, g);
				}
				g.permList.add(permInfo);
				if(permInfo.label != null) {
					g.permSummary += "\n - " + permInfo.label;
				}
				if(permInfo.isDangerous) {
					g.hasDangerous = true;
				}
			}
		}
	}
	
	private boolean checkDangerous(String perm)
	{
		for(String p: dangerousPermissions) {
			if(p.equals(perm)) {
				return true;
			}
		}
		return false;
	}
	
	public PermissionInfo getPermissionInfo(String perm)
	{
		PermissionInfo permInfo = new PermissionInfo();
		permInfo.permission = perm;
		permInfo.isDangerous = checkDangerous(perm);

		if(xmlPermissions != null) {
			MyXPath permXPath = xmlPermissions.getNode("/permissions/permission[@name='" + perm + "']");
			if(permXPath != null) {
				permInfo.permGroup = permXPath.getAttributes("android:permissionGroup");
				permInfo.label = getInfoString(permXPath.getAttributes("android:label"));
				permInfo.desc = getInfoString(permXPath.getAttributes("android:description"));
				if(permInfo.label != null) permInfo.label = permInfo.label.replaceAll("\"", "");
				if(permInfo.desc != null) permInfo.desc = permInfo.desc.replaceAll("\"", "");
			}
		}
		//Log.i(permInfo.permission + ", " + permInfo.permGroup + ", " + permInfo.label + ", " + permInfo.desc);
		return permInfo;
	}
	
	public PermissionGroup getPermissionGroup(String group)
	{
		PermissionGroup permGroup = new PermissionGroup();
		permGroup.permGroup = group;
		permGroup.permList = new ArrayList<PermissionInfo>();

		if(xmlPermissions != null) {
			MyXPath groupXPath = xmlPermissions.getNode("/permissions/permission-group[@name='" +  group + "']");
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
			permGroup.permSummary = "[" + permGroup.permGroup + "]";
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
