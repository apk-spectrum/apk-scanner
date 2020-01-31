package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;

import com.apkscanner.gui.tabpanels.ResourceNode;
import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.tool.ImgExtractorWrapper;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.ZipFileUtil;

@SuppressWarnings("serial")
public class LoadResTreeImgFileAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_LOAD_FS_IMG_FILE";

	public LoadResTreeImgFileAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		TreeExpansionEvent orgEvent = (TreeExpansionEvent) e.getSource();

		JTree resTree = (JTree) orgEvent.getSource();
		ResourceNode node = (ResourceNode) orgEvent.getPath().getLastPathComponent();

		loadFsImg(resTree, node);
	}

	private void loadFsImg(final JTree resTree, final ResourceNode node) {
		final ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) {
			Log.e("LoadResTreeImgFileAction() apkInfo is null");
			return;
		}

		if (node.getUserObject() instanceof TreeNodeData) {
			TreeNodeData resObj = (TreeNodeData) node.getUserObject();
			if(".img".equals(resObj.getExtension())) {
				new SwingWorker<String, Void>() {
					@Override
					protected String doInBackground() throws Exception {
						TreeNodeData resObj = (TreeNodeData) node.getUserObject();
						String imgPath = apkInfo.tempWorkPath + File.separator + resObj.getPath().replace("/", File.separator);
						String extPath = imgPath + "_";
						if(!new File(imgPath).exists()) {
							ZipFileUtil.unZip(apkInfo.filePath, resObj.getPath(), imgPath);
						}
						if(SystemUtil.isWindows()) {
							ImgExtractorWrapper.extracte(imgPath, extPath);
						} else {

						}
						return extPath;
					}

					@Override
					protected void done() {
						String topPath = null;
						try {
							topPath = get();
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
						if(topPath == null || topPath.isEmpty()) return;
						File root = new File(topPath);
						if(!root.exists() || !root.isDirectory()) return;

						node.removeAllChildren();
						node.add(root);
						resTree.updateUI();
					}
				}.execute();
			}
		}
	}
}
