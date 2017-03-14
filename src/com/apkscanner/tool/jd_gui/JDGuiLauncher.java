package com.apkscanner.tool.jd_gui;

import java.io.File;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;

public class JDGuiLauncher
{
	static public void run(String jarFilePath)
	{
		if(jarFilePath == null || !(new File(jarFilePath).isFile())) {
			Log.e("No such file : " + jarFilePath);
			return;
		}
		ConsolCmd.exc(new String[] {"java", "-jar", Resource.BIN_JDGUI.getPath(), jarFilePath});
	}
}