package com.apkscanner.tool.aapt;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class AaptNativeWrapper {

	static private final int SEM_COUNT = 10;
	static private Semaphore semaphore = new Semaphore(SEM_COUNT, true);
	static private boolean nativeLocked = false;

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
			return run_l(new String[] {"dump", "strings", apkFilePath});
		}

		static public String[] getBadging(String apkFilePath, boolean includeMetaData)
		{
			if(includeMetaData) {
				return run_l(new String[] {"dump", "--include-meta-data", "badging", apkFilePath});	
			} else {
				return run_l(new String[] {"dump", "badging", apkFilePath});				
			}
		}

		static public String[] getPermissions(String apkFilePath)
		{
			return run_l(new String[] {"dump", "permissions", apkFilePath});
		}

		static public String[] getResources(String apkFilePath, boolean includeResourceValues)
		{
			Log.i("getResources() " + apkFilePath);
			if(includeResourceValues) {
				return run_l(new String[] {"dump", "--values", "resources", apkFilePath});	
			} else {
				return run_l(new String[] {"dump", "resources", apkFilePath});				
			}
		}

		static public String[] getConfigurations(String apkFilePath)
		{
			return run_l(new String[] {"dump", "configurations", apkFilePath});
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
			return run_l(cmd.toArray(new String[0]));
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
			return run_l(cmd.toArray(new String[0]));
		}
	}

	private static String[] run_l(String[] params) {
		semaphore.acquireUninterruptibly();
		String[] ret = run(params);
		semaphore.release();
		return ret;
	}
	
	public static void lock() {
		synchronized (semaphore) {
			if(nativeLocked) return;
			semaphore.acquireUninterruptibly(SEM_COUNT);
			nativeLocked = true;
		}
	}
	
	public static void unlock() {
		synchronized (semaphore) {
			if(nativeLocked) semaphore.release(SEM_COUNT);
			nativeLocked = false;
		}
	}
	
	private native static String[] run(String[] params);

	static {
		if (SystemUtil.isLinux()) {
			System.loadLibrary("c++");
		}
		System.loadLibrary("AaptNativeWrapper");
		//System.load("Y:\\android-sdk-build\\out\\host\\windows-x86\\lib64\\libAaptNativeWrapper.dll");
		//System.load("/home/local_depot/android-sdk-build/out/host/linux-x86/lib64/libAaptNativeWrapper.so");
	}
}
