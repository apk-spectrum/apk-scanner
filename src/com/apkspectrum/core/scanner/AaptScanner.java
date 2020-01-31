package com.apkspectrum.core.scanner;

import java.io.File;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.Launcher;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.URITool;
import com.apkspectrum.util.ZipFileUtil;

public class AaptScanner extends ApkScanner
{
	protected AaptNativeScanner resourceScanner;
	protected AaptManifestReader manifestReader;

	public AaptScanner(StatusListener statusListener)
	{
		super(statusListener);
	}

	@Override
	public void openApk(final String apkFilePath, final String frameworkRes)
	{
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
		apkInfo.a2xConvert = new AxmlToXml(resourceScanner);

		setType();

		stateChanged(STATUS_STANBY);

		Log.v("Temp path : " + apkInfo.tempWorkPath);

		manifestReader = new AaptManifestReader(null, apkInfo.manifest);
		manifestReader.setResourceScanner(resourceScanner);
		manifestReader.setManifestPath(manifestPath);
		Log.i("xmlTreeSync completed");

		Log.i("I: read basic info...");
		manifestReader.readBasicInfo();
		Log.i("readBasicInfo() completed");

		Log.i("read permissions start");
		manifestReader.readPermissions();
		Log.i("read permissions completed");

		apkInfo.manifest.application.icons = changeURLpath(apkInfo.manifest.application.icons, manifestReader);

		readApexInfo();

		Log.i("read basic info completed");
		stateChanged(STATUS_BASIC_INFO_COMPLETED);

		new Thread(new Runnable() {
			public void run() {
				Log.i("read signatures...");
				solveCert();
				stateChanged(STATUS_CERT_COMPLETED);
				Log.i("read signatures completed...");
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				Log.i("I: read libraries list...");
				apkInfo.libraries = ZipFileUtil.findFiles(apkInfo.filePath, ".so", null);
				stateChanged(STATUS_LIB_COMPLETED);

				Log.i("I: read Resource list...");
				apkInfo.resources = ZipFileUtil.findFiles(apkInfo.filePath, null, null);
				stateChanged(STATUS_RESOURCE_COMPLETED);
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				Log.i("I: read aapt dump resources...");
				apkInfo.resourcesWithValue = AaptNativeWrapper.Dump.getResources(apkInfo.filePath, true);
				stateChanged(STATUS_RES_DUMP_COMPLETED);
				Log.i("resources completed");
			}
		}).start();

		// Activity/Service/Receiver/provider intent-filter
		Log.i("I: read components...");
		manifestReader.readComponents();
		stateChanged(STATUS_ACTIVITY_COMPLETED);

		// widget
		Log.i("I: read widgets...");
		apkInfo.widgets = manifestReader.getWidgetList(apkInfo.filePath);
		stateChanged(STATUS_WIDGET_COMPLETED);
	}

	protected void readApexInfo() {
		if(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX) return;

		apkInfo.manifest.application.labels = new ResourceInfo[1];
		apkInfo.manifest.application.labels[0] = new ResourceInfo(apkInfo.manifest.packageName);

		byte[] rawData = ZipFileUtil.readData(apkInfo.filePath, "apex_manifest.json");
		if(rawData != null) {
			try {
				JSONObject apexManifest = (JSONObject)(new JSONParser()).parse(new String(rawData));
				apkInfo.manifest.packageName = (String) apexManifest.get("name");
				apkInfo.manifest.versionName = apkInfo.manifest.versionCode.toString();
				Object data = apexManifest.get("version");
				if(data instanceof Long) {
					apkInfo.manifest.versionCode = (int)(long)data;
				} else if(data instanceof Integer) {
					apkInfo.manifest.versionCode = (int)data;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	protected ResourceInfo[] changeURLpath(ResourceInfo[] icons, AaptManifestReader manifestReader) {
		if(icons != null && icons.length > 0) {
			String jarPath = "jar:" + new File(apkInfo.filePath).toURI() + "!/";
			for(ResourceInfo r: icons) {
				if(r.name == null)  continue;

				if(r.name.endsWith(".xml")) {
					Log.w("image resource is xml : " + r.name);
					String[] iconXml = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { r.name });
					AaptXmlTreePath iconXmlPath = new AaptXmlTreePath();
					iconXmlPath.createAaptXmlTree(iconXml);
					AaptXmlTreeNode iconNode = iconXmlPath.getNode("//item[@"+iconXmlPath.getAndroidNamespaceTag()+":drawable]");
					if(iconNode != null) {
						icons = manifestReader.getAttrResourceValues(iconNode, ":drawable", iconXmlPath.getAndroidNamespaceTag());
						if(icons == null || icons.length == 0) {
							icons = new ResourceInfo[] { new ResourceInfo(null) };
						} else {
							for(ResourceInfo r2: icons) {
								r2.name = jarPath + URITool.encodeURI(r2.name);
							}
						}
					}
				} else {
					r.name = jarPath + URITool.encodeURI(r.name);
				}
			}
		} else {
			icons = new ResourceInfo[] { new ResourceInfo(null) };
		}
		return icons;
	}

	private void deleteTempPath(String tmpPath, String apkPath)
	{
		if(tmpPath != null && !tmpPath.isEmpty()) {
			Log.i("delete Folder : "  + tmpPath);
			FileUtil.deleteDirectory(new File(tmpPath));
		}
		if(apkPath != null && !apkPath.isEmpty() && apkPath.startsWith(FileUtil.getTempPath())) {
			File parent = new File(apkPath).getParentFile();
			Log.i("delete temp APK folder : "  + parent.getPath());
			while(parent != null && parent.exists() && parent.getParentFile() != null
					&& parent.getParentFile().listFiles().length == 1
					&& parent.getParentFile().getAbsolutePath().length() > FileUtil.getTempPath().length()) {
				parent = parent.getParentFile();
			}
			FileUtil.deleteDirectory(parent);
			if(new File(apkPath).exists()) {
				Log.i("failure: not delete apk file");
				new Launcher().run("delete-temp-path", apkPath);
			}
		}
	}

	@Override
	public void clear(boolean sync)
	{
		if(apkInfo == null)
			return;

		if(resourceScanner != null) {
			resourceScanner.clear(true);
			resourceScanner = null;
		}
		AaptNativeScanner.lock();
		AaptNativeWrapper.lock();

		final String tmpPath = apkInfo.tempWorkPath;
		final String apkPath = apkInfo.filePath;
		if(sync) {
			deleteTempPath(tmpPath, apkPath);
			AaptNativeScanner.unlock();
			AaptNativeWrapper.unlock();
		} else {
			new Thread(new Runnable() {
				public void run()
				{
					deleteTempPath(tmpPath, apkPath);
					AaptNativeScanner.unlock();
					AaptNativeWrapper.unlock();
				}
			}).start();
		}
		apkInfo = null;
	}
}
