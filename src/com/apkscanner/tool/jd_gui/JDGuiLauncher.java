package com.apkscanner.tool.jd_gui;

import java.io.File;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class JDGuiLauncher
{
	static public void run(String jarFilePath)
	{
		if(jarFilePath == null || !(new File(jarFilePath).isFile())) {
			Log.e("No such file : " + jarFilePath);
			return;
		}
		SystemUtil.exec(new String[] {"java", "-jar", Resource.BIN_JDGUI.getPath(), jarFilePath});
	}
}