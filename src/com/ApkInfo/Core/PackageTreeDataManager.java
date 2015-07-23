package com.ApkInfo.Core;

import java.util.ArrayList;

public class PackageTreeDataManager {
	ArrayList<PackageTreeDataObject> ArrayDataObject;
	
	
	public PackageTreeDataManager() {
		// TODO Auto-generated constructor stub
		ArrayDataObject = new ArrayList<PackageTreeDataObject>();
		
		
		
	}
	
	public class PackageTreeDataObject {	
		String strCodePath;
		String strversionCode;
		String strversionName;
		String LibraryDir;
	}
	
	public ArrayList<PackageTreeDataObject> getDataArray() {		
		return ArrayDataObject;
	}		
	
}


