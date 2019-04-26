package com.apkscanner;

import javax.swing.JFrame;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.easymode.EasyMainUI;

public class UIController extends JFrame{
	public static final String APKSCANNER_GUI_APKSCANNER = "APKSCANNER";
	public static final String APKSCANNER_GUI_EASY_APKSCANNER = "EASY_APKSCANNER";
	
	MainUI mainUI = null;
	EasyMainUI easymainUI = null;
	public UIController() {
		
	}
	public void show(ApkScanner scanner) {
//		if(obj instanceof MainUI) {
//			mainUI = (MainUI)obj;
//		} else if(obj instanceof EasyMainUI){
//			easymainUI = (EasyMainUI) obj;
//		}
	}	
}
