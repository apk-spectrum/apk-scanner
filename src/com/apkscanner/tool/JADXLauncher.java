package com.apkscanner.tool;

import java.io.File;

import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RStr;
import com.apkspectrum.swing.tabbedpaneui.MessageBoxPane;
import com.apkspectrum.util.ConsolCmd;
import com.apkspectrum.util.ConsolCmd.ConsoleOutputObserver;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

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
			MessageBoxPane.showWarring(null, RStr.MSG_WARN_UNSUPPORTED_JVM.get());
		}

		Thread t = new Thread(new Runnable() {
			public void run()
			{
				ConsolCmd.exec(new String[] {"java", "-version"}, true, observer);
				ConsolCmd.exec(new String[] {RFile.BIN_JADX_GUI.get(), jarFilePath}, true, observer);
				if(observer != null) {
					observer.ConsolOutput("JADXLauncher Completed");
				}
			}
		});
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}
}
