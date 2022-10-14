package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.SystemUtil;

@SuppressWarnings("serial")
public class OpenResFileArchiveExplorerAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_FILE_ARCHIVE";

	public OpenResFileArchiveExplorerAction(ApkActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent comp = (JComponent) e.getSource();
		TreeNodeData resObj = (TreeNodeData) comp.getClientProperty(TreeNodeData.class);

		if(resObj== null || resObj.isFolder()) return;

		String resPath = uncompressRes(resObj);
		SystemUtil.openArchiveExplorer(resPath);
	}
}
