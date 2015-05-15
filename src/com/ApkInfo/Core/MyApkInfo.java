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
	public Long lApkSize;
	
	public ArrayList<Object[]> arrWidgets = new ArrayList<Object[]>();
	public ArrayList<String> ImagePathList = new ArrayList<String>();
	public ArrayList<String> LibPathList = new ArrayList<String>();
	
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
		
		for(int i = 0; i < arrWidgets.size(); i++){
			Object[] widgetInfo = (Object[])arrWidgets.get(i);
			widgetInfo[0] = widgetInfo[0] != null || !((String)widgetInfo[0]).matches("unkown") ? widgetInfo[0] : strIconPath;
			widgetInfo[1] = widgetInfo[1] != null || !((String)widgetInfo[1]).matches("unkown") ? widgetInfo[1] : strLabelname;
			widgetInfo[2] = widgetInfo[2] != null || !((String)widgetInfo[2]).matches("unkown") ? widgetInfo[2] : "1 X 1";
			widgetInfo[3] = widgetInfo[3] != null || !((String)widgetInfo[3]).matches("unkown") ? widgetInfo[3] : strPackageName;
			widgetInfo[4] = widgetInfo[4] != null || !((String)widgetInfo[4]).matches("unkown") ? widgetInfo[4] : "unkown";
			
			if(((String)widgetInfo[3]).matches("^\\..*")) {
				widgetInfo[3] = strPackageName + (String)widgetInfo[3];
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
			Object[] widgetInfo = (Object[])arrWidgets.get(i);
        	System.out.println("widget Icon = " + widgetInfo[0] + ", Title " + widgetInfo[1] 
					+ ", Size " + widgetInfo[2] + ", Activity " + widgetInfo[3] + ", Type " + widgetInfo[4]);
		}
	}
}
