package com.apkspectrum.core.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.apkspectrum.core.signer.SignatureReport;
import com.apkspectrum.core.signer.SignatureReportByApksig;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.resource._RConst;
import com.apkspectrum.resource._RProp;
import com.apkspectrum.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.tool.adb.AdbWrapper;
import com.apkspectrum.util.ConsolCmd.ConsoleOutputObserver;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.ZipFileUtil;

abstract public class ApkScanner
{
	public static final String APKSCANNER_TYPE_AAPT = "AAPT";
	public static final String APKSCANNER_TYPE_AAPTLIGHT = "AAPTLIGHT";
	public static final String APKSCANNER_TYPE_APKTOOL = "APKTOOL";

	public static final int STATUS_STANBY = 0x00;
	public static final int STATUS_BASIC_INFO_COMPLETED = 0x01;
	public static final int STATUS_WIDGET_COMPLETED = 0x02;
	public static final int STATUS_LIB_COMPLETED = 0x04;
	public static final int STATUS_RESOURCE_COMPLETED = 0x08;
	public static final int STATUS_RES_DUMP_COMPLETED = 0x10;
	public static final int STATUS_ACTIVITY_COMPLETED = 0x20;
	public static final int STATUS_CERT_COMPLETED = 0x40;
	public static final int STATUS_ALL_COMPLETED = 0x7F;

	public static final int NO_ERR = 0;
	public static final int ERR_NO_SUCH_FILE = -1;
	public static final int ERR_NO_SUCH_MANIFEST = -2;
	public static final int ERR_FAILURE_PULL_APK = -3;
	public static final int ERR_CAN_NOT_ACCESS_ASSET = -4;
	public static final int ERR_WRONG_MANIFEST = -5;
	public static final int ERR_FAILURE_VERIFY_CERT = -6;

	// @deprecated
	public static final int ERR_UNAVAIlABLE_PARAM = -99;
	// @deprecated
	public static final int ERR_UNKNOWN = -100;

	protected ApkInfo apkInfo = null;

	protected StatusListener statusListener = null;
	protected int scanningStatus;
	protected int lastErrorCode;

	public interface StatusListener
	{
		public void onStart(long estimatedTime);
		public void onSuccess();
		public void onError(int error);
		public void onCompleted();
		public void onProgress(int step, String msg);
		public void onStateChanged(int status);
	}

	public ApkScanner(StatusListener statusListener)
	{
		setStatusListener(statusListener);
	}

	public StatusListener getStatusListener() {
		return this.statusListener;
	}

	public void setStatusListener(StatusListener statusListener) {
		setStatusListener(statusListener, true);
	}

	public void setStatusListener(StatusListener statusListener, boolean evokeCompleted)
	{
		synchronized(this) {
			this.statusListener = statusListener;
			if(statusListener == null || !evokeCompleted) return;

			if(lastErrorCode != NO_ERR) {
				statusListener.onError(lastErrorCode);
			} else if(scanningStatus != 0) {
				for(int state = 1; (state & STATUS_ALL_COMPLETED) != 0; state <<= 1) {
					if(!isCompleted(state)) continue;
					Log.v(state + " is compleated sooner than register listener");
					statusListener.onStateChanged(state);
				}
				if(isCompleted(STATUS_ALL_COMPLETED)) {
					statusListener.onSuccess();
					statusListener.onCompleted();
				}
			}
		}
	}

	public void openApk(final String apkFilePath)
	{
		openApk(apkFilePath, _RProp.S.FRAMEWORK_RES.get());
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
			framework = _RProp.S.FRAMEWORK_RES.get();
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
					String percent = output;//.replaceAll("\\[\\s*([0-9]*)%\\] .*", "$1");
					//if(!percent.equals(output)) {
					if(!percent.equals(prePercent)) {
						prePercent = percent;
						statusListener.onProgress(0, percent);
					}
					//}
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

	protected void solveCert()
	{
		if(!(new File(apkInfo.filePath)).exists()) {
			Log.e("No such apk file");
			return;
		}

		ArrayList<String> certList = new ArrayList<String>();
		ArrayList<String> certFiles = new ArrayList<String>();

		SignatureReport sr = null;
		if(SystemUtil.checkJvmVersion("1.8")) {
			sr = new SignatureReportByApksig(new File(apkInfo.filePath));

			for(int i = 0; i < sr.getSize(); i ++) {
				certList.add(sr.getReport(i));
			}
			apkInfo.signatureScheme = sr.getSignatureScheme();

			if(sr.contains("MD5", _RConst.SAMSUNG_KEY_MD5)) {
				apkInfo.featureFlags |= ApkInfo.APP_FEATURE_SAMSUNG_SIGN;
			}
			if(sr.contains("MD5", _RConst.SS_TEST_KEY_MD5)) {
				apkInfo.featureFlags |= ApkInfo.APP_FEATURE_PLATFORM_SIGN;
			}
		}

		apkInfo.certificates = null;
		if(!certList.isEmpty()) {
			apkInfo.certificates = certList.toArray(new String[certList.size()]);
		}

		String[] certFilePaths = ZipFileUtil.findFiles(apkInfo.filePath, null, "^META-INF/.*");

		if(certList.isEmpty() && certFilePaths == null) {
			Log.e("No such folder : META-INFO/");
			return;
		}

		certList.clear();
		for (String s : certFilePaths) {
			String ext = s.toUpperCase();
			if(ext.endsWith(".MF") || ext.endsWith(".SF")) {
				certFiles.add(s);
			} else if(ext.endsWith(".RSA") || ext.endsWith(".DSA") || ext.endsWith(".EC")) {
				try(ZipFile zipFile = new ZipFile(apkInfo.filePath)) {
					ZipEntry entry = zipFile.getEntry(s);
					if(entry != null) {
						try(InputStream is = zipFile.getInputStream(entry)) {
							sr = new SignatureReport(is, apkInfo.signatureScheme);
							for(int i = 0; i < sr.getSize(); i ++) {
								certList.add(sr.getReport(i));
							}
							if(!certList.isEmpty()) {
								apkInfo.certificates = certList.toArray(new String[certList.size()]);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		apkInfo.certFiles = null;
		if(!certFiles.isEmpty()) {
			apkInfo.certFiles = certFiles.toArray(new String[certFiles.size()]);
		}
	}

	protected void setType() {
		setType(apkInfo.filePath);
	}

	protected void setType(String fileName) {
		if(fileName == null) return;
		fileName = fileName.toLowerCase();
		if(fileName.endsWith(".apk")) {
			apkInfo.type = ApkInfo.PACKAGE_TYPE_APK;
		} else if(fileName.endsWith(".apex")) {
			apkInfo.type = ApkInfo.PACKAGE_TYPE_APEX;
		} else {
			apkInfo.type = ApkInfo.PACKAGE_TYPE_UNKNOWN;
		}
	}

	protected void scanningStarted() {
		synchronized (this) {
			lastErrorCode = 0;

			if(statusListener != null) {
				statusListener.onStart(0);
			}
		}
	}

	protected void errorOccurred(int errorCode) {
		synchronized (this) {
			lastErrorCode = errorCode;
			if(statusListener != null) {
				statusListener.onError(errorCode);
				statusListener.onCompleted();
			}
		}
	}

	protected void stateChanged(int status) {
		synchronized (this) {
			if(status == STATUS_STANBY) {
				scanningStatus = 0;
			} else {
				scanningStatus |= status;
			}

			if(statusListener != null) {
				statusListener.onStateChanged(status);

				if(scanningStatus == STATUS_ALL_COMPLETED) {
					Log.i("I: completed... ");
					statusListener.onStateChanged(STATUS_ALL_COMPLETED);
					statusListener.onSuccess();
					statusListener.onCompleted();
				}
			}
		}
	}

	public int getStatus() {
		return scanningStatus;
	}

	public boolean isScanning() {
		synchronized (this) {
			return (lastErrorCode == NO_ERR && scanningStatus != 0 && scanningStatus != STATUS_ALL_COMPLETED);
		}
	}

	public boolean isCompleted(int status) {
		synchronized (this) {
			return (status != 0 && (scanningStatus & status) == status);
		}
	}

	public int getLastErrorCode() {
		synchronized (this) {
			return lastErrorCode;
		}
	}

	public String getLastErrorMessage() {
		synchronized (this) {
			return getErrorMessage(lastErrorCode);
		}
	}

	public String getErrorMessage(int errCode) {
		switch(errCode) {
		case NO_ERR: return null;
		case ERR_NO_SUCH_FILE: return "No such apk file.";
		case ERR_NO_SUCH_MANIFEST: return "No such manifest file in the APK.";
		case ERR_FAILURE_PULL_APK: return "Can not pull apk from device.";
		case ERR_CAN_NOT_ACCESS_ASSET: return "Access fail for asset of APK.";
		case ERR_WRONG_MANIFEST: return "Manifest was wrong format.";
		case ERR_FAILURE_VERIFY_CERT: return "Bad signed.";
		case ERR_UNKNOWN: default: return "Unknown Error: " + errCode;
		}
	}

	public static ApkScanner getInstance() {
		return ApkScanner.getInstance(_RProp.B.USE_EASY_UI.get()
				? ApkScanner.APKSCANNER_TYPE_AAPTLIGHT : ApkScanner.APKSCANNER_TYPE_AAPT);
	}

	public static ApkScanner getInstance(String name) {
		if(name == null || APKSCANNER_TYPE_AAPT.equalsIgnoreCase(name)) {
			return new AaptLightScanner(null, false);
		} else if(APKSCANNER_TYPE_AAPTLIGHT.equalsIgnoreCase(name)) {
			return new AaptLightScanner(null);
		}
		return new AaptScanner(null);
	}

	public String getScannerType() {
		if(this.getClass().equals(AaptScanner.class)) {
			return APKSCANNER_TYPE_AAPT;
		} else if(this.getClass().equals(AaptLightScanner.class)) {
			return APKSCANNER_TYPE_AAPTLIGHT;
		}
		return APKSCANNER_TYPE_AAPT;
	}

	public static String getPackageName(final String apkFilePath) {
		if(apkFilePath == null || apkFilePath.isEmpty()
				|| !new File(apkFilePath).isFile()) {
			Log.e("No such file " + apkFilePath);
			return null;
		}

		Log.i("getDump AndroidManifest...");
		String[] androidManifest = AaptNativeWrapper.Dump.getXmltree(apkFilePath, new String[] { "AndroidManifest.xml" });
		if(androidManifest == null || androidManifest.length == 0) {
			Log.e("Failure : Can't read the AndroidManifest.xml");
			return null;
		}

		Log.i("createAaptXmlTree...");
		AaptXmlTreePath manifestPath = new AaptXmlTreePath();
		manifestPath.createAaptXmlTree(androidManifest);
		if(manifestPath.getNode("/manifest") == null || manifestPath.getNode("/manifest/application") == null) {
			Log.e("Failure : Wrong format. Don't have '<manifest>' or '<application>' tag");
			return null;
		}

		return manifestPath.getNode("/manifest").getAttribute("package");
	}
}
