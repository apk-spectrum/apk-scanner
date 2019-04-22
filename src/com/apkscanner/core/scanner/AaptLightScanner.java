package com.apkscanner.core.scanner;

import java.io.File;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner.StatusListener;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AaptXmlTreeNode;
import com.apkscanner.tool.aapt.AaptXmlTreePath;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class AaptLightScanner extends AaptScanner {

	AaptNativeScanner resourceScanner;
	
	public AaptLightScanner(StatusListener statusListener) {
		super(statusListener);
	}

	@Override
	public void openApk(String apkFilePath, String frameworkRes) {
		Log.i("---aaptLightScanner---");
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

		final AaptManifestReader manifestReader = new AaptManifestReader(null, apkInfo.manifest);
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
		

		
		// Activity/Service/Receiver/provider intent-filter
		Log.i("I: read components...");
		manifestReader.readComponents();
		stateChanged(Status.ACTIVITY_COMPLETED);


		stateChanged(Status.ALL_COMPLETED);

	}

	private ResourceInfo[] changeURLpath(ResourceInfo[] icons, AaptManifestReader manifestReader) {		
		if(icons != null && icons.length > 0) {
			String urlFilePath = null;
			urlFilePath = apkInfo.filePath.replaceAll("#", "%23");

			String jarPath = "jar:file:" + urlFilePath + "!/";
			for(ResourceInfo r: icons) {
				if(r.name == null) {
					r.name = Resource.IMG_DEF_APP_ICON.getPath();
				} else if(r.name.endsWith(".qmg")) {
					r.name = Resource.IMG_QMG_IMAGE_ICON.getPath();
				} else if(r.name.endsWith(".xml")) {
					Log.w("image resource is xml : " + r.name);
					String[] iconXml = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { r.name });
					AaptXmlTreePath iconXmlPath = new AaptXmlTreePath();
					iconXmlPath.createAaptXmlTree(iconXml);
					AaptXmlTreeNode iconNode = iconXmlPath.getNode("//item[@"+iconXmlPath.getAndroidNamespaceTag()+":drawable]");
					if(iconNode != null) {
						icons = manifestReader.getAttrResourceValues(iconNode, ":drawable", iconXmlPath.getAndroidNamespaceTag());
						if(icons == null || icons.length == 0) {
							icons = new ResourceInfo[] { new ResourceInfo(Resource.IMG_DEF_APP_ICON.getPath()) };
						} else {
							for(ResourceInfo r2: icons) {
								r2.name = jarPath + r2.name;
							}
						}
					}
				} else {
					r.name = jarPath + r.name;
				}
			}
		} else {
			icons = new ResourceInfo[] { new ResourceInfo(Resource.IMG_DEF_APP_ICON.getPath()) };
		}
		return icons;
	}	
}
