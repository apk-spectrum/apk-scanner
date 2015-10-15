package com.apkscanner.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.apkscanner.data.AaptXmlTreeNode;
import com.apkscanner.data.AaptXmlTreePath;
import com.apkscanner.data.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;
import com.apkscanner.util.FileUtil.FSStyle;

public class AaptToolManager extends ApkScannerStub
{
	private AaptXmlTreePath manifestPath = null;
	private String namespace = null;
	private String[] androidManifest = null;
	private String[] resourcesWithValue = null;
	
	public AaptToolManager(StatusListener statusListener)
	{
		super(statusListener);
		stateChanged(Status.UNINITIALIZE);
	}
	
	@Override
	public void openApk(final String apkFilePath, final String frameworkRes)
	{
		apkInfo = new ApkInfo();
		
		File apkFile = new File(apkFilePath);
		if(!apkFile.exists()) {
			Log.e("No Such APK file");
			if(statusListener != null) statusListener.OnError();
			return;
		}

		if(statusListener != null) statusListener.OnStart();
		
		apkInfo.ApkPath = apkFilePath;
		apkInfo.ApkSize = FileUtil.getFileSize(apkFile, FSStyle.FULL);
		apkInfo.WorkTempPath = FileUtil.makeTempPath(apkInfo.ApkPath.substring(apkInfo.ApkPath.lastIndexOf(File.separator)));
		Log.i("Temp path : " + apkInfo.WorkTempPath);
		
		new Thread(new Runnable() {
			public void run()
			{
				stateChanged(Status.INITIALIZING);

				progress(5, "I: start open apk");
				progress(5, "I: getDump AndroidManifest...");
				androidManifest = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] { "AndroidManifest.xml" });

				progress(30, "I: read aapt dump resources...");
				resourcesWithValue = AaptWrapper.Dump.getResources(apkFilePath, true);

				progress(30, "I: createAaptXmlTree...");
				manifestPath = new AaptXmlTreePath();
				manifestPath.createAaptXmlTree(androidManifest);
				namespace = manifestPath.getNamespace() + ":"; 
				
				stateChanged(Status.INITIALIZEED);

				progress(5, "I: read basic info...");
				AaptXmlTreeNode manifestTag = manifestPath.getNode("/manifest"); 

				// package
				if(manifestTag != null) {
					apkInfo.PackageName = getAttrValue(manifestTag , "package");
					apkInfo.VersionCode = hex2IntString(getAttrValue(manifestTag, "versionCode"));
					apkInfo.VersionName = getAttrValue(manifestTag, "versionName");
					apkInfo.SharedUserId = getAttrValue(manifestTag, "sharedUserId");
				}
				
				AaptXmlTreeNode usesSdkTag = manifestPath.getNode("/manifest/uses-sdk");
				if(usesSdkTag != null) {
					apkInfo.TargerSDKversion = hex2IntString(getAttrValue(usesSdkTag, "targetSdkVersion"));
					apkInfo.MinSDKversion = hex2IntString(getAttrValue(usesSdkTag, "minSdkVersion"));
				}

				// label & icon
				AaptXmlTreeNode applicationTag = manifestPath.getNode("/manifest/application");
				if(applicationTag != null) {
					apkInfo.Labelname = getAttrValues(applicationTag, "label", true);
					String[] iconPaths = getAttrValues(applicationTag, "icon");
					if(iconPaths != null && iconPaths.length > 0) {
						apkInfo.IconPath = iconPaths[iconPaths.length-1];
					}
					if(apkInfo.IconPath != null && apkInfo.IconPath.startsWith("@")) {
						if(frameworkRes != null) {
							String[] backup = resourcesWithValue;
							for(String res: frameworkRes.split(";")) {
								progress(10, "I: include resources... " + res);
								resourcesWithValue = AaptWrapper.Dump.getResources(res, true);
								
								iconPaths = getResourceValues(apkInfo.IconPath, false);
								if(iconPaths != null && iconPaths.length > 0) {
									apkInfo.IconPath = "jar:file:" + res.replaceAll("#", "%23") + "!/" + iconPaths[iconPaths.length-1];
									break;
								}
							}
							resourcesWithValue = backup;
						}
					} else if(apkInfo.IconPath != null) {
						apkInfo.IconPath = "jar:file:" + apkInfo.ApkPath.replaceAll("#", "%23") + "!/" + apkInfo.IconPath;
					} else {
						apkInfo.IconPath = Resource.IMG_DEF_APP_ICON.getPath();
					}
					
					if(apkInfo.IconPath.endsWith("qmg")) {
						apkInfo.IconPath = Resource.IMG_QMG_IMAGE_ICON.getPath();
					}
	
					String debuggable = getAttrValue(applicationTag, "debuggable");
					apkInfo.debuggable = debuggable != null && debuggable.toLowerCase().equals("true");
				} else {
					Log.w("Warring: node was not existed : /manifest/application");
				}

		        // hidden
		        if(manifestPath.getNode("/manifest/application/activity/intent-filter/category[@"+namespace+"name='android.intent.category.LAUNCHER']") == null) {
		        	apkInfo.isHidden = true;
		        }

		        // startup
		        if(manifestPath.getNode("/manifest/uses-permission[@"+namespace+"name='android.permission.RECEIVE_BOOT_COMPLETED']") != null) {
		        	apkInfo.Startup = "START_UP";
		        } else {
		        	apkInfo.Startup = "";
		        }
		        Log.i("Startup : " + apkInfo.Startup + apkInfo.isHidden);
		        
		        progress(5, "I: read permissions...");
		        // permission
		        apkInfo.ProtectionLevel = "";
		        //progress(5,"parsing permission...\n");
		        AaptXmlTreeNode[] permTag = manifestPath.getNodeList("/manifest/uses-permission");
		        for( int idx=0; idx < permTag.length; idx++ ){
		        	if(idx==0) apkInfo.Permissions = "<uses-permission> [" + permTag.length + "]";
		        	String perm = getAttrValue(permTag[idx], "name");
		        	apkInfo.Permissions += "\n" + perm;
		        	apkInfo.PermissionList.add(perm);
		        	String sig = getAttrValue(permTag[idx], "protectionLevel");
		        	if(sig != null && sig.equals("0x2")) {
		        		apkInfo.Permissions += " - <SIGNATURE>";
		        		apkInfo.ProtectionLevel = "SIGNATURE";
		        	}
		        }
		        permTag = manifestPath.getNodeList("/manifest/permission");
		        for( int idx=0; idx < permTag.length; idx++ ){
		        	if(idx==0) apkInfo.Permissions += "\n\n<permission> [" + permTag.length + "]";
		        	String perm = getAttrValue(permTag[idx], "name");
		        	apkInfo.Permissions += "\n" + perm;
		        	apkInfo.PermissionList.add(perm);
		        	String sig = getAttrValue(permTag[idx], "protectionLevel");
		        	if(sig != null && sig.equals("0x2")) {
		        		apkInfo.Permissions += " - <SIGNATURE>";
		        		apkInfo.ProtectionLevel = "SIGNATURE";
		        	}
		        }
		        PermissionGroupManager permGroupManager = new PermissionGroupManager(apkInfo.PermissionList.toArray(new String[0]));
		        apkInfo.PermGroupMap = permGroupManager.getPermGroupMap();
		        
				stateChanged(Status.BASIC_INFO_COMPLETED);
		        
		        // widget
		        progress(5, "I: read widgets...");
		        AaptXmlTreeNode[] widgetTag = manifestPath.getNodeList("/manifest/application/receiver/meta-data[@"+namespace+"name='android.appwidget.provider']/..");
		        //Log.i("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
		        for( int idx=0; idx < widgetTag.length; idx++ ){
		        	Object[] widgetExtraInfo = {apkInfo.IconPath, ""};

		        	String widgetTitle = null;
		        	String widgetActivity = null;
		        	String tmp[] = getAttrValues(widgetTag[idx], "label");
		        	if(tmp !=null && tmp.length > 0) {
		        		widgetTitle = tmp[0];
		        	}
		        	tmp = getAttrValues(widgetTag[idx], "name");
		        	if(tmp !=null && tmp.length > 0) {
		        		widgetActivity = tmp[0];
		        	}

		        	Object[] extraInfo = getWidgetInfo(getResourceValues(widgetTag[idx].getNode("meta-data").getAttribute(namespace + "resource"), false));
		        	if(extraInfo != null) {
		        		widgetExtraInfo = extraInfo;
		        	}
		        	
		        	apkInfo.WidgetList.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
		        }
		        
		        widgetTag = manifestPath.getNodeList("/manifest/application/activity-alias/intent-filter/action[@"+namespace+"name='android.intent.action.CREATE_SHORTCUT']/../..");
		        //Log.i("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
		        for( int idx=0; idx < widgetTag.length; idx++ ){
		        	String widgetTitle = null;
		        	String widgetActivity = null;
		        	String tmp[] = apkInfo.Labelname;
		        	if(tmp != null && tmp.length > 0) {
		        		widgetTitle = tmp[0];
		        	}
		        	tmp = getAttrValues(widgetTag[idx], "name");
		        	if(tmp != null && tmp.length > 0) {
		        		widgetActivity = tmp[0];
		        	}

		        	apkInfo.WidgetList.add(new Object[] {apkInfo.IconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
		        }
		        stateChanged(Status.WIDGET_COMPLETED);

		        progress(5, "I: read activitys...");
		        // Activity/Service/Receiver/provider intent-filter
		        apkInfo.ActivityList.addAll(getActivityInfo("activity"));
		        apkInfo.ActivityList.addAll(getActivityInfo("service"));
		        apkInfo.ActivityList.addAll(getActivityInfo("receiver"));
		        apkInfo.ActivityList.addAll(getActivityInfo("provider"));
		        stateChanged(Status.ACTIVITY_COMPLETED);
		        
		        apkInfo.verify();

		        progress(5, "I: completed...");
		        stateChanged(Status.CERT_COMPLETED);
		        
		        if(statusListener != null) statusListener.OnSuccess();
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run()
			{
		        progress(5, "I: read Imanges list...");
		        Collections.addAll(apkInfo.ImageList, ZipFileUtil.findFiles(apkInfo.ApkPath, ".png;.qmg;.jpg;.gif", ".*drawable.*"));
		        stateChanged(Status.IMAGE_COMPLETED);
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run()
			{
		        progress(5, "I: read signatures...");
				solveCert();
				stateChanged(Status.CERT_COMPLETED);
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run()
			{
		        progress(5, "I: read lib list...");
		        Collections.addAll(apkInfo.LibList, ZipFileUtil.findFiles(apkInfo.ApkPath, ".so", null));
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
			xml.append(getResourceName(node.getAttribute(name)));
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
	
	private String hex2IntString(String hexString)
	{
		if(hexString == null || hexString.isEmpty() || !hexString.matches("^(0x)?[0-9a-f]*"))
			return "";
		return String.valueOf(Integer.parseInt(hexString.replaceAll("^0x([0-9a-f]*)", "$1"), 16));
	}

	private String[] getResourceValues(String id, boolean withConfig)
	{
		ArrayList<String> values = new ArrayList<String>();
		
		if(id == null || !id.startsWith("@"))
			return new String[] { id };
		String filter = "^\\s*resource 0x0*" + id.replaceAll("@0x0*(.*)", "$1") + ".*";
		String config = null;

		if(resourcesWithValue == null)
			return null;

		for(int i = 0; i < resourcesWithValue.length; i++) {
			if(withConfig && resourcesWithValue[i].indexOf(" config (default)") >= 0) {
				config = "";
			} else if(withConfig && resourcesWithValue[i].indexOf(" config ") >= 0) {
				config = resourcesWithValue[i].replaceAll(".*config \\(?(\\w*)\\)?.*", "$1 : ");
			}
			if(!resourcesWithValue[i].matches(filter))
				continue;
			//Log.i(resourcesWithValue[i]);

			if(i+1 < resourcesWithValue.length) {
				String val = resourcesWithValue[i+1].replaceAll("^\\s*\\([^\\(\\)]*\\) (.*)", "$1").replaceAll("^['\"](.*)['\"]\\s*$", "$1");
				if(withConfig)
					val = config + val;
				values.add(val);
				//Log.d("getResourceValues() id " + id + ", val " + val);
			}
		}

		return values.toArray(new String[0]);
	}
	
	private String getAttrValue(AaptXmlTreeNode node, String attr)
	{
		return getAttrValue(node, namespace, attr);
	}
	
	private String getAttrValue(AaptXmlTreeNode node, String namespace, String attr)
	{
		String value[] = getAttrValues(node, namespace, attr);
		if(value == null || value.length == 0)
			return null;
		return value[0];
	}
	
	private String[] getAttrValues(AaptXmlTreeNode node, String attr)
	{
		return getAttrValues(node, namespace, attr);
	}
	
	private String[] getAttrValues(AaptXmlTreeNode node, String attr, boolean withConfig)
	{
		return getAttrValues(node, namespace, attr, withConfig);
	}
	
	private String[] getAttrValues(AaptXmlTreeNode node, String namespace, String attr)
	{
		return getAttrValues(node, namespace, attr, false);
	}
	
	private String[] getAttrValues(AaptXmlTreeNode node, String namespace, String attr, boolean withConfig)
	{
		//Log.d("getAttrValues() " + node + ", namespace : " + namespace + ", attr : " + attr);
		String value = node.getAttribute(namespace + attr);
		String[] resVal = null;
		if(value == null) {
			value = node.getAttribute(attr);
		}
		while(value != null && value.startsWith("@")) {
			resVal = getResourceValues(value, withConfig);
			if(resVal == null || resVal.length == 0)
				break;
			value = resVal[0];
		}
		if(resVal == null || resVal.length == 0) {
			resVal = new String[] { value };
		}
		return resVal;
	}
	
	private ArrayList<Object[]> getActivityInfo(String tag)
	{
		ArrayList<Object[]> activityList = new ArrayList<Object[]>();
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/"+tag);
        for( int idx=0; idx < activityTag.length; idx++ ){
        	String name = getAttrValue(activityTag[idx], "name");
        	String startup = "X";
        	String intents = "";

        	AaptXmlTreeNode[] intentFilter = manifestPath.getNodeList("/manifest/application/"+tag+"[@"+namespace+"name='" + name + "']/intent-filter/action");
        	for( int i=0; i < intentFilter.length; i++ ){
        		String act = getAttrValue(intentFilter[i], "name");
        		if(i==0) intents += "<intent-filter> [" + intentFilter.length + "]";
        		intents += "\n" + act;
        		if(act.equals("android.intent.action.BOOT_COMPLETED"))
        			startup = "O";
        	}
        	
        	if(manifestPath.getNode("/manifest/application/"+tag+"[@"+namespace+"name='" + name + "']/intent-filter/category[@"+namespace+"name='android.intent.category.LAUNCHER']") != null) {
        		name += " - LAUNCHER";
        		activityList.add(0, new Object[] { name, tag, startup, intents });
        	} else {
        		activityList.add(new Object[] { name, tag, startup, intents });
        	}
        }

        return activityList;
	}
	
	private Object[] getWidgetInfo(String[] widgetResPath)
	{
		String Size = "";
		String IconPath = "";
		
		if(widgetResPath == null || widgetResPath.length <= 0
				|| apkInfo.ApkPath == null || !(new File(apkInfo.ApkPath)).exists()) {
			return new Object[] { IconPath, Size };
		}
		
		String[] wdgXml = AaptWrapper.Dump.getXmltree(apkInfo.ApkPath, widgetResPath);
		AaptXmlTreePath widgetTree = new AaptXmlTreePath(wdgXml);
		String widgetNamespace = widgetTree.getNamespace() + ":";
		Log.i("widgetNamespace : " + widgetNamespace);
		
		String width = "0";
		String height = "0";

		AaptXmlTreeNode widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"minWidth");
		if(widgetNode != null) {
			width = getAttrValue(widgetNode, widgetNamespace, "minWidth");
			if(width.startsWith("0x")) {
				Log.w("Unknown widget width " + width);
				width = "0";
			} else {
				width = width.replaceAll("^([0-9]*).*", "$1");
			}
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"minHeight");
		if(widgetNode != null) {
			height = getAttrValue(widgetNode, widgetNamespace, "minHeight");
			if(height.startsWith("0x")) {
				Log.w("Unknown widget height " + height);
				height = "0";
			} else {
				height = height.replaceAll("^([0-9]*).*", "$1");
			}
		}
		
		if(!"0".equals(width) && !"0".equals(height)) {
			Size = (int)Math.ceil((Float.parseFloat(width) - 40) / 76 + 1) + " X " + (int)Math.ceil((Float.parseFloat(height) - 40) / 76 + 1);
			Size += "\n(" + width + " X " + height + ")";
		} else {
			Size = "Unknown";
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"resizeMode");
		if(widgetNode != null) {
			String ReSizeMode = getAttrValue(widgetNode, widgetNamespace, "resizeMode");
			if("0x0".equals(ReSizeMode)) {
				ReSizeMode = null;
			} else if("0x1".equals(ReSizeMode)) {
				ReSizeMode = "horizontal";
			} else if("0x2".equals(ReSizeMode)) {
				ReSizeMode = "vertical";
			} else if("0x3".equals(ReSizeMode)) {
				ReSizeMode = "horizontal|vertical";
			}
			if(ReSizeMode != null) {
				Size += "\n\n[ReSizeMode]\n" + ReSizeMode.replaceAll("\\|", "\n");
			}
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"previewImage");
		if(widgetNode != null) {
			String iconPaths[] = getAttrValues(widgetNode, widgetNamespace, "previewImage");
			if(iconPaths != null && iconPaths.length > 0) {
				IconPath = "jar:file:" + apkInfo.ApkPath.replaceAll("#", "%23") + "!/" + iconPaths[iconPaths.length-1];
			}
			if(IconPath.endsWith("qmg")) {
				IconPath = Resource.IMG_QMG_IMAGE_ICON.getPath();
			}
		}
		
		//Log.d("widget IconPath " + IconPath);
		//Log.d("widget size " + Size);

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
		
		if(!ZipFileUtil.unZip(apkInfo.ApkPath, "META-INF/", certPath)) {
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
		if(apkInfo == null)
			return;
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

	private void progress(int step, String msg)
	{
		if(statusListener != null) {
			statusListener.OnProgress(step, msg);
		}
	}
	
	private void stateChanged(Status status)
	{
		if(statusListener != null) {
			if(apkInfo != null) apkInfo.verify();
			statusListener.OnStateChanged(status);
		}
	}
}
