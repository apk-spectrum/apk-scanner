package com.apkspectrum.tool;

import java.io.File;

import com.apkspectrum.resource._RFile;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

public class JDGuiLauncher
{
	static public void run(String jarFilePath)
	{
		if(jarFilePath == null || !(new File(jarFilePath).isFile())) {
			Log.e("No such file : " + jarFilePath);
			return;
		}
		SystemUtil.exec(new String[] {"java", "-jar", _RFile.BIN_JDGUI.get(), jarFilePath});
	}
}