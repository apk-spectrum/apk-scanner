package com.apkscanner.apkinfo;

public class ApkInfo
{
	public static final int APP_FEATURE_LAUNCHER = 0x1;
	public static final int APP_FEATURE_MAIN = 0x2;
	public static final int APP_FEATURE_STARTUP = 0x4;
	public static final int APP_FEATURE_DEBUGGABLE = 0x8;
	public static final int APP_FEATURE_SYSTEMUID = 0x10;

	public static final int APP_FEATURE_PLATFORM_SIGN = 0x100;
	public static final int APP_FEATURE_SAMSUNG_SIGN = 0x200;
	
	public static final int APP_FEATURE_HAS_SIGNATURE = 0x10000;
	public static final int APP_FEATURE_HAS_SIGNATUREORSYSTEM = 0x10000;
	public static final int APP_FEATURE_HAS_SYSTEM = 0x10000;
	
	// common
	public String filePath = null;
	public Long fileSize = null;
	public String tempWorkPath = null;

	public Integer featureFlags = 0;

	public Integer permissionProtectionLevel = 0;
	public Integer usesPermissionProtectionLevel = 0;
	
	public ManifestInfo manifest = new ManifestInfo();
	
	public WidgetInfo[] widgets = null;
	public String[] images = null;
	public String[] xmls = null;
	public String[] librarys = null;
	public String[] certificates = null;
	
	public String[] resourcesWithValue = null;
}
