package com.apkspectrum.core.permissionmanager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.apkspectrum.core.permissionmanager.RevokedPermissionInfo.RevokedReason;
import com.apkspectrum.data.apkinfo.PermissionInfo;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.data.apkinfo.UsesPermissionInfo;
import com.apkspectrum.data.apkinfo.UsesPermissionSdk23Info;
import com.apkspectrum.resource._RFile;
import com.apkspectrum.resource._RImg;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.XmlPath;

public class PermissionManager
{
	public static final String GROUP_NAME_UNSPECIFIED = "Unspecified Group";
	public static final String GROUP_NAME_REVOKED = "Revoked Group";
	public static final String GROUP_NAME_DECLARED = "Declared Group";

	private static XmlPath xmlPermissionDB;

	public enum UsesPermissionTag {
		UsesPermission, UsesPermissionSdk23, All
	}

	private Map<String, PermissionRecord> recordMap;
	private Map<String, PermissionGroupRecord> recordGroupMap;
	private Map<Integer, Map<String, PermissionGroupInfoExt>> cacheGroupInfo;
	private Map<String, DeclaredPermissionInfo> declaredMap;
	private Map<String, UsesPermissionInfo> unknownSource;

	private boolean isPlatformSigned;
	private boolean isSignAsRevoked;

	private int sdkVersion = -1;
	private int targetSdkVersion = -1;

	static {
		String xmlPath = _RFile.DATA_PERMISSIONS_HISTORY.getPath();
		File xmlFile = new File(xmlPath);
		if(xmlFile.canRead()) {
			xmlPermissionDB = new XmlPath(xmlFile);
		} else {
			xmlPermissionDB = new XmlPath(_RFile.RAW_PERMISSIONS_HISTORY.getResourceAsStream());
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
		manager.setPlatformSigned(true);
		return manager;
	}

	private void addAllPermissions() {
		XmlPath allPermissions = xmlPermissionDB.getNodeList("/permission-history/permissions/permission");
		synchronized(recordMap) {
			for(int i=allPermissions.getCount()-1; i>=0; --i) {
				PermissionRecord record = new PermissionRecord(allPermissions.getNode(i));
				recordMap.put(record.name, record);
			}
		}
	}

	public void addUsesPermission(UsesPermissionInfo[] usesePermission) {
		if(usesePermission != null && usesePermission.length > 0) {
			synchronized(recordMap) {
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

	public void setPlatformSigned(boolean isPlatformSigned) {
		this.isPlatformSigned = isPlatformSigned;
		recordGroupMap.clear();
		cacheGroupInfo.clear();
	}

	public boolean isPlatformSigned() {
		return isPlatformSigned;
	}

	public void setTreatSignAsRevoked(boolean isSignAsRevoked) {
		this.isSignAsRevoked = isSignAsRevoked;
		recordGroupMap.clear();
		cacheGroupInfo.clear();
	}

	public boolean isTreatSignAsRevoked() {
		return isSignAsRevoked;
	}

	public void clearPermissions() {
		synchronized(recordMap) {
			recordMap.clear();
			recordGroupMap.clear();
			cacheGroupInfo.clear();
			declaredMap.clear();
			unknownSource.clear();
			isPlatformSigned = false;
		}
	}

	public int getCount() {
		return recordMap.size() + declaredMap.size() + unknownSource.size();
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}

	public void setSdkVersion(int sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public int getSdkVersion() {
		return sdkVersion;
	}

	public void setTargetSdkVersion(int targetSdkVersion) {
		this.targetSdkVersion = targetSdkVersion;
	}

	public int getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public PermissionRecord[] getPermissionRecords() {
		synchronized(recordMap) {
			return recordMap.values().toArray(new PermissionRecord[recordMap.size()]);
		}
	}

	public PermissionRecord getPermissionRecord(String name) {
		synchronized(recordMap) {
			if(recordMap.containsKey(name)) {
				return (PermissionRecord) recordMap.get(name);
			}
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
		synchronized(recordMap) {
			for(PermissionRecord record: recordMap.values()) {
				switch(tag) {
				case UsesPermission:
					if(record.sdk23) continue; break;
				case UsesPermissionSdk23:
					if(!record.sdk23) continue; break;
				default: break;
				}
				RevokedPermissionInfo reason = makeRevokedReason(record, sdk);
				if(reason == null || reason.reason != RevokedReason.NO_REVOKED) continue;
				PermissionInfoExt info = record.getInfomation(sdk);
				if(info != null) perms.add(info);
			}
		}
		for(DeclaredPermissionInfo declared: declaredMap.values()) {
			switch(tag) {
			case UsesPermission:
				if(declared.sdk23) continue; break;
			case UsesPermissionSdk23:
				if(!declared.sdk23) continue; break;
			default: break;
			}
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(declared, sdk);
			if(reason.reason == RevokedReason.NO_REVOKED) perms.add(declared);
		}
		return perms.toArray(new PermissionInfo[perms.size()]);
	}

	public RevokedPermissionInfo[] getRevokedPermissions() {
		return getRevokedPermissions(sdkVersion);
	}

	public RevokedPermissionInfo[] getRevokedPermissions(int sdk) {
		List<RevokedPermissionInfo> perms = new ArrayList<>();
		synchronized(recordMap) {
			for(PermissionRecord record: recordMap.values()) {
				RevokedPermissionInfo reason = makeRevokedReason(record, sdk);
				if(reason.reason != RevokedReason.NO_REVOKED) perms.add(reason);
			}
		}
		for(DeclaredPermissionInfo declared: declaredMap.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(declared, sdk);
			if(reason.reason != RevokedReason.NO_REVOKED) perms.add(reason);
		}
		for(UsesPermissionInfo info: unknownSource.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(info);
			if(reason.reason != RevokedReason.NO_REVOKED) perms.add(reason);
		}
		return perms.toArray(new RevokedPermissionInfo[perms.size()]);
	}

	public PermissionInfo getPermission(String name) {
		return getPermission(name, sdkVersion);
	}

	public PermissionInfo getPermission(String name, int sdk) {
		PermissionRecord record = getPermissionRecord(name);
		if(record == null) return declaredMap.get(name);
		RevokedPermissionInfo reason = makeRevokedReason(record, sdk);
		return reason.reason != RevokedReason.NO_REVOKED ? record.getInfomation(sdk) : null;
	}

	public RevokedPermissionInfo getRevokedPermission(String name) {
		return getRevokedPermission(name);
	}

	public RevokedPermissionInfo getRevokedPermission(String name, int sdk) {
		PermissionRecord record = getPermissionRecord(name);
		if(record != null) {
			return makeRevokedReason(record, sdk);
		} else if(declaredMap.containsKey(name)) {
			return RevokedPermissionInfo.makeRevokedReason(declaredMap.get(name), sdk);
		} else if(unknownSource.containsKey(name)) {
			return RevokedPermissionInfo.makeRevokedReason(unknownSource.get(name));
		}
		RevokedPermissionInfo reason = new RevokedPermissionInfo();
		reason.name = name;
		reason.reason = RevokedReason.UNKNOWN_REASON;
		return reason;
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
		if(groups != null) {
			return groups.values().toArray(new PermissionGroupInfoExt[groups.size()]);
		}

		synchronized(recordMap) {
			groups = new HashMap<>();
			for(PermissionRecord record: recordMap.values()) {
				RevokedPermissionInfo reason = makeRevokedReason(record, sdk);
				String groupName = null;
				PermissionInfo permInfo = null;
				if(reason == null || reason.reason != RevokedReason.NO_REVOKED) {
					groupName = GROUP_NAME_REVOKED;
				} else {
					permInfo = (PermissionInfoExt)record.getInfomation(sdk);
					if(permInfo != null) groupName = permInfo.permissionGroup;
					if(groupName == null || groupName.isEmpty()) {
						groupName = GROUP_NAME_UNSPECIFIED;
					}
				}
				PermissionGroupInfoExt groupInfo = groups.get(groupName);
				if(groupInfo == null) {
					PermissionGroupRecord groupRecord = getPermissionGroupRecord(groupName);
					if(groupRecord != null) {
						groupInfo = groupRecord.getInfomation(sdk);
						if(groupInfo != null && (groupInfo.icon == null || groupInfo.icon.trim().isEmpty())) {
							groupInfo.icon = groupRecord.getPresentIcon();
							groupInfo.icons = getResource(groupInfo.icon, -1);
						}
						if(groupInfo != null) {
							groupInfo.permissions = new ArrayList<>();
						}
					}
					if(groupInfo == null) groupInfo = makeGroup(groupName);
					groups.put(groupInfo.name, groupInfo);
				}
				if(permInfo instanceof PermissionInfoExt) {
					groupInfo.protectionFlags |= ((PermissionInfoExt)permInfo).protectionFlags;
				}
				groupInfo.permissions.add(permInfo != null ? permInfo : reason);
			}
		}
		for(DeclaredPermissionInfo declared: declaredMap.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(declared, sdk);
			boolean isGrant = reason.reason == RevokedReason.NO_REVOKED;
			PermissionGroupInfoExt groupInfo = groups.get(GROUP_NAME_DECLARED);
			if(groupInfo == null) groupInfo = makeGroup(GROUP_NAME_DECLARED);
			groupInfo.permissions.add(isGrant ? declared : reason);
			groups.put(groupInfo.name, groupInfo);
		}
		for(UsesPermissionInfo info: unknownSource.values()) {
			RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(info);
			PermissionGroupInfoExt groupInfo = groups.get(GROUP_NAME_REVOKED);
			if(groupInfo == null) groupInfo = makeGroup(GROUP_NAME_REVOKED);
			groupInfo.permissions.add(reason);
			groups.put(groupInfo.name, groupInfo);
		}

		PermissionGroupInfoExt[] result = groups.values().toArray(new PermissionGroupInfoExt[groups.size()]);
		Arrays.sort(result, new Comparator<PermissionGroupInfoExt>() {
			@Override
			public int compare(PermissionGroupInfoExt info1, PermissionGroupInfoExt info2) {
				return info2.getPriority() - info1.getPriority();
			}
		});

		groups = new LinkedHashMap<>();
		for(PermissionGroupInfoExt info: result) {
			groups.put(info.name, info);
		}
		cacheGroupInfo.put(sdk, groups);

		return result;
	}

	private PermissionGroupInfoExt makeGroup(String name) {
		PermissionGroupInfoExt groupInfo = new PermissionGroupInfoExt();
		groupInfo.name = name;
		switch(name) {
		case GROUP_NAME_REVOKED:
			groupInfo.priority = -1000;
			groupInfo.icon = "@drawable/perm_group_revoked";
			break;
		case GROUP_NAME_DECLARED:
			groupInfo.priority = -200;
			groupInfo.icon = "@drawable/perm_group_declared";
			//https://www.flaticon.com/free-icon/contract_684872#term=pencil&page=1&position=36
			break;
		case GROUP_NAME_UNSPECIFIED:
		default:
			groupInfo.priority = -100;
			groupInfo.icon = "@drawable/perm_group_unknown";
			break;
		}
		groupInfo.icons = getResource(groupInfo.icon, -1);
		groupInfo.permissions = new ArrayList<>();
		return groupInfo;
	}

	public PermissionInfo[] getGroupPermissions(String groupName) {
		return getGroupPermissions(groupName, sdkVersion);
	}

	public PermissionInfo[] getGroupPermissions(String groupName, int sdk) {
		List<PermissionInfo> list = new ArrayList<>();
		if(GROUP_NAME_UNSPECIFIED.equals(groupName)) {
			groupName = "";
		}
		synchronized(recordMap) {
			for(PermissionRecord record: recordMap.values()) {
				RevokedPermissionInfo reason = makeRevokedReason(record, sdk);
				if(reason == null || reason.reason != RevokedReason.NO_REVOKED) continue;
				PermissionInfoExt info = record.getInfomation(sdk);
				if(((info.permissionGroup == null || info.permissionGroup.isEmpty())
						&& (groupName == null || groupName.isEmpty()))
						|| (groupName != null && groupName.equals(info.permissionGroup))) {
					list.add(info);
				}
			}
		}
		if(GROUP_NAME_DECLARED.equals(groupName)) {
			for(DeclaredPermissionInfo declared: declaredMap.values()) {
				RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(declared, sdk);
				boolean isGrant = reason.reason == RevokedReason.NO_REVOKED;
				list.add(isGrant ? declared : reason);
			}
		}
		if(GROUP_NAME_REVOKED.equals(groupName)) {
			for(UsesPermissionInfo info: unknownSource.values()) {
				RevokedPermissionInfo reason = RevokedPermissionInfo.makeRevokedReason(info);
				list.add(reason);
			}
		}

		return list.toArray(new PermissionInfo[list.size()]);
	}

	public PermissionInfo[] getDeclarePermissions() {
		return declaredMap.values().toArray(new PermissionInfo[declaredMap.size()]);
	}

	public UsesPermissionInfo[] getUnknownSourcePermissions() {
		return unknownSource.values().toArray(new UsesPermissionInfo[unknownSource.size()]);
	}

	public RevokedPermissionInfo makeRevokedReason(PermissionRecord record, int sdk) {
		return RevokedPermissionInfo.makeRevokedReason(record, sdk, targetSdkVersion, (!isPlatformSigned && isSignAsRevoked));
	}

	public static PermissionRepository getPermissionRepository() {
		XmlPath node = xmlPermissionDB.getNode("/permission-history/sources");
		return node != null ? new PermissionRepository(node) : null;
	}

	public ResourceInfo[] getResource(String name) {
		return getResource(name, sdkVersion);
	}

	public static ResourceInfo[] getResource(String name, int sdk) {
		if(name == null || name.isEmpty()) return null;
		ResourceInfo[] resVal = null;
		if(name.startsWith("@string/")) {
			XmlPath resources = null;
			synchronized(xmlPermissionDB) {
				resources = xmlPermissionDB.getNodeList("/permission-history/resources/*");
			}
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
			String path = id;
			URL url = _RImg.valueOf(id.toUpperCase()).getURL();
			if(url != null) path = url.toString();
			resVal = new ResourceInfo[] { new ResourceInfo(path, null) };
		} else {
			resVal = new ResourceInfo[] { new ResourceInfo(name, null) };
		}
		return resVal;
	}
}
