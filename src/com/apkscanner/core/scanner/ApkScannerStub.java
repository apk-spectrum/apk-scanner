package com.apkscanner.core.scanner;

import java.io.File;
import java.util.ArrayList;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbWrapper;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

abstract public class ApkScannerStub
{
	protected ApkInfo apkInfo = null;
	protected StatusListener statusListener = null;

	protected long startTime;

	public enum Status {
		BASIC_INFO_COMPLETED,
		PERM_INFO_COMPLETED,
		WIDGET_COMPLETED,
		LIB_COMPLETED,
		RESOURCE_COMPLETED,
		RES_DUMP_COMPLETED,
		ACTIVITY_COMPLETED,
		CERT_COMPLETED,
		ALL_COMPLETED
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
		setStatusListener(statusListener);
	}
	
	public void setStatusListener(StatusListener statusListener)
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
					AdbWrapper.pullApk(devNum, path, dest, null);
					frameworkRes += dest + ";"; 
				} else {
					frameworkRes += s + ";"; 
				}
			}
		}

		if(statusListener != null) statusListener.OnProgress(1, "I: start to pull apk " + devApkFilePath + "\n");
		AdbWrapper.pullApk(devSerialNumber, devApkFilePath, tempApkFilePath, null);
		
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

	protected String[] solveCert()
	{
		String certPath = apkInfo.tempWorkPath + File.separator + "META-INF";
		
		Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
		String keytoolPackage;
		if(javaVersion >= 1.8) {
			keytoolPackage = "sun.security.tools.keytool.Main";
		} else {
			keytoolPackage = "sun.security.tools.KeyTool";
		}

		ArrayList<String> certList = new ArrayList<String>();
		ArrayList<String> certFiles = new ArrayList<String>();
		
		if(!(new File(apkInfo.filePath)).exists()) {
			return null;
		}
		
		if(!ZipFileUtil.unZip(apkInfo.filePath, "META-INF/", certPath)) {
			Log.e("META-INFO 폴더가 존재 하지 않습니다 :");
			return null;
		}
		
		for (String s : (new File(certPath)).list()) {
			if(!s.toUpperCase().endsWith(".RSA") && !s.toUpperCase().endsWith(".DSA") && !s.toUpperCase().endsWith(".EC") ) {
				File f = new File(certPath + File.separator + s);
				if(f.isFile()) {
					certFiles.add(certPath + File.separator + s);
				}
				continue;
			}

			File rsaFile = new File(certPath + File.separator + s);
			if(!rsaFile.exists()) continue;

			String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
			String[] result = ConsolCmd.exc(cmd, false, null);

		    String certContent = "";

		    boolean isSamsungSign = false;
		    boolean isPlatformTestKey = false;
		    
		    for(int i=0; i < result.length; i++){
	    		if(!certContent.isEmpty() && result[i].matches("^.*\\[[0-9]*\\]:$")) {
	    			certList.add(certContent);
			    	certContent = "";
	    		}
	    		if(result[i].matches("^.*:( [^ ,]+=(\".*\")?[^,]*,?)+$")) {
	    			if(result[i].indexOf("CN=") > -1) {
	    				String CN = result[i].replaceAll(".*CN=([^,]*).*", "$1");
	    				if("Samsung Cert".equals(CN)) {
	    					isSamsungSign = true;
	    				} else if("Android".equals(CN)) {
	    					isPlatformTestKey = true;
	    				}
	    			}
	    		}
	    		if((isSamsungSign || isPlatformTestKey)
	    				&& result[i].matches("^[^\\s]+[^:]*: ([0-9a-z]+)+$")) {
	    			String serialNumber = result[i].replaceAll("^[^\\s]+[^:]*: ([0-9a-z]+)+$", "$1");
	    			if(isSamsungSign && !Resource.STR_SAMSUNG_KEY_SERIAL.getString().equals(serialNumber)) {
		    			Log.w(Resource.STR_SAMSUNG_KEY_SERIAL.getString() + " " + serialNumber);
	    				isSamsungSign = false;
	    			} else if(isPlatformTestKey && !Resource.STR_SS_TEST_KEY_SERIAL.getString().equals(serialNumber)) {
		    			Log.w(Resource.STR_SS_TEST_KEY_SERIAL.getString() + " " + serialNumber);
	    				isPlatformTestKey = false;
	    			}
	    		}
	    		certContent += (certContent.isEmpty() ? "" : "\n") + result[i];
		    }
		    
		    if(isSamsungSign) apkInfo.featureFlags |= ApkInfo.APP_FEATURE_SAMSUNG_SIGN;
		    if(isPlatformTestKey) apkInfo.featureFlags |= ApkInfo.APP_FEATURE_PLATFORM_SIGN;
		    
		    certList.add(certContent);
		}

		apkInfo.certFiles = certFiles.toArray(new String[0]);

		return certList.toArray(new String[0]);
	}
	
	protected void stateChanged(Status status)
	{
		if(statusListener != null) {
			//if(apkInfo != null) apkInfo.verify();
			statusListener.OnStateChanged(status);
		}
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
