package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.component.ApkFileChooser;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
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
		ApkScanner scanner = getApkScanner();
		if(scanner == null) return;

		ApkInfo apkInfo = scanner.getApkInfo();
		if(apkInfo == null) {
			Log.e("evtShowManifest() apkInfo is null");
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
				AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
				a2x.setMultiLinePrint(RProp.B.PRINT_MULTILINE_ATTR.get());

				FileWriter fw = new FileWriter(new File(manifestPath));
				fw.write(a2x.toString());
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
