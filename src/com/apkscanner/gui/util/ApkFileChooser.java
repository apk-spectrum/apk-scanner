package com.apkscanner.gui.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import com.apkscanner.resource.Resource;

public class ApkFileChooser
{
	static public JFileChooser getFileChooser(String openPath, int type, File defaultFile)
	{
		JFileChooser jfc = new JFileChooser(openPath);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setDialogType(type);
		jfc.setSelectedFile(defaultFile);
		
		return jfc;
	}
	
	static public File openApkFile(Component component)
	{
		JFileChooser jfc = getFileChooser((String)Resource.PROP_LAST_FILE_OPEN_PATH.getData(""), JFileChooser.OPEN_DIALOG, null);
		jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Resource.STR_LABEL_APK_FILE_DESC.getString(),"apk"));

		if(jfc.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
			return null;
		
		File dir = jfc.getSelectedFile();
		if(dir != null) {
			Resource.PROP_LAST_FILE_OPEN_PATH.setData(dir.getParentFile().getAbsolutePath());
		}
		return dir;
	}
	
	static public String openApkFilePath(Component component)
	{
		File dir = openApkFile(component);
		if(dir == null) return null;
		return dir.getPath();
	}
	
	static public File saveApkFile(Component component, String defaultFilePath)
	{
		if(!defaultFilePath.endsWith(".apk")) {
			defaultFilePath += ".apk"; 
		}

		JFileChooser jfc = getFileChooser((String)Resource.PROP_LAST_FILE_SAVE_PATH.getData(""), JFileChooser.SAVE_DIALOG, new File(defaultFilePath));
		jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Resource.STR_LABEL_APK_FILE_DESC.getString(),"apk"));

		if(jfc.showSaveDialog(component) != JFileChooser.APPROVE_OPTION)
			return null;

		File selFile = jfc.getSelectedFile();
		if (jfc.getFileFilter() != jfc.getAcceptAllFileFilter() && !selFile.getPath().endsWith(".apk")) {
			selFile = new File(jfc.getSelectedFile().getPath() + ".apk");
        }

		if(selFile != null) {
			Resource.PROP_LAST_FILE_SAVE_PATH.setData(selFile.getParentFile().getAbsolutePath());
		}
		return selFile;
	}
	
	static public String saveApkFilePath(Component component, String defaultFilePath)
	{
		File dir = saveApkFile(component, defaultFilePath);
		if(dir == null) return null;
		return dir.getPath();
	}
}
