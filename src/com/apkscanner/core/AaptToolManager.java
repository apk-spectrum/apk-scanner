package com.apkscanner.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.apkscanner.data.AaptXmlTreeNode;
import com.apkscanner.data.AaptXmlTreePath;
import com.apkscanner.data.ApkInfo;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;
import com.apkscanner.util.FileUtil.FSStyle;

public class AaptToolManager extends ApkScannerStub
{
	private AaptXmlTreePath manifestPath = null;
	
	private String androidManifest[];
	private String resourcesWithValue[];
	
	public AaptToolManager(StatusListener statusListener)
	{
		super(statusListener);
	}

	@Override
	public void openApk(final String apkFilePath, String frameworkRes)
	{
		new Thread(new Runnable() {
			public void run()
			{
				if(statusListener != null) statusListener.OnStart();
				
				apkInfo = new ApkInfo();
				
				File apkFile = new File(apkFilePath);

				if(!apkFile.exists()) {
					Log.e("No Such APK file");
					return;
				}
				//apkFilePath = apkFile.getAbsolutePath();
				apkInfo.ApkPath = apkFilePath; 
				
				apkInfo.WorkTempPath = FileUtil.makeTempPath(apkInfo.ApkPath.substring(apkInfo.ApkPath.lastIndexOf(File.separator)));
				Log.i("Temp path : " + apkInfo.WorkTempPath);

				apkInfo.ApkSize = FileUtil.getFileSize(apkFile, FSStyle.FULL);
				
				if(apkFilePath != null && (new File(apkFilePath)).exists()) {
					apkInfo.ApkPath = apkFilePath;
				}
				
				if(statusListener != null) statusListener.OnProgress(5, "I: start open apk");

				if(statusListener != null) statusListener.OnProgress(5, "I: getDump AndroidManifest...");
				androidManifest = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] {"AndroidManifest.xml"});

				if(statusListener != null) statusListener.OnProgress(10, "I: read aapt dump resources...");
				resourcesWithValue = AaptWrapper.Dump.getResources(apkFilePath, true);

				if(statusListener != null) statusListener.OnProgress(20, "I: createAaptXmlTree...");
				manifestPath = new AaptXmlTreePath();
				manifestPath.createAaptXmlTree(androidManifest);
				
				
				if(statusListener != null) statusListener.OnProgress(5, "I: read basic info...");
				
				AaptXmlTreeNode manifestTag = manifestPath.getNode("/manifest"); 

				// package
				apkInfo.PackageName = manifestTag.getAttribute("package");
				apkInfo.VersionCode = hex2IntString(manifestTag.getAttribute("android:versionCode"));
				apkInfo.VersionName = manifestTag.getAttribute("android:versionName");
				apkInfo.SharedUserId = manifestTag.getAttribute("android:sharedUserId");
				
				AaptXmlTreeNode usesSdkTag = manifestPath.getNode("/manifest/uses-sdk");
				if(usesSdkTag != null) {
					apkInfo.TargerSDKversion = hex2IntString(usesSdkTag.getAttribute("android:targetSdkVersion"));
					apkInfo.MinSDKversion = hex2IntString(usesSdkTag.getAttribute("android:minSdkVersion"));
				}

				// label & icon
				AaptXmlTreeNode applicationTag = manifestPath.getNode("/manifest/application");
				apkInfo.Labelname = getResourceValues(applicationTag.getAttribute("android:label"), true);
				String iconPaths[] = getResourceValues(applicationTag.getAttribute("android:icon"), false);
				if(iconPaths.length > 0) {
					apkInfo.IconPath = iconPaths[iconPaths.length-1];
				}

				String debuggable = applicationTag.getAttribute("android:debuggable");
				apkInfo.debuggable = debuggable != null && debuggable.toLowerCase().equals("true");

		        // hidden
		        if(manifestPath.getNode("/manifest/application/activity/intent-filter/category[@android:name='android.intent.category.LAUNCHER']") != null) {
		        	apkInfo.isHidden = true;
		        }

		        // startup
		        if(manifestPath.getNode("/manifest/uses-permission[@android:name='android.permission.RECEIVE_BOOT_COMPLETED']") != null) {
		        	apkInfo.Startup = "START_UP";
		        } else {
		        	apkInfo.Startup = "";
		        }
		        Log.i("Startup : " + apkInfo.Startup + apkInfo.isHidden);
		        
		        if(statusListener != null) statusListener.OnProgress(5, "I: read permissions...");
		        // permission
		        apkInfo.ProtectionLevel = "";
		        //progress(5,"parsing permission...\n");
		        AaptXmlTreeNode[] permTag = manifestPath.getNodeList("/manifest/uses-permission");
		        Log.i(">>>>>> permTag length " + permTag.length);
		        for( int idx=0; idx < permTag.length; idx++ ){
		        	if(idx==0) apkInfo.Permissions = "<uses-permission> [" + permTag.length + "]";
		        	apkInfo.Permissions += "\n" + permTag[idx].getAttribute("android:name");
		        	apkInfo.PermissionList.add(permTag[idx].getAttribute("android:name"));
		        	String sig = permTag[idx].getAttribute("android:protectionLevel");
		        	if(sig != null && sig.equals("signature")) {
		        		apkInfo.Permissions += " - <SIGNATURE>";
		        		apkInfo.ProtectionLevel = "SIGNATURE";
		        	}
		        }
		        permTag = manifestPath.getNodeList("/manifest/permission");
		        for( int idx=0; idx < permTag.length; idx++ ){
		        	if(idx==0) apkInfo.Permissions += "\n\n<permission> [" + permTag.length + "]";
		        	apkInfo.Permissions += "\n" + permTag[idx].getAttribute("android:name");
		        	apkInfo.PermissionList.add(permTag[idx].getAttribute("android:name"));
		        	String sig = permTag[idx].getAttribute("android:protectionLevel");
		        	if(sig != null && sig.equals("signature")) {
		        		apkInfo.Permissions += " - <SIGNATURE>";
		        		apkInfo.ProtectionLevel = "SIGNATURE";
		        	}
		        }
		        PermissionGroupManager permGroupManager = new PermissionGroupManager(apkInfo.PermissionList.toArray(new String[0]));
		        apkInfo.PermGroupMap = permGroupManager.getPermGroupMap();
		        
		        // widget
		        if(statusListener != null) statusListener.OnProgress(5, "I: read widgets...");
		        AaptXmlTreeNode[] widgetTag = manifestPath.getNodeList("/manifest/application/receiver/meta-data[@android:name='android.appwidget.provider']/..");
		        //Log.i("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
		        for( int idx=0; idx < widgetTag.length; idx++ ){
		        	Object[] widgetExtraInfo = {apkInfo.IconPath, ""};

		        	String widgetTitle = null;
		        	String widgetActivity = null;
		        	String tmp[] = getResourceValues(widgetTag[idx].getAttribute("android:label"), false);
		        	if(tmp.length > 0) {
		        		widgetTitle = tmp[0];
		        	}
		        	tmp = getResourceValues(widgetTag[idx].getAttribute("android:name"), false);
		        	if(tmp.length > 0) {
		        		widgetActivity = tmp[0];
		        	}

		        	Object[] extraInfo = getWidgetInfo(getResourceValues(widgetTag[idx].getNode("meta-data").getAttribute("android:resource"), false));
		        	if(extraInfo != null) {
		        		widgetExtraInfo = extraInfo;
		        	}
		        	
		        	apkInfo.WidgetList.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
		        }
		        
		        widgetTag = manifestPath.getNodeList("/manifest/application/activity-alias/intent-filter/action[@android:name='android.intent.action.CREATE_SHORTCUT']/../..");
		        //Log.i("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
		        for( int idx=0; idx < widgetTag.length; idx++ ){
		        	String widgetTitle = null;
		        	String widgetActivity = null;
		        	String tmp[] = getResourceValues(widgetTag[idx].getAttribute("android:label"), false);
		        	if(tmp.length > 0) {
		        		widgetTitle = tmp[0];
		        	}
		        	tmp = getResourceValues(widgetTag[idx].getAttribute("android:name"), false);
		        	if(tmp.length > 0) {
		        		widgetActivity = tmp[0];
		        	}

		        	apkInfo.WidgetList.add(new Object[] {apkInfo.IconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
		        }

		        if(statusListener != null) statusListener.OnProgress(5, "I: read activitys...");
		        // Activity/Service/Receiver/provider intent-filter
		        apkInfo.ActivityList.addAll(getActivityInfo("activity"));
		        apkInfo.ActivityList.addAll(getActivityInfo("service"));
		        apkInfo.ActivityList.addAll(getActivityInfo("receiver"));
		        apkInfo.ActivityList.addAll(getActivityInfo("provider"));

		        if(statusListener != null) statusListener.OnProgress(5, "I: read Imanges list...");
		        Collections.addAll(apkInfo.ImageList, ZipFileUtil.findFiles(apkInfo.ApkPath, ".png", ".*drawable.*"));
		        if(statusListener != null) statusListener.OnProgress(5, "I: read Imanges list...");
		        Collections.addAll(apkInfo.LibList, ZipFileUtil.findFiles(apkInfo.ApkPath, ".so", null));

		        if(statusListener != null) statusListener.OnProgress(5, "I: read signatures...");
				solveCert();
		        
		        apkInfo.verify();
		        
		        if(statusListener != null) statusListener.OnSuccess();
			}
		}).start();
	}
	
	private String hex2IntString(String hexString)
	{
		if(hexString == null || hexString.isEmpty() || !hexString.matches("^(0x)?[0-9a-f]*"))
			return "0";
		return String.valueOf(Integer.parseInt(hexString.replaceAll("^0x([0-9a-f]*)", "$1"), 16));
	}

	private String[] getResourceValues(String id, boolean withConfig)
	{
		ArrayList<String> values = new ArrayList<String>();
		
		if(!id.startsWith("@"))
			return new String[] { id };
		String filter = "  resource " + id.substring(1);
		String config = null;

		if(resourcesWithValue == null)
			return null;

		for(int i = 0; i < resourcesWithValue.length; i++) {
			if(withConfig && resourcesWithValue[i].indexOf(" config ") >= 0) {
				config = resourcesWithValue[i].replaceAll(".*config (.*)", "$1 ");
			}
			if(resourcesWithValue[i].indexOf(filter) < 0)
				continue;
			
			Log.i(resourcesWithValue[i]);

			if(i+1 < resourcesWithValue.length) {
				String val = resourcesWithValue[i+1].replaceAll("^\\s*\\([^\\(\\)]*\\) (.*)", "$1").replaceAll("^['\"](.*)['\"]\\s*$", "$1");
				if(withConfig)
					val = config + val;
				values.add(val);
				Log.d("getResourceValues() id " + id + ", val " + val);
			}
		}

		return values.toArray(new String[0]);
	}
	
	private ArrayList<Object[]> getActivityInfo(String tag)
	{
		ArrayList<Object[]> activityList = new ArrayList<Object[]>();
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/"+tag);
        for( int idx=0; idx < activityTag.length; idx++ ){
        	String name = activityTag[idx].getAttribute("android:name");
        	String startup = "X";
        	String intents = "";

        	AaptXmlTreeNode[] intentFilter = manifestPath.getNodeList("/manifest/application/"+tag+"[@name='" + name + "']/intent-filter/action");
        	for( int i=0; i < intentFilter.length; i++ ){
        		String act = intentFilter[i].getAttribute("android:name");
        		if(i==0) intents += "<intent-filter> [" + intentFilter.length + "]";
        		intents += "\n" + act;
        		if(act.equals("android.intent.action.BOOT_COMPLETED"))
        			startup = "O";
        	}
        	
        	if(manifestPath.getNode("/manifest/application/"+tag+"[@name='" + name + "']/intent-filter/category[@name='android.intent.category.LAUNCHER']") != null) {
        		name += " - LAUNCHER";
        	}
        	
        	activityList.add(new Object[] { name, tag, startup, intents });
        }

        return activityList;
	}
	
	private Object[] getWidgetInfo(String[] widgetResPath)
	{
		String Size = "";
		String IconPath = "";
		String ReSizeMode = "";
		
		if(widgetResPath == null || widgetResPath.length <= 0
				|| apkInfo.ApkPath == null || !(new File(apkInfo.ApkPath)).exists()) {
			return new Object[] { IconPath, Size };
		}
		
		String[] wdgXml = AaptWrapper.Dump.getXmltree(apkInfo.ApkPath, widgetResPath);
		AaptXmlTreePath widgetTree = new AaptXmlTreePath(wdgXml);
		
		String width = "0";
		String Height = "0";

		AaptXmlTreeNode widgetNode = widgetTree.getNode("/appwidget-provider/@android:minWidth");
		if(widgetNode != null) {
			width = widgetNode.getAttribute("android:minWidth");
			String tmp[] = getResourceValues(width, false);
			if(tmp.length > 0) {
				width = tmp[tmp.length-1].replaceAll("^([0-9]*).*", "$1");
			}
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@android:minHeight");
		if(widgetNode != null) {
			Height = widgetNode.getAttribute("android:minHeight");
			String tmp[] = getResourceValues(Height, false);
			if(tmp.length > 0) {
				Height = tmp[tmp.length-1].replaceAll("^([0-9]*).*", "$1");
			}
		}
		
		Size = (int)Math.ceil((Float.parseFloat(width) - 40) / 76 + 1) + " X " + (int)Math.ceil((Float.parseFloat(Height) - 40) / 76 + 1);
		Size += "\n(" + width + " X " + Height + ")";

		widgetNode = widgetTree.getNode("/appwidget-provider/@android:resizeMode");
		if(widgetNode != null) {
			ReSizeMode = widgetNode.getAttribute("android:resizeMode");
			Size += "\n\n[ReSizeMode]\n" + ReSizeMode.replaceAll("\\|", "\n");
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@android:previewImage");
		if(widgetNode != null) {
			String iconPaths[] = getResourceValues(widgetNode.getAttribute("android:previewImage"), false);
			if(iconPaths.length > 0) {
				IconPath = iconPaths[iconPaths.length-1];
			}
		}
		
		Log.d("widget IconPath " + IconPath);
		Log.d("widget size " + Size);

		return new Object[] { IconPath, Size };
	}
	
	private boolean solveCert()
	{
		String certPath = apkInfo.WorkTempPath + File.separator + "META-INF";
		
		Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
		String keytoolPackage;
		if(javaVersion >= 1.8) {
			keytoolPackage = "sun.security.tools.keytool.Main";
		} else {
			keytoolPackage = "sun.security.tools.KeyTool";
		}

		apkInfo.CertList.clear();
		
		if(!(new File(apkInfo.ApkPath)).exists()) {
			return false;
		}
		
		if(!ZipFileUtil.unZip(apkInfo.ApkPath, "META-INF", certPath)) {
			Log.e("META-INFO 폴더가 존재 하지 않습니다 :");
			return false;
		}
		
		for (String s : (new File(certPath)).list()) {
			if(!s.endsWith(".RSA") && !s.endsWith(".DSA") ) continue;

			File rsaFile = new File(certPath + File.separator + s);
			if(!rsaFile.exists()) continue;

			String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
			String[] result = ConsolCmd.exc(cmd, false, null);

		    String certContent = "";
		    apkInfo.CertSummary = "<certificate[1]>\n";
		    for(int i=0; i < result.length; i++){
	    		if(!certContent.isEmpty() && result[i].matches("^.*\\[[0-9]*\\]:$")){
	    			apkInfo.CertList.add(certContent);
	    			apkInfo.CertSummary += "<certificate[" + (apkInfo.CertList.size() + 1) + "]>\n";
			    	certContent = "";
	    		}
	    		if(result[i].matches("^.*:( [^ ,]+=(\".*\")?[^,]*,?)+$")) {
	    			apkInfo.CertSummary += result[i] + "\n";
	    		}
	    		certContent += (certContent.isEmpty() ? "" : "\n") + result[i];
		    }
		    apkInfo.CertList.add(certContent);
		}
		return true;
	}

	private void deleteTempPath()
	{
		Log.i("delete Folder : "  + apkInfo.WorkTempPath);
		if(apkInfo.WorkTempPath != null && !apkInfo.WorkTempPath.isEmpty()) {
			FileUtil.deleteDirectory(new File(apkInfo.WorkTempPath));
		}
		if(isPackageTempApk && apkInfo.ApkPath != null && !apkInfo.ApkPath.isEmpty()) {
			FileUtil.deleteDirectory(new File(apkInfo.ApkPath).getParentFile());
		}
		apkInfo = null;
	}

	@Override
	public void clear(boolean sync)
	{
		if(sync) {
			deleteTempPath();
		} else {
			new Thread(new Runnable() {
				public void run()
				{
					deleteTempPath();
				}
			}).start();
		}
	}
}
