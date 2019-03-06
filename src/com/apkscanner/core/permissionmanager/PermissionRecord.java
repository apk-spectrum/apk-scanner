package com.apkscanner.core.permissionmanager;

import com.apkscanner.util.XmlPath;

public class PermissionRecord extends UnitRecord<PermissionInfoExt>  {
	public Integer maxSdkVersion;
	public Boolean sdk23;

	public PermissionRecord(XmlPath node) {
		super(PermissionInfoExt.class, node);
	}
}