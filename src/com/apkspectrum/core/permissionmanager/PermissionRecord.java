package com.apkspectrum.core.permissionmanager;

import com.apkspectrum.util.XmlPath;

public class PermissionRecord extends UnitRecord<PermissionInfoExt>  {
	public Integer maxSdkVersion;
	public Boolean sdk23;

	public PermissionRecord(XmlPath node) {
		super(PermissionInfoExt.class, node);
	}
}
