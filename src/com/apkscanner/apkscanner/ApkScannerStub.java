package com.apkscanner.apkscanner;

import java.io.File;

import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.EstimatedTimeEnRoute;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;

abstract public class ApkScannerStub
{
	protected ApkInfo apkInfo = null;
	protected StatusListener statusListener = null;

	protected long startTime;

	public enum Status {
		BASIC_INFO_COMPLETED,
		WIDGET_COMPLETED,
		LIB_COMPLETED,
		IMAGE_COMPLETED,
		ACTIVITY_COMPLETED,
		CERT_COMPLETED,
		ALL_COMPLETED,
	}
	
	public interface StatusListener
	{
		public void OnStart(long estimatedTime);
		public void OnSuccess();
		public void OnError();
		public void OnComplete();
		public void OnProgress(int step, String msg);
		public void OnStateChanged(Status status);
	}
	
	public ApkScannerStub(StatusListener statusListener)
	{
		this.statusListener = statusListener;
	}

	public void openApk(final String apkFilePath)
	{
		openApk(apkFilePath, (String)Resource.PROP_FRAMEWORK_RES.getData());
	}
	
	abstract public void openApk(final String apkFilePath, String frameworkRes);

	abstract public void clear(boolean sync);

	public void openPackage(String devSerialNumber, String devApkFilePath, String framework)
	{
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
	
	protected void timeRecordStart()
	{
		startTime = System.currentTimeMillis();
	}
	
	protected void timeRecordEnd()
	{
		long leadTime = System.currentTimeMillis() - startTime;
		Log.i("lead time : " + leadTime);
		EstimatedTimeEnRoute.setRealLeadTime(apkInfo.filePath, leadTime);
	}
}
