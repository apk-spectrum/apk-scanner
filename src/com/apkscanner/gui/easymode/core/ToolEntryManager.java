package com.apkscanner.gui.easymode.core;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkscanner.util.Log;

public class ToolEntryManager {

	static ArrayList<ToolEntry> ShowEntry = new ArrayList<ToolEntry>();
	static ArrayList<ToolEntry> hideEntry = new ArrayList<ToolEntry>();
	static public ApkScanner apkScanner;
	static public JFrame mainframe = null;

	static ArrayList<ToolEntry> allEntry;

	static ArrayList<ToolEntry> deviceEntry;


	public static MessageBoxPool messagePool;

	public static void initToolEntryManager(UiEventHandler eventHandler) {
		Log.i("start toolentryManager");
		allEntry = new ArrayList<ToolEntry>(Arrays.asList(
//				new ToolEntry(RStr.APP_NAME.get(), "Excute Original Scanner",
//						RImg.APP_ICON.getImageIcon(100, 100)),
				new ToolEntry(RComp.BTN_TOOLBAR_OPEN, UiEventHandler.ACT_CMD_OPEN_APK),
				new ToolEntry(RComp.BTN_TOOLBAR_OPEN_PACKAGE, UiEventHandler.ACT_CMD_OPEN_PACKAGE),
				new ToolEntry(RComp.BTN_TOOLBAR_MANIFEST, UiEventHandler.ACT_CMD_SHOW_MANIFEST),
				new ToolEntry(RComp.BTN_TOOLBAR_EXPLORER, UiEventHandler.ACT_CMD_SHOW_EXPLORER),
				new ToolEntry(RComp.BTN_TOOLBAR_INSTALL, UiEventHandler.ACT_CMD_INSTALL_APK),
				new ToolEntry(RComp.BTN_TOOLBAR_SIGN, UiEventHandler.ACT_CMD_SIGN_APK),
//				new ToolEntry(RStr.BTN_LAUNCH.get(), RStr.BTN_LAUNCH_LAB.get(),
//						RImg.TOOLBAR_LAUNCH.getImageIcon()),
//				new ToolEntry(RStr.BTN_DEL.get(), RStr.BTN_DEL.get(),
//						RImg.TOOLBAR_UNINSTALL.getImageIcon()),
//				new ToolEntry(RStr.MENU_CLEAR_DATA.get(), RStr.MENU_CLEAR_DATA.get(),
//						RImg.TOOLBAR_CLEAR.getImageIcon()),
//				new ToolEntry(RStr.BTN_DETAILS_INFO.get(), RStr.BTN_DETAILS_INFO.get(),
//						RImg.TOOLBAR_SEARCH.getImageIcon()),
				new ToolEntry(RComp.BTN_TOOLBAR_OPEN_CODE, UiEventHandler.ACT_CMD_OPEN_DECOMPILER),
				new ToolEntry(RComp.BTN_TOOLBAR_SETTING, UiEventHandler.ACT_CMD_OPEN_SETTINGS),
				new ToolEntry(RComp.BTN_TOOLBAR_ABOUT, UiEventHandler.ACT_CMD_SHOW_ABOUT)));
		Log.i("start deviceEntry toolEntry");
		deviceEntry = new ArrayList<ToolEntry>(Arrays.asList(
//				new ToolEntry(RStr.APP_NAME.get(), "Excute Original Scanner",
//				RImg.APP_ICON.getImageIcon(100, 100)),
		new ToolEntry(RComp.BTN_TOOLBAR_LAUNCH, UiEventHandler.ACT_CMD_LAUNCH_APP),
		new ToolEntry(RStr.BTN_DEL.get(), RStr.BTN_DEL.get(),
				RImg.TOOLBAR_UNINSTALL.getImageIcon(), UiEventHandler.ACT_CMD_UNINSTALL_APP),
		new ToolEntry(RStr.MENU_CLEAR_DATA.get(), RStr.MENU_CLEAR_DATA.get(),
				RImg.TOOLBAR_CLEAR.getImageIcon(), UiEventHandler.ACT_CMD_CLEAR_APP_DATA),
		new ToolEntry(RStr.BTN_DETAILS_INFO.get(), RStr.BTN_DETAILS_INFO.get(),
				RImg.TOOLBAR_SEARCH.getImageIcon(), UiEventHandler.ACT_CMD_SHOW_INSTALLED_PACKAGE_INFO)));

		Log.i("start MessageBoxPool");
		messagePool = new MessageBoxPool(mainframe);

		Log.i("start refreshToolManager");
		refreshToolManager();
	}

	public static ArrayList<ToolEntry> getDeviceToolbarList() {
		return deviceEntry;
	}

	public static ArrayList<ToolEntry> getAllToolbarList() {
		return allEntry;
	}

	public static ArrayList<ToolEntry> getShowToolbarList() {
		refreshToolManager();
		return ShowEntry;
	}

	public static ArrayList<ToolEntry> getHideToolbarList() {
		refreshToolManager();
		return hideEntry;
	}

	public static void refreshToolManager() {
		ShowEntry.clear();
		String[] toollist = RProp.S.EASY_GUI_TOOLBAR.get().split(",");

		for (String str : toollist) {
			if(str.length() !=0)
			ShowEntry.add(allEntry.get(Integer.parseInt(str)));
		}

		hideEntry.clear();
		for (ToolEntry entry : allEntry) {
			if (ShowEntry.indexOf(entry) == -1) {
				hideEntry.add(entry);
			}
		}
	}

	public static int findIndexFromAllEntry(ToolEntry obj) {
		for (int i = 0; i < allEntry.size(); i++) {
			ToolEntry entry = allEntry.get(i);
			if (obj.getTitle().equals(entry.getTitle())) {
				return i;
			}
		}
		return 0;
	}
}
