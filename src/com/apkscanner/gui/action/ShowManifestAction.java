package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.component.ApkFileChooser;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

@SuppressWarnings("serial")
public class ShowManifestAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_SHOW_MANIFEST";

	public ShowManifestAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean withShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
		evtShowManifest(getWindow(e), withShift);
	}

	private void evtShowManifest(Window owner, boolean saveAs) {
		ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowManifest() apkInfo is null");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}

		try {
			String manifestPath = null;
			File manifestFile = null;
			if(!saveAs) {
				manifestPath = apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml";
				manifestFile = new File(manifestPath);
			} else {
				JFileChooser jfc = ApkFileChooser.getFileChooser(RProp.S.LAST_FILE_SAVE_PATH.get(), JFileChooser.SAVE_DIALOG, new File("AndroidManifest.xml"));
				if(jfc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;
				manifestFile = jfc.getSelectedFile();
				if(manifestFile == null) return;
				RProp.S.LAST_FILE_SAVE_PATH.set(manifestFile.getParentFile().getAbsolutePath());
				manifestPath = manifestFile.getAbsolutePath();
			}

			if(saveAs || !manifestFile.exists()) {
				if(!manifestFile.getParentFile().exists()) {
					if(FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
						Log.d("sucess make folder");
					}
				}

				String[] convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] {"AndroidManifest.xml"});
				FileWriter fw = new FileWriter(new File(manifestPath));
				fw.write(apkInfo.a2xConvert.convertToText(convStrings));
				fw.close();
			} else {
				Log.e("already existed file : " + manifestPath);
			}

			if(!saveAs) SystemUtil.openEditor(manifestPath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
