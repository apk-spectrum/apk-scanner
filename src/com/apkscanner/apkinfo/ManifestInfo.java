package com.apkscanner.apkinfo;

public class ManifestInfo
{
	public static final int MANIFEST_FEATURE_LAUNCHUR = 0x1;
	public static final int MANIFEST_FEATURE_STARTUP = 0x2;
	public static final int MANIFEST_FEATURE_INSTRUMENTATION = 0x4;
	public static final int MANIFEST_FEATURE_SYSTEMUID = 0x8;

	// manifest attributes
	public String packageName = null; // "string"
	public String sharedUserId = null; // "string"
	public ResourceInfo[] sharedUserLabels = null; // "string resource" 
	public Integer versionCode = null; // "integer"
	public String versionName = null; // "string"
	public String installLocation = null; // ["auto" | "internalOnly" | "preferExternal"]

	public ApplicationInfo application = new ApplicationInfo();
	public CompatibleScreensInfo[] compatibleScreens = null;
	public InstrumentationInfo[] instrumentation = null;
	public PermissionInfo[] permission = null;
	public PermissionGroupInfo[] permissionGroup = null;
	public PermissionTreeInfo[] permissionTree = null;
	public SupportsGlTextureInfo[] supportsGlTexture = null;
	public SupportsScreensInfo[] supportsScreens = null;
	public UsesConfigurationInfo[] usesConfiguration = null;
	public UsesFeatureInfo[] usesFeature = null;
	public UsesLibraryInfo[] usesLibrary = null;
	public UsesPermissionInfo[] usesPermission = null;
	public UsesPermissionInfo[] usesPermissionSdk23 = null;
	public UsesSdkInfo usesSdk = new UsesSdkInfo();
	
	public Integer featureFlags = 0;
}
