package com.apkscanner.tool.dex2jar;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;

public class DexLuncher {
	public interface DexWrapperListener
	{    		
		public void OnError();
		public void OnSuccess();
	}
	
	static public void openDex(final String dexFilePath, final DexWrapperListener listener)
	{			
		new Thread(new Runnable() {
			public void run()
			{
				String jarFilePath = dexFilePath.replaceAll("\\.dex$", ".jar");
							
				String[] cmdLog = null;
				
				Log.i("Start DEX2JAR");
				if(System.getProperty("os.name").indexOf("Window") >-1) {
					cmdLog =ConsolCmd.exc(new String[] {Resource.BIN_DEX2JAR_WIN.getPath(), 
							dexFilePath, "-o", jarFilePath});
				} else {  //for linux
					cmdLog =ConsolCmd.exc(new String[] {"sh", Resource.BIN_DEX2JAR_LNX.getPath(), 
							dexFilePath, "-o", jarFilePath});				
				}
				//open JD GUI
				for( int i=0 ; i<cmdLog.length; i++)
				{
					Log.i("DEX2JAR Log : "+ cmdLog[i]);	
				}
				Log.i("End DEX2JAR");
				listener.OnSuccess();
				cmdLog =ConsolCmd.exc(new String[] {"java", "-jar", Resource.BIN_JDGUI.getPath(), jarFilePath});
				
			}
		}).start();
	}
	
	
}