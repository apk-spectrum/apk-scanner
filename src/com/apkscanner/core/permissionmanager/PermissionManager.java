package com.apkscanner.core.permissionmanager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apkscanner.core.permissionmanager.RevokedPermissionInfo.RevokedReason;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.data.apkinfo.UsesPermissionSdk23Info;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class PermissionManager
{
	private static XmlPath xmlPermissionDB;

	public enum UsesPermissionTag {
		UsesPermission, UsesPermissionSdk23, All
	}

	private Map<String, PermissionRecord> recordMap;
	private Map<String, PermissionGroupRecord> recordGroupMap;
	private Map<Integer, Map<String, PermissionGroupInfoExt>> cacheGroupInfo;
	private Map<String, DeclaredPermissionInfo> declaredMap;
	private Map<String, UsesPermissionInfo> unknownSource;

	private int sdkVersion = -1;

	static {
		String xmlPath = Resource.getUTF8Path() + File.separator + "data" + File.separator + "PermissionsHistory.xml";
		File xmlFile = new File(xmlPath);
		if(xmlFile.canRead()) {
			xmlPermissionDB = new XmlPath(xmlFile);
		} else {
			xmlPermissionDB = new XmlPath(Resource.class.getResourceAsStream("/values/PermissionsHistory.xml"));
		}
	}

	public PermissionManager() {
		recordMap = new HashMap<>();
		recordGroupMap = new HashMap<>();
		cacheGroupInfo = new HashMap<>();
		declaredMap = new HashMap<>();
		unknownSource = new HashMap<>();
	}

	public PermissionManager(UsesPermissionInfo[] usesePermission) {
		this();
		addUsesPermission(usesePermission);
	}

	public static PermissionManager createAllPermissionManager() {
		PermissionManager manager = new PermissionManager();
		manager.addAllPermissions();
		return manager;
	}

	private void addAllPermissions() {
		XmlPath allPermissions = xmlPermissionDB.getNodeList("/permission-history/permissions/permission");
		for(int i=allPermissions.getCount()-1; i>=0; --i) {
			PermissionRecord record = new PermissionRecord(allPermissions.getNode(i));
			recordMap.put(record.name, record);
		}
	}

	public void addUsesPermission(UsesPermissionInfo[] usesePermission) {
		if(usesePermission != null && usesePermission.length > 0) {
			for(UsesPermissionInfo info: usesePermission) {
				PermissionRecord record = getPermissionRecord(info.name);
				if(record == null) {
					Log.v("record is null : " + info.name);
					if(declaredMap.containsKey(info.name)) {
						DeclaredPermissionInfo declared = declaredMap.get(info.name);
						declared.isUsed = true;
						declared.sdk23 = info instanceof UsesPermissionSdk23Info;
						declared.maxSdkVersion = info.maxSdkVersion;
					} else {
						unknownSource.put(info.name, info);
					}
					continue;
				}
				record.maxSdkVersion = info.maxSdkVersion;
				record.sdk23 = info instanceof UsesPermissionSdk23Info;
				recordMap.put(info.name, record);
			}
			cacheGroupInfo.clear();
		}
	}

	public void addDeclarePemission(PermissionInfo[] permissions) {
		if(permissions == null) return;
		for(PermissionInfo info: permissions) {
			if(info == null || info.name == null || info.name.trim().isEmpty()) continue;
			DeclaredPermissionInfo declared = new DeclaredPermissionInfo(info);
			if(unknownSource.containsKey(info.name)) {
				UsesPermissionInfo usesInfo = unknownSource.get(info.name);
				declared.isUsed = true;
				declared.sdk23 = usesInfo instanceof UsesPermissionSdk23Info;
				declared.maxSdkVersion = usesInfo.maxSdkVersion;
				unknownSource.remove(info.name);
			}
			declaredMap.put(info.name, declared);
		}
	}

	public void clearPermissions() {
		recordMap.clear();
		recordGroupMap.clear();
		cacheGroupInfo.clear();
		declaredMap.clear();
		unknownSource.clear();
	}

	public void setSdkVersion(int sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public int getSdkVersion() {
		return sdkVersion;
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
			Log.v("Unknown permission : " + name);
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
			Log.d("permission name is null or empty");
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

	public PermissionInfo[] getPermissions() {
		return getPermissions(sdkVersion, UsesPermissionTag.All);
	}

	public PermissionInfo[] getPermissions(UsesPermissionTag tag) {
		return getPermissions(sdkVersion, tag);
	}

	public PermissionInfo[] getPermissions(int sdk) {
		return getPermissions(sdk, UsesPermissionTag.All);
	}

	public PermissionInfo[] getPermissions(int sdk, UsesPermissionTag tag) {
		List<PermissionInfo> perms = new ArrayList<>();
		for(PermissionRecord record: recordMap.values()) {
			switch(tag) {
			case UsesPermission:
				if(record.sdk23) continue; break;
			case UsesPermissionSdk23:
				if(!record.sdk23) continue; break;
			default: break;
			}
			PermissionInfoExt info = record.getInfomation(sdk);
			if(info != null) perms.add(info);
		}
		for(DeclaredPermissionInfo declared: declaredMap.values()) {
			switch(tag) {
			case UsesPermission:
				if(declared.sdk23) continue; break;
			case UsesPermissionSdk23:
				if(!declared.sdk23) continue; break;
			default: break;
			}
			perms.add(declared);
		}
		return perms.toArray(new PermissionInfo[perms.size()]);
	}

	public RevokedPermissionInfo[] getRevokedPermissions() {
		return getRevokedPermissions(sdkVersion);
	}

	public RevokedPermissionInfo[] getRevokedPermissions(int sdk) {
		List<RevokedPermissionInfo> perms = new ArrayList<>();
		for(PermissionRecord record: recordMap.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(record, sdk);
			if(reason.reason != RevokedReason.NO_REVOKED) perms.add(reason);
		}
		for(DeclaredPermissionInfo declared: declaredMap.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(declared, sdk);
			if(reason.reason != RevokedReason.NO_REVOKED) perms.add(reason);
		}
		for(UsesPermissionInfo info: unknownSource.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(info, sdk);
			if(reason.reason != RevokedReason.NO_REVOKED) perms.add(reason);
		}
		return perms.toArray(new RevokedPermissionInfo[perms.size()]);
	}

	public PermissionInfo getPermission(String name) {
		return getPermission(name, sdkVersion);
	}

	public PermissionInfo getPermission(String name, int sdk) {
		PermissionRecord record = getPermissionRecord(name);
		return record != null ? record.getInfomation(sdk) : null;
	}

	public PermissionGroupInfoExt getPermissionGroup(String name) {
		return getPermissionGroup(name, sdkVersion);
	}

	public PermissionGroupInfoExt getPermissionGroup(String name, int sdk) {
		Map<String, PermissionGroupInfoExt> groups = cacheGroupInfo.get(sdk);
		if(groups == null) {
			getPermissionGroups(sdk);
			groups = cacheGroupInfo.get(sdk);
		}
		return groups != null ? groups.get(name) : null;
	}

	public PermissionGroupInfoExt[] getPermissionGroups() {
		return getPermissionGroups(sdkVersion);
	}

	public PermissionGroupInfoExt[] getPermissionGroups(int sdk) {
		Map<String, PermissionGroupInfoExt> groups = cacheGroupInfo.get(sdk);
		if(groups == null) {
			groups = new HashMap<>();
			for(PermissionRecord record: recordMap.values()) {
				PermissionInfoExt permInfo = (PermissionInfoExt)record.getInfomation(sdk);
				if(permInfo == null) {
					Log.v("permission info is null : " + record.name + " in " + sdk);
					continue;
				}
				String groupName = permInfo.permissionGroup;
				if(groupName == null || groupName.isEmpty()) groupName = "Unspecified Group";
				PermissionGroupInfoExt groupInfo = groups.get(groupName);
				if(groupInfo == null) {
					PermissionGroupRecord groupRecord = getPermissionGroupRecord(permInfo.permissionGroup);
					if(groupRecord != null) {
						groupInfo = groupRecord.getInfomation(sdk);
						if(groupInfo != null && (groupInfo.icon == null || groupInfo.icon.trim().isEmpty())) {
							groupInfo.icon = groupRecord.getInfomation(1000).icon;
							Log.v("group icon is null " + groupInfo.name);
							if(groupInfo.icon == null || groupInfo.icon.trim().isEmpty()) {
								groupInfo.icon = "@drawable/perm_group_unknown";
							}
						}
					}
					if(groupInfo == null){
						groupInfo = new PermissionGroupInfoExt();
						groupInfo.name = groupName;
						groupInfo.icon = "@drawable/perm_group_unknown";
						groupInfo.icons = getResource(groupInfo.icon, -1);
					}
					groupInfo.permissions = new ArrayList<>();
					groups.put(groupInfo.name, groupInfo);
				}
				groupInfo.protectionFlags |= permInfo.protectionFlags;
				groupInfo.permissions.add(permInfo);
			}
			cacheGroupInfo.put(sdk, groups);
		}
		PermissionGroupInfoExt[] result = groups.values().toArray(new PermissionGroupInfoExt[groups.size()]);
		Arrays.sort(result, new Comparator<PermissionGroupInfoExt>() {
			@Override
			public int compare(PermissionGroupInfoExt info1, PermissionGroupInfoExt info2) {
				int priority1 = info1.priority != null ? info1.priority : 0;
				int priority2 = info2.priority != null ? info2.priority : 0;
				return priority2 - priority1;
			}
		});
		return result;
	}

	public PermissionInfo[] getGroupPermissions(String groupName) {
		return getGroupPermissions(groupName, sdkVersion);
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

	public PermissionInfo[] getDeclarePermissions() {
		return declaredMap.values().toArray(new PermissionInfo[declaredMap.size()]);
	}

	public static PermissionRepository getPermissionRepository() {
		return new PermissionRepository(xmlPermissionDB.getNode("/permission-history/sources"));
	}

	public ResourceInfo[] getResource(String name) {
		return getResource(name, sdkVersion);
	}

	public static ResourceInfo[] getResource(String name, int sdk) {
		if(name == null || name.isEmpty()) return null;
		ResourceInfo[] resVal = null;
		if(name.startsWith("@string/")) {
			XmlPath resources = xmlPermissionDB.getNodeList("/permission-history/resources/*");
			if(resources == null) return new ResourceInfo[] { new ResourceInfo(name, null) };
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
						if(value != null) value = value.replaceAll("\"", "");
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
			resVal = list.toArray(new ResourceInfo[list.size()]);
		} else if(name.startsWith("@drawable/")) {
			String id = name.substring(10);
			String path = "/icons/" + id + ".png";
			URL url = Resource.class.getResource(path);
			if(url != null) path = url.toString();
			resVal = new ResourceInfo[] { new ResourceInfo(path, null) };
		} else {
			resVal = new ResourceInfo[] { new ResourceInfo(name, null) };
		}
		return resVal;
	}
}
