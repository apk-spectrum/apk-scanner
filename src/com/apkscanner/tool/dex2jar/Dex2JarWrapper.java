package com.apkscanner.tool.dex2jar;

import java.io.File;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class Dex2JarWrapper {
	public interface DexWrapperListener
	{    		
		public void OnError();
		public void OnSuccess(String outputFilePath);
	}
	
	static public void openDex(final String dexFilePath, final DexWrapperListener listener)
	{			
		new Thread(new Runnable() {
			public void run()
			{
				if(dexFilePath == null || !(new File(dexFilePath)).isFile()) {
					if(listener != null) listener.OnError();
					return;
				}
				String jarFilePath = dexFilePath.replaceAll("\\.dex$", ".jar");
				jarFilePath = jarFilePath.replaceAll("\\.apk$", ".jar");
							
				String[] cmdLog = null;

				Log.i("Start DEX2JAR");
				if(SystemUtil.isWindows()) {
					cmdLog = ConsolCmd.exc(new String[] {Resource.BIN_DEX2JAR_WIN.getPath(), 
							dexFilePath, "-o", jarFilePath});
				} else {  //for linux
					cmdLog = ConsolCmd.exc(new String[] {"sh", Resource.BIN_DEX2JAR_LNX.getPath(), 
							dexFilePath, "-o", jarFilePath});				
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
				
				if(listener != null) {
					if(successed)
						listener.OnSuccess(jarFilePath);
					else
						listener.OnError();
				}
			}
		}).start();
	}
	
}