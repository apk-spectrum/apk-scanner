package com.apkscanner.tool.apktool;

import java.io.File;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;

public class ApktoolWrapper {

	static private final String ApktoolVer = getApkToolVersion();
	
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
}
