package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ApkInfo.UI.MainUI;
import com.ApkInfo.UI.MyProgressBarDemo;

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

		// label & icon
		xmlAndroidManifest.getNode("/manifest/application");
        apkInfo.strLabelname = getResourceInfo(xmlAndroidManifest.getAttributes("android:label"));
        apkInfo.strIconPath = getResourceInfo(xmlAndroidManifest.getAttributes("android:icon"));

        
        // hidden
        if(xmlAndroidManifest.isNode("//category[@name='android.intent.category.LAUNCHER']")) {
        	apkInfo.strHidden = "X - LAUNCHER";
        } else {
        	apkInfo.strHidden = "O - HIDDEN";
        }

        // permission
        MainUI.ProgressBarDlg.addProgress(5,"parsing permission...\n");
        xmlAndroidManifest.getNodeList("//uses-permission");
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	apkInfo.strPermissions += (idx==0 ? "":"\n");
        	apkInfo.strPermissions += xmlAndroidManifest.getAttributes(idx, "android:name");
        }
        xmlAndroidManifest.getNodeList("//permission");
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	apkInfo.strPermissions += (apkInfo.strPermissions=="" ? "":"\n");
        	apkInfo.strPermissions += xmlAndroidManifest.getAttributes(idx, "android:name");
        }

        // widget
        MainUI.ProgressBarDlg.addProgress(5,"parsing widget...\n");
        xmlAndroidManifest.getNodeList("//meta-data[@name='android.appwidget.provider']");
        System.out.println("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	String widgetTitle = apkInfo.strLabelname;
        	String widgetActivity = "Unknown";
        	Object[] widgetExtraInfo = {apkInfo.strIconPath, "Unknown"};
        	
        	Node parent = xmlAndroidManifest.getNodeList().item(idx).getParentNode();
        	if(parent != null) {
       			widgetTitle = getResourceInfo(new MyXPath(parent).getAttributes("android:label"));
    			widgetActivity = getResourceInfo(new MyXPath(parent).getAttributes("android:name"));

	        	if(widgetActivity.matches("^\\..*")) {
	        		widgetActivity = apkInfo.strPackageName + widgetActivity;
	        	}
        	}

    		Object[] extraInfo = getWidgetInfo(xmlAndroidManifest.getAttributes(idx, "android:resource"));
        	if(extraInfo != null) {
        		widgetExtraInfo = extraInfo;
	        	if (widgetExtraInfo[0] == null || widgetExtraInfo[0].equals("Unknown")) {
	        		widgetExtraInfo[0] = apkInfo.strIconPath;
	        	}
        	}
        	
        	System.out.println("widget Icon = " + widgetExtraInfo[0]);
        	System.out.println("widget Title = " + widgetTitle);
        	System.out.println("widget Size = " + widgetExtraInfo[1]);
        	System.out.println("widget Activity = " + widgetActivity);
        	System.out.println("widget Type = Normal");

        	apkInfo.arrWidgets.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
        }
        
        xmlAndroidManifest.getNodeList("//action[@name='android.intent.action.CREATE_SHORTCUT']");
        System.out.println("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	String widgetTitle = apkInfo.strLabelname;
        	String widgetActivity = apkInfo.strPackageName;

        	Node parent = xmlAndroidManifest.getNodeList().item(idx).getParentNode();
        	if(parent != null) parent = parent.getParentNode();
        	
   			widgetTitle = getResourceInfo(new MyXPath(parent).getAttributes("android:label"));
   			widgetActivity = getResourceInfo(new MyXPath(parent).getAttributes("android:name"));
        	if(widgetActivity.matches("^\\..*")) {
        		widgetActivity = apkInfo.strPackageName + widgetActivity;
        	}

        	System.out.println("widget Icon = " + apkInfo.strIconPath);
        	System.out.println("widget Title = " + widgetTitle);
        	System.out.println("widget Size = 1 X 1");
        	System.out.println("widget Activity = " + widgetActivity);
        	System.out.println("widget Type = Shortcut");
        	
        	apkInfo.arrWidgets.add(new Object[] {apkInfo.strIconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
        }

        System.out.println("Package = " + apkInfo.strPackageName);
        System.out.println("Label = " + apkInfo.strLabelname);
        System.out.println("VersionName = " + apkInfo.strVersionName);
        System.out.println("VersionCode = " + apkInfo.strVersionCode);
        System.out.println("minSdkVersion = " + apkInfo.strMinSDKversion);
        System.out.println("targetSdkVersion = " + apkInfo.strTargerSDKversion);
        System.out.println("Hidden = " + apkInfo.strHidden);
        System.out.println("Hidden = " + apkInfo.strPermissions);
        System.out.println("Icon = " + apkInfo.strIconPath);

		return apkInfo;
	}
	
	private static String getResourceInfo(String id) {
		
		String result = null;
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);
		String query = "//";
		
		if(id == null) return null;
		if(!id.matches("^@.*")) {
			//System.out.println("id is start without @");
			result = new String(id);
			return result;
		} else if(id.matches("^@drawable/.*")) {
			System.out.println("@drawable");
			String imgName = new String(id.substring(10));
			long maxImgSize = 0;
			System.out.println(id + "->" +imgName);

			for (String s : (new File(resXmlPath)).list()) {
				//System.out.println("dir " + s);
				if(!s.matches("^drawable.*")) continue;

				File imgFile = new File(resXmlPath + s + File.separator + imgName + ".png");
				if(!imgFile.exists()) continue;

				if(imgFile.length() > maxImgSize) {
					System.out.println(imgFile.getPath() + ", " + maxImgSize);
					result = new String(imgFile.getPath());
					maxImgSize = imgFile.length();
				}
			}
			return result;
		} else if(id.matches("^@string/.*")) {
			System.out.println("@stirng");

			query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
			for (String s : (new File(resXmlPath)).list()) {
				if(!s.matches("^values.*")) continue;

				File dimensFile = new File(resXmlPath + s + File.separator + "strings.xml");
				if(!dimensFile.exists()) continue;
				
		        result = new MyXPath(dimensFile.getAbsolutePath()).getNode(query).getTextContent();
		        if(result != null) break;; 
			}
	        System.out.println(">> " + result);
			return result;
		} else if(id.matches("^@dimen/.*")) {
			System.out.println("string@dimen");

			query = "//resources/dimen[@name='"+id.substring(id.indexOf("/")+1)+"']";
			for (String s : (new File(resXmlPath)).list()) {
				if(!s.matches("^values.*")) continue;

				File dimensFile = new File(resXmlPath + s + File.separator + "dimens.xml");
				if(!dimensFile.exists()) continue;
				
		        result = new MyXPath(dimensFile.getAbsolutePath()).getNode(query).getTextContent();
		        if(result != null) break;; 
			}
	        System.out.println(">> " + result);
			return result;
		} else {
			System.out.println("Unknown id " + id);
			return new String(id);
		}

	}
	
	private static Object[] getWidgetInfo(String resource) {

		System.out.println("getWidgetInfo() " + resource);
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);

		String Size = "Unknown";
		String IconPath = "Unknown";
		
		if(!resource.matches("^@xml/.*")) {
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
				Size = (int)Math.ceil(Float.parseFloat(width) / 75) + " X " + (int)Math.ceil(Float.parseFloat(Height) / 75);
		    	System.out.println("Size " + Size);
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
