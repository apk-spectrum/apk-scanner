package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.Launcher;
import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.tabpanels.DefaultNodeData;
import com.apkspectrum.util.SystemUtil;

@SuppressWarnings("serial")
public class OpenResTreeFileAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_TREE_FILE";

	public OpenResTreeFileAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		final JTree resTree = (JTree) e.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
		if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof DefaultNodeData)) {
			return;
		}
		final DefaultNodeData resObj = (DefaultNodeData) node.getUserObject();
		if(resObj.isFolder() || resObj.getLoadingState()) return;

		final String resPath = uncompressRes(resObj);
		switch(resObj.getExtension()) {
		case ".dex":
			Component c;
			handler.sendEvent(c = new Component() {
				{
					handler.putData(Integer.toString(hashCode()), resPath);
				}

				@Override
				public void setEnabled(boolean enabled) {
					resObj.setLoadingState(!enabled);
					resTree.repaint();
				}
			}, UiEventHandler.ACT_CMD_OPEN_DECOMPILER);
			handler.putData(Integer.toString(c.hashCode()), null);
			break;
		case ".apk":
			Launcher.run(resPath);
			break;
		default:
			SystemUtil.openFile(resPath);
			break;
		}
	}
}
