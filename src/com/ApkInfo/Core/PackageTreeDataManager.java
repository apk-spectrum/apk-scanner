package com.ApkInfo.Core;

import java.util.ArrayList;

import com.ApkInfo.Core.AdbWrapper.PackageListObject;

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
		ArrayDataObject =  AdbWrapper.getPackageList(device);
	}
	
	public ArrayList<PackageListObject> getDataArray() {		
		return ArrayDataObject;
	}		
	
}


