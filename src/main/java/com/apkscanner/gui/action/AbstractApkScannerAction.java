package com.apkscanner.gui.action;

import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.AbstractUIAction;
import com.apkspectrum.swing.ActionEventHandler;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.ZipFileUtil;

@SuppressWarnings("serial")
public abstract class AbstractApkScannerAction extends AbstractUIAction
{
	public static final String APK_SCANNER_KEY = "APK_SCANNER_KEY";
	public static final String OWNER_WINDOW_KEY = "WINDOW_KEY";

	public AbstractApkScannerAction() { }

	public AbstractApkScannerAction(ApkActionEventHandler h) { super(h); }

	@Override
	public ApkActionEventHandler getHandler() {
		return (ApkActionEventHandler) super.getHandler();
	}

	@Override
	public void setHandler(ActionEventHandler h) {
		if(!(h instanceof ApkActionEventHandler)) {
			Log.w("The event handler must be type ApkActionEventHandler.");
			return;
		}
		super.setHandler(h);
	}

	protected ApkScanner getApkScanner() {
		if(handler == null) return null;
		return (ApkScanner) handler.getData(APK_SCANNER_KEY);
	}

	protected ApkInfo getApkInfo() {
		ApkScanner scanner = getApkScanner();
		return scanner != null ? scanner.getApkInfo() : null;
	}

	protected Window getWindow() {
		if(handler == null) return null;
		return (Window) handler.getData(OWNER_WINDOW_KEY);
	}

	protected String uncompressRes(TreeNodeData resObj) {
		return uncompressRes(resObj, null);
	}

	protected String uncompressRes(TreeNodeData resObj, String destPath) {
		if(resObj == null || resObj.isFolder()) return null;

		String path = resObj.getPath();
		if(resObj.getURI() != null
				&& "file".equals(resObj.getURI().getScheme())) {
			destPath = resObj.getPath();
		} else {
			ApkInfo apkInfo = getApkInfo();
			if(apkInfo == null) {
				Log.e("OpenResFileAction() apkInfo is null");
				return null;
			}

			if(destPath == null) {
				destPath = apkInfo.tempWorkPath + File.separator
						+ path.replace("/", File.separator);
			}
			File destFile = new File(destPath);
			if (!destFile.exists() && !destFile.getParentFile().exists()) {
				String newFolder = destFile.getParentFile().getAbsolutePath();
				if (FileUtil.makeFolder(newFolder)) {
					Log.i("sucess make folder : " + newFolder);
				}
			}

			boolean convAxml2Xml = false;
			String[] convStr = null;
			if (path.equals("AndroidManifest.xml")
					|| (path.startsWith("res/") && path.endsWith(".xml"))) {
				convStr = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath,
														new String[] { path });
				convAxml2Xml = RConst.AXML_VEIWER_TYPE_XML.equals(
												RProp.S.AXML_VIEWER_TYPE.get());
			} else if ("resources.arsc".equals(path)) {
				convStr = apkInfo.resourcesWithValue;
				destPath += ".txt";
			} else {
				ZipFileUtil.unZip(apkInfo.filePath, path, destPath);
			}

			if (convStr != null) {
				try (FileWriter fw = new FileWriter(new File(destPath))) {
					if(convAxml2Xml) {
						fw.write(apkInfo.a2xConvert.convertToText(convStr));
					} else {
						for (String s : convStr)
							fw.write(s + System.lineSeparator());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return destPath;
	}
}
