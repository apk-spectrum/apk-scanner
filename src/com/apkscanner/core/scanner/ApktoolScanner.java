package com.apkscanner.core.scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class ApktoolScanner extends ApkScanner
{
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
	
	public ApktoolScanner(StatusListener statusListener)
	{
		super(statusListener);
		apkInfo = new ApkInfo();
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
			apkInfo.filePath = apkPath;
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

	@Override
	public void openApk(String apkFilePath, String frameworkRes)
	{

		setApkFile(apkFilePath);
		addFameworkRes(frameworkRes);
		
		if(apkInfo.filePath == null) return;
		//Log.i("solve()....start ");
		synchronized(this) {
			mProcess = new ProcessThead(this, ProcessCmd.SOLVE_RESOURCE, null /*statusListener*/);
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
			apkInfo.tempWorkPath = FileUtil.makeTempPath(apkInfo.filePath);
			Log.i("Temp path : " + apkInfo.tempWorkPath);

			apkInfo.fileSize = (new File(apkInfo.filePath)).length();
			
			boolean isSolve = solveAPK(apkInfo.filePath, apkInfo.tempWorkPath);
			if(isSolve) {
				XmlToMyApkinfo();
				
				apkInfo.resources = FileUtil.findFiles(new File(apkInfo.tempWorkPath + File.separator + "res"), ".png", ".*drawable.*").toArray(new String[0]);
				apkInfo.libraries = FileUtil.findFiles(new File(apkInfo.tempWorkPath + File.separator + "lib"), ".so", null).toArray(new String[0]);
	
				solveCert();
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
				ConsolCmd.exc(cmd, true, new ConsolCmd.ConsoleOutputObserver() {
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
			String[] result = ConsolCmd.exc(cmd, true, new ConsolCmd.ConsoleOutputObserver() {
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
			if(mListener != null) mListener.onProgress(step, msg);
		}
		
		private void XmlToMyApkinfo()
		{
			progress(5,"Check Yml....\n");
			YmlToMyApkinfo();

			progress(5,"parsing AndroidManifest....\n");
			File manifestFile = new File(apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml");
			XmlPath xmlAndroidManifest = new XmlPath(manifestFile);

			// package
			XmlPath node = xmlAndroidManifest.getNode("/manifest");
			if(node == null) return;
			apkInfo.manifest.packageName = node.getAttribute("package");
			apkInfo.manifest.sharedUserId = node.getAttribute("android:sharedUserId");
			/*
			if(apkInfo.VersionCode == null || apkInfo.VersionCode.isEmpty()) {
				apkInfo.VersionCode = xmlAndroidManifest.getAttributes("android:versionCode");
			}
			
			if(apkInfo.VersionName == null || apkInfo.VersionName.isEmpty()) {
				apkInfo.VersionName = xmlAndroidManifest.getAttributes("android:versionName");
			}

			// label & icon
			xmlAndroidManifest.getNode("/manifest/application");
			apkInfo.Labelname = getMutiLang(xmlAndroidManifest.getAttributes("android:label"));
			apkInfo.IconPath = getResourceInfo(xmlAndroidManifest.getAttributes("android:icon"));
			
			String debuggable = xmlAndroidManifest.getAttributes("android:debuggable");
			apkInfo.debuggable = debuggable != null && debuggable.equals("true");
	        
	        // hidden
	        if(!xmlAndroidManifest.isNode("//category[@name='android.intent.category.LAUNCHER']")) {
	        	apkInfo.isHidden = true;
	        }
	        
	        // startup
	        if(xmlAndroidManifest.isNode("//uses-permission[@name='android.permission.RECEIVE_BOOT_COMPLETED']")) {
	        	apkInfo.Startup = "START_UP";
	        } else {
	        	apkInfo.Startup = "";
	        }

	        // permission
	        apkInfo.ProtectionLevel = "";
	        progress(5,"parsing permission...\n");
	        xmlAndroidManifest.getNodeList("//uses-permission");
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	if(idx==0) apkInfo.Permissions = "<uses-permission> [" + xmlAndroidManifest.getLength() + "]";
	        	apkInfo.Permissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
	        	apkInfo.PermissionList.add(xmlAndroidManifest.getAttributes(idx, "android:name"));
	        	String sig = xmlAndroidManifest.getAttributes(idx, "android:protectionLevel");
	        	if(sig != null && sig.equals("signature")) {
	        		apkInfo.Permissions += " - <SIGNATURE>";
	        		apkInfo.ProtectionLevel = "SIGNATURE";
	        	}
	        }
	        xmlAndroidManifest.getNodeList("//permission");
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	if(idx==0) apkInfo.Permissions += "\n\n<permission> [" + xmlAndroidManifest.getLength() + "]";
	        	apkInfo.Permissions += "\n" + xmlAndroidManifest.getAttributes(idx, "android:name");
	        	apkInfo.PermissionList.add(xmlAndroidManifest.getAttributes(idx, "android:name"));
	        	String sig = xmlAndroidManifest.getAttributes(idx, "android:protectionLevel");
	        	if(sig != null && sig.equals("signature")) {
	        		apkInfo.Permissions += " - <SIGNATURE>";
	        		apkInfo.ProtectionLevel = "SIGNATURE";
	        	}
	        }
	        
	        PermissionGroupManager permGroupManager = new PermissionGroupManager(apkInfo.PermissionList.toArray(new String[0]));
	        apkInfo.PermGroupMap = permGroupManager.getPermGroupMap();

	        // widget
	        progress(5,"parsing widget...\n");
	        xmlAndroidManifest.getNodeList("//meta-data[@name='android.appwidget.provider']");
	        //Log.i("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	Object[] widgetExtraInfo = {apkInfo.IconPath, ""};
	        	
	        	MyXPath parent = xmlAndroidManifest.getParentNode(idx);
	        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
	        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));
	   			Object[] extraInfo = getWidgetInfo(xmlAndroidManifest.getAttributes(idx, "android:resource"));
	        	if(extraInfo != null) {
	        		widgetExtraInfo = extraInfo;
	        	}
	        	
	        	apkInfo.WidgetList.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetActivity, "Normal"});
	        }
	        
	        xmlAndroidManifest.getNodeList("//action[@name='android.intent.action.CREATE_SHORTCUT']");
	        //Log.i("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
	        for( int idx=0; idx < xmlAndroidManifest.getLength(); idx++ ){
	        	MyXPath parent = xmlAndroidManifest.getParentNode(idx).getParentNode();
	        	String widgetTitle = getResourceInfo(parent.getAttributes("android:label"));
	        	String widgetActivity = getResourceInfo(parent.getAttributes("android:name"));

	        	apkInfo.WidgetList.add(new Object[] {apkInfo.IconPath, widgetTitle, "1 X 1", widgetActivity, "Shortcut"});
	        }
	        
	        // Activity/Service/Receiver/provider intent-filter
	        getActivityInfo(xmlAndroidManifest, "activity");
	        getActivityInfo(xmlAndroidManifest, "service");
	        getActivityInfo(xmlAndroidManifest, "receiver");
	        getActivityInfo(xmlAndroidManifest, "provider");
	        
	        apkInfo.verify();
	        */
		}
		
		private String getResourceInfo(String id)
		{
			if(id == null) return null;

			String result = null;
			String resXmlPath = new String(apkInfo.tempWorkPath + File.separator + "res" + File.separator);
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
					XmlPath node = new XmlPath(resFile).getNode(query);
					if(node != null) {
				        result = node.getTextContent();
				        if(result != null) break;
					}
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

			String resXmlPath = new String(apkInfo.tempWorkPath + File.separator + "res" + File.separator);
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
				XmlPath node = new XmlPath(resFile).getNode(query);
				if(node == null) continue;
				String value = node.getTextContent();
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
			XmlPath tagPaths = xmlAndroidManifest.getNodeList("//"+tag);
	        for( int idx=0; idx < tagPaths.getCount(); idx++ ){
	        	String name = tagPaths.getAttribute(idx, "android:name");
	        	String startup = "X";
	        	String intents = "";

	        	XmlPath intentsNode = xmlAndroidManifest.getNodeList("//"+tag+"[@name='" + name + "']/intent-filter/action");
	        	for( int i=0; i < intentsNode.getCount(); i++ ){
	        		String act = intentsNode.getAttribute(i, "android:name");
	        		if(i==0) intents += "<intent-filter> [" + intentsNode.getCount() + "]";
	        		intents += "\n" + act;
	        		if(act.equals("android.intent.action.BOOT_COMPLETED"))
	        			startup = "O";
	        	}
	        	
	        	if(intentsNode.isNodeExisted("//"+tag+"[@name='" + name + "']/intent-filter/category[@name='android.intent.category.LAUNCHER']")) {
	        		name += " - LAUNCHER";
	        	}
	        	
	        	//apkInfo.ActivityList.add(new Object[] { name, tag, startup, intents });
	        }
		}
		
		@SuppressWarnings("unused")
		private Object[] getWidgetInfo(String resource) {

			//Log.i("getWidgetInfo() " + resource);
			String resXmlPath = new String(apkInfo.tempWorkPath + File.separator + "res" + File.separator);

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

				XmlPath xpath = new XmlPath(xmlFile).getNode("//appwidget-provider");
				if(xpath == null) continue;
		        
				if(Size.isEmpty() && xpath.getAttribute("android:minWidth") != null
						&& xpath.getAttribute("android:minHeight") != null) {
					String width = xpath.getAttribute("android:minWidth");
					String Height = xpath.getAttribute("android:minHeight");
					width = getResourceInfo(width).replaceAll("^([0-9]*).*", "$1");
					Height = getResourceInfo(Height).replaceAll("^([0-9]*).*", "$1");
					//Size = ((Integer.parseInt(width) - 40) / 70 + 1) + " X " + ((Integer.parseInt(Height) - 40) / 70 + 1);
					Size = (int)Math.ceil((Float.parseFloat(width) - 40) / 76 + 1) + " X " + (int)Math.ceil((Float.parseFloat(Height) - 40) / 76 + 1);
					Size += "\n(" + width + " X " + Height + ")";
			    	//Log.i("Size " + Size + ", width " + width + ", height " + Height);
				}
				
				if(ReSizeMode.isEmpty() && xpath.getAttribute("android:resizeMode") != null) {
					ReSizeMode = xpath.getAttribute("android:resizeMode");
				}

				if((IconPath == null || IconPath.isEmpty()) 
						&& xpath.getAttribute("android:previewImage") != null) {
					String icon = xpath.getAttribute("android:previewImage");
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
			String ymlPath = new String(apkInfo.tempWorkPath + File.separator + "apktool.yml");
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
						apkInfo.manifest.versionCode = Integer.parseInt(sLine.replaceFirst("\\s*versionCode:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1"));
						//apkInfo.manifest.versionCode = getResourceInfo(apkInfo.VersionCode);
					} else if(sLine.matches("^\\s*versionName:.*")) {
						apkInfo.manifest.versionName = sLine.replaceFirst("\\s*versionName:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
						//apkInfo.VersionName = getResourceInfo(apkInfo.VersionName);
					} else if(sLine.matches("^\\s*minSdkVersion:.*")) {
						apkInfo.manifest.usesSdk.minSdkVersion = Integer.parseInt(sLine.replaceFirst("\\s*minSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1"));
					} else if(sLine.matches("^\\s*targetSdkVersion:.*")) {
						apkInfo.manifest.usesSdk.targetSdkVersion = Integer.parseInt(sLine.replaceFirst("\\s*targetSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1"));
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
		
		private void deleteTempPath()
		{
			Log.i("delete Folder : "  + apkInfo.tempWorkPath);
			if(apkInfo.tempWorkPath != null && !apkInfo.tempWorkPath.isEmpty()) {
				FileUtil.deleteDirectory(new File(apkInfo.tempWorkPath+"-res"));

				File parent = new File(apkInfo.tempWorkPath);
				while(parent != null && parent.exists() && parent.getParentFile() != null 
						&& parent.getParentFile().listFiles().length == 1 
						&& parent.getParentFile().getAbsolutePath().length() > FileUtil.getTempPath().length()) {
					parent = parent.getParentFile();
				}
				FileUtil.deleteDirectory(parent);
			}
			if(isPackageTempAPK && apkInfo.filePath != null && !apkInfo.filePath.isEmpty()) {
				File parent = new File(apkInfo.filePath).getParentFile();
				Log.i("delete temp APK folder : "  + parent.getPath());
				while(parent != null && parent.exists() && parent.getParentFile() != null 
						&& parent.getParentFile().listFiles().length == 1 
						&& parent.getParentFile().getAbsolutePath().length() > FileUtil.getTempPath().length()) {
					parent = parent.getParentFile();
				}
				
				FileUtil.deleteDirectory(parent);
			}
			apkInfo = null;
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
				
				if(mListener != null) mListener.onStart(EstimatedTimeEnRoute.calc(apkInfo.filePath));
				switch(mCmd) {
				case SOLVE_RESOURCE:
				case SOLVE_CODE:
				case SOLVE_BOTH:
					if (solve() == true) {
						if(mListener != null) mListener.onSuccess();
					} else {
						if(mListener != null) mListener.onError(ERR_UNKNOWN);
					}
					break;
				case DELETE_TEMP_PATH:
					deleteTempPath();
					if(mListener != null) mListener.onSuccess();
					break;
				}
				if(mListener != null) mListener.onCompleted();
			}
		}
	}
}
