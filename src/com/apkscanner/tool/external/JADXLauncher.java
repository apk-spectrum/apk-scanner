package com.apkscanner.tool.external;

import java.io.File;

import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class JADXLauncher {
	static public void run(final String jarFilePath)
	{
		if(jarFilePath == null || !(new File(jarFilePath).isFile())) {
			Log.e("No such file : " + jarFilePath);
			return;
		}

		if(!SystemUtil.checkJvmVersion("1.8")) {
			MessageBoxPool.show(null, MessageBoxPool.MSG_WARN_UNSUPPORTED_JVM, "");
		}
		
		Thread t = new Thread(new Runnable() {
			public void run()
			{
				ConsolCmd.exc(new String[] {"java", "-version"}, true);
				ConsolCmd.exc(new String[] {Resource.BIN_JADX_GUI.getPath(), jarFilePath}, true);
			}
		});
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}
}
