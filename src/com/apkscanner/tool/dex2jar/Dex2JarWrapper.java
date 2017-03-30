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
		public void onError(String message);
		public void onSuccess(String outputFilePath);
	}

	static public void openDex(final String dexFilePath, final DexWrapperListener listener)
	{
		new Thread(new Runnable() {
			public void run() {
				if(dexFilePath == null || !(new File(dexFilePath)).isFile()) {
					Log.e("No such file : " + dexFilePath);
					if(listener != null) {
						listener.onError("No such file : " + dexFilePath);
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
						listener.onError("Unknown OS : " + SystemUtil.OS);
						listener.onCompleted();
					}
					return;
				}

				StringBuilder sb = new StringBuilder();
				boolean successed = true;
				
				for(String s: cmdLog)
				{
					sb.append(s+"\n");
					Log.i("DEX2JAR Log : "+ s);
					if(s.indexOf("Can not find classes.dex") > -1) {
						successed = false;
					}
				}
				Log.i("End DEX2JAR");

				if(!successed) {
					if(listener != null)
						listener.onError(sb.toString());
					listener.onCompleted();
					return;
				}

				if(!new File(jarFilePath).isFile()) {
					Log.e("No such file : " + jarFilePath);
					successed = false;
				}

				if(listener != null) {
					if(successed)
						listener.onSuccess(jarFilePath);
					else
						listener.onError("No such file : " + jarFilePath);
					listener.onCompleted();
				}
			}
		}).start();
	}
}