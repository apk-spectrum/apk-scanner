package com.apkscanner.tool.aapt;

import java.util.ArrayList;

import com.apkscanner.util.Log;

public class AaptNativeWrapper {

	static public class List
	{
		static public String[] getList(String apkFilePath, boolean androidData, boolean verbose)
		{
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add("list");
			if(verbose) cmd.add("-v");
			if(androidData) cmd.add("-a");
			cmd.add(apkFilePath);
			return run(cmd.toArray(new String[0]));
		}
	}
	
	static public class Dump
	{
		static public String[] getStrings(String apkFilePath)
		{
			return run(new String[] {"dump", "strings", apkFilePath});
		}

		static public String[] getBadging(String apkFilePath, boolean includeMetaData)
		{
			if(includeMetaData) {
				return run(new String[] {"dump", "--include-meta-data", "badging", apkFilePath});	
			} else {
				return run(new String[] {"dump", "badging", apkFilePath});				
			}
		}

		static public String[] getPermissions(String apkFilePath)
		{
			return run(new String[] {"dump", "permissions", apkFilePath});
		}

		static public String[] getResources(String apkFilePath, boolean includeResourceValues)
		{
			Log.i("getResources() " + apkFilePath);
			if(includeResourceValues) {
				return run(new String[] {"dump", "--values", "resources", apkFilePath});	
			} else {
				return run(new String[] {"dump", "resources", apkFilePath});				
			}
		}

		static public String[] getConfigurations(String apkFilePath)
		{
			return run(new String[] {"dump", "configurations", apkFilePath});
		}

		static public String[] getXmltree(String apkFilePath, String[] assets)
		{
			//Log.i("getXmltree() " + apkFilePath);
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add("dump");
			cmd.add("xmltree");
			cmd.add(apkFilePath);
			for(String a: assets) {
				cmd.add(a);
			}
			return run(cmd.toArray(new String[0]));
		}

		static public String[] getXmlstrings(String apkFilePath, String[] assets)
		{
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add("dump");
			cmd.add("xmlstrings");
			cmd.add(apkFilePath);
			for(String a: assets) {
				cmd.add(a);
			}
			return run(cmd.toArray(new String[0]));
		}
	}
	
	public static class ResourceTable {
		private Object resTable;
		public ResourceTable(String apkFilePath)
		{
			resTable = getResTable(apkFilePath);
		}
		
		public void release() {
			if(resTable != null) {
				realeaseResTable(resTable);
				resTable = null;
			}
		}
		
		public String getResourceName(int resId) {
			return AaptNativeWrapper.getResourceName(resTable, resId);
		}
	}

	private native static String[] run(String[] params);
	
	public native static Object getResTable(String apkFilePath);
	public native static void realeaseResTable(Object resTable);
	public native static String getResourceName(Object resTable, int resId);

	static {
		if (System.getProperty("os.name").indexOf("Linux") > -1) {
			System.loadLibrary("c++");
		}
		System.loadLibrary("AaptNativeWrapper");
		//System.load("Y:\\android-sdk-build\\out\\host\\windows-x86\\lib64\\libAaptNativeWrapper.dll");
		//System.load("/home/local_depot/android-sdk-build/out/host/linux-x86/lib64/libAaptNativeWrapper.so");
	}
}
