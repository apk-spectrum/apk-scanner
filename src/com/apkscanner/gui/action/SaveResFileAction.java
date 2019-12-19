package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import com.apkscanner.gui.component.ApkFileChooser;
import com.apkscanner.gui.tabpanels.ResourceObject;
import com.apkscanner.gui.tabpanels.ResourceType;
import com.apkscanner.resource.RProp;
import com.apkscanner.util.FileUtil;

@SuppressWarnings("serial")
public class SaveResFileAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SAVE_RESOURCE_FILE";

	public SaveResFileAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		final JComponent comp = (JComponent) e.getSource();
		final ResourceObject resObj = (ResourceObject) comp.getClientProperty(ResourceObject.class);
		if(resObj.isFolder || resObj.getLoadingState()) return;

		File destFile = getSaveFile(null, resObj.path.replace("/", File.separator));
		if(destFile == null) return;
		String destPath = destFile.getAbsolutePath();

		if(resObj.type == ResourceType.LOCAL) {
			destPath = resObj.path;
			FileUtil.copy(resObj.path, destPath);
		} else {
			destPath = uncompressRes(resObj, destPath);
		}
	}

	public File getSaveFile(Component component, String defaultFilePath) {
		JFileChooser jfc = ApkFileChooser.getFileChooser(RProp.S.LAST_FILE_SAVE_PATH.get(), JFileChooser.SAVE_DIALOG, new File(defaultFilePath));
		//jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(RStr.LABEL_APK_FILE_DESC.get(),"apk"));

		if(jfc.showSaveDialog(component) != JFileChooser.APPROVE_OPTION)
			return null;

		File dir = jfc.getSelectedFile();
		if(dir != null) {
			RProp.S.LAST_FILE_SAVE_PATH.set(dir.getParentFile().getAbsolutePath());
		}
		return dir;
	}
}
