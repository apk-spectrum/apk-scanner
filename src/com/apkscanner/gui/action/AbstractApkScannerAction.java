package com.apkscanner.gui.action;

import java.awt.Window;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;

@SuppressWarnings("serial")
public abstract class AbstractApkScannerAction extends AbstractUIAction
{
	public static final String APK_SCANNER_KEY = "APK_SCANNER_KEY";
	public static final String OWNER_WINDOW_KEY = "WINDOW_KEY";

	public AbstractApkScannerAction() { }

	public AbstractApkScannerAction(ActionEventHandler h) { super(h); }

	protected ApkScanner getApkScanner() {
		if(handler == null) return null;
		return (ApkScanner) handler.getData(APK_SCANNER_KEY);
	}

	protected ApkInfo getApkInfo() {
		ApkScanner scanner = getApkScanner();
		return scanner != null ? scanner.getApkInfo() : null;
	}

	protected Window getWindow() {
		if(handler == null) return null;
		return (Window) handler.getData(OWNER_WINDOW_KEY);
	}
}
