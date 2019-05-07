package com.apkscanner;

import javax.swing.JFrame;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.MainUI;
import com.apkscanner.resource.Resource;

public class UIController {
	public static final String APKSCANNER_GUI_APKSCANNER = "APKSCANNER";
	public static final String APKSCANNER_GUI_EASY_APKSCANNER = "EASY_APKSCANNER";
	
//	MainUI mainUI = null;
//	EasyMainUI easymainUI = null;
	static JFrame mainframe = null;
	
	public UIController() {

	}
	public UIController(ApkScanner apkScanner) {
		
		boolean isEasyGui = (boolean) Resource.PROP_USE_EASY_UI.getData();
		mainframe = new JFrame();
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
	public void show(ApkScanner scanner) {
//		if(obj instanceof MainUI) {
//			mainUI = (MainUI)obj;
//		} else if(obj instanceof EasyMainUI){
//			easymainUI = (EasyMainUI) obj;
//		}
	}
	
	public static void changeGui(String state, ApkScanner apkScanner) {
		mainframe.getContentPane().removeAll();
		if(state.equals(APKSCANNER_GUI_APKSCANNER)) {
			new MainUI(apkScanner, mainframe);
			mainframe.setVisible(true);	
		}
	}
	
	private void restart(ApkScanner apkScanner) {
		if(apkScanner.getApkInfo() != null) {
			Launcher.run(apkScanner.getApkInfo().filePath);
		} else {
			Launcher.run();
		}
		mainframe.dispose();
	}
}
