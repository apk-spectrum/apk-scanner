package com.apkspectrum.core.permissionmanager;

import com.apkspectrum.data.apkinfo.PermissionInfo;

public class DeclaredPermissionInfo extends PermissionInfo {
	public boolean isUsed;
	public boolean sdk23;
	public Integer maxSdkVersion;
	public DeclaredPermissionInfo(PermissionInfo info) {
		super(info);
	}
}