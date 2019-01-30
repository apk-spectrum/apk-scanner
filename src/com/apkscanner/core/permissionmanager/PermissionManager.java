package com.apkscanner.core.permissionmanager;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class PermissionManager
{
	private static XmlPath xmlPermissionDB;

	private Map<String, PermissionRecord> recordMap;
	private Map<String, PermissionGroupRecord> recordGroupMap;

	static {
		xmlPermissionDB = new XmlPath(Resource.class.getResourceAsStream("/values/PermissionsHistory.xml"));
	}

	public PermissionManager() {
		this(null);
	}

	public PermissionManager(UsesPermissionInfo[] usesePermission) {
		recordMap = new HashMap<>();
		recordGroupMap = new HashMap<>();
		if(usesePermission != null) {
			for(UsesPermissionInfo info: usesePermission) {
				PermissionRecord record = getPermissionRecord(info.name);
				record.maxSdkVersion = info.maxSdkVersion;
				recordMap.put(info.name, record);
			}
		} else {
			XmlPath allPermissions = xmlPermissionDB.getNodeList("/permission-history/permissions/permission");
			for(int i=allPermissions.getCount()-1; i>=0; --i) {
				PermissionRecord record = new PermissionRecord(allPermissions.getNode(i));
				recordMap.put(record.name, record);
			}
		}
	}

	public PermissionRecord[] getPermissionRecords() {
		return recordMap.values().toArray(new PermissionRecord[recordMap.size()]);
	}

	public PermissionRecord getPermissionRecord(String name) {
		if(recordMap.containsKey(name)) {
			return (PermissionRecord) recordMap.get(name);
		}
		if(name == null || name.trim().isEmpty()) {
			Log.e("permission name is null or empty");
			return null;
		}
		XmlPath permission = xmlPermissionDB.getNode("/permission-history/permissions/permission[@name='" + name + "']");
		if(permission == null) {
			Log.e("Unknown permission : " + name);
			return null;
		}
		PermissionRecord perm = new PermissionRecord(permission);
		return perm;
	}

	public PermissionGroupRecord getPermissionGroupRecord(String name) {
		if(recordGroupMap.containsKey(name)) {
			return recordGroupMap.get(name);
		}
		if(name == null || name.trim().isEmpty()) {
			Log.e("permission name is null or empty");
			return null;
		}
		XmlPath node = xmlPermissionDB.getNode("/permission-history/permission-groups/permission-group[@name='" + name + "']");
		if(node == null) {
			Log.e("Unknown permission-goup : " + name);
			return null;
		}
		PermissionGroupRecord group = new PermissionGroupRecord(this, node);
		recordGroupMap.put(name, group);
		return group;
	}

	public PermissionInfoExt[] getPermissions(int sdk) {
		List<PermissionInfoExt> perms = new ArrayList<>();
		for(PermissionRecord record: recordMap.values()) {
			perms.add(record.getInfomation(sdk));
		}
		return perms.toArray(new PermissionInfoExt[perms.size()]);
	}

	public PermissionInfoExt getPermission(String name, int sdk) {
		PermissionRecord record = getPermissionRecord(name);
		return record != null ? record.getInfomation(sdk) : null;
	}

	public PermissionGroupInfoExt[] getPermissionGroups(int sdk) {
		Map<String, PermissionGroupInfoExt> groups = new HashMap<>();
		for(PermissionRecord record: recordMap.values()) {
			PermissionInfoExt permInfo = (PermissionInfoExt)record.getInfomation(sdk);
			String groupName = permInfo.permissionGroup;
			if(groupName == null || groupName.isEmpty()) groupName = "Unspecified Group";
			PermissionGroupInfoExt groupInfo = groups.get(groupName);
			if(groupInfo == null) {
				PermissionGroupRecord groupRecord = getPermissionGroupRecord(permInfo.permissionGroup);
				if(groupRecord != null) {
					groupInfo = groupRecord.getInfomation(sdk);
				} else {
					groupInfo = new PermissionGroupInfoExt();
					groupInfo.name = groupName;
					groupInfo.icon = "@drawable/perm_group_unknown";
					groupInfo.icons = getResource(groupInfo.icon, -1);
				}
				groupInfo.permissions = new ArrayList<>();
				groups.put(groupInfo.name, groupInfo);
			}
			groupInfo.permissions.add(permInfo);
		}
		return groups.values().toArray(new PermissionGroupInfoExt[groups.size()]);
	}

	public PermissionInfoExt[] getGroupPermissions(String groupName, int sdk) {
		List<PermissionInfoExt> list = new ArrayList<>();
		for(PermissionRecord record: recordMap.values()) {
			PermissionInfoExt info = record.getInfomation(sdk);
			if(info == null) continue;
			if((info.permissionGroup == null && groupName == null)
					|| (groupName != null && groupName.equals(info.permissionGroup))) {
				list.add(info);
			}
		}
		return list.toArray(new PermissionInfoExt[list.size()]);
	}

	public static PermissionRepository getPermissionRepository() {
		return new PermissionRepository(xmlPermissionDB.getNode("/permission-history/sources"));
	}

	public static ResourceInfo[] getResource(String name, int sdk) {
		if(name == null || name.isEmpty()) return null;
		ResourceInfo[] resVal = null;
		if(name.startsWith("@string/")) {
			XmlPath resources = xmlPermissionDB.getNodeList("/permission-history/resources/*");
			if(resources == null) return null;
			List<ResourceInfo> list = new ArrayList<>();

			String id = name.substring(8);
			for(int i = 0; i < resources.getCount(); i++) {
				XmlPath strings = resources.getNode(i).getNodeList("string[@name='" + id + "']");
				String value = null;
				for(int j = 0; j < strings.getCount(); j++) {
					String sdkVer = strings.getAttribute(j, "sdk");
					if(value == null || sdkVer == null
							|| (sdk > 0 && Integer.parseInt(sdkVer) >= sdk)) {
						value = strings.getTextContent(j);
						//Log.d("sdkVer " + sdkVer + " : " + value + " , " + sdk);
					} else {
						//Log.d("break sdkVer " + sdkVer + " : " + value + " + " + id);
						break;
					}
				}
				if(value != null) {
					String config = resources.getAttribute(i, "config");
					list.add(new ResourceInfo(value, config));
					//Log.d("value " + value + ", config " + config );
				}
			}
			resVal = list.toArray(new ResourceInfo[0]);
		} else if(name.startsWith("@drawable/")) {
			String id = name.substring(10);
			String path = null;
			URL url = Resource.class.getResource("/icons/" + id + ".png");
			if(url != null) path = url.getPath();
			resVal = new ResourceInfo[] { new ResourceInfo(path, null) };
		} else {
			resVal = new ResourceInfo[] { new ResourceInfo(name, null) };
		}
		return resVal;
	}
}
