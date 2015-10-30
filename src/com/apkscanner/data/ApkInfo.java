package com.apkscanner.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.apkscanner.core.PermissionGroupManager.PermissionGroup;

public class ApkInfo
{
	public String[] Labelname = null;
	public String PackageName = null;
	public String VersionName = null;
	public String VersionCode = null;
	public String MinSDKversion = null;
	public String TargerSDKversion = null;
	public String MaxSDKversion = null;
	public String Signing = null;
	public boolean isHidden = false;
	public String IconPath = null;
	public String Permissions = null;
	public String Startup = null;
	public String ProtectionLevel = null;
	public boolean debuggable = false; 
	public String SharedUserId = null;
	public String ApkSize = null;
	public String CertSummary = null;
	public String CertCN = null;
	
	public boolean hasSignatureLevel = false;
	public boolean hasSystemLevel = false;
	public boolean hasSignatureOrSystemLevel = false;
	
	public ArrayList<Object[]> WidgetList = new ArrayList<Object[]>();
	public ArrayList<String> ImageList = new ArrayList<String>();
	public ArrayList<String> LibList = new ArrayList<String>();

	public ArrayList<Object[]> ActivityList = new ArrayList<Object[]>();
	public ArrayList<String> CertList = new ArrayList<String>();
	
	public ArrayList<String> PermissionList = new ArrayList<String>();
	public HashMap<String, PermissionGroup> PermGroupMap;

	public String ApkPath = null;
	public String WorkTempPath = null;
	
	public void verify() {
		if(PackageName == null) PackageName = "";
		if(Labelname == null || (Labelname.length > 0 &&  Labelname[0] == null)) Labelname = new String[] { PackageName };
		if(VersionName == null) VersionName = "";
		if(VersionCode == null) VersionCode = "";
		if(MinSDKversion == null) MinSDKversion = "";
		if(TargerSDKversion == null) TargerSDKversion = "";
		if(MaxSDKversion == null) MaxSDKversion = "";
		if(Signing == null) Signing = "";
		if(IconPath == null) IconPath = "";
		if(Permissions == null) Permissions = "";
		if(Startup == null) Startup = "";
		if(ProtectionLevel == null) ProtectionLevel = "";
		if(SharedUserId == null) SharedUserId = "";
		if(ApkSize == null) ApkSize = "";
		
		for(int i = 0; i < WidgetList.size(); i++){
			Object[] info = (Object[])WidgetList.get(i);
			info[0] = info[0] != null && !((String)info[0]).isEmpty() ? info[0] : IconPath;
			info[1] = info[1] != null && !((String)info[1]).isEmpty() ? info[1] : Labelname[0];
			info[2] = info[2] != null && !((String)info[2]).isEmpty() ? info[2] : "1 X 1";
			info[3] = info[3] != null && !((String)info[3]).isEmpty() ? info[3] : PackageName;
			info[4] = info[4] != null && !((String)info[4]).isEmpty() ? info[4] : "Unknown";
			
			if(((String)info[3]).startsWith(".")) {
				info[3] = PackageName + (String)info[3];
        	}
		}

		for(int i = 0; i < ActivityList.size(); i++){
			Object[] info = (Object[])ActivityList.get(i);
			
			info[0] = info[0] != null && !((String)info[0]).isEmpty() ? info[0] : PackageName;
			info[1] = info[1] != null && !((String)info[1]).isEmpty() ? info[1] : "Unknown";
			info[2] = info[2] != null && !((String)info[2]).isEmpty() ? info[2] : "X";
			info[3] = info[3] != null && !((String)info[3]).isEmpty() ? info[3] : "";

			if(((String)info[0]).startsWith(".")) {
				info[0] = PackageName + (String)info[0];
        	}
		}
	}
}
