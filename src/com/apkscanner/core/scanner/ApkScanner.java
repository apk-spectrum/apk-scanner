package com.apkscanner.core.scanner;

import java.io.File;
import java.util.ArrayList;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbWrapper;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.ConsolCmd.ConsoleOutputObserver;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

abstract public class ApkScanner
{
	public static final int NO_ERR = 0;
	public static final int ERR_NO_SUCH_FILE = -1;
	public static final int ERR_NO_SUCH_MANIFEST = -2;
	public static final int ERR_FAILURE_PULL_APK = -3;
	public static final int ERR_CAN_NOT_ACCESS_ASSET = -4;
	public static final int ERR_CAN_NOT_READ_MANIFEST = -5;
	public static final int ERR_WRONG_MANIFEST = -5;
	public static final int ERR_FAILURE_VERIFY_CERT = -6;

	public static final int ERR_DEVICE_DISCONNECTED = -2;
	public static final int ERR_UNAVAIlABLE_PARAM = -99;
	public static final int ERR_UNKNOWN = -100;

	protected ApkInfo apkInfo = null;
	protected StatusListener statusListener = null;

	protected long startTime;

	protected int completedCount = 0;

	public enum Status {
		STANBY,
		BASIC_INFO_COMPLETED,
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
		public void onStart(long estimatedTime);
		public void onSuccess();
		public void onError(int error);
		public void onCompleted();
		public void onProgress(int step, String msg);
		public void onStateChanged(Status status);
	}
	
	public ApkScanner(StatusListener statusListener)
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
		if(statusListener != null) statusListener.onProgress(1, "I: Open package\n");
		if(statusListener != null) statusListener.onProgress(1, "I: apk path in device : " + devApkFilePath + "\n");
		
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
					if(statusListener != null) statusListener.onProgress(1, "I: start to pull resource apk " + path + "\n");
					AdbWrapper.pullApk(devNum, path, dest, null);
					frameworkRes += dest + ";"; 
				} else {
					frameworkRes += s + ";"; 
				}
			}
		}

		if(statusListener != null) statusListener.onProgress(1, "I: start to pull apk " + devApkFilePath + "\n");
		boolean ret = AdbWrapper.pullApk(devSerialNumber, devApkFilePath, tempApkFilePath, new ConsoleOutputObserver() {
			String prePercent = null;
			@Override
			public boolean ConsolOutput(String output) {
				if(statusListener != null) {
					String percent = output.replaceAll("\\[\\s*([0-9]*)%\\] .*", "$1");
					if(!percent.equals(output)) {
						if(!percent.equals(prePercent)) {
							prePercent = percent;
							statusListener.onProgress(0, percent);
						}
					}
				}
				return true;
			}
		});

		if(!ret || !(new File(tempApkFilePath)).exists()) {
			Log.e("openPackage() failure : apk pull - " + tempApkFilePath);
			if(statusListener != null) {
				statusListener.onError(ERR_FAILURE_PULL_APK);
				statusListener.onCompleted();
			}
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
			Log.e("No such apk file");
			return null;
		}

		if(!ZipFileUtil.unZip(apkInfo.filePath, "META-INF/", certPath)) {
			Log.e("No such folder : META-INFO/");
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
			if(!rsaFile.exists()) {
				Log.e("No such file : " + rsaFile.getAbsolutePath());
				continue;
			}

			String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
			String[] result = ConsolCmd.exc(cmd, false, null);
			if(result == null || result.length == 0) {
				Log.e("Fail printcert : " + rsaFile.getAbsolutePath());
				continue;
			} else if(result[0].indexOf(keytoolPackage) > -1) {
				Log.e("Failure: Unkown keytool package : " + keytoolPackage);
				Log.e("       : " + rsaFile.getAbsolutePath());
				continue;
			}

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

		apkInfo.certFiles = null;
		if(!certFiles.isEmpty()) {
			apkInfo.certFiles = certFiles.toArray(new String[0]);
		}

		if(certList.isEmpty()) {
			return null;
		}

		return certList.toArray(new String[0]);
	}
	
	protected void stateChanged(Status status)
	{
		if(statusListener != null) {
			statusListener.onStateChanged(status);
		}

		synchronized (this) {
			switch(status) {
			case STANBY:
				completedCount = 0;
				break;
			case BASIC_INFO_COMPLETED:
			case WIDGET_COMPLETED:
			case LIB_COMPLETED:
			case RESOURCE_COMPLETED:
			case RES_DUMP_COMPLETED:
			case ACTIVITY_COMPLETED:
			case CERT_COMPLETED:
				completedCount++;
				break;
			default:
				break;
			}

			if(completedCount == 7) {
				statusListener.onStateChanged(Status.ALL_COMPLETED);
				Log.i("I: completed... ");
	        	statusListener.onSuccess();
	        	statusListener.onCompleted();
	        	completedCount = 0;
			}
		}
	}
}
