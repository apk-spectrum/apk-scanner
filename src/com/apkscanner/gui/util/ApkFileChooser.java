package com.apkscanner.gui.util;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;

import com.apkscanner.resource.Resource;

public class ApkFileChooser
{
	static private FileDialog getFileChooser(Component component, String openPath, int type, File defaultFile)
	{
//		JFileChooser jfc = new JFileChooser((String)Resource.PROP_LAST_FILE_OPEN_PATH.getData(""));
//		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		jfc.setDialogType(type);
//		jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Resource.STR_LABEL_APK_FILE_DESC.getString(),"apk"));
//		jfc.setSelectedFile(defaultFile);
		
		FileDialog jfc = new FileDialog((Frame) component, "탐색기", FileDialog.LOAD);
		
		jfc.setFilenameFilter(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".apk");
            }
        });
		
		return jfc;
	}
	
	static public File openApkFile(Component component)
	{
		FileDialog jfc = getFileChooser(component, (String)Resource.PROP_LAST_FILE_OPEN_PATH.getData(""), JFileChooser.OPEN_DIALOG, null);

		//if(jfc.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
		//	return null;
		jfc.setVisible(true);
		
		
		File dir = jfc.getFiles()[0];
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
//		JFileChooser jfc = getFileChooser((String)Resource.PROP_LAST_FILE_SAVE_PATH.getData(""), JFileChooser.SAVE_DIALOG, new File(defaultFilePath));
//
//		if(jfc.showSaveDialog(component) != JFileChooser.APPROVE_OPTION)
//			return null;
//
//		File dir = jfc.getSelectedFile();
//		if(dir != null) {
//			Resource.PROP_LAST_FILE_SAVE_PATH.setData(dir.getParentFile().getAbsolutePath());
//		}
//		return dir;
		return null;
	}
	
	static public String saveApkFilePath(Component component, String defaultFilePath)
	{
		File dir = saveApkFile(component, defaultFilePath);
		if(dir == null) return null;
		return dir.getPath();
	}
}
