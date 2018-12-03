package com.apkscanner.gui.easymode.core;

import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.easymode.EasyGuiMain;
import com.apkscanner.gui.easymode.EasyLightApkScanner;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class ToolEntryManager {
	
	static ArrayList<ToolEntry> ShowEntry = new ArrayList<ToolEntry>();
	static ArrayList<ToolEntry> hideEntry  = new ArrayList<ToolEntry>();
	static public EasyLightApkScanner Apkscanner;
	static public JFrame mainframe = null;	
	
	static ArrayList<ToolEntry> allEntry;

	
	public ToolEntryManager() {
		
	}
	public static void initToolEntryManager() {
		allEntry = new ArrayList<ToolEntry>(
				Arrays.asList(new ToolEntry(Resource.STR_APP_NAME.getString(), "Start Scanner", Resource.IMG_APP_ICON.getImageIcon(100, 100)),
						new ToolEntry(Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString(), Resource.IMG_TOOLBAR_OPEN.getImageIcon()), 
						new ToolEntry(Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.STR_BTN_OPEN_PACKAGE_LAB.getString(), Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString(), Resource.IMG_TOOLBAR_MANIFEST.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_SIGN.getString(), Resource.STR_BTN_SIGN_LAB.getString(), Resource.IMG_TOOLBAR_SIGNNING.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_DEL.getString(), Resource.STR_BTN_DEL.getString(), Resource.IMG_TOOLBAR_UNINSTALL.getImageIcon()),
						new ToolEntry(Resource.STR_MENU_CLEAR_DATA.getString(), Resource.STR_MENU_CLEAR_DATA.getString(), Resource.IMG_TOOLBAR_CLEAR.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_DETAILS_INFO.getString(), Resource.STR_BTN_DETAILS_INFO.getString(), Resource.IMG_TOOLBAR_SEARCH.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENING_CODE_LAB.getString(), Resource.IMG_TOOLBAR_OPENCODE.getImageIcon()),
						new ToolEntry(Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString(), Resource.IMG_TOOLBAR_ABOUT.getImageIcon())));
		refreshToolManager();
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
	
	public  static void refreshToolManager() {
		ShowEntry.clear();		
		String[] toollist = Resource.PROP_EASY_GUI_TOOLBAR.getData().toString().split(",");
		for(String str: toollist) {
			ShowEntry.add(allEntry.get(Integer.parseInt(str)));
		}
		
		hideEntry.clear();
		for(ToolEntry entry: allEntry) {
			if(ShowEntry.indexOf(entry) == -1) {
				hideEntry.add(entry);
			}
		}
	}
	
	public static void excuteEntry(String cmd) {
		Log.d("Tool Click - " + cmd);
		if(cmd.equals(Resource.STR_BTN_OPEN.getString())) {
			final String apkFilePath = ApkFileChooser.openApkFilePath(EasyGuiMain.frame);
			if(apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}		
			Launcher.run(apkFilePath);
		} else if(cmd.equals(Resource.STR_APP_NAME.getString())) {
			
			Launcher.run(Apkscanner.getApkInfo().filePath, false);
			
		} else if(cmd.equals(Resource.STR_BTN_OPEN_PACKAGE.getString())) {			
			PackageTreeDlg Dlg = new PackageTreeDlg(mainframe);
			if(Dlg.showTreeDlg() != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}
			final String device = Dlg.getSelectedDevice();
			final String apkFilePath = Dlg.getSelectedApkPath();
			final String frameworkRes = Dlg.getSelectedFrameworkRes();
			Launcher.run(device, apkFilePath, frameworkRes);			
		} else if(cmd.equals(Resource.STR_BTN_MANIFEST.getString())) {
			ApkInfo apkInfo = Apkscanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowManifest() apkInfo is null");
				return;
			}
			String manifestPath = null;
			File manifestFile = null;
			
			manifestPath = apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml";
			manifestFile = new File(manifestPath);
			
			if(!manifestFile.exists()) {
				if(!manifestFile.getParentFile().exists()) {
					if(FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
						Log.d("sucess make folder");
					}
				}
				String[] convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] {"AndroidManifest.xml"});
				AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
				a2x.setMultiLinePrint((boolean)Resource.PROP_PRINT_MULTILINE_ATTR.getData());

				FileWriter fw;
				try {
					fw = new FileWriter(new File(manifestPath));
					fw.write(a2x.toString());
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Log.e("already existed file : " + manifestPath);
			}
			
			SystemUtil.openEditor(manifestPath);	
			
		} else if(cmd.equals(Resource.STR_BTN_EXPLORER.getString())) {
			ApkInfo apkInfo = Apkscanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				return;
			}
			SystemUtil.openArchiveExplorer(apkInfo.filePath);
		} else if(cmd.equals(Resource.STR_BTN_INSTALL.getString())) {
			ApkInfo apkInfo = Apkscanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				return;
			}
			ApkInstallWizard wizard = new ApkInstallWizard(apkInfo.filePath, mainframe);
			wizard.start();
		} else if(cmd.equals(Resource.STR_BTN_SIGN.getString())) {
			
		} else if(cmd.equals(Resource.STR_BTN_LAUNCH.getString())) {
			
		} else if(cmd.equals(Resource.STR_BTN_DEL.getString())) {
			
		} else if(cmd.equals(Resource.STR_MENU_CLEAR_DATA.getString())) {
			
		} else if(cmd.equals(Resource.STR_BTN_DETAILS_INFO.getString())) {
			
		} else if(cmd.equals(Resource.STR_BTN_OPENCODE.getString())) {
			
		} else if(cmd.equals(Resource.STR_BTN_ABOUT.getString())) {
			
		}
	}
	
	public void excuteEntry(ToolEntry entry) {		
		excuteEntry(entry.getTitle());		
	}

	public static int findIndexFromAllEntry(ToolEntry obj) {
		// TODO Auto-generated method stub
		for(int i=0;i < allEntry.size(); i++) {
			ToolEntry entry = allEntry.get(i);
			if(obj.title.equals(entry.title)) {
				return i;
			}
		}
		return 0;
	}
}
