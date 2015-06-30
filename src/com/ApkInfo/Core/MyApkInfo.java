package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class MyApkInfo {
	public String strLabelname = "Unknown";
	public String strPackageName = "Unknown";
	public String strVersionName = "Unknown";
	public String strVersionCode = "Unknown";
	public String strMinSDKversion = "Unknown";
	public String strTargerSDKversion = "Unknown";
	public String strSigning = "Unknown";
	public String strHidden = "Unknown";
	public String strIconPath = "Unknown";
	public String strPermissions = "Unknown";
	public String strStartup = "Unknown";
	public String strProtectionLevel = "Unknown";
	public String strSharedUserId = "Unknown";
	public String strApkSize = "Unknown";
	
	public ArrayList<Object[]> arrWidgets = new ArrayList<Object[]>();
	public ArrayList<String> ImagePathList = new ArrayList<String>();
	public ArrayList<String> LibPathList = new ArrayList<String>();

	public ArrayList<Object[]> ActivityList = new ArrayList<Object[]>();
	public ArrayList<Object[]> CertList = new ArrayList<Object[]>();
	
	public ArrayList<String> ListPermission = new ArrayList<String>();;
	
	public String strWorkAPKPath = null;
	public String strAPKPath = null; 
	
	private ProgressingMoniter progress = null;
	
	public interface ProgressingMoniter
	{
		public void progress(int step, String msg);
	}
	
	public MyApkInfo()
	{
	}

	public MyApkInfo(String path)
	{
		this(path, null);
	}

	public MyApkInfo(String path, ProgressingMoniter progress)
	{
		strAPKPath = path;
		this.progress = progress;
		
		strWorkAPKPath = CoreApkTool.makeTempPath(path);
		System.out.println("Temp path : " + strWorkAPKPath);

		//APK 풀기 
		CoreApkTool.solveAPK(strAPKPath, strWorkAPKPath);

		XmlToMyApkinfo();

		ImagePathList = CoreApkTool.findFiles(new File(strWorkAPKPath + File.separator + "res"), ".png", ".*drawable.*");
		LibPathList = CoreApkTool.findFiles(new File(strWorkAPKPath + File.separator + "lib"), ".so", null);

		CertList = CoreCertTool.solveCert(strWorkAPKPath + File.separator + "original" + File.separator + "META-INF" + File.separator);
	}
	
	private void progress(int step, String msg)
	{
		if(progress == null) return;
		progress.progress(step, msg);
	}
	
	private void XmlToMyApkinfo()
	{
		progress(5,"Check Yml....\n");
		YmlToMyApkinfo();

		progress(5,"parsing AndroidManifest....\n");
		MyXPath xmlAndroidManifest = new MyXPath(strWorkAPKPath + File.separator + "AndroidManifest.xml");

		// package
		xmlAndroidManifest.getNode("/manifest");
		strPackageName = xmlAndroidManifest.getAttributes("package");
		strSharedUserId = xmlAndroidManifest.getAttributes("android:sharedUserId");
		
		if(strVersionCode == "Unknown") {
			strVersionCode = xmlAndroidManifest.getAttributes("android:versionCode");
		}
		
		if(strVersionName == "Unknown") {
			strVersionName = xmlAndroidManifest.getAttributes("android:versionName");
		}

		// label & icon
		xmlAndroidManifest.getNode("/manifest/application");
        strLabelname = getResourceInfo(xmlAndroidManifest.getAttributes("android:label"));
        strIconPath = getResourceInfo(xmlAndroidManifest.getAttributes("android:icon"));

        
        // hidden
        if(xmlAndroidManifest.isNode("//category[@name='android.intent.category.LAUNCHER']")) {
        	strHidden = "LAUNCHER";
        } else {
        	strHidden = "HIDDEN";
        }
        
        // startup
        if(xmlAndroidManifest.isNode("//uses-permission[@name='android.permission.RECEIVE_BOOT_COMPLETED']")) {
        	strStartup = "START_UP";
        } else {
        	strStartup = "";
        }

        // permission
        strProtectionLevel = "";
        progress(5,"parsing permission...\n");
        xmlAndroidManifest.getNodeList("//uses-permission");
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	if(idx==0) strPermissions = "<uses-permission> [" + xmlAndroidManifest.getLength() + "]";
        	strPermissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
        	String sig = xmlAndroidManifest.getAttributes(idx, "android:protectionLevel");
        	if(sig != null && sig.equals("signature")) {
        		strPermissions += " - <SIGNATURE>";
        		strProtectionLevel = "SIGNATURE";
        	}
        }
        xmlAndroidManifest.getNodeList("//permission");
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	if(idx==0) strPermissions += "\n\n<permission> [" + xmlAndroidManifest.getLength() + "]";
        	strPermissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
        	String sig = xmlAndroidManifest.getAttributes(idx, "android:protectionLevel");
        	if(sig != null && sig.equals("signature")) {
        		strPermissions += " - <SIGNATURE>";
        		strProtectionLevel = "SIGNATURE";
        	}
        }

        // widget
        progress(5,"parsing widget...\n");
        xmlAndroidManifest.getNodeList("//meta-data[@name='android.appwidget.provider']");
        //System.out.println("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	Object[] widgetExtraInfo = {strIconPath, "Unknown"};
        	
        	MyXPath parent = xmlAndroidManifest.getParentNode(idx);
        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));
   			Object[] extraInfo = getWidgetInfo(xmlAndroidManifest.getAttributes(idx, "android:resource"));
        	if(extraInfo != null) {
        		widgetExtraInfo = extraInfo;
        	}
        	
        	arrWidgets.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
        }
        
        xmlAndroidManifest.getNodeList("//action[@name='android.intent.action.CREATE_SHORTCUT']");
        //System.out.println("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
        	MyXPath parent = xmlAndroidManifest.getParentNode(idx).getParentNode();
        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));

        	arrWidgets.add(new Object[] {strIconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
        }
        
        // Activity/Service/Receiver/provider intent-filter
        getActivityInfo(xmlAndroidManifest, "activity");
        getActivityInfo(xmlAndroidManifest, "service");
        getActivityInfo(xmlAndroidManifest, "receiver");
        getActivityInfo(xmlAndroidManifest, "provider");
        
        verify();
	}
	
	private String getResourceInfo(String id) {

		if(id == null) return null;

		String result = null;
		String resXmlPath = new String(strWorkAPKPath + File.separator + "res" + File.separator);
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
			//System.out.println("@drawable");

			filter = "^drawable.*";
			fileName = new String(id.substring(10)) + ".png";
			type = "image";
		} else if(id.matches("^@string/.*")) {
			//System.out.println("@stirng");
			
			filter = "^values.*";
			query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
			fileName = "strings.xml";
		} else if(id.matches("^@dimen/.*")) {
			//System.out.println("string@dimen");

			filter = "^values.*";
			query = "//resources/dimen[@name='"+id.substring(id.indexOf("/")+1)+"']";
			fileName = "dimens.xml";
		} else {
			System.out.println("getResourceInfo() Unknown id " + id);
			return new String(id);
		}
		
		for (String s : (new File(resXmlPath)).list()) {
			if(!s.matches(filter)) continue;

			File resFile = new File(resXmlPath + s + File.separator + fileName);
			if(!resFile.exists()) continue;

	        //System.out.println(" - " + resFile.getAbsolutePath() + ", " + type);
			if(type.equals("image")) {
				if(resFile.length() > maxImgSize) {
					//System.out.println(resFile.getPath() + ", " + maxImgSize);
					result = new String(resFile.getPath());
					maxImgSize = resFile.length();
				}
			} else {
		        result = new MyXPath(resFile.getAbsolutePath()).getNode(query).getTextContent();
		        if(result != null) break;;
			}
		}
        //System.out.println(">> " + result);
		return result;
	}
	
	private void getActivityInfo(MyXPath xmlAndroidManifest, String tag) {
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
        	
        	ActivityList.add(new Object[] { name, tag, startup, intents });
        }
	}
	
	private Object[] getWidgetInfo(String resource) {

		//System.out.println("getWidgetInfo() " + resource);
		String resXmlPath = new String(strWorkAPKPath + File.separator + "res" + File.separator);

		String Size = "Unknown";
		String IconPath = "Unknown";
		String ReSizeMode = "";
		
		if(resource == null || !resource.matches("^@xml/.*")) {
			return new Object[] { IconPath, Size };
		}

		String widgetXml = new String(resource.substring(5));
		//System.out.println("widgetXml : " + widgetXml);

		for (String s : (new File(resXmlPath)).list()) {
			if(!s.matches("^xml.*")) continue;

			File xmlFile = new File(resXmlPath + s + File.separator + widgetXml + ".xml");
			if(!xmlFile.exists()) continue;
			
			//System.out.println("xmlFile " + xmlFile.getAbsolutePath());

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
		    	//System.out.println("Size " + Size + ", width " + width + ", height " + Height);
			}
			
			if(ReSizeMode.isEmpty() && xpath.getAttributes("android:resizeMode") != null) {
				ReSizeMode = xpath.getAttributes("android:resizeMode");
			}

			if(IconPath.equals("Unknown") && xpath.getAttributes("android:previewImage") != null) {
				String icon = xpath.getAttributes("android:previewImage");
				IconPath = getResourceInfo(icon);
				//System.out.println("icon " + IconPath);
			}
		}
		
		if(!ReSizeMode.isEmpty()) {
			Size += "\n\n[ReSizeMode]\n" + ReSizeMode.replaceAll("\\|", "\n");
		}
    	
		return new Object[] { IconPath, Size };
	}
	
	@SuppressWarnings("resource")
	private void YmlToMyApkinfo() {
		String ymlPath = new String(strWorkAPKPath + File.separator + "apktool.yml");
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
					strVersionCode = sLine.replaceFirst("\\s*versionCode:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					strVersionCode = getResourceInfo(strVersionCode);
				} else if(sLine.matches("^\\s*versionName:.*")) {
					strVersionName = sLine.replaceFirst("\\s*versionName:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					strVersionName = getResourceInfo(strVersionName);
				} else if(sLine.matches("^\\s*minSdkVersion:.*")) {
					strMinSDKversion = sLine.replaceFirst("\\s*minSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
				} else if(sLine.matches("^\\s*targetSdkVersion:.*")) {
					strTargerSDKversion = sLine.replaceFirst("\\s*targetSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void verify() {
		strLabelname = strLabelname != null ? strLabelname : "Unknown";
		strPackageName = strPackageName != null ? strPackageName : "Unknown";
		strVersionName = strVersionName != null ? strVersionName : "Unknown";
		strVersionCode = strVersionCode != null ? strVersionCode : "Unknown";
		strMinSDKversion = strMinSDKversion != null ? strMinSDKversion : "Unknown";
		strTargerSDKversion = strTargerSDKversion != null ? strTargerSDKversion : "Unknown";
		strSigning = strSigning != null ? strSigning : "Unknown";
		strHidden = strHidden != null ? strHidden : "Unknown";
		strIconPath = strIconPath != null ? strIconPath : "Unknown";
		strPermissions = strPermissions != null ? strPermissions : "";
		strStartup = strStartup != null ? strStartup : "Unknown";
		strProtectionLevel = strProtectionLevel != null ? strProtectionLevel : "Unknown";
		strSharedUserId = strSharedUserId != null ? strSharedUserId : "";
		
		for(int i = 0; i < arrWidgets.size(); i++){
			Object[] info = (Object[])arrWidgets.get(i);
			info[0] = info[0] != null && !((String)info[0]).matches("Unknown") ? info[0] : strIconPath;
			info[1] = info[1] != null && !((String)info[1]).matches("Unknown") ? info[1] : strLabelname;
			info[2] = info[2] != null && !((String)info[2]).matches("Unknown") ? info[2] : "1 X 1";
			info[3] = info[3] != null && !((String)info[3]).matches("Unknown") ? info[3] : strPackageName;
			info[4] = info[4] != null && !((String)info[4]).matches("Unknown") ? info[4] : "Unknown";
			
			if(((String)info[3]).matches("^\\..*")) {
				info[3] = strPackageName + (String)info[3];
        	}
		}

		for(int i = 0; i < ActivityList.size(); i++){
			Object[] info = (Object[])ActivityList.get(i);
			
			info[0] = info[0] != null && !((String)info[0]).matches("Unknown") ? info[0] : strPackageName;
			info[1] = info[1] != null && !((String)info[1]).matches("Unknown") ? info[1] : "Unknown";
			info[2] = info[2] != null && !((String)info[2]).matches("Unknown") ? info[2] : "X";
			info[3] = info[3] != null && !((String)info[3]).matches("Unknown") ? info[3] : "";

			if(((String)info[0]).matches("^\\..*")) {
				info[0] = strPackageName + (String)info[0];
        	}
		}
	}
	
	public void dump() {
        System.out.println("Package = " + strPackageName);
        System.out.println("Label = " + strLabelname);
        System.out.println("VersionName = " + strVersionName);
        System.out.println("VersionCode = " + strVersionCode);
        System.out.println("minSdkVersion = " + strMinSDKversion);
        System.out.println("targetSdkVersion = " + strTargerSDKversion);
        System.out.println("Hidden = " + strHidden);
        System.out.println("Permissions = " + strPermissions);
        System.out.println("Icon = " + strIconPath);

		for(int i = 0; i < arrWidgets.size(); i++){
			Object[] info = (Object[])arrWidgets.get(i);
        	System.out.println("widget Icon = " + info[0] + ", Title " + info[1] 
					+ ", Size " + info[2] + ", Activity " + info[3] + ", Type " + info[4]);
		}

		for(int i = 0; i < ActivityList.size(); i++){
			Object[] info = (Object[])ActivityList.get(i);
        	System.out.println("Activity name = " + info[0] + ", tag " + info[1] 
					+ ", startup " + info[2] + ", Intents " + info[3]);
		}
	}
}
