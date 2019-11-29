package com.apkscanner.tool.external;

import java.io.File;

import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.RFile;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ConsolCmd.ConsoleOutputObserver;

public class JADXLauncher
{
	static public void run(final String jarFilePath) {
		run(jarFilePath, null);
	}

	static public void run(final String jarFilePath, final ConsoleOutputObserver observer) {
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
				ConsolCmd.exec(new String[] {"java", "-version"}, true, observer);
				ConsolCmd.exec(new String[] {RFile.BIN_JADX_GUI.get(), jarFilePath}, true, observer);
			}
		});
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}
}
