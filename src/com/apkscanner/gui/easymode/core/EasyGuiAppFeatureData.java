package com.apkscanner.gui.easymode.core;

import java.util.ArrayList;

import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.CompatibleScreensInfo;
import com.apkscanner.data.apkinfo.SupportsGlTextureInfo;
import com.apkscanner.data.apkinfo.SupportsScreensInfo;
import com.apkscanner.data.apkinfo.UsesConfigurationInfo;
import com.apkscanner.data.apkinfo.UsesFeatureInfo;
import com.apkscanner.data.apkinfo.UsesLibraryInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;

public class EasyGuiAppFeatureData {
	public String installLocation = null;

	public boolean isHidden = false;
	public boolean isStartup = false;
	public boolean debuggable = false;
	public boolean isInstrumentation = false;
	public boolean isnoSign = false;
	public String sharedUserId = "";

	public Long ApkSize = 0L;
	public String apkPath = "";
	public String checkSumMd5 = "";

	public boolean isSamsungSign = false;
	public boolean isPlatformSign = false;
	public String CertSummary = "";

	public PermissionManager permissionGroupManager = null; 
	public ArrayList<UsesPermissionInfo> allPermissions;
	
	public boolean hasSignatureLevel = false;
	public boolean hasSystemLevel = false;
	public boolean hasSignatureOrSystemLevel = false;

	public String deviceRequirements = "";
	
	public EasyGuiAppFeatureData(ApkInfo apkInfo) {		
		setFeature(apkInfo);
	}
	public EasyGuiAppFeatureData() {	
		
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

		deviceRequirements = "";
		
	}
	
	public void setFeature(ApkInfo apkInfo) {
		
		
		sharedUserId = apkInfo.manifest.sharedUserId;
		installLocation = apkInfo.manifest.installLocation;

		isHidden = ApkInfoHelper.isHidden(apkInfo);
		isStartup = ApkInfoHelper.isStartup(apkInfo);
		isInstrumentation = ApkInfoHelper.isInstrumentation(apkInfo);
		debuggable = ApkInfoHelper.isDebuggable(apkInfo);
		isnoSign = (apkInfo.certificates ==null)? true : false;
		
		
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
		//very low perfor
		//checkSumMd5 = FileUtil.getMessageDigest(new File(apkPath), "MD5");
		hasSignatureLevel = false; // apkInfo.hasSignatureLevel;
		hasSignatureOrSystemLevel = false; // apkInfo.hasSignatureOrSystemLevel;
		hasSystemLevel = false; // apkInfo.hasSystemLevel;

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
