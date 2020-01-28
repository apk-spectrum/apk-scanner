package com.apkscanner;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;

import com.apkscanner.resource.RFile;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

public class Launcher
{
	static private ArrayList<String> defaultCmd;

	static public void run()
	{
		SystemUtil.exec(getDefaultCmd());
	}

	static public void run(String apkFilePath, boolean easyScanner)
	{
		if(apkFilePath == null) return;

		ArrayList<String> cmd = new ArrayList<String>(getDefaultCmd());
		if(!easyScanner) {
			cmd.add("o");
		} else {
			cmd.add("e");
		}
		cmd.add(apkFilePath);

		for(String str: cmd ) {
			Log.d(str);
		}

		SystemUtil.exec(cmd);
	}

	static public void run(String apkFilePath)
	{
		if(apkFilePath == null) return;

		ArrayList<String> cmd = new ArrayList<String>(getDefaultCmd());
		cmd.add(apkFilePath);
		Log.d(""+ cmd.toArray().toString());
		SystemUtil.exec(cmd);
	}

	static public void run(String devSerialNum, String devApkFilePath, String frameworkRes)
	{
		if(devApkFilePath == null) return;

		ArrayList<String> cmd = new ArrayList<String>(getDefaultCmd());
		cmd.add("package");

		if(devSerialNum != null && !devSerialNum.isEmpty()) {
			cmd.add("-d");
			cmd.add(devSerialNum);
		}

		if(frameworkRes != null && !frameworkRes.isEmpty()) {
			cmd.add("-f");
			cmd.add(frameworkRes);
		}

		cmd.add(devApkFilePath);

		SystemUtil.exec(cmd);
	}

	static public void install(String apkFilePath)
	{
		if(apkFilePath == null) return;

		ArrayList<String> cmd = new ArrayList<String>(getDefaultCmd());
		cmd.add("install");
		cmd.add(apkFilePath);

		SystemUtil.exec(cmd);
	}

	static public void deleteTempPath(String path)
	{
		if(path == null) return;

		ArrayList<String> cmd = new ArrayList<String>(getDefaultCmd());
		cmd.add("delete-temp-path");
		cmd.add(path);

		SystemUtil.exec(cmd);
	}

	static private ArrayList<String> getDefaultCmd()
	{
		if(defaultCmd != null)
			return defaultCmd;

		defaultCmd = new ArrayList<String>();

		String appPath = null;
		try {
			appPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			Log.w(e.getMessage());
			appPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			if(SystemUtil.isWindows() && appPath.startsWith("/")) {
				appPath = appPath.substring(1);
			}
		}

		StringBuilder classPathBuilder = new StringBuilder();
		classPathBuilder.append(appPath);
		classPathBuilder.append(File.pathSeparator);
		classPathBuilder.append(RFile.LIB_ALL.get());

		String classPath = null;
		try {
			classPath = URLDecoder.decode(classPathBuilder.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e1) { }

		defaultCmd.add("java");
		defaultCmd.add("-Dfile.encoding=utf-8");
		defaultCmd.add("-Djava.library.path=" + RFile.BIN_PATH.get());
		defaultCmd.add("-cp");
		defaultCmd.add(classPath);
		defaultCmd.add(Main.class.getName());

		return defaultCmd;
	}
}