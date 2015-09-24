package com.apkscanner.core;

import java.io.File;

import com.apkscanner.data.AaptXmlTreePath;
import com.apkscanner.data.ApkInfo;
import com.apkscanner.util.Log;

public class AaptToolManager
{
	private ApkInfo apkInfo = null;
	private AaptXmlTreePath manifestPath = null;
	
	public AaptToolManager()
	{
		this(null, false);
	}

	public AaptToolManager(String apkPath)
	{
		this(apkPath, false);
	}

	public AaptToolManager(String apkPath, boolean isPackage)
	{
		manifestPath = new AaptXmlTreePath();
		apkInfo = getApkInfo(apkPath);
	}


	public ApkInfo getApkInfo(String apkFilePath) {
		apkInfo = new ApkInfo();
		
		File apkFile = new File(apkFilePath);

		if(!apkFile.exists()) {
			Log.e("No Such APK file");
			return null;
		}
		apkFilePath = apkFile.getAbsolutePath();
		
		if(apkFilePath != null && (new File(apkFilePath)).exists()) {
			apkInfo.ApkPath = apkFilePath;
		}
		
		Log.v("getApkInfo()");
		String androidManifest[] = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] {"AndroidManifest.xml"});
		if(!"N: android=http://schemas.android.com/apk/res/android".equals(androidManifest[0])) {
			Log.w("Schemas was not http://schemas.android.com/apk/res/android\n" + androidManifest[0]);
		}
		
		manifestPath.createAaptXmlTree(androidManifest);
		
		apkInfo.PackageName = manifestPath.getNode("/manifest").getAttribute("pacakge");
		apkInfo.VersionCode = manifestPath.getNode("/manifest").getAttribute("android:versionCode");
		apkInfo.VersionName = manifestPath.getNode("/manifest").getAttribute("android:versionName");
		
		
		Log.i(apkInfo.PackageName);
		Log.i(apkInfo.VersionCode);
		Log.i(apkInfo.VersionName);
		
		return apkInfo;
	}
}
