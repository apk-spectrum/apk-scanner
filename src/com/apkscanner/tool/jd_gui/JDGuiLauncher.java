package com.apkscanner.tool.jd_gui;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;

public class JDGuiLauncher {
	static public void run(String jarFilePath)
	{
		ConsolCmd.exc(new String[] {"java", "-jar", Resource.BIN_JDGUI.getPath(), jarFilePath});
	}
}
