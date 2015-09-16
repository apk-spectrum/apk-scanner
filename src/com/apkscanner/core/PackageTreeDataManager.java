package com.apkscanner.core;

import java.util.ArrayList;

import com.apkscanner.core.AdbWrapper.PackageListObject;

public class PackageTreeDataManager {
	ArrayList<PackageListObject> ArrayDataObject;
	String device;

	public PackageTreeDataManager() {
		this(null);
	}
	
	public PackageTreeDataManager(String device) {
		 scanPackage(device);
	}
	
	public void scanPackage(String device)
	{
		this.device = device;
		if(device == null) return;
		ArrayDataObject = AdbWrapper.getPackageList(device);
	}
	
	public String getApkPath(String device, String packageName)
	{
		return AdbWrapper.getPackageInfo(device, packageName).apkPath;
	}
	
	public ArrayList<PackageListObject> getDataArray() {		
		return ArrayDataObject;
	}

}


