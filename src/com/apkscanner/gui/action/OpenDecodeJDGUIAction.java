package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.tool.external.Dex2JarWrapper;
import com.apkscanner.tool.external.JDGuiLauncher;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

@SuppressWarnings("serial")
public class OpenDecodeJDGUIAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER_JDGUI";

	public OpenDecodeJDGUIAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenDecompiler(getWindow(e));
	}

	private void evtOpenDecompiler(final Window owner) {
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null || apkInfo.filePath == null
				|| !new File(apkInfo.filePath).exists()) {
			Log.e("evtOpenJDGUI() apkInfo is null");
			return;
		}

		if(!ZipFileUtil.exists(apkInfo.filePath, "classes.dex")) {
			Log.e("No such file : classes.dex");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_CLASSES_DEX);
			return;
		}

		//toolBar.setEnabledAt(ButtonSet.OPEN_CODE, false);
		String jarfileName = apkInfo.tempWorkPath + File.separator + (new File(apkInfo.filePath)).getName().replaceAll("\\.apk$", ".jar");
		Dex2JarWrapper.convert(apkInfo.filePath, jarfileName, new Dex2JarWrapper.DexWrapperListener() {
			@Override
			public void onCompleted() {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						//toolBar.setEnabledAt(ButtonSet.OPEN_CODE, true);
					}
				});
			}

			@Override
			public void onError(final String message) {
				Log.e("Failure: Fail Dex2Jar : " + message);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						MessageBoxPool.show(owner, MessageBoxPool.MSG_FAILURE_DEX2JAR, message);
					}
				});
			}

			@Override
			public void onSuccess(String jarFilePath) {
				JDGuiLauncher.run(jarFilePath);
			}
		});
	}
}