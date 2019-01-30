package com.apkscanner.core.permissionmanager;

import java.util.Arrays;

import com.apkscanner.util.XmlPath;

public class PermissionGroupRecord extends UnitRecord<PermissionGroupInfoExt> {
	private PermissionManager manager;

	public PermissionGroupRecord(PermissionManager manager, XmlPath node) {
		super(PermissionGroupInfoExt.class, node);
		this.manager = manager;
	}

	@Override
	public PermissionGroupInfoExt getInfomation(int sdk) {
		PermissionGroupInfoExt info = super.getInfomation(sdk);
		if(info.permissions == null) {
			info.permissions = Arrays.asList(manager.getGroupPermissions(name, sdk));
		}
		return info;
	}
}
