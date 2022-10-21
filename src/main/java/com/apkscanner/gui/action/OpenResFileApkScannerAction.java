package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import com.apkscanner.Launcher;
import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkspectrum.swing.ApkActionEventHandler;

public class OpenResFileApkScannerAction extends AbstractApkScannerAction
{
	private static final long serialVersionUID = -7557580374362000167L;

	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_FILE_APK_SCANNER";

	public OpenResFileApkScannerAction(ApkActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent comp = (JComponent) e.getSource();
		TreeNodeData resObj = (TreeNodeData) comp.getClientProperty(TreeNodeData.class);

		if(resObj== null || resObj.isFolder()) return;

		String resPath = uncompressRes(resObj);
		Launcher.run(resPath);
	}
}
