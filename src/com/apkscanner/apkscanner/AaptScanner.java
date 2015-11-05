package com.apkscanner.apkscanner;

import java.io.File;
import java.util.ArrayList;

import com.apkscanner.Launcher;
import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.apkinfo.PermissionInfo;
import com.apkscanner.apkinfo.ResourceInfo;
import com.apkscanner.core.AaptWrapper;
import com.apkscanner.core.EstimatedTimeEnRoute;
import com.apkscanner.data.AaptXmlTreeNode;
import com.apkscanner.data.AaptXmlTreePath;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class AaptScanner extends ApkScannerStub
{
	private AaptXmlTreePath manifestPath = null;
	private String[] androidManifest = null;
	private String[] resourcesWithValue = null;
	
	public AaptScanner(StatusListener statusListener)
	{
		super(statusListener);
		//stateChanged(Status.UNINITIALIZE);
	}
	
	@Override
	public void openApk(final String apkFilePath, final String frameworkRes)
	{
		timeRecordStart();

		apkInfo = new ApkInfo();
		final AaptManifestReader manifestReader = new AaptManifestReader(null, null, apkInfo.manifest);
		
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
			statusListener.OnStart(EstimatedTimeEnRoute.calc(apkFile.getAbsolutePath()));
		}
		
		apkInfo.filePath = apkFile.getAbsolutePath();
		apkInfo.fileSize = apkFile.length();
		apkInfo.tempWorkPath = FileUtil.makeTempPath(apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)));
		Log.i("Temp path : " + apkInfo.tempWorkPath);
		
		final Object xmlTreeSync = new Object();
		final Object resouresSync = new Object();
		final Object SignSync = new Object();
		final Object PermSync = new Object();
		
		new Thread(new Runnable() {
			public void run()
			{
				synchronized(resouresSync) {
					resouresSync.notify();
					try {
						resouresSync.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Log.i("I: read aapt dump resources...");
					resourcesWithValue = AaptWrapper.Dump.getResources(apkInfo.filePath, true);
					manifestReader.setResources(resourcesWithValue);
					Log.i("resources completed");
				}
			}
		}).start();
		synchronized(resouresSync) {
			try {
				resouresSync.wait();
				resouresSync.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		new Thread(new Runnable() {
			public void run()
			{
				synchronized(xmlTreeSync) {
					xmlTreeSync.notify();
					try {
						xmlTreeSync.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Log.i("I: getDump AndroidManifest...");
					androidManifest = AaptWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { "AndroidManifest.xml" });

					Log.i("I: createAaptXmlTree...");
					manifestPath = new AaptXmlTreePath();
					manifestPath.createAaptXmlTree(androidManifest);
					manifestReader.setManifestPath(manifestPath);
					Log.i("xmlTreeSync completed");
				}
			}
		}).start();
		synchronized(xmlTreeSync) {
			try {
				xmlTreeSync.wait();
				xmlTreeSync.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
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
		synchronized(SignSync) {
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
				//stateChanged(Status.INITIALIZING);
				synchronized(xmlTreeSync) {
			        Log.i("get xmlTreeSync");
				}
				
				new Thread(new Runnable() {
					public void run()
					{
						synchronized(PermSync) {
							PermSync.notify();
							try {
								PermSync.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Log.i("read permissions start");
							manifestReader.readPermissions();
							Log.i("read permissions completed");
						}
					}
				}).start();
				synchronized(PermSync) {
					try {
						PermSync.wait();
						PermSync.notify();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		        
				synchronized(resouresSync) {
					Log.i("I: read resources completed");
				}
				
				Log.i("I: read basic info...");
				
				manifestReader.readBasicInfo();
				
				ResourceInfo[] icons = apkInfo.manifest.application.icons;
				if(icons != null & icons.length > 0) {
					String jarPath = "jar:file:" + apkInfo.filePath.replaceAll("#", "%23") + "!/";
					for(ResourceInfo r: icons) {
						if(r.name == null) {
							r.name = Resource.IMG_DEF_APP_ICON.getPath();
						}if(r.name.endsWith("qmg")) {
							r.name = Resource.IMG_QMG_IMAGE_ICON.getPath();
						} else if(r.name.endsWith(".xml")) {
							Log.w("image resource is xml : " + r.name);
							String[] iconXml = AaptWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { r.name });
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
		        synchronized(SignSync) {
		        	Log.i("sync SignSync");
		        	synchronized(PermSync) {
		        		Log.i("sync PermSync");
			        	timeRecordEnd();
		        		stateChanged(Status.BASIC_INFO_COMPLETED);
		        	}
		        }

				new Thread(new Runnable() {
					public void run()
					{
				        // Activity/Service/Receiver/provider intent-filter
						Log.i("I: read activitys...");
				        manifestReader.readActivityInfo();
				        manifestReader.readActivityAliasInfo();
				        manifestReader.readServiceInfo();
				        manifestReader.readReceiverInfo();
				        manifestReader.readProviderInfo();
				        stateChanged(Status.ACTIVITY_COMPLETED);
					}
				}).run();
				
				new Thread(new Runnable() {
					public void run()
					{
				        // widget
						Log.i("I: read widgets...");

				        apkInfo.widgets = manifestReader.getWidgetList(apkInfo.filePath);
				        stateChanged(Status.WIDGET_COMPLETED);
					}
				}).run();
				
				Log.i("I: completed...");
		        
		        if(statusListener != null) {
		        	statusListener.OnSuccess();
		        	statusListener.OnComplete();
		        }
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run()
			{
				Log.i("I: read Imanges list...");
		        apkInfo.images = ZipFileUtil.findFiles(apkInfo.filePath, ".png;.qmg;.jpg;.gif", null);
		        stateChanged(Status.IMAGE_COMPLETED);
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run()
			{
				Log.i("I: read lib list...");
		        apkInfo.librarys = ZipFileUtil.findFiles(apkInfo.filePath, ".so", null);
		        stateChanged(Status.LIB_COMPLETED);
			}
		}).start();
	}
	
	public String[] getAndroidManifest()
	{
		return androidManifest;
	}
	
	private String getResourceName(String id)
	{
		if(resourcesWithValue == null || id == null || !id.startsWith("@"))
			return id;
		String name = id;
		String filter = "spec resource " + id.substring(1);
		for(String s: resourcesWithValue) {
			if(s.indexOf(filter) > -1) {
				name = s.replaceAll(".*:(.*):.*", "@$1");
				break;
			}
		}
		return name;
	}
	
	private String makeNodeXml(AaptXmlTreeNode node, String namespace, String depthSpace)
	{
		StringBuilder xml = new StringBuilder(depthSpace);

		xml.append("<" + node.getName());
		if(node.getName().equals("manifest")) {
			xml.append(" xmlns:");
			xml.append(manifestPath.getNamespace());
			xml.append("=\"http://schemas.android.com/apk/res/android\"");
		}
		for(String name: node.getAttributeList()) {
			xml.append(" ");
			xml.append(name);
			xml.append("=\"");
			if(name.endsWith("protectionLevel")) {
				String protection = node.getAttribute(name);
	        	if(protection != null && protection.startsWith("0x")) {
	        		int level = Integer.parseInt(protection.substring(2), 16);
	        		xml.append(PermissionInfo.protectionToString(level));
	        	} else {
	        		xml.append(protection);
	        	}
			} else {
				xml.append(getResourceName(node.getAttribute(name)));
			}
			xml.append("\"");
		}
		if(node.getNodeCount() > 0) {
			xml.append(">\r\n");
			for(AaptXmlTreeNode child: node.getNodeList()) {
				xml.append(makeNodeXml(child, namespace, depthSpace + "    "));
			}
			xml.append(depthSpace);
			xml.append("</");
			xml.append(node.getName());
			xml.append(">\r\n");
		} else {
			xml.append("/>\r\n");
		}
		
		return xml.toString();
	}
	
	public String makeAndroidManifestXml()
	{
		if(manifestPath == null) return null;
		
		AaptXmlTreeNode topNode = manifestPath.getNode("/manifest");
		if(topNode == null) return null;
		
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\r\n");
		
		xml.append(makeNodeXml(topNode, manifestPath.getNamespace(), ""));
		
		return xml.toString();
	}

	private String[] solveCert()
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
		
		if(!(new File(apkInfo.filePath)).exists()) {
			return null;
		}
		
		if(!ZipFileUtil.unZip(apkInfo.filePath, "META-INF/", certPath)) {
			Log.e("META-INFO 폴더가 존재 하지 않습니다 :");
			return null;
		}
		
		for (String s : (new File(certPath)).list()) {
			if(!s.endsWith(".RSA") && !s.endsWith(".DSA") && !s.endsWith(".EC") ) continue;

			File rsaFile = new File(certPath + File.separator + s);
			if(!rsaFile.exists()) continue;

			String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
			String[] result = ConsolCmd.exc(cmd, false, null);

		    String certContent = "";

		    for(int i=0; i < result.length; i++){
	    		if(!certContent.isEmpty() && result[i].matches("^.*\\[[0-9]*\\]:$")) {
	    			certList.add(certContent);
			    	certContent = "";
	    		}
	    		if(result[i].matches("^.*:( [^ ,]+=(\".*\")?[^,]*,?)+$")) {
	    			if(result[i].indexOf("CN=") > -1) {
	    				String CN = result[i].replaceAll(".*CN=([^,]*).*", "$1");
	    				if("Samsung Cert".equals(CN)) {
	    					apkInfo.featureFlags |= ApkInfo.APP_FEATURE_SAMSUNG_SIGN;
	    				} else if("Android".equals(CN)) {
	    					apkInfo.featureFlags |= ApkInfo.APP_FEATURE_PLATFORM_SIGN;
	    				}
	    			}
	    		}
	    		certContent += (certContent.isEmpty() ? "" : "\n") + result[i];
		    }
		    certList.add(certContent);
		}

		return certList.toArray(new String[0]);
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
	}
	
	private void stateChanged(Status status)
	{
		if(statusListener != null) {
			//if(apkInfo != null) apkInfo.verify();
			statusListener.OnStateChanged(status);
		}
	}
}
