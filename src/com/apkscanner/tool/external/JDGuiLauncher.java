package com.apkscanner.tool.external;

import java.io.File;

import com.apkscanner.resource.RFile;
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
		SystemUtil.exec(new String[] {"java", "-jar", RFile.BIN_JDGUI.get(), jarFilePath});
	}
}