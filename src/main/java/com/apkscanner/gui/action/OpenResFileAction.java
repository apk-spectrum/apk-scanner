package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import com.apkscanner.gui.tabpanels.DefaultNodeData;
import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.SystemUtil;

public class OpenResFileAction extends AbstractApkScannerAction
{
	private static final long serialVersionUID = -4837703825343870483L;

	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_FILE";

	public OpenResFileAction(ApkActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		final JComponent comp = (JComponent) e.getSource();
		final DefaultNodeData resObj = (DefaultNodeData) comp.getClientProperty(TreeNodeData.class);
		if(resObj.isFolder() || resObj.getLoadingState()) return;

		String resPath = uncompressRes(resObj);

		SystemUtil.openFile(resPath);
	}
}
