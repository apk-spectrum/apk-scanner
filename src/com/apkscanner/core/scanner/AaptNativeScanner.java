package com.apkscanner.core.scanner;

import java.io.File;

import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.util.Log;

public class AaptNativeScanner extends ApkScannerStub
{
	
	private long assetsHandle;
	
	public AaptNativeScanner(StatusListener statusListener) {
		super(statusListener);
	}

	@Override
	public void openApk(String apkFilePath, String frameworkRes) {
		assetsHandle = createAssetManager();
		if(!addPackage(assetsHandle, apkFilePath)) {
			Log.e("ERROR: Failed to add package to an AssetManager : " + apkFilePath);
			return;
		}
		Log.i("INFO: Successed to add package to an AssetManager : " + apkFilePath);
		
		boolean wasSetFrameworkRes = false;
		if(frameworkRes != null && !frameworkRes.isEmpty()) {
			for(String framework: frameworkRes.split(";")) {
				if(framework.isEmpty()) continue;
				if(new File(framework).isFile()) {
					if(addPackage(assetsHandle, frameworkRes)) {
						wasSetFrameworkRes = true;
						Log.i("INFO: Successed to add resource package to the AssetManager : " + frameworkRes);
					} else {
						Log.w("WRRAING: Failed to add resource package to the AssetManager : " + frameworkRes);
					}
				}
			}
		}

		if(!wasSetFrameworkRes) {
			Log.i("INFO: Didn't set the package of resources. so, set package of the default resources.");
			String selfPath = getClass().getResource("/AndroidManifest.xml").toString();
			if(selfPath.startsWith("jar:")) {
				selfPath = selfPath.replaceAll("jar:file:(.*)!/AndroidManifest.xml", "$1");
			} else {
				selfPath = getClass().getResource("/").getPath();
			}

			File jarFile = new File(selfPath);
			if(!jarFile.exists()) {
				Log.w("WRRAING: Failed to get self path");
			} else {
				if(addPackage(assetsHandle, jarFile.getAbsolutePath())) {
					Log.i("INFO: Successed to add resource package to the AssetManager : " + jarFile.getAbsolutePath());
				} else {
					Log.w("WRRAING: Failed to add resource package to the AssetManager : " + jarFile.getAbsolutePath());
				}
			}
		}
	}

	@Override
	public void clear(boolean sync) {
		realeaseAssetManager(assetsHandle);	
	}
	
	public String getResourceName(int resId) {
		String type = getResourceType(assetsHandle, resId);
		String name = getResourceName(assetsHandle, resId);
		return "@" + type + "/" + name;
	}
	
	public String getResourceName(String id) {
		if(id == null || !id.startsWith("@0x")) return id;
		return getResourceName(Integer.parseInt(id.substring(3), 16));
	}
	
	public ResourceInfo[] getResourceValues(int resId) {
		String type = getResourceType(assetsHandle, resId);
		ResourceInfo[] valses = getResourceValues(assetsHandle, resId);
		if("reference".equals(type)) {
			for(ResourceInfo info: valses) {
				if(info.name != null && info.name.startsWith("0x")) {
					info.name = "@" + info.name; 
				}
			}
		}
		return getResourceValues(assetsHandle, resId);
	}

	public ResourceInfo[] getResourceValues(String id) {
		if(id == null || !id.startsWith("@0x")) return new ResourceInfo[] { new ResourceInfo(id, null) };
		return getResourceValues(Integer.parseInt(id.substring(3), 16));
	}

	public native static long createAssetManager();
	public native static void realeaseAssetManager(long handle);
	
	public native static boolean addPackage(long handle, String apkFilePath);

	public native static String getResourceName(long handle, int resId);
	public native static String getResourceType(long handle, int resId);
	public native static ResourceInfo[] getResourceValues(long handle, int resId);
	public native static ResourceInfo getResourceString(long handle, int resId, String config);

	static {
		if (System.getProperty("os.name").indexOf("Linux") > -1) {
			System.loadLibrary("c++");
		}
		System.loadLibrary("AaptNativeWrapper");
		//System.load("Y:\\android-sdk-build\\out\\host\\windows-x86\\lib64\\libAaptNativeWrapper.dll");
		//System.load("/home/local_depot/android-sdk-build/out/host/linux-x86/lib64/libAaptNativeWrapper.so");
	}
}
