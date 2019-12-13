package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.Launcher;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.tabpanels.ResourceObject;
import com.apkscanner.gui.tabpanels.ResourceType;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

@SuppressWarnings("serial")
public class OpenResTreeFileAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_RESOURCE_FILE";

	public OpenResTreeFileAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		final JTree resTree = (JTree) e.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
		if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof ResourceObject)) {
			return;
		}
		final ResourceObject resObj = (ResourceObject) node.getUserObject();
		if(resObj.isFolder) return;

		final String resPath = uncompressRes(resObj);
		if (resPath.toLowerCase().endsWith(".dex")) {
			handler.sendEvent(new Component() {
				{
					handler.putData(Integer.toString(hashCode()), resPath);
				}

				@Override
				public void setEnabled(boolean enabled) {
					resObj.setLoadingState(!enabled);
					resTree.repaint();
				}
			}, UiEventHandler.ACT_CMD_OPEN_DECOMPILER);
		} else if (resObj.path.toLowerCase().endsWith(".apk")) {
			Launcher.run(resPath);
		} else {
			SystemUtil.openFile(resPath);
		}
	}

	private String uncompressRes(ResourceObject resObj) {
		if(resObj.isFolder) return null;

		String resPath = null;
		if(resObj.type == ResourceType.LOCAL) {
			resPath = resObj.path;
		} else {
			ApkInfo apkInfo = getApkInfo();
			if(apkInfo == null) {
				Log.e("OpenResFileAction() apkInfo is null");
				return null;
			}

			resPath = apkInfo.tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
			File resFile = new File(resPath);
			if (!resFile.exists() && !resFile.getParentFile().exists()) {
				if (FileUtil.makeFolder(resFile.getParentFile().getAbsolutePath())) {
					Log.i("sucess make folder : " + resFile.getParentFile().getAbsolutePath());
				}
			}

			String[] convStrings = null;
			if (resObj.attr == ResourceObject.ATTR_AXML) {
				convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { resObj.path });
			} else if ("resources.arsc".equals(resObj.path)) {
				convStrings = apkInfo.resourcesWithValue;
				resPath += ".txt";
			} else {
				ZipFileUtil.unZip(apkInfo.filePath, resObj.path, resPath);
			}

			if (convStrings != null) {
				StringBuilder sb = new StringBuilder();
				for (String s : convStrings)
					sb.append(s + "\n");
				try (FileWriter fw = new FileWriter(new File(resPath))) {
					fw.write(sb.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resPath;
	}
}
