package com.apkscanner.core.scanner;

import java.io.File;
import java.util.concurrent.Semaphore;

import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.util.Log;

public class AaptNativeScanner extends ApkScannerStub
{

	static private final int SEM_COUNT = 10;
	static private Semaphore semaphore = new Semaphore(SEM_COUNT, true);
	static private boolean nativeLocked = false;
	
	private long assetsHandle;
	
	public AaptNativeScanner(StatusListener statusListener) {
		super(statusListener);
		assetsHandle = 0;
	}

	@Override
	public void openApk(String apkFilePath, String frameworkRes) {
		if(assetsHandle != 0) {
			realeaseAssetManager(assetsHandle);
		}
		assetsHandle = createAssetManager();
		if(!addPackage(assetsHandle, apkFilePath)) {
			Log.e("ERROR: Failed to add package to an AssetManager : " + apkFilePath);
			realeaseAssetManager();
			return;
		}
		Log.i("INFO: Successed to add package to an AssetManager : " + apkFilePath);
		
		if(getPackageId(apkFilePath) == 0x01) {
			Log.i("INFO: It's resource package : " + apkFilePath);			
		} else {
			boolean wasSetFrameworkRes = false;
			if(frameworkRes != null && !frameworkRes.isEmpty()) {
				for(String framework: frameworkRes.split(";")) {
					if(framework.isEmpty()) continue;
					if(new File(framework).isFile()) {
						if(addResPackage(assetsHandle, framework)) {
							wasSetFrameworkRes = true;
							Log.i("INFO: Successed to add resource package to the AssetManager : " + framework);
						} else {
							Log.w("WRRAING: Failed to add resource package to the AssetManager : " + framework);
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
					if(addResPackage(assetsHandle, jarFile.getAbsolutePath())) {
						Log.i("INFO: Successed to add resource package to the AssetManager : " + jarFile.getAbsolutePath());
					} else {
						Log.w("WRRAING: Failed to add resource package to the AssetManager : " + jarFile.getAbsolutePath());
					}
				}
			}
			
		}
	}

	private void realeaseAssetManager() {
		realeaseAssetManager(assetsHandle);
		assetsHandle = 0;
	}
	
	public boolean hasAssetManager() {
		return assetsHandle != 0;
	}

	@Override
	public void clear(boolean sync) {
		realeaseAssetManager();
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

	private static void semAcquire() {
		semaphore.acquireUninterruptibly();
	}

	private static void semRelease() {
		semaphore.release();
	}

	public static void lock() {
		synchronized (semaphore) {
			if(nativeLocked) return;
			semaphore.acquireUninterruptibly(SEM_COUNT);
			nativeLocked = true;
		}
	}

	public static void unlock() {
		synchronized (semaphore) {
			if(nativeLocked) semaphore.release(SEM_COUNT);
			nativeLocked = false;
		}
	}

	public static long createAssetManager() {
		semAcquire();
		long ret = nativeCreateAssetManager();
		semRelease();
		return ret;
	}

	public static void realeaseAssetManager(long handle) {
		semAcquire();
		nativeRealeaseAssetManager(handle);
		semRelease();
	}

	public static int getPackageId(String apkFilePath) {
		semAcquire();
		int ret = nativeGetPackageId(apkFilePath);
		semRelease();
		return ret;
	}

	public static boolean addPackage(long handle, String apkFilePath) {
		semAcquire();
		boolean ret = nativeAddPackage(handle, apkFilePath);
		semRelease();
		return ret;
	}

	public static boolean addResPackage(long handle, String apkFilePath) {
		semAcquire();
		boolean ret = nativeAddResPackage(handle, apkFilePath);
		semRelease();
		return ret;
	}

	public static String getResourceName(long handle, int resId) {
		semAcquire();
		String ret = nativeGetResourceName(handle, resId);
		semRelease();
		return ret;
	}

	public static String getResourceType(long handle, int resId) {
		semAcquire();
		String ret = nativeGetResourceType(handle, resId);
		semRelease();
		return ret;
	}

	public static ResourceInfo[] getResourceValues(long handle, int resId) {
		semAcquire();
		ResourceInfo[] ret = nativeGetResourceValues(handle, resId);
		semRelease();
		return ret;
	}

	public static ResourceInfo getResourceString(long handle, int resId, String config) {
		semAcquire();
		ResourceInfo ret = nativeGetResourceString(handle, resId, config);
		semRelease();
		return ret;
	}

	private native static long nativeCreateAssetManager();
	private native static void nativeRealeaseAssetManager(long handle);

	private native static int nativeGetPackageId(String apkFilePath);
	
	private native static boolean nativeAddPackage(long handle, String apkFilePath);
	private native static boolean nativeAddResPackage(long handle, String apkFilePath);
	
	private native static String nativeGetResourceName(long handle, int resId);
	private native static String nativeGetResourceType(long handle, int resId);
	private native static ResourceInfo[] nativeGetResourceValues(long handle, int resId);
	private native static ResourceInfo nativeGetResourceString(long handle, int resId, String config);

	static {
		if (System.getProperty("os.name").indexOf("Linux") > -1) {
			System.loadLibrary("c++");
		}
		System.loadLibrary("AaptNativeWrapper");
		//System.load("Y:\\android-sdk-build\\out\\host\\windows-x86\\lib64\\libAaptNativeWrapper.dll");
		//System.load("/home/local_depot/android-sdk-build/out/host/linux-x86/lib64/libAaptNativeWrapper.so");
	}
}
