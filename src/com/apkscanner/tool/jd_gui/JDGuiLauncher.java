package com.apkscanner.tool.jd_gui;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;

public class JDGuiLauncher {
	static public boolean run(String jarFilePath)
	{
		boolean successed = true;
		String[] cmdLog = ConsolCmd.exc(new String[] {"java", "-jar", Resource.BIN_JDGUI.getPath(), jarFilePath});
		for(String s: cmdLog) {
			if(s.indexOf("err") > -1) {
				successed = false;
			}
		}
		if(!successed) {
			for(String s: cmdLog) {
				Log.e(s);
			}
		}
		return successed;
	}
}
