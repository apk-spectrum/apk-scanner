package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.ApkInfo.UI.MainUI;

public class CoreXmlTool {
	
	static String APkworkPath;
	static MyApkInfo apkInfo;
	
	public static MyApkInfo XmlToMyApkinfo(String workPath) {
		
		apkInfo = new MyApkInfo();
		APkworkPath = new String(workPath);
		
		MainUI.ProgressBarDlg.addProgress(5,"Check Yml....\n");
		YmlToMyApkinfo();

		MainUI.ProgressBarDlg.addProgress(5,"parsing AndroidManifest....\n");
		MyXPath xmlAndroidManifest = new MyXPath(APkworkPath + File.separator + "AndroidManifest.xml");

		// package
		xmlAndroidManifest.getNode("/manifest");
		apkInfo.strPackageName = xmlAndroidManifest.getAttributes("package");
		
		if(apkInfo.strVersionCode == "Unknown") {
			apkInfo.strVersionCode = xmlAndroidManifest.getAttributes("android:versionCode");
		}
		
		if(apkInfo.strVersionName == "Unknown") {
			apkInfo.strVersionName = xmlAndroidManifest.getAttributes("android:versionName");
		}

		// label & icon
		xmlAndroidManifest.getNode("/manifest/application");
        apkInfo.strLabelname = getResourceInfo(xmlAndroidManifest.getAttributes("android:label"));
        apkInfo.strIconPath = getResourceInfo(xmlAndroidManifest.getAttributes("android:icon"));

        
        // hidden
        if(xmlAndroidManifest.isNode("//category[@name='android.intent.category.LAUNCHER']")) {
        	apkInfo.strHidden = "LAUNCHER";
        } else {
        	apkInfo.strHidden = "HIDDEN";
        }
        
        // startup
        if(xmlAndroidManifest.isNode("//uses-permission[@name='android.permission.RECEIVE_BOOT_COMPLETED']")) {
        	apkInfo.strStartup = "O - RECEIVE_BOOT_COMPLETED";
        } else {
        	apkInfo.strStartup = "X";
        }

        // permission
        MainUI.ProgressBarDlg.addProgress(5,"parsing permission...\n");
        xmlAndroidManifest.getNodeList("//uses-permission");
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	if(idx==0) apkInfo.strPermissions = "<uses-permission> [" + xmlAndroidManifest.getLength() + "]";
        	apkInfo.strPermissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
        }
        xmlAndroidManifest.getNodeList("//permission");
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	if(idx==0) apkInfo.strPermissions += "\n\n<permission> [" + xmlAndroidManifest.getLength() + "]";
        	apkInfo.strPermissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
        }

        // widget
        MainUI.ProgressBarDlg.addProgress(5,"parsing widget...\n");
        xmlAndroidManifest.getNodeList("//meta-data[@name='android.appwidget.provider']");
        System.out.println("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	Object[] widgetExtraInfo = {apkInfo.strIconPath, "Unknown"};
        	
        	MyXPath parent = xmlAndroidManifest.getParentNode(idx);
        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));
   			Object[] extraInfo = getWidgetInfo(xmlAndroidManifest.getAttributes(idx, "android:resource"));
        	if(extraInfo != null) {
        		widgetExtraInfo = extraInfo;
        	}
        	
        	apkInfo.arrWidgets.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
        }
        
        xmlAndroidManifest.getNodeList("//action[@name='android.intent.action.CREATE_SHORTCUT']");
        System.out.println("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	MyXPath parent = xmlAndroidManifest.getParentNode(idx).getParentNode();
        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));

        	apkInfo.arrWidgets.add(new Object[] {apkInfo.strIconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
        }
        
        // Activity/Service/Receiver/provider intent-filter
        getActivityInfo(xmlAndroidManifest, "activity");
        getActivityInfo(xmlAndroidManifest, "service");
        getActivityInfo(xmlAndroidManifest, "receiver");
        getActivityInfo(xmlAndroidManifest, "provider");
        
        apkInfo.verify();
        apkInfo.dump();

		return apkInfo;
	}
	
	private static String getResourceInfo(String id) {

		if(id == null) return null;

		String result = null;
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);
		String query = "//";
		String filter = "";
		String fileName = "";
		String type = "string";
		long maxImgSize = 0;
		
		if(!id.matches("^@.*")) {
			//System.out.println("id is start without @");
			result = new String(id);
			return result;
		} else if(id.matches("^@drawable/.*")) {
			System.out.println("@drawable");

			filter = "^drawable.*";
			fileName = new String(id.substring(10)) + ".png";
			type = "image";
		} else if(id.matches("^@string/.*")) {
			System.out.println("@stirng");
			
			filter = "^values.*";
			query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
			fileName = "strings.xml";
		} else if(id.matches("^@dimen/.*")) {
			System.out.println("string@dimen");

			filter = "^values.*";
			query = "//resources/dimen[@name='"+id.substring(id.indexOf("/")+1)+"']";
			fileName = "dimens.xml";
		} else {
			System.out.println("Unknown id " + id);
			return new String(id);
		}
		
		for (String s : (new File(resXmlPath)).list()) {
			if(!s.matches(filter)) continue;

			File resFile = new File(resXmlPath + s + File.separator + fileName);
			if(!resFile.exists()) continue;

	        //System.out.println(" - " + resFile.getAbsolutePath() + ", " + type);
			if(type.equals("image")) {
				if(resFile.length() > maxImgSize) {
					System.out.println(resFile.getPath() + ", " + maxImgSize);
					result = new String(resFile.getPath());
					maxImgSize = resFile.length();
				}
			} else {
		        result = new MyXPath(resFile.getAbsolutePath()).getNode(query).getTextContent();
		        if(result != null) break;;
			}
		}
        System.out.println(">> " + result);
		return result;
	}
	
	private static void getActivityInfo(MyXPath xmlAndroidManifest, String tag) {
        xmlAndroidManifest.getNodeList("//"+tag);
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	String name = xmlAndroidManifest.getAttributes(idx, "android:name");
        	String startup = "X";
        	String intents = "";

        	MyXPath intentsNode = new MyXPath(xmlAndroidManifest);
        	intentsNode.getNodeList("//"+tag+"[@name='" + name + "']/intent-filter/action");
        	for( int i=0; i < intentsNode.getLength(); i++ ){
        		String act = intentsNode.getAttributes(i, "android:name");
        		if(i==0) intents += "<intent-filter> [" + intentsNode.getLength() + "]";
        		intents += "\n" + act;
        		if(act.equals("android.intent.action.BOOT_COMPLETED"))
        			startup = "O";
        	}
        	
        	if(intentsNode.isNode("//"+tag+"[@name='" + name + "']/intent-filter/category[@name='android.intent.category.LAUNCHER']")) {
        		name += " - LAUNCHER";
        	}
        	
        	apkInfo.ActivityList.add(new Object[] { name, tag, startup, intents });
        }
	}
	
	private static Object[] getWidgetInfo(String resource) {

		System.out.println("getWidgetInfo() " + resource);
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);

		String Size = "Unknown";
		String IconPath = "Unknown";
		
		if(resource == null || !resource.matches("^@xml/.*")) {
			return new Object[] { IconPath, Size };
		}

		String widgetXml = new String(resource.substring(5));
		System.out.println("widgetXml : " + widgetXml);

		for (String s : (new File(resXmlPath)).list()) {
			if(!s.matches("^xml.*")) continue;

			File xmlFile = new File(resXmlPath + s + File.separator + widgetXml + ".xml");
			if(!xmlFile.exists()) continue;
			
			System.out.println("xmlFile " + xmlFile.getAbsolutePath());

			MyXPath xpath = new MyXPath(xmlFile.getAbsolutePath());
			
			xpath.getNode("//appwidget-provider");
	        
			if(Size.equals("Unknown") && xpath.getAttributes("android:minWidth") != null
					&& xpath.getAttributes("android:minHeight") != null) {
				String width = xpath.getAttributes("android:minWidth");
				String Height = xpath.getAttributes("android:minHeight");
				width = getResourceInfo(width).replaceAll("^([0-9]*).*", "$1");
				Height = getResourceInfo(Height).replaceAll("^([0-9]*).*", "$1");
				//Size = ((Integer.parseInt(width) - 40) / 70 + 1) + " X " + ((Integer.parseInt(Height) - 40) / 70 + 1);
				Size = (int)Math.ceil((Float.parseFloat(width) - 40) / 76 + 1) + " X " + (int)Math.ceil((Float.parseFloat(Height) - 40) / 76 + 1);
				Size += "\n(" + width + " X " + Height + ")";
		    	System.out.println("Size " + Size + ", width " + width + ", height " + Height);
			}

			if(IconPath.equals("Unknown") && xpath.getAttributes("android:previewImage") != null) {
				String icon = xpath.getAttributes("android:previewImage");
				IconPath = getResourceInfo(icon);
				System.out.println("icon " + IconPath);
			}
		}
    	
		return new Object[] { IconPath, Size };
	}
	
	@SuppressWarnings("resource")
	private static void YmlToMyApkinfo() {
		String ymlPath = new String(APkworkPath + File.separator + "apktool.yml");
		File ymlFile = new File(ymlPath);

		if(!ymlFile.exists()) {
			return;
		}
		
		try {
		    BufferedReader inFile;
		    String sLine = null;
			inFile = new BufferedReader(new FileReader(ymlFile));
			while( (sLine = inFile.readLine()) != null ) {
				if(sLine.matches("^\\s*versionCode:.*")) {
					apkInfo.strVersionCode = sLine.replaceFirst("\\s*versionCode:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					apkInfo.strVersionCode = getResourceInfo(apkInfo.strVersionCode);
				} else if(sLine.matches("^\\s*versionName:.*")) {
					apkInfo.strVersionName = sLine.replaceFirst("\\s*versionName:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					apkInfo.strVersionName = getResourceInfo(apkInfo.strVersionName);
				} else if(sLine.matches("^\\s*minSdkVersion:.*")) {
					apkInfo.strMinSDKversion = sLine.replaceFirst("\\s*minSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
				} else if(sLine.matches("^\\s*targetSdkVersion:.*")) {
					apkInfo.strTargerSDKversion = sLine.replaceFirst("\\s*targetSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
