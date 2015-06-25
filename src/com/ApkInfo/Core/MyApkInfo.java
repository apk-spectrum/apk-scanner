package com.ApkInfo.Core;

import java.util.ArrayList;

public class MyApkInfo {
	public String strLabelname;
	public String strPackageName;
	public String strVersionName;
	public String strVersionCode;
	public String strMinSDKversion;
	public String strTargerSDKversion;
	public String strSigning;
	public String strHidden;
	public String strIconPath;
	public String strPermissions;
	public String strStartup;
	public String strProtectionLevel;
	public String strSharedUserId;
	public Long lApkSize;
	
	public ArrayList<Object[]> arrWidgets = new ArrayList<Object[]>();
	public ArrayList<String> ImagePathList = new ArrayList<String>();
	public ArrayList<String> LibPathList = new ArrayList<String>();

	public ArrayList<Object[]> ActivityList = new ArrayList<Object[]>();
	public ArrayList<Object[]> CertList = null;
	
	public ArrayList<String> ListPermission;
	
	public String strWorkAPKPath;
	
	public MyApkInfo() {
		strLabelname="Unknown";
		strPackageName="Unknown";
		strVersionName="Unknown";
		strVersionCode="Unknown";
		strMinSDKversion="Unknown";
		strTargerSDKversion="Unknown";
		strSigning="Unknown";
		strHidden="Unknown";
		strIconPath="Unknown";
		strPermissions="";
		strStartup="Unknown";
		strProtectionLevel = "Unknown";
		strSharedUserId = "Unknown";
		lApkSize = (long) 0;
	}
	public MyApkInfo(String Path) {		
		strWorkAPKPath  = Path;
	}
	
	public void verify() {
		strLabelname = strLabelname != null ? strLabelname : "Unknown";
		strPackageName = strPackageName != null ? strPackageName : "Unknown";
		strVersionName = strVersionName != null ? strVersionName : "Unknown";
		strVersionCode = strVersionCode != null ? strVersionCode : "Unknown";
		strMinSDKversion = strMinSDKversion != null ? strMinSDKversion : "Unknown";
		strTargerSDKversion = strTargerSDKversion != null ? strTargerSDKversion : "Unknown";
		strSigning = strSigning != null ? strSigning : "Unknown";
		strHidden = strHidden != null ? strHidden : "Unknown";
		strIconPath = strIconPath != null ? strIconPath : "Unknown";
		strPermissions = strPermissions != null ? strPermissions : "";
		strStartup = strStartup != null ? strStartup : "Unknown";
		strProtectionLevel = strProtectionLevel != null ? strProtectionLevel : "Unknown";
		strSharedUserId = strSharedUserId != null ? strSharedUserId : "";
		
		for(int i = 0; i < arrWidgets.size(); i++){
			Object[] info = (Object[])arrWidgets.get(i);
			info[0] = info[0] != null && !((String)info[0]).matches("Unknown") ? info[0] : strIconPath;
			info[1] = info[1] != null && !((String)info[1]).matches("Unknown") ? info[1] : strLabelname;
			info[2] = info[2] != null && !((String)info[2]).matches("Unknown") ? info[2] : "1 X 1";
			info[3] = info[3] != null && !((String)info[3]).matches("Unknown") ? info[3] : strPackageName;
			info[4] = info[4] != null && !((String)info[4]).matches("Unknown") ? info[4] : "Unknown";
			
			if(((String)info[3]).matches("^\\..*")) {
				info[3] = strPackageName + (String)info[3];
        	}
		}

		for(int i = 0; i < ActivityList.size(); i++){
			Object[] info = (Object[])ActivityList.get(i);
			
			info[0] = info[0] != null && !((String)info[0]).matches("Unknown") ? info[0] : strPackageName;
			info[1] = info[1] != null && !((String)info[1]).matches("Unknown") ? info[1] : "Unknown";
			info[2] = info[2] != null && !((String)info[2]).matches("Unknown") ? info[2] : "X";
			info[3] = info[3] != null && !((String)info[3]).matches("Unknown") ? info[3] : "";

			if(((String)info[0]).matches("^\\..*")) {
				info[0] = strPackageName + (String)info[0];
        	}
		}
	}
	
	public void dump() {
        System.out.println("Package = " + strPackageName);
        System.out.println("Label = " + strLabelname);
        System.out.println("VersionName = " + strVersionName);
        System.out.println("VersionCode = " + strVersionCode);
        System.out.println("minSdkVersion = " + strMinSDKversion);
        System.out.println("targetSdkVersion = " + strTargerSDKversion);
        System.out.println("Hidden = " + strHidden);
        System.out.println("Permissions = " + strPermissions);
        System.out.println("Icon = " + strIconPath);

		for(int i = 0; i < arrWidgets.size(); i++){
			Object[] info = (Object[])arrWidgets.get(i);
        	System.out.println("widget Icon = " + info[0] + ", Title " + info[1] 
					+ ", Size " + info[2] + ", Activity " + info[3] + ", Type " + info[4]);
		}

		for(int i = 0; i < ActivityList.size(); i++){
			Object[] info = (Object[])ActivityList.get(i);
        	System.out.println("Activity name = " + info[0] + ", tag " + info[1] 
					+ ", startup " + info[2] + ", Intents " + info[3]);
		}
	}
}
