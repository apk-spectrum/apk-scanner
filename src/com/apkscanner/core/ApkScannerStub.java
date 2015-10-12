package com.apkscanner.core;

import java.io.File;

import com.apkscanner.data.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;

abstract public class ApkScannerStub
{
	protected ApkInfo apkInfo = null;
	protected StatusListener statusListener = null;

	protected boolean isPackageTempApk = false;

	public enum Status {
		UNINITIALIZE,
		INITIALIZING,
		INITIALIZEED,
		SOLVE_RESOURCE,
		SOLVE_CODE,
		SOLVE_BOTH,
		STANDBY,
		DELETEING
	}
	
	public interface StatusListener
	{
		public void OnStart();
		public void OnSuccess();
		public void OnError();
		public void OnComplete();
		public void OnProgress(int step, String msg);
		public void OnStateChange();
	}
	
	public ApkScannerStub(StatusListener statusListener)
	{
		this.statusListener = statusListener;
	}

	public void openApk(final String apkFilePath)
	{
		openApk(apkFilePath, null);
	}
	
	abstract public void openApk(final String apkFilePath, String frameworkRes);

	abstract public void clear(boolean sync);

	public void openPackage(String devSerialNumber, String devApkFilePath, String framework)
	{
		isPackageTempApk = true;

		if(statusListener != null) statusListener.OnProgress(1, "I: Open package\n");
		if(statusListener != null) statusListener.OnProgress(1, "I: apk path in device : " + devApkFilePath + "\n");
		
		String tempApkFilePath = "/" + devSerialNumber + devApkFilePath;
		tempApkFilePath = tempApkFilePath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
		tempApkFilePath = FileUtil.makeTempPath(tempApkFilePath)+".apk";

		if(framework == null) {
			framework = (String)Resource.PROP_FRAMEWORK_RES.getData();
		}

		String frameworkRes = "";
		if(framework != null && !framework.isEmpty()) {
			for(String s: framework.split(";")) {
				if(s.startsWith("@")) {
					String devNum = s.replaceAll("^@([^/]*)/.*", "$1");
					String path = s.replaceAll("^@[^/]*", "");
					String dest = (new File(tempApkFilePath).getParent()) + File.separator + path.replaceAll(".*/", "");
					if(statusListener != null) statusListener.OnProgress(1, "I: start to pull resource apk " + path + "\n");
					AdbWrapper.PullApk_sync(devNum, path, dest);
					frameworkRes += dest + ";"; 
				} else {
					frameworkRes += s + ";"; 
				}
			}
		}

		if(statusListener != null) statusListener.OnProgress(1, "I: start to pull apk " + devApkFilePath + "\n");
		AdbWrapper.PullApk_sync(devSerialNumber, devApkFilePath, tempApkFilePath);
		
		if(!(new File(tempApkFilePath)).exists()) {
			Log.e("openPackage() failure : apk pull - " + tempApkFilePath);
			return;
		}

		openApk(tempApkFilePath, frameworkRes);
	}
	
	public ApkInfo getApkInfo()
	{
		return apkInfo;
	}
}
