package com.apkscanner.tool.dex2jar;

import java.io.File;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class Dex2JarWrapper
{
	public interface DexWrapperListener
	{
		public void onCompleted();
		public void onError();
		public void onSuccess(String outputFilePath);
	}

	static public void openDex(final String dexFilePath, final DexWrapperListener listener)
	{
		new Thread(new Runnable() {
			public void run() {
				if(dexFilePath == null || !(new File(dexFilePath)).isFile()) {
					Log.e("No such file : " + dexFilePath);
					if(listener != null) {
						listener.onError();
						listener.onCompleted();
					}
					return;
				}

				String jarFilePath = dexFilePath.replaceAll("\\.dex$", ".jar");
				jarFilePath = jarFilePath.replaceAll("\\.apk$", ".jar");

				String[] cmdLog = null;

				Log.i("Start DEX2JAR");
				if(SystemUtil.isWindows()) {
					cmdLog = ConsolCmd.exc(new String[] {Resource.BIN_DEX2JAR.getPath(), 
							dexFilePath, "-o", jarFilePath});
				} else if(SystemUtil.isLinux()) {
					cmdLog = ConsolCmd.exc(new String[] {"sh", Resource.BIN_DEX2JAR.getPath(), 
							dexFilePath, "-o", jarFilePath});				
				} else {
					Log.e("Unknown OS : " + SystemUtil.OS);
					if(listener != null) {
						listener.onError();
						listener.onCompleted();
					}
					return;
				}

				boolean successed = true;
				for( int i=0 ; i<cmdLog.length; i++)
				{
					Log.i("DEX2JAR Log : "+ cmdLog[i]);
					if(cmdLog[i].indexOf("Can not find classes.dex") > -1) {
						successed = false;
					}
				}
				Log.i("End DEX2JAR");

				if(!new File(jarFilePath).isFile()) {
					Log.e("No such file : " + jarFilePath);
					successed = false;
				}

				if(listener != null) {
					if(successed)
						listener.onSuccess(jarFilePath);
					else
						listener.onError();
					listener.onCompleted();
				}
			}
		}).start();
	}
}