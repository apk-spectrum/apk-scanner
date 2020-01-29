package com.apkspectrum.core.permissionmanager;

public interface UnitInformation {
	public String getName();
	public int getApiLevel();
	public int getProtectionFlags();
	public String getLabel();
	public String getDescription();
	public String getNonLocalizedDescription();
	public String getIcon();
	public String getRequest();
	public int getPriority();
	public String getProtectionLevel();
	public String getPermissionGroup();
	public String getPermissionFlags();
}
