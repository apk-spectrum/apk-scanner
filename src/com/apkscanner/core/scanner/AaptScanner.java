package com.apkscanner.core.scanner;

import java.io.File;

import com.apkscanner.Launcher;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AaptXmlTreeNode;
import com.apkscanner.tool.aapt.AaptXmlTreePath;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class AaptScanner extends ApkScannerStub
{
	private AaptXmlTreePath manifestPath = null;
	private AaptNativeScanner resourceScanner;
	
	public AaptScanner(StatusListener statusListener)
	{
		super(statusListener);
		//stateChanged(Status.UNINITIALIZE);
		resourceScanner = null;
	}
	
	@Override
	public void openApk(final String apkFilePath, final String frameworkRes)
	{
		if(apkFilePath == null) {
			Log.e("APK file path is null");
			if(statusListener != null) {
				statusListener.OnError();
	        	statusListener.OnComplete();
			}
			return;
		}

		File apkFile = new File(apkFilePath);
		if(!apkFile.exists()) {
			Log.e("No Such APK file");
			if(statusListener != null) {
				statusListener.OnError();
	        	statusListener.OnComplete();
			}
			return;
		}

		if(statusListener != null) {
			statusListener.OnStart(1);
		}

		apkInfo = new ApkInfo();
		apkInfo.filePath = apkFile.getAbsolutePath();
		apkInfo.fileSize = apkFile.length();
		apkInfo.tempWorkPath = FileUtil.makeTempPath(apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)));
		Log.i("Temp path : " + apkInfo.tempWorkPath);
		
		Log.i("I: new resourceScanner...");
		if(resourceScanner != null) {
			resourceScanner.clear(true);
		}
		resourceScanner = new AaptNativeScanner(null);
		Log.i("I: open resource apk");
		resourceScanner.openApk(apkInfo.filePath, frameworkRes);
		apkInfo.resourceScanner = resourceScanner;
		
		final AaptManifestReader manifestReader = new AaptManifestReader(null, apkInfo.manifest);
		manifestReader.setResourceScanner(resourceScanner);
		
		final Object SignSync = new Object();
		synchronized(SignSync) {
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(SignSync) {
						SignSync.notify();
						try {
							SignSync.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				        Log.i("read signatures...");
				        apkInfo.certificates = solveCert();
						stateChanged(Status.CERT_COMPLETED);
						Log.i("read signatures completed...");
					}
				}
			}).start();
			try {
				SignSync.wait();
				SignSync.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		new Thread(new Runnable() {
			public void run()
			{
				new Thread(new Runnable() {
					public void run()
					{
						Log.i("I: read lib list...");
				        apkInfo.librarys = ZipFileUtil.findFiles(apkInfo.filePath, ".so", null);
				        stateChanged(Status.LIB_COMPLETED);
				        
						Log.i("I: read Resource list...");
				        apkInfo.resources = ZipFileUtil.findFiles(apkInfo.filePath, null, null);
				        stateChanged(Status.RESOURCE_COMPLETED);
					}
				}).start();
				
				Log.i("I: read aapt dump resources...");
				apkInfo.resourcesWithValue = AaptNativeWrapper.Dump.getResources(apkInfo.filePath, true);
				stateChanged(Status.RES_DUMP_COMPLETED);
				Log.i("resources completed");
			}
		}).start();
		
		Log.i("I: getDump AndroidManifest...");
		String[] androidManifest = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { "AndroidManifest.xml" });
		
		Log.i("I: createAaptXmlTree...");
		manifestPath = new AaptXmlTreePath();
		manifestPath.createAaptXmlTree(androidManifest);
		
		manifestReader.setManifestPath(manifestPath);
		Log.i("xmlTreeSync completed");

		Log.e("I: read basic info...");
		manifestReader.readBasicInfo();
		Log.e("readBasicInfo() completed");

		Log.i("read permissions start");
		manifestReader.readPermissions();
		Log.i("read permissions completed");
		//stateChanged(Status.PERM_INFO_COMPLETED);
		
		ResourceInfo[] icons = apkInfo.manifest.application.icons;
		if(icons != null & icons.length > 0) {
			String jarPath = "jar:file:" + apkInfo.filePath.replaceAll("#", "%23") + "!/";
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
					AaptXmlTreeNode iconNode = iconXmlPath.getNode("//item[@"+iconXmlPath.getNamespace()+":drawable]");
					if(iconNode != null) {
						icons = manifestReader.getAttrResourceValues(iconNode, ":drawable", iconXmlPath.getNamespace());
					}
					if(icons == null || icons.length == 0) {
						icons = new ResourceInfo[] { new ResourceInfo(Resource.IMG_DEF_APP_ICON.getPath()) };
					} else {
						for(ResourceInfo r2: icons) {
							r2.name = jarPath + r2.name;
						}
					}
				} else {
					r.name = jarPath + r.name;
				}
			}
		} else {
			icons = new ResourceInfo[] { new ResourceInfo(Resource.IMG_DEF_APP_ICON.getPath()) };
		}
		apkInfo.manifest.application.icons = icons;

		Log.i("read basic info completed");
		stateChanged(Status.BASIC_INFO_COMPLETED);

        // Activity/Service/Receiver/provider intent-filter
		Log.i("I: read activitys...");
        manifestReader.readComponents();
        stateChanged(Status.ACTIVITY_COMPLETED);

        // widget
		Log.i("I: read widgets...");
        apkInfo.widgets = manifestReader.getWidgetList(apkInfo.filePath);
        stateChanged(Status.WIDGET_COMPLETED);
		
		Log.i("I: completed...");
        
        if(statusListener != null) {
        	statusListener.OnSuccess();
        	statusListener.OnComplete();
        }
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
				Launcher.deleteTempPath(apkPath);
			}
		}
	}

	@Override
	public void clear(boolean sync)
	{
		if(apkInfo == null)
			return;
		final String tmpPath = apkInfo.tempWorkPath;
		final String apkPath = apkInfo.filePath;
		if(sync) {
			deleteTempPath(tmpPath, apkPath);
		} else {
			new Thread(new Runnable() {
				public void run()
				{
					deleteTempPath(tmpPath, apkPath);
				}
			}).start();
		}
		apkInfo = null;

		if(resourceScanner != null) {
			resourceScanner.clear(true);
			resourceScanner = null;
		}
	}
}
