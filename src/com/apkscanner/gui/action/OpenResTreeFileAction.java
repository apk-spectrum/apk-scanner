package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.Launcher;
import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.tabpanels.ResourceObject;
import com.apkscanner.util.SystemUtil;

@SuppressWarnings("serial")
public class OpenResTreeFileAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_TREE_FILE";

	public OpenResTreeFileAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		final JTree resTree = (JTree) e.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
		if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof ResourceObject)) {
			return;
		}
		final ResourceObject resObj = (ResourceObject) node.getUserObject();
		if(resObj.isFolder || resObj.getLoadingState()) return;

		final String resPath = uncompressRes(resObj);
		if (resPath.toLowerCase().endsWith(".dex")) {
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
		} else if (resObj.path.toLowerCase().endsWith(".apk")) {
			Launcher.run(resPath);
		} else {
			SystemUtil.openFile(resPath);
		}
	}
}
