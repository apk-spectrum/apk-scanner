package com.apkscanner.tool.aapt;

import java.io.File;
import java.util.ArrayList;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;

public class AaptWrapper
{
	static private final Object initSync = new Object();
	static private final String aaptCmd = getAaptCmd();
	static private String aaptVersion = null;

	static public class Dump
	{
		static public String[] getStrings(String apkFilePath)
		{
			return ConsolCmd.exc(new String[] {aaptCmd, "dump", "strings", apkFilePath});
		}

		static public String[] getBadging(String apkFilePath, boolean includeMetaData)
		{
			if(includeMetaData) {
				return ConsolCmd.exc(new String[] {aaptCmd, "dump", "--include-meta-data", "badging", apkFilePath});	
			} else {
				return ConsolCmd.exc(new String[] {aaptCmd, "dump", "badging", apkFilePath});				
			}
		}

		static public String[] getPermissions(String apkFilePath)
		{
			return ConsolCmd.exc(new String[] {aaptCmd, "dump", "permissions", apkFilePath});
		}

		static public String[] getResources(String apkFilePath, boolean includeResourceValues)
		{
			Log.i("getResources() " + apkFilePath);
			if(includeResourceValues) {
				return ConsolCmd.exc(new String[] {aaptCmd, "dump", "--values", "resources", apkFilePath});	
			} else {
				return ConsolCmd.exc(new String[] {aaptCmd, "dump", "resources", apkFilePath});				
			}
		}

		static public String[] getConfigurations(String apkFilePath)
		{
			return ConsolCmd.exc(new String[] {aaptCmd, "dump", "configurations", apkFilePath});
		}

		static public String[] getXmltree(String apkFilePath, String[] assets)
		{
			//Log.i("getXmltree() " + apkFilePath);
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add(aaptCmd);
			cmd.add("dump");
			cmd.add("xmltree");
			cmd.add(apkFilePath);
			for(String a: assets) {
				cmd.add(a);
			}
			return ConsolCmd.exc(cmd.toArray(new String[0]));
		}

		static public String[] getXmlstrings(String apkFilePath, String[] assets)
		{
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add(aaptCmd);
			cmd.add("dump");
			cmd.add("xmlstrings");
			cmd.add(apkFilePath);
			for(String a: assets) {
				cmd.add(a);
			}
			return ConsolCmd.exc(cmd.toArray(new String[0]));
		}
	}

	static public String[] getList(String apkFilePath, boolean androidData, boolean verbose)
	{
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(aaptCmd);
		cmd.add("list");
		if(verbose) cmd.add("-v");
		if(androidData) cmd.add("-a");
		cmd.add(apkFilePath);
		return ConsolCmd.exc(cmd.toArray(new String[0]));
	}

	static public String getVersion()
	{
		synchronized(initSync) {
			if(aaptVersion == null) {
				String aapt = getAaptCmd();
				if(aapt == null) return null;
				String[] result = ConsolCmd.exc(new String[] {aapt, "version"});
				return result[0];
			}
		}
		return aaptVersion;
	}

	static private String getAaptCmd()
	{
		String cmd;
		synchronized(initSync) {
			cmd = aaptCmd;
			if(cmd == null) {
				cmd = Resource.BIN_AAPT.getPath();

				if(!(new File(cmd)).exists()) {
					Log.e("no such aapt tool" + aaptCmd);
					cmd = null;
				}
			}
		}
		return cmd;
	}
}
