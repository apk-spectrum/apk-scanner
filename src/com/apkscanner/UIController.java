package com.apkscanner;

import java.awt.EventQueue;

import javax.swing.JFrame;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.MainUI;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class UIController {
	public static final String APKSCANNER_GUI_APKSCANNER = "APKSCANNER";
	public static final String APKSCANNER_GUI_EASY_APKSCANNER = "EASY_APKSCANNER";

//	MainUI mainUI = null;
//	EasyMainUI easymainUI = null;
	static JFrame mainframe = null;

	public static void createAndShowGUI(ApkScanner apkScanner) {
		Log.i("start UIController");
		boolean isEasyGui = (boolean) Resource.PROP_USE_EASY_UI.getData();
		mainframe = new JFrame();
		Log.i("creat frame");
		if(	isEasyGui) {
			new EasyMainUI(apkScanner, mainframe);
		} else {
			new MainUI(apkScanner, mainframe);
		}

		mainframe.setVisible(true);

		if(!(boolean) Resource.PROP_SKIP_STARTUP_EASY_UI_DLG.getData()) {
			if(EasyMainUI.showDlgStartupEasyMode(mainframe)) {
				restart(apkScanner);
			}
		}
	}

	public static void changeGui(String state, ApkScanner apkScanner) {
		mainframe.getContentPane().removeAll();
		if(state.equals(APKSCANNER_GUI_APKSCANNER)) {
			if(apkScanner == null) {
				apkScanner = ApkScanner.getInstance(ApkScanner.APKSCANNER_TYPE_AAPT);
			} else if(!ApkScanner.APKSCANNER_TYPE_AAPT.equals(apkScanner.getScannerType())) {
				final String apkPath = apkScanner.getApkInfo() != null ? apkScanner.getApkInfo().filePath : null;
				apkScanner = ApkScanner.getInstance(ApkScanner.APKSCANNER_TYPE_AAPT);
				final ApkScanner scanner = apkScanner;
				if(apkPath != null) {
					Thread thread = new Thread(new Runnable() {
						public void run() {
							scanner.openApk(apkPath);
						}
					});
					thread.setPriority(Thread.NORM_PRIORITY);
					thread.start();
				}
			}
			final ApkScanner scanner = apkScanner;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new MainUI(scanner, mainframe);
					mainframe.setVisible(true);
				}
			});
		}
	}

	private static void restart(ApkScanner apkScanner) {
		if(apkScanner.getApkInfo() != null) {
			Launcher.run(apkScanner.getApkInfo().filePath);
		} else {
			Launcher.run();
		}
		mainframe.dispose();
	}
}
