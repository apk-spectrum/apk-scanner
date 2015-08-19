package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ApkInfo.Core.CoreApkTool.FSStyle;
import com.ApkInfo.Core.PermissionGroupManager.PermissionGroup;
import com.ApkInfo.Resource.Resource;

public class ApkManager
{
	private ApkInfo mApkInfo = null;
	static private final String ApktoolVer = getApkToolVersion();

	private boolean isPackageTempAPK = false;
 	
	//private Status mState = Status.UNINITIALIZE;
	
	private ArrayList<String> mFrameworkResList;
	
	private ProcessThead mProcess = null;
	
	public class ApkInfo
	{
		public String[] Labelname = null;
		public String PackageName = null;
		public String VersionName = null;
		public String VersionCode = null;
		public String MinSDKversion = null;
		public String TargerSDKversion = null;
		public String Signing = null;
		public boolean isHidden = false;
		public String IconPath = null;
		public String Permissions = null;
		public String Startup = null;
		public String ProtectionLevel = null;
		public String SharedUserId = null;
		public String ApkSize = null;
		public String CertSummary = null;
		
		public ArrayList<Object[]> WidgetList = new ArrayList<Object[]>();
		public ArrayList<String> ImageList = new ArrayList<String>();
		public ArrayList<String> LibList = new ArrayList<String>();

		public ArrayList<Object[]> ActivityList = new ArrayList<Object[]>();
		public ArrayList<String> CertList = new ArrayList<String>();
		
		public ArrayList<String> PermissionList = new ArrayList<String>();
		public HashMap<String, PermissionGroup> PermGroupMap;

		public String ApkPath = null;
		public String WorkTempPath = null;
		
		public void verify() {
			if(Labelname == null) Labelname = new String[] {""};
			if(PackageName == null) PackageName = "";
			if(VersionName == null) VersionName = "";
			if(VersionCode == null) VersionCode = "";
			if(MinSDKversion == null) MinSDKversion = "";
			if(TargerSDKversion == null) TargerSDKversion = "";
			if(Signing == null) Signing = "";
			if(IconPath == null) IconPath = "";
			if(Permissions == null) Permissions = "";
			if(Startup == null) Startup = "";
			if(ProtectionLevel == null) ProtectionLevel = "";
			if(SharedUserId == null) SharedUserId = "";
			if(ApkSize == null) ApkSize = "";
			
			for(int i = 0; i < WidgetList.size(); i++){
				Object[] info = (Object[])WidgetList.get(i);
				info[0] = info[0] != null && !((String)info[0]).isEmpty() ? info[0] : IconPath;
				info[1] = info[1] != null && !((String)info[1]).isEmpty() ? info[1] : Labelname;
				info[2] = info[2] != null && !((String)info[2]).isEmpty() ? info[2] : "1 X 1";
				info[3] = info[3] != null && !((String)info[3]).isEmpty() ? info[3] : PackageName;
				info[4] = info[4] != null && !((String)info[4]).isEmpty() ? info[4] : "Unknown";
				
				if(((String)info[3]).matches("^\\..*")) {
					info[3] = PackageName + (String)info[3];
	        	}
			}

			for(int i = 0; i < ActivityList.size(); i++){
				Object[] info = (Object[])ActivityList.get(i);
				
				info[0] = info[0] != null && !((String)info[0]).isEmpty() ? info[0] : PackageName;
				info[1] = info[1] != null && !((String)info[1]).isEmpty() ? info[1] : "Unknown";
				info[2] = info[2] != null && !((String)info[2]).isEmpty() ? info[2] : "X";
				info[3] = info[3] != null && !((String)info[3]).isEmpty() ? info[3] : "";

				if(((String)info[0]).matches("^\\..*")) {
					info[0] = PackageName + (String)info[0];
	        	}
			}
		}
	}
	
	public interface StatusListener
	{
		public void OnStart();
		public void OnSuccess();
		public void OnError();
		public void OnComplete();
		public void OnProgress(int step, String msg);
		public void OnStateChange();
	}

	public enum Status {
		UNINITIALIZE,
		INITIALIZING,
		INITIALIZEED,
		SOLVE_RESOURCE,
		SOLVE_CODE,
		SOLVE_BOTH,
		STANDBY,
		DELETEING
	}
	
	public enum ProcessCmd {
		SOLVE_RESOURCE,
		SOLVE_CODE,
		SOLVE_BOTH,
		DELETE_TEMP_PATH,
	}
	
	public enum SolveType {
		RESOURCE,
		CODE,
		BOTH
	}
	
	public ApkManager()
	{
		this(null, null, false);
	}

	public ApkManager(String apkPath)
	{
		this(apkPath, null, false);
	}

	public ApkManager(String apkPath, boolean isPackage)
	{
		this(apkPath, null, isPackage);
	}

	public ApkManager(String apkPath, String frameworkResPath)
	{
		this(apkPath, frameworkResPath, false);
	}

	public ApkManager(String apkPath, String frameworkResPath, boolean isPackage)
	{
		mApkInfo = new ApkInfo();
		mFrameworkResList = new ArrayList<String>();
		setApkFile(apkPath);
		addFameworkRes(frameworkResPath);
		isPackageTempAPK = isPackage;
	}

	public void setApkFile(String apkPath) {
		File apkFile = new File(apkPath);

		if(!apkFile.exists()) {
			System.out.println("No Such APK file");
			return;
		}
		apkPath = apkFile.getAbsolutePath();
		
		if(apkPath != null && (new File(apkPath)).exists()) {
			mApkInfo.ApkPath = apkPath;
		}
	}

	public void addFameworkRes(String resApkPath) {
		if(resApkPath == null) return;
		
		
		for(String file: resApkPath.split(";")) {
			if(file.isEmpty()) continue;
			File resFile = new File(file);
			if(!resFile.exists()) {
				System.out.println("No Such res file : " + file);
				continue;
			}
			mFrameworkResList.add(resFile.getAbsolutePath());
		}
	}
	
	static public String getApkToolVersion()
	{
		if(ApktoolVer == null) {
			String apkToolPath = Resource.BIN_APKTOOL_JAR.getPath();
			if(!(new File(apkToolPath)).exists()) {
				System.out.println("No such file : apktool.jar");
				return null;
			}
			String[] result = MyConsolCmd.exc(new String[] {"java", "-jar", apkToolPath, "--version"}, false);
	
			return result[0];
		}
		return ApktoolVer;
	}
	
	public void solve(SolveType type, StatusListener listener)
	{
		if(mApkInfo.ApkPath == null) return;
		//System.out.println("solve()....start ");
		synchronized(this) {
			mProcess = new ProcessThead(this, ProcessCmd.SOLVE_RESOURCE, listener);
			mProcess.start();

			try {
				this.wait();
				this.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("solve()....end ");
	}

	public void clear(boolean wait, StatusListener listener)
	{
		//System.out.println("clear()....start ");
		mProcess = new ProcessThead(this, ProcessCmd.DELETE_TEMP_PATH, listener);
		synchronized(this) {
			mProcess.start();
			
			try {
				this.wait();
				this.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			if(wait) mProcess.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println("clear()....end ");
	}
	
	public final ApkInfo getApkInfo()
	{
		return mApkInfo;
	}

	class ProcessThead extends Thread {
		Object mOwner;
		ProcessCmd mCmd;
		StatusListener mListener;
		
		ProcessThead(Object owner, ProcessCmd cmd, StatusListener listener) {
			mOwner = owner;
			mCmd = cmd;
			mListener = listener;
		}
		
		private boolean solve()
		{
			mApkInfo.WorkTempPath = CoreApkTool.makeTempPath(mApkInfo.ApkPath);
			System.out.println("Temp path : " + mApkInfo.WorkTempPath);

			mApkInfo.ApkSize = CoreApkTool.getFileSize((new File(mApkInfo.ApkPath)), FSStyle.FULL);
			
			boolean isSolve = solveAPK(mApkInfo.ApkPath, mApkInfo.WorkTempPath);
			if(isSolve) {
				XmlToMyApkinfo();
				
				mApkInfo.ImageList = CoreApkTool.findFiles(new File(mApkInfo.WorkTempPath + File.separator + "res"), ".png", ".*drawable.*");
				mApkInfo.LibList = CoreApkTool.findFiles(new File(mApkInfo.WorkTempPath + File.separator + "lib"), ".so", null);
	
				solveCert(mApkInfo.WorkTempPath + File.separator + "original" + File.separator + "META-INF" + File.separator);
			}
			return isSolve;
		}
		
		private boolean solveAPK(String APKFilePath, String solvePath)
		{
			String apkToolPath = Resource.BIN_APKTOOL_JAR.getPath();
			System.out.println("apkToolPath : " + apkToolPath);

			if(!(new File(apkToolPath)).exists()) {
				System.out.println("No such file : apktool.jar");
				return false;
			}

			for(String framework: mFrameworkResList) {
				if(!(new File(framework)).exists()) continue;
				String[] cmd = {"java", "-jar", apkToolPath, "install-framework", "-p", solvePath+"-res", framework};
				MyConsolCmd.exc(cmd, true, new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
				    	if(output.matches("^I:.*"))
				    		progress(1,output + "\n");
				    	return true;
					}
				});
			}
			
			boolean isSuccess = true;
			String[] cmd = new String[] {"java", "-jar", apkToolPath, "d", "-s", "-f", "-o", solvePath, "-p", solvePath+"-res", APKFilePath};
			String[] result = MyConsolCmd.exc(cmd, true, new MyConsolCmd.OutputObserver() {
				@Override
				public boolean ConsolOutput(String output) {
			    	if(output.matches("^I:.*"))
			    		progress(5,output + "\n");
			    	else
			    		progress(0,output + "\n");
			    	return true;
				}
			});
			
			for(String s: result) {
				if(s.matches("^Exception.*")) {
					isSuccess = false;
					break;
				}
			}
			
			return isSuccess;
		}

		private void progress(int step, String msg)
		{
			if(mListener != null) mListener.OnProgress(step, msg);
		}
		
		private void XmlToMyApkinfo()
		{
			progress(5,"Check Yml....\n");
			YmlToMyApkinfo();

			progress(5,"parsing AndroidManifest....\n");
			MyXPath xmlAndroidManifest = new MyXPath(mApkInfo.WorkTempPath + File.separator + "AndroidManifest.xml");

			// package
			xmlAndroidManifest.getNode("/manifest");
			mApkInfo.PackageName = xmlAndroidManifest.getAttributes("package");
			mApkInfo.SharedUserId = xmlAndroidManifest.getAttributes("android:sharedUserId");
			
			if(mApkInfo.VersionCode == null || mApkInfo.VersionCode.isEmpty()) {
				mApkInfo.VersionCode = xmlAndroidManifest.getAttributes("android:versionCode");
			}
			
			if(mApkInfo.VersionName == null || mApkInfo.VersionName.isEmpty()) {
				mApkInfo.VersionName = xmlAndroidManifest.getAttributes("android:versionName");
			}

			// label & icon
			xmlAndroidManifest.getNode("/manifest/application");
			mApkInfo.Labelname = getMutiLang(xmlAndroidManifest.getAttributes("android:label"));
			mApkInfo.IconPath = getResourceInfo(xmlAndroidManifest.getAttributes("android:icon"));

	        
	        // hidden
	        if(!xmlAndroidManifest.isNode("//category[@name='android.intent.category.LAUNCHER']")) {
	        	mApkInfo.isHidden = true;
	        }
	        
	        // startup
	        if(xmlAndroidManifest.isNode("//uses-permission[@name='android.permission.RECEIVE_BOOT_COMPLETED']")) {
	        	mApkInfo.Startup = "START_UP";
	        } else {
	        	mApkInfo.Startup = "";
	        }

	        // permission
	        mApkInfo.ProtectionLevel = "";
	        progress(5,"parsing permission...\n");
	        xmlAndroidManifest.getNodeList("//uses-permission");
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	if(idx==0) mApkInfo.Permissions = "<uses-permission> [" + xmlAndroidManifest.getLength() + "]";
	        	mApkInfo.Permissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
	        	mApkInfo.PermissionList.add(xmlAndroidManifest.getAttributes(idx, "android:name"));
	        	String sig = xmlAndroidManifest.getAttributes(idx, "android:protectionLevel");
	        	if(sig != null && sig.equals("signature")) {
	        		mApkInfo.Permissions += " - <SIGNATURE>";
	        		mApkInfo.ProtectionLevel = "SIGNATURE";
	        	}
	        }
	        xmlAndroidManifest.getNodeList("//permission");
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	if(idx==0) mApkInfo.Permissions += "\n\n<permission> [" + xmlAndroidManifest.getLength() + "]";
	        	mApkInfo.Permissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
	        	mApkInfo.PermissionList.add(xmlAndroidManifest.getAttributes(idx, "android:name"));
	        	String sig = xmlAndroidManifest.getAttributes(idx, "android:protectionLevel");
	        	if(sig != null && sig.equals("signature")) {
	        		mApkInfo.Permissions += " - <SIGNATURE>";
	        		mApkInfo.ProtectionLevel = "SIGNATURE";
	        	}
	        }
	        
	        PermissionGroupManager permGroupManager = new PermissionGroupManager(mApkInfo.PermissionList.toArray(new String[0]));
	        mApkInfo.PermGroupMap = permGroupManager.getPermGroupMap();

	        // widget
	        progress(5,"parsing widget...\n");
	        xmlAndroidManifest.getNodeList("//meta-data[@name='android.appwidget.provider']");
	        //System.out.println("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	Object[] widgetExtraInfo = {mApkInfo.IconPath, ""};
	        	
	        	MyXPath parent = xmlAndroidManifest.getParentNode(idx);
	        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
	        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));
	   			Object[] extraInfo = getWidgetInfo(xmlAndroidManifest.getAttributes(idx, "android:resource"));
	        	if(extraInfo != null) {
	        		widgetExtraInfo = extraInfo;
	        	}
	        	
	        	mApkInfo.WidgetList.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
	        }
	        
	        xmlAndroidManifest.getNodeList("//action[@name='android.intent.action.CREATE_SHORTCUT']");
	        //System.out.println("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	MyXPath parent = xmlAndroidManifest.getParentNode(idx).getParentNode();
	        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
	        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));

	        	mApkInfo.WidgetList.add(new Object[] {mApkInfo.IconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
	        }
	        
	        // Activity/Service/Receiver/provider intent-filter
	        getActivityInfo(xmlAndroidManifest, "activity");
	        getActivityInfo(xmlAndroidManifest, "service");
	        getActivityInfo(xmlAndroidManifest, "receiver");
	        getActivityInfo(xmlAndroidManifest, "provider");
	        
	        mApkInfo.verify();
		}
		
		private String getResourceInfo(String id)
		{
			if(id == null) return null;

			String result = null;
			String resXmlPath = new String(mApkInfo.WorkTempPath + File.separator + "res" + File.separator);
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
		
		private String[] getMutiLang(String id)
		{
			if(id == null || !id.matches("^@string/.*"))
				return null;

			ArrayList<String> result = new ArrayList<String>();

			String resXmlPath = new String(mApkInfo.WorkTempPath + File.separator + "res" + File.separator);
			String query = "//";
			String filter = "";
			String fileName = "";
			
			filter = "^values.*";
			query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
			fileName = "strings.xml";
			
			for (String s : (new File(resXmlPath)).list()) {
				if(!s.matches(filter)) continue;

				File resFile = new File(resXmlPath + s + File.separator + fileName);
				if(!resFile.exists()) continue;
				String value = new MyXPath(resFile.getAbsolutePath()).getNode(query).getTextContent();
		        if(value != null) {
		        	String lang = s.replaceAll("values-?", "");
		        	if(lang.isEmpty()) {
		        		result.add(0, value);	
		        	} else {
		        		result.add(value + " - " + s.replaceAll("values-?", ""));	
		        	}
		        }
			}

			return result.toArray(new String[0]);
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
	        	
	        	mApkInfo.ActivityList.add(new Object[] { name, tag, startup, intents });
	        }
		}
		
		private Object[] getWidgetInfo(String resource) {

			//System.out.println("getWidgetInfo() " + resource);
			String resXmlPath = new String(mApkInfo.WorkTempPath + File.separator + "res" + File.separator);

			String Size = "";
			String IconPath = "";
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
		        
				if(Size.isEmpty() && xpath.getAttributes("android:minWidth") != null
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

				if(IconPath.isEmpty() && xpath.getAttributes("android:previewImage") != null) {
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
		
		private void YmlToMyApkinfo() {
			String ymlPath = new String(mApkInfo.WorkTempPath + File.separator + "apktool.yml");
			File ymlFile = new File(ymlPath);
		    BufferedReader inFile = null;

			if(!ymlFile.exists()) {
				return;
			}
			
			try {
			    String sLine = null;
				inFile = new BufferedReader(new FileReader(ymlFile));
				while( (sLine = inFile.readLine()) != null ) {
					if(sLine.matches("^\\s*versionCode:.*")) {
						mApkInfo.VersionCode = sLine.replaceFirst("\\s*versionCode:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
						mApkInfo.VersionCode = getResourceInfo(mApkInfo.VersionCode);
					} else if(sLine.matches("^\\s*versionName:.*")) {
						mApkInfo.VersionName = sLine.replaceFirst("\\s*versionName:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
						mApkInfo.VersionName = getResourceInfo(mApkInfo.VersionName);
					} else if(sLine.matches("^\\s*minSdkVersion:.*")) {
						mApkInfo.MinSDKversion = sLine.replaceFirst("\\s*minSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					} else if(sLine.matches("^\\s*targetSdkVersion:.*")) {
						mApkInfo.TargerSDKversion = sLine.replaceFirst("\\s*targetSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(inFile != null) inFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public ArrayList<String> solveCert(String CertPath) {
			Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
			String keytoolPackage;
			if(javaVersion >= 1.8) {
				keytoolPackage = "sun.security.tools.keytool.Main";
			} else {
				keytoolPackage = "sun.security.tools.KeyTool";
			}

			mApkInfo.CertList.clear();
			if(!(new File(CertPath)).exists()) {
				System.out.println("META-INFO 폴더가 존재 하지 않습니다 :");
				return mApkInfo.CertList;
			}
			
			for (String s : (new File(CertPath)).list()) {
				if(!s.matches(".*\\.RSA") && !s.matches(".*\\.DSA") ) continue;

				File rsaFile = new File(CertPath + s);
				if(!rsaFile.exists()) continue;

				String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
				String[] result = MyConsolCmd.exc(cmd, false, null);

			    String certContent = "";
			    mApkInfo.CertSummary = "<certificate[1]>\n";
			    for(int i=0; i < result.length; i++){
		    		if(!certContent.isEmpty() && result[i].matches("^.*\\[[0-9]*\\]:$")){
		    			mApkInfo.CertList.add(certContent);
		    			mApkInfo.CertSummary += "<certificate[" + (mApkInfo.CertList.size() + 1) + "]>\n";
				    	certContent = "";
		    		}
		    		if(result[i].matches("^.*:( [^ ,]+=(\".*\")?[^,]*,?)+$")) {
		    			mApkInfo.CertSummary += result[i] + "\n";
		    		}
		    		certContent += (certContent.isEmpty() ? "" : "\n") + result[i];
			    }
			    mApkInfo.CertList.add(certContent);
			}
			return mApkInfo.CertList;
		}

		private void deleteTempPath()
		{
			System.out.println("delete Folder : "  + mApkInfo.WorkTempPath);
			if(mApkInfo.WorkTempPath != null && !mApkInfo.WorkTempPath.isEmpty()) {
				CoreApkTool.deleteDirectory(new File(mApkInfo.WorkTempPath+"-res"));

				File parent = new File(mApkInfo.WorkTempPath);
				while(parent != null && parent.exists() && parent.getParentFile() != null 
						&& parent.getParentFile().listFiles().length == 1 
						&& parent.getParentFile().getAbsolutePath().length() > CoreApkTool.getTempPath().length()) {
					parent = parent.getParentFile();
				}
				CoreApkTool.deleteDirectory(parent);
			}
			if(isPackageTempAPK && mApkInfo.ApkPath != null && !mApkInfo.ApkPath.isEmpty()) {
				File parent = new File(mApkInfo.ApkPath).getParentFile();
				System.out.println("delete temp APK folder : "  + parent.getPath());
				while(parent != null && parent.exists() && parent.getParentFile() != null 
						&& parent.getParentFile().listFiles().length == 1 
						&& parent.getParentFile().getAbsolutePath().length() > CoreApkTool.getTempPath().length()) {
					parent = parent.getParentFile();
				}
				
				CoreApkTool.deleteDirectory(parent);
			}
			mApkInfo = null;
		}

		public void run()
		{
			//System.out.println("ProcessThead run()~~~");
			synchronized(mOwner) {
				mOwner.notify();
				try {
					mOwner.wait();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				if(mListener != null) mListener.OnStart();
				switch(mCmd) {
				case SOLVE_RESOURCE:
				case SOLVE_CODE:
				case SOLVE_BOTH:
					if (solve() == true) {
						if(mListener != null) mListener.OnSuccess();
					} else {
						if(mListener != null) mListener.OnError();
					}
					break;
				case DELETE_TEMP_PATH:
					deleteTempPath();
					if(mListener != null) mListener.OnSuccess();
					break;
				}
				if(mListener != null) mListener.OnComplete();
			}
		}
	}
}
