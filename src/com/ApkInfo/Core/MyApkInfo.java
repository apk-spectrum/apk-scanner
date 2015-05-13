package com.ApkInfo.Core;

import java.util.ArrayList;

import javax.xml.xpath.XPathExpressionException;

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
	
}
