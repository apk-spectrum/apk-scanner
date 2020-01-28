package com.apkscanner;

import java.util.ArrayList;
import java.util.List;

public class Launcher
{
	private static com.apkspectrum.util.Launcher launcher
					= new com.apkspectrum.util.Launcher();

	static public void run() {
		launcher.run();
	}

	static public void run(String apkFilePath) {
		if(apkFilePath == null) return;

		launcher.run(apkFilePath);
	}

	static public void run(String devSerialNum, String devApkFilePath, String frameworkRes) {
		if(devApkFilePath == null) return;

		List<String> params = new ArrayList<String>();
		params.add("package");

		if(devSerialNum != null && !devSerialNum.isEmpty()) {
			params.add("-d");
			params.add(devSerialNum);
		}

		if(frameworkRes != null && !frameworkRes.isEmpty()) {
			params.add("-f");
			params.add(frameworkRes);
		}

		params.add(devApkFilePath);

		launcher.run(params.toArray(new String[params.size()]));
	}

	static public void install(String apkFilePath) {
		if(apkFilePath == null) return;

		launcher.run("install", apkFilePath);
	}

	static public void deleteTempPath(String path) {
		if(path == null) return;

		launcher.run("delete-temp-path", path);
	}
}