package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import com.apkscanner.gui.tabpanels.DefaultNodeData;
import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkspectrum.util.SystemUtil;

@SuppressWarnings("serial")
public class OpenResFileAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_FILE";

	public OpenResFileAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		final JComponent comp = (JComponent) e.getSource();
		final DefaultNodeData resObj = (DefaultNodeData) comp.getClientProperty(TreeNodeData.class);
		if(resObj.isFolder() || resObj.getLoadingState()) return;

		String resPath = uncompressRes(resObj);

		SystemUtil.openFile(resPath);
	}
}
