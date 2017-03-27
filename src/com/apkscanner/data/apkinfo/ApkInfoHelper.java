package com.apkscanner.data.apkinfo;

import java.util.ArrayList;

public class ApkInfoHelper
{
	public static final int INSTALL_LOCATION_NONE = 0;
	public static final int INSTALL_LOCATION_AUTO = 1;
	public static final int INSTALL_LOCATION_INTERNAL_ONLY = 2;
	public static final int INSTALL_LOCATION_PREFER_EXTERNAL = 3;

	private ApkInfo apkInfo;

	public ApkInfoHelper(ApkInfo apkInfo) {
		this.apkInfo = apkInfo;
	}

	public boolean hasLauncher() {
		return hasLauncher(apkInfo);
	}

	public boolean isHidden() {
		return isHidden(apkInfo);
	}

	public boolean isStartup() {
		return isStartup(apkInfo);
	}

	public boolean isInstrumentation() {
		return isInstrumentation(apkInfo);
	}

	public boolean isDebuggable() {
		return isDebuggable(apkInfo);
	}

	public boolean isSigned() {
		return isDebuggable(apkInfo);
	}


	public int getInstallLocation() {
		return getInstallLocation(apkInfo);
	}

	public int getComponentCount() {
		return getComponentCount(apkInfo);
	}

	public ComponentInfo[] getLauncherActivityList(boolean includeMain) {
		return getLauncherActivityList(apkInfo, includeMain);
	}

	public static boolean hasLauncher(ApkInfo apkInfo) {
		return (apkInfo != null && (apkInfo.manifest.featureFlags & ManifestInfo.MANIFEST_FEATURE_LAUNCHUR) != 0);
	}

	public static boolean isHidden(ApkInfo apkInfo) {
		return (apkInfo != null && (apkInfo.manifest.featureFlags & ManifestInfo.MANIFEST_FEATURE_LAUNCHUR) == 0);
	}

	public static boolean isStartup(ApkInfo apkInfo) {
		return (apkInfo != null && (apkInfo.manifest.featureFlags & ManifestInfo.MANIFEST_FEATURE_INSTRUMENTATION) != 0);
	}

	public static boolean isInstrumentation(ApkInfo apkInfo) {
		return (apkInfo != null && (apkInfo.manifest.featureFlags & ManifestInfo.MANIFEST_FEATURE_INSTRUMENTATION) != 0);
	}

	public static boolean isDebuggable(ApkInfo apkInfo) {
		return (apkInfo != null && apkInfo.manifest.application.debuggable != null && apkInfo.manifest.application.debuggable);
	}

	public static boolean isSigned(ApkInfo apkInfo) {
		return (apkInfo != null && apkInfo.certificates == null && apkInfo.certificates.length > 0);
	}

	public static int getInstallLocation(ApkInfo apkInfo) {
		int location = INSTALL_LOCATION_NONE;
		if(apkInfo != null && apkInfo.manifest.installLocation != null) {
			if("auto".equals(apkInfo.manifest.installLocation)) {
				location = INSTALL_LOCATION_AUTO;
			} else if("internalOnly".equals(apkInfo.manifest.installLocation)) {
				location = INSTALL_LOCATION_INTERNAL_ONLY;
			} else if("preferExternal".equals(apkInfo.manifest.installLocation)) {
				location = INSTALL_LOCATION_PREFER_EXTERNAL;
			}
		}
		return location;
	}

	public static int getComponentCount(ApkInfo apkInfo) {
		int cnt = 0;
		if(apkInfo != null) {
			ApplicationInfo app = apkInfo.manifest.application;
			if(app.activity != null) cnt += app.activity.length;
			if(app.activityAlias != null) cnt += app.activityAlias.length;
			if(app.receiver != null) cnt += app.receiver.length;
			if(app.provider != null) cnt += app.provider.length;
			if(app.service != null) cnt += app.service.length;
		}
		return cnt;
	}

	public static ComponentInfo[] getLauncherActivityList(ApkInfo apkInfo, boolean includeMain) {
		ArrayList<ComponentInfo> launcherList = new ArrayList<ComponentInfo>();
		ArrayList<ComponentInfo> mainList = new ArrayList<ComponentInfo>(); 
		if(apkInfo != null) {
			if(apkInfo.manifest.application.activity != null) {
				for(ActivityInfo info: apkInfo.manifest.application.activity) {
					if((info.enabled == null || info.enabled) &&
							(info.exported == null || info.exported) &&
							(info.permission == null || info.permission.isEmpty()) &&
							(info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
						if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0)
							launcherList.add(info);
						else if(includeMain) {
							mainList.add(info);
						}
					}
				}
			}
			if(apkInfo.manifest.application.activityAlias != null) {
				for(ActivityAliasInfo info: apkInfo.manifest.application.activityAlias) {
					if((info.enabled == null || info.enabled) &&
							(info.exported == null || info.exported) &&
							(info.permission == null || info.permission.isEmpty()) &&
							(info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
						if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0)
							launcherList.add(info);
						else if(includeMain) {
							mainList.add(info);
						}
					}
				}
			}
			launcherList.addAll(mainList);
		}
		return launcherList.toArray(new ComponentInfo[0]);
	}
}
