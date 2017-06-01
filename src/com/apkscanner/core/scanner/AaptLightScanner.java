package com.apkscanner.core.scanner;

import java.io.File;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AaptXmlTreePath;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class AaptLightScanner extends ApkScanner {

	public AaptLightScanner() {
		super(null);
	}

	@Override
	public void openApk(String apkFilePath, String frameworkRes) {
		Log.i("openApk() " + apkFilePath + ", res " + frameworkRes);
		if(apkFilePath == null) {
			Log.e("APK file path is null");
			errorOccurred(ERR_UNAVAIlABLE_PARAM);
			return;
		}

		File apkFile = new File(apkFilePath);
		if(!apkFile.exists()) {
			Log.e("No Such APK file");
			errorOccurred(ERR_NO_SUCH_FILE);
			return;
		}

		scanningStarted();

		Log.i("I: new resourceScanner...");
		AaptNativeScanner resourceScanner = new AaptNativeScanner(null);

		Log.i("I: add asset apk");
		resourceScanner.openApk(apkFile.getAbsolutePath(), frameworkRes);
		if(!resourceScanner.hasAssetManager()){
			resourceScanner = null;
			Log.e("Failure : Can't open the AssetManager");
			errorOccurred(ERR_CAN_NOT_ACCESS_ASSET);
			return;
		}

		Log.i("I: getDump AndroidManifest...");
		String[] androidManifest = AaptNativeWrapper.Dump.getXmltree(apkFile.getAbsolutePath(), new String[] { "AndroidManifest.xml" });
		if(androidManifest == null || androidManifest.length == 0) {
			Log.e("Failure : Can't read the AndroidManifest.xml");
			errorOccurred(ERR_CAN_NOT_READ_MANIFEST);
			return;
		}

		Log.i("I: createAaptXmlTree...");
		AaptXmlTreePath manifestPath = new AaptXmlTreePath();
		manifestPath.createAaptXmlTree(androidManifest);

		if(manifestPath.getNode("/manifest") == null || manifestPath.getNode("/manifest/application") == null) {
			Log.e("Failure : Wrong format. Don't have '<manifest>' or '<application>' tag");
			errorOccurred(ERR_WRONG_MANIFEST);
			return;
		}

		stateChanged(Status.STANBY);

		apkInfo = new ApkInfo();
		apkInfo.filePath = apkFile.getAbsolutePath();
		apkInfo.fileSize = apkFile.length();
		apkInfo.tempWorkPath = FileUtil.makeTempPath(apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)));
		apkInfo.resourceScanner = resourceScanner;

		Log.v("Temp path : " + apkInfo.tempWorkPath);

		final AaptManifestReader manifestReader = new AaptManifestReader(null, apkInfo.manifest);
		manifestReader.setResourceScanner(resourceScanner);
		manifestReader.setManifestPath(manifestPath);
		Log.i("xmlTreeSync completed");

		Log.i("I: read basic info...");
		manifestReader.readBasicInfo();
		Log.i("readBasicInfo() completed");

		Log.i("read permissions start");
		manifestReader.readPermissions();
		Log.i("read permissions completed");

		Log.i("read basic info completed");
		stateChanged(Status.BASIC_INFO_COMPLETED);

		Log.i("read signatures...");
		apkInfo.certificates = solveCert();
		stateChanged(Status.CERT_COMPLETED);
		Log.i("read signatures completed...");
		
		Log.i("I: read libraries list...");
		apkInfo.libraries = ZipFileUtil.findFiles(apkInfo.filePath, ".so", null);
		stateChanged(Status.LIB_COMPLETED);
		
		// Activity/Service/Receiver/provider intent-filter
		Log.i("I: read components...");
		manifestReader.readComponents();
		stateChanged(Status.ACTIVITY_COMPLETED);

		if(resourceScanner != null) {
			resourceScanner.clear(true);
		}
	}

	@Override
	public void clear(boolean sync) {
		
	}
}
