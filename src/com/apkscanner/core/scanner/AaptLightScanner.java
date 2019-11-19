package com.apkscanner.core.scanner;

import java.io.File;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AaptXmlTreePath;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class AaptLightScanner extends AaptScanner {

	private boolean isLightMode;

	public AaptLightScanner(StatusListener statusListener) {
		this(statusListener, true);
	}

	public AaptLightScanner(StatusListener statusListener, boolean lightMode) {
		super(statusListener);
		isLightMode = lightMode;
	}

	@Override
	public void openApk(String apkFilePath, String frameworkRes) {
		if(!isLightMode) {
			super.openApk(apkFilePath, frameworkRes);
			return;
		}

		Log.i("---aaptLightScanner---");
		Log.i("openApk() " + apkFilePath + ", res " + frameworkRes);
		EasyMainUI.corestarttime = System.currentTimeMillis();
		
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
		if(resourceScanner != null) {
			resourceScanner.clear(true);
		}
		resourceScanner = new AaptNativeScanner(null);

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
			errorOccurred(ERR_NO_SUCH_MANIFEST);
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

		apkInfo = new ApkInfo();
		apkInfo.filePath = apkFile.getAbsolutePath();
		apkInfo.fileSize = apkFile.length();
		apkInfo.tempWorkPath = FileUtil.makeTempPath(apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)));
		apkInfo.resourceScanner = resourceScanner;

		stateChanged(Status.STANBY);

		Log.v("Temp path : " + apkInfo.tempWorkPath);

		manifestReader = new AaptManifestReader(null, apkInfo.manifest);
		manifestReader.setResourceScanner(resourceScanner);
		manifestReader.setManifestPath(manifestPath);
		Log.i("xmlTreeSync completed");

		Log.i("I: read basic info...");
		manifestReader.readBasicInfo();
		Log.i("readBasicInfo() completed");
		
		//2018. 10. 26		//////////////////////////////////////////
		// for easy gui
		apkInfo.manifest.application.icons = changeURLpath(apkInfo.manifest.application.icons, manifestReader);
		///////////////////////////////////////////////////////////

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

		stateChanged(Status.ALL_COMPLETED);
	}

	public void setLightMode(boolean lightMode) {
		if(isLightMode == lightMode) return;
		if(isLightMode && !lightMode && getStatus() == Status.ALL_COMPLETED.value()) {
			// widget
			Log.i("I: read widgets...");
			apkInfo.widgets = manifestReader.getWidgetList(apkInfo.filePath);
			stateChanged(Status.WIDGET_COMPLETED);

			Thread thread = new Thread(new Runnable() {
				public void run() {
					Log.i("I: read Resource list...");
					apkInfo.resources = ZipFileUtil.findFiles(apkInfo.filePath, null, null);
					stateChanged(Status.RESOURCE_COMPLETED);
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();

			thread = new Thread(new Runnable() {
				public void run() {
					Log.i("I: read aapt dump resources...");
					apkInfo.resourcesWithValue = AaptNativeWrapper.Dump.getResources(apkInfo.filePath, true);
					stateChanged(Status.RES_DUMP_COMPLETED);
					Log.i("resources completed");
				}
			});
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
			
		}
		isLightMode = lightMode;
	}
}
