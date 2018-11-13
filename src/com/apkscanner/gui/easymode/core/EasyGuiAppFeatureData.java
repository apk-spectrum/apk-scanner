package com.apkscanner.gui.easymode.core;

import java.io.File;
import java.util.ArrayList;

import com.apkscanner.core.scanner.PermissionGroupManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.CompatibleScreensInfo;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.SupportsGlTextureInfo;
import com.apkscanner.data.apkinfo.SupportsScreensInfo;
import com.apkscanner.data.apkinfo.UsesConfigurationInfo;
import com.apkscanner.data.apkinfo.UsesFeatureInfo;
import com.apkscanner.data.apkinfo.UsesLibraryInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.util.FileUtil;

public class EasyGuiAppFeatureData {
	public String installLocation = null;

	public boolean isHidden = false;
	public boolean isStartup = false;
	public boolean debuggable = false;
	public boolean isInstrumentation = false;
	public String sharedUserId = "";

	public Long ApkSize = 0L;
	public String apkPath = "";
	public String checkSumMd5 = "";

	public boolean isSamsungSign = false;
	public boolean isPlatformSign = false;
	public String CertSummary = "";

	public String allPermissionsList = "";
	public String signaturePermissions = "";
	public String notGrantPermmissions = "";
	public String deprecatedPermissions = "";

	public PermissionGroupManager permissionGroupManager = null; 

	public boolean hasSignatureLevel = false;
	public boolean hasSystemLevel = false;
	public boolean hasSignatureOrSystemLevel = false;

	public String deviceRequirements = "";
	
	public EasyGuiAppFeatureData(ApkInfo apkInfo) {		
		setFeature(apkInfo);
	}
	
	public void removeData()
	{
		installLocation = null;
		isHidden = false;
		isStartup = false;
		debuggable = false;
		isInstrumentation = false;
		sharedUserId = "";
		ApkSize = 0L;
		checkSumMd5 = "";

		isSamsungSign = false;
		isPlatformSign = false;

		allPermissionsList = "";
		signaturePermissions = "";
		notGrantPermmissions = "";
		deprecatedPermissions = "";

		deviceRequirements = "";
		
	}
	
	private void setFeature(ApkInfo apkInfo) {
		sharedUserId = apkInfo.manifest.sharedUserId;
		installLocation = apkInfo.manifest.installLocation;

		isHidden = ApkInfoHelper.isHidden(apkInfo);
		isStartup = ApkInfoHelper.isStartup(apkInfo);
		isInstrumentation = ApkInfoHelper.isInstrumentation(apkInfo);
		debuggable = ApkInfoHelper.isDebuggable(apkInfo);

		isSamsungSign = (apkInfo.featureFlags & ApkInfo.APP_FEATURE_SAMSUNG_SIGN) != 0 ? true : false;
		isPlatformSign = (apkInfo.featureFlags & ApkInfo.APP_FEATURE_PLATFORM_SIGN) != 0 ? true : false;

		CertSummary = ""; // apkInfo.CertSummary;
		if(apkInfo.certificates != null) {
			for(String sign: apkInfo.certificates) {
				String[] line = sign.split("\n");
				if(line.length >= 3) {
					CertSummary += line[0] + "\n" + line[1] + "\n" + line[2] + "\n\n";
				} else {
					CertSummary += "error\n";
				}
			}
		}

		ApkSize = apkInfo.fileSize;
		apkPath = apkInfo.filePath;
		checkSumMd5 = FileUtil.getMessageDigest(new File(apkPath), "MD5");

		hasSignatureLevel = false; // apkInfo.hasSignatureLevel;
		hasSignatureOrSystemLevel = false; // apkInfo.hasSignatureOrSystemLevel;
		hasSystemLevel = false; // apkInfo.hasSystemLevel;
		notGrantPermmissions = "";

		ArrayList<UsesPermissionInfo> allPermissions = new ArrayList<UsesPermissionInfo>(); 
		StringBuilder permissionList = new StringBuilder();
		if(apkInfo.manifest.usesPermission != null && apkInfo.manifest.usesPermission.length > 0) {
			permissionList.append("<uses-permission> [" +  apkInfo.manifest.usesPermission.length + "]\n");
			for(UsesPermissionInfo info: apkInfo.manifest.usesPermission) {
				allPermissions.add(info);
				permissionList.append(info.name + " - " + info.protectionLevel);
				if(info.isSignatureLevel()) hasSignatureLevel = true;
				if(info.isSignatureOrSystemLevel()) hasSignatureOrSystemLevel = true;
				if(info.isSystemLevel()) hasSystemLevel = true;
				if(((info.isSignatureLevel() || info.isSignatureOrSystemLevel()) && !(isSamsungSign || isPlatformSign)) || info.isSystemLevel()) {
					notGrantPermmissions += info.name + " - " + info.protectionLevel + "\n";
				}
				if(info.maxSdkVersion != null) {
					permissionList.append(", maxSdkVersion : " + info.maxSdkVersion);
				}
				if(info.isDeprecated()) {
					deprecatedPermissions += info.getDeprecatedMessage() + "\n\n";
				}
				permissionList.append("\n");
			}
		}
		if(apkInfo.manifest.usesPermissionSdk23 != null && apkInfo.manifest.usesPermissionSdk23.length > 0) {
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<uses-permission-sdk-23> [" +  apkInfo.manifest.usesPermissionSdk23.length + "]\n");
			for(UsesPermissionInfo info: apkInfo.manifest.usesPermissionSdk23) {
				allPermissions.add(info);
				permissionList.append(info.name + " - " + info.protectionLevel);
				if(info.isSignatureLevel()) hasSignatureLevel = true;
				if(info.isSignatureOrSystemLevel()) hasSignatureOrSystemLevel = true;
				if(info.isSystemLevel()) hasSystemLevel = true;
				if(((info.isSignatureLevel() || info.isSignatureOrSystemLevel()) && !(isSamsungSign || isPlatformSign)) || info.isSystemLevel()) {
					notGrantPermmissions += info.name + " - " + info.protectionLevel + "\n";
				}
				if(info.maxSdkVersion != null) {
					permissionList.append(", maxSdkVersion : " + info.maxSdkVersion);
				}
				if(info.isDeprecated()) {
					deprecatedPermissions += info.getDeprecatedMessage() + "\n\n";
				}
				permissionList.append("\n");
			}
		}

		signaturePermissions = "";
		if(apkInfo.manifest.permission != null && apkInfo.manifest.permission.length > 0) {
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<permission> [" +  apkInfo.manifest.permission.length + "]\n");
			for(PermissionInfo info: apkInfo.manifest.permission) {
				permissionList.append(info.name + " - " + info.protectionLevel + "\n");
				if(!"normal".equals(info.protectionLevel)) {
					signaturePermissions += info.name + " - " + info.protectionLevel + "\n";
				}
			}
		}
		allPermissionsList = permissionList.toString();
		//permissionGroupManager = new PermissionGroupManager(allPermissions.toArray(new UsesPermissionInfo[allPermissions.size()]));

		StringBuilder deviceReqData = new StringBuilder();
		if(apkInfo.manifest.compatibleScreens != null) {
			for(CompatibleScreensInfo info: apkInfo.manifest.compatibleScreens) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.supportsScreens != null) {
			for(SupportsScreensInfo info: apkInfo.manifest.supportsScreens) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.usesFeature != null) {
			for(UsesFeatureInfo info: apkInfo.manifest.usesFeature) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.usesConfiguration != null) {
			for(UsesConfigurationInfo info: apkInfo.manifest.usesConfiguration) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.usesLibrary != null) {
			deviceReqData.append("uses library :\n");
			for(UsesLibraryInfo info: apkInfo.manifest.usesLibrary) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.supportsGlTexture != null) {
			for(SupportsGlTextureInfo info: apkInfo.manifest.supportsGlTexture) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}

		deviceRequirements = deviceReqData.toString();
	}
	
}
