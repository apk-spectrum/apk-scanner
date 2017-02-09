package com.apkscanner.gui.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

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

		if(selFile.exists()) {
			if(!selFile.canWrite()) {
				Log.e("Can't wirte file : " + selFile.getPath());
				JOptionPane.showMessageDialog(jfc, Resource.STR_MSG_CANNOT_WRITE_FILE.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE);
				return null;
			}
			int ret = JOptionPane.showConfirmDialog(jfc, Resource.STR_QUESTION_SAVE_OVERWRITE.getString(), Resource.STR_LABEL_QUESTION.getString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(ret != JOptionPane.YES_OPTION) {
				return null;
			}
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
