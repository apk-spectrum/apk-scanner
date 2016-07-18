package com.apkscanner.tool.apktool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.apkscanner.core.scanner.ApkScannerStub;
import com.apkscanner.core.scanner.EstimatedTimeEnRoute;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class ApktoolManager extends ApkScannerStub
{	
	static private final String ApktoolVer = getApkToolVersion();

	private ApkInfo mApkInfo;
	private boolean isPackageTempAPK = false;
 	
	//private Status mState = Status.UNINITIALIZE;
	
	private ArrayList<String> mFrameworkResList;
	
	private ProcessThead mProcess = null;
	
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
	
	public ApktoolManager(StatusListener statusListener)
	{
		super(statusListener);
		mApkInfo = new ApkInfo();
		mFrameworkResList = new ArrayList<String>();
	}

	public void setApkFile(String apkPath) {
		File apkFile = new File(apkPath);

		if(!apkFile.exists()) {
			Log.e("No Such APK file");
			return;
		}
		apkPath = apkFile.getAbsolutePath();
		
		if(apkPath != null && (new File(apkPath)).exists()) {
			mApkInfo.filePath = apkPath;
		}
	}

	public void addFameworkRes(String resApkPath) {
		if(resApkPath == null) return;
		
		
		for(String file: resApkPath.split(";")) {
			if(file.isEmpty()) continue;
			File resFile = new File(file);
			if(!resFile.exists()) {
				Log.w("No Such res file : " + file);
				continue;
			}
			mFrameworkResList.add(resFile.getAbsolutePath());
		}
	}
	
	static public String getApkToolVersion()
	{
		if(ApktoolVer == null) {
			String apkToolPath = Resource.LIB_APKTOOL_JAR.getPath();
			if(!(new File(apkToolPath)).exists()) {
				Log.e("No such file : apktool.jar");
				return null;
			}
			String[] result = ConsolCmd.exc(new String[] {"java", "-jar", apkToolPath, "--version"}, false);
	
			return result[0];
		}
		return ApktoolVer;
	}

	@Override
	public void openApk(String apkFilePath, String frameworkRes)
	{

		setApkFile(apkFilePath);
		addFameworkRes(frameworkRes);
		
		if(mApkInfo.filePath == null) return;
		//Log.i("solve()....start ");
		synchronized(this) {
			mProcess = new ProcessThead(this, ProcessCmd.SOLVE_RESOURCE, statusListener);
			mProcess.start();

			try {
				this.wait();
				this.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void clear(boolean sync)
	{
		//Log.i("clear()....start ");
		mProcess = new ProcessThead(this, ProcessCmd.DELETE_TEMP_PATH, null);
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
			if(sync) mProcess.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Log.i("clear()....end ");
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
			mApkInfo.tempWorkPath = FileUtil.makeTempPath(mApkInfo.filePath);
			Log.i("Temp path : " + mApkInfo.tempWorkPath);

			mApkInfo.fileSize = (new File(mApkInfo.filePath)).length();
			
			boolean isSolve = solveAPK(mApkInfo.filePath, mApkInfo.tempWorkPath);
			if(isSolve) {
				XmlToMyApkinfo();
				
				mApkInfo.resources = FileUtil.findFiles(new File(mApkInfo.tempWorkPath + File.separator + "res"), ".png", ".*drawable.*").toArray(new String[0]);
				mApkInfo.librarys = FileUtil.findFiles(new File(mApkInfo.tempWorkPath + File.separator + "lib"), ".so", null).toArray(new String[0]);
	
				solveCert(mApkInfo.tempWorkPath + File.separator + "original" + File.separator + "META-INF" + File.separator);
			}
			return isSolve;
		}
		
		private boolean solveAPK(String APKFilePath, String solvePath)
		{
			String apkToolPath = Resource.LIB_APKTOOL_JAR.getPath();
			Log.i("apkToolPath : " + apkToolPath);

			if(!(new File(apkToolPath)).exists()) {
				Log.i("No such file : apktool.jar");
				return false;
			}

			for(String framework: mFrameworkResList) {
				if(!(new File(framework)).exists()) continue;
				String[] cmd = {"java", "-jar", apkToolPath, "install-framework", "-p", solvePath+"-res", framework};
				ConsolCmd.exc(cmd, true, new ConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
				    	if(output.startsWith("I:"))
				    		progress(1,output + "\n");
				    	return true;
					}
				});
			}
			
			boolean isSuccess = true;
			String[] cmd = new String[] {"java", "-jar", apkToolPath, "d", "-s", "-f", "-o", solvePath, "-p", solvePath+"-res", APKFilePath};
			String[] result = ConsolCmd.exc(cmd, true, new ConsolCmd.OutputObserver() {
				@Override
				public boolean ConsolOutput(String output) {
			    	if(output.startsWith("I:"))
			    		progress(5,output + "\n");
			    	else
			    		progress(0,output + "\n");
			    	return true;
				}
			});
			
			for(String s: result) {
				if(s.startsWith("Exception")) {
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
			XmlPath xmlAndroidManifest = new XmlPath(mApkInfo.tempWorkPath + File.separator + "AndroidManifest.xml");

			// package
			xmlAndroidManifest.getNode("/manifest");
			mApkInfo.manifest.packageName = xmlAndroidManifest.getAttributes("package");
			mApkInfo.manifest.sharedUserId = xmlAndroidManifest.getAttributes("android:sharedUserId");
			/*
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
			
			String debuggable = xmlAndroidManifest.getAttributes("android:debuggable");
			mApkInfo.debuggable = debuggable != null && debuggable.equals("true");
	        
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
	        //Log.i("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
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
	        //Log.i("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
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
	        */
		}
		
		private String getResourceInfo(String id)
		{
			if(id == null) return null;

			String result = null;
			String resXmlPath = new String(mApkInfo.tempWorkPath + File.separator + "res" + File.separator);
			String query = "//";
			String filter = "";
			String fileName = "";
			String type = "string";
			long maxImgSize = 0;
			
			if(!id.startsWith("@")) {
				//Log.i("id is start without @");
				result = new String(id);
				return result;
			} else if(id.startsWith("@drawable/")) {
				//Log.i("@drawable");

				filter = "drawable";
				fileName = new String(id.substring(10)) + ".png";
				type = "image";
			} else if(id.startsWith("@string/")) {
				//Log.i("@stirng");
				
				filter = "values";
				query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
				fileName = "strings.xml";
			} else if(id.startsWith("@dimen/")) {
				//Log.i("string@dimen");

				filter = "values";
				query = "//resources/dimen[@name='"+id.substring(id.indexOf("/")+1)+"']";
				fileName = "dimens.xml";
			} else {
				Log.w("getResourceInfo() Unknown id " + id);
				return new String(id);
			}
			
			for (String s : (new File(resXmlPath)).list()) {
				if(!s.startsWith(filter)) continue;

				File resFile = new File(resXmlPath + s + File.separator + fileName);
				if(!resFile.exists()) continue;

		        //Log.i(" - " + resFile.getAbsolutePath() + ", " + type);
				if(type.equals("image")) {
					if(resFile.length() > maxImgSize) {
						//Log.i(resFile.getPath() + ", " + maxImgSize);
						result = new String(resFile.getPath());
						maxImgSize = resFile.length();
					}
				} else {
			        result = new XmlPath(resFile.getAbsolutePath()).getNode(query).getTextContent();
			        if(result != null) break;;
				}
			}
	        //Log.i(">> " + result);
			return result;
		}
		
		@SuppressWarnings("unused")
		private String[] getMutiLang(String id)
		{
			if(id == null || !id.startsWith("@string/"))
				return new String[] { id };

			ArrayList<String> result = new ArrayList<String>();

			String resXmlPath = new String(mApkInfo.tempWorkPath + File.separator + "res" + File.separator);
			String query = "//";
			String filter = "";
			String fileName = "";
			
			filter = "values";
			query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
			fileName = "strings.xml";
			
			for (String s : (new File(resXmlPath)).list()) {
				if(!s.startsWith(filter)) continue;

				File resFile = new File(resXmlPath + s + File.separator + fileName);
				if(!resFile.exists()) continue;
				String value = new XmlPath(resFile.getAbsolutePath()).getNode(query).getTextContent();
				if(value != null && value.startsWith("@")) {
					return getMutiLang(value);
				} else if(value != null) {
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
		
		@SuppressWarnings("unused")
		private void getActivityInfo(XmlPath xmlAndroidManifest, String tag) {
	        xmlAndroidManifest.getNodeList("//"+tag);
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	String name = xmlAndroidManifest.getAttributes(idx, "android:name");
	        	String startup = "X";
	        	String intents = "";

	        	XmlPath intentsNode = new XmlPath(xmlAndroidManifest);
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
	        	
	        	//mApkInfo.ActivityList.add(new Object[] { name, tag, startup, intents });
	        }
		}
		
		@SuppressWarnings("unused")
		private Object[] getWidgetInfo(String resource) {

			//Log.i("getWidgetInfo() " + resource);
			String resXmlPath = new String(mApkInfo.tempWorkPath + File.separator + "res" + File.separator);

			String Size = "";
			String IconPath = "";
			String ReSizeMode = "";
			
			if(resource == null || !resource.startsWith("@xml/")) {
				return new Object[] { IconPath, Size };
			}

			String widgetXml = new String(resource.substring(5));
			//Log.i("widgetXml : " + widgetXml);

			for (String s : (new File(resXmlPath)).list()) {
				if(!s.startsWith("xml")) continue;

				File xmlFile = new File(resXmlPath + s + File.separator + widgetXml + ".xml");
				if(!xmlFile.exists()) continue;
				
				//Log.i("xmlFile " + xmlFile.getAbsolutePath());

				XmlPath xpath = new XmlPath(xmlFile.getAbsolutePath());
				
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
			    	//Log.i("Size " + Size + ", width " + width + ", height " + Height);
				}
				
				if(ReSizeMode.isEmpty() && xpath.getAttributes("android:resizeMode") != null) {
					ReSizeMode = xpath.getAttributes("android:resizeMode");
				}

				if((IconPath == null || IconPath.isEmpty()) 
						&& xpath.getAttributes("android:previewImage") != null) {
					String icon = xpath.getAttributes("android:previewImage");
					IconPath = getResourceInfo(icon);
					//Log.i("icon " + IconPath);
				}
			}
			
			if(!ReSizeMode.isEmpty()) {
				Size += "\n\n[ReSizeMode]\n" + ReSizeMode.replaceAll("\\|", "\n");
			}
	    	
			return new Object[] { IconPath, Size };
		}
		
		private void YmlToMyApkinfo() {
			String ymlPath = new String(mApkInfo.tempWorkPath + File.separator + "apktool.yml");
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
						mApkInfo.manifest.versionCode = Integer.parseInt(sLine.replaceFirst("\\s*versionCode:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1"));
						//mApkInfo.manifest.versionCode = getResourceInfo(mApkInfo.VersionCode);
					} else if(sLine.matches("^\\s*versionName:.*")) {
						mApkInfo.manifest.versionName = sLine.replaceFirst("\\s*versionName:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
						//mApkInfo.VersionName = getResourceInfo(mApkInfo.VersionName);
					} else if(sLine.matches("^\\s*minSdkVersion:.*")) {
						mApkInfo.manifest.usesSdk.minSdkVersion = Integer.parseInt(sLine.replaceFirst("\\s*minSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1"));
					} else if(sLine.matches("^\\s*targetSdkVersion:.*")) {
						mApkInfo.manifest.usesSdk.targetSdkVersion = Integer.parseInt(sLine.replaceFirst("\\s*targetSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1"));
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
		
		public ArrayList<String> solveCert(String CertPath)
		{
			/*
			Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
			String keytoolPackage;
			if(javaVersion >= 1.8) {
				keytoolPackage = "sun.security.tools.keytool.Main";
			} else {
				keytoolPackage = "sun.security.tools.KeyTool";
			}

			mApkInfo.certificates.CertList.clear();
			if(!(new File(CertPath)).exists()) {
				Log.e("META-INFO 폴더가 존재 하지 않습니다 :");
				return mApkInfo.CertList;
			}
			
			for (String s : (new File(CertPath)).list()) {
				if(!s.endsWith(".RSA") && !s.endsWith(".DSA") ) continue;

				File rsaFile = new File(CertPath + s);
				if(!rsaFile.exists()) continue;

				String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
				String[] result = ConsolCmd.exc(cmd, false, null);

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
			*/
			return null;
		}

		private void deleteTempPath()
		{
			Log.i("delete Folder : "  + mApkInfo.tempWorkPath);
			if(mApkInfo.tempWorkPath != null && !mApkInfo.tempWorkPath.isEmpty()) {
				FileUtil.deleteDirectory(new File(mApkInfo.tempWorkPath+"-res"));

				File parent = new File(mApkInfo.tempWorkPath);
				while(parent != null && parent.exists() && parent.getParentFile() != null 
						&& parent.getParentFile().listFiles().length == 1 
						&& parent.getParentFile().getAbsolutePath().length() > FileUtil.getTempPath().length()) {
					parent = parent.getParentFile();
				}
				FileUtil.deleteDirectory(parent);
			}
			if(isPackageTempAPK && mApkInfo.filePath != null && !mApkInfo.filePath.isEmpty()) {
				File parent = new File(mApkInfo.filePath).getParentFile();
				Log.i("delete temp APK folder : "  + parent.getPath());
				while(parent != null && parent.exists() && parent.getParentFile() != null 
						&& parent.getParentFile().listFiles().length == 1 
						&& parent.getParentFile().getAbsolutePath().length() > FileUtil.getTempPath().length()) {
					parent = parent.getParentFile();
				}
				
				FileUtil.deleteDirectory(parent);
			}
			mApkInfo = null;
		}

		public void run()
		{
			//Log.i("ProcessThead run()~~~");
			synchronized(mOwner) {
				mOwner.notify();
				try {
					mOwner.wait();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				if(mListener != null) mListener.OnStart(EstimatedTimeEnRoute.calc(apkInfo.filePath));
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
