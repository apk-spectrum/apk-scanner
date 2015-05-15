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
	public Long lApkSize;
	
	public ArrayList<Object[]> arrWidgets = new ArrayList<Object[]>();
	public ArrayList<String> ImagePathList = new ArrayList<String>();
	public ArrayList<String> LibPathList = new ArrayList<String>();

	public ArrayList<Object[]> ActivityList = new ArrayList<Object[]>();
	
	public ArrayList<String> ListPermission;
	
	public String strWorkAPKPath;
	
	public MyApkInfo() {
		strLabelname="unkown";
		strPackageName="unkown";
		strVersionName="unkown";
		strVersionCode="unkown";
		strMinSDKversion="unkown";
		strTargerSDKversion="unkown";
		strSigning="unkown";
		strHidden="unkown";
		strIconPath="unkown";
		strPermissions="";
		strStartup="unkown";
		lApkSize = (long) 0;
	}
	public MyApkInfo(String Path) {		
		strWorkAPKPath  = Path;
	}
	
	public void verify() {
		strLabelname = strLabelname != null ? strLabelname : "unkown";
		strPackageName = strPackageName != null ? strPackageName : "unkown";
		strVersionName = strVersionName != null ? strVersionName : "unkown";
		strVersionCode = strVersionCode != null ? strVersionCode : "unkown";
		strMinSDKversion = strMinSDKversion != null ? strMinSDKversion : "unkown";
		strTargerSDKversion = strTargerSDKversion != null ? strTargerSDKversion : "unkown";
		strSigning = strSigning != null ? strSigning : "unkown";
		strHidden = strHidden != null ? strHidden : "unkown";
		strIconPath = strIconPath != null ? strIconPath : "unkown";
		strPermissions = strPermissions != null ? strPermissions : "";
		strStartup = strStartup != null ? strStartup : "unkown";
		
		for(int i = 0; i < arrWidgets.size(); i++){
			Object[] info = (Object[])arrWidgets.get(i);
			info[0] = info[0] != null && !((String)info[0]).matches("unkown") ? info[0] : strIconPath;
			info[1] = info[1] != null && !((String)info[1]).matches("unkown") ? info[1] : strLabelname;
			info[2] = info[2] != null && !((String)info[2]).matches("unkown") ? info[2] : "1 X 1";
			info[3] = info[3] != null && !((String)info[3]).matches("unkown") ? info[3] : strPackageName;
			info[4] = info[4] != null && !((String)info[4]).matches("unkown") ? info[4] : "unkown";
			
			if(((String)info[3]).matches("^\\..*")) {
				info[3] = strPackageName + (String)info[3];
        	}
		}

		for(int i = 0; i < ActivityList.size(); i++){
			Object[] info = (Object[])ActivityList.get(i);
			
			info[0] = info[0] != null && !((String)info[0]).matches("unkown") ? info[0] : strPackageName;
			info[1] = info[1] != null && !((String)info[1]).matches("unkown") ? info[1] : "unkown";
			info[2] = info[2] != null && !((String)info[2]).matches("unkown") ? info[2] : "X";
			info[3] = info[3] != null && !((String)info[3]).matches("unkown") ? info[3] : "";

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
