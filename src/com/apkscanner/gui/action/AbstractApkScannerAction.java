package com.apkscanner.gui.action;

import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.ZipFileUtil;

@SuppressWarnings("serial")
public abstract class AbstractApkScannerAction extends AbstractUIAction
{
	public static final String APK_SCANNER_KEY = "APK_SCANNER_KEY";
	public static final String OWNER_WINDOW_KEY = "WINDOW_KEY";

	public AbstractApkScannerAction() { }

	public AbstractApkScannerAction(ActionEventHandler h) { super(h); }

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
				destPath = apkInfo.tempWorkPath + File.separator + path.replace("/", File.separator);
			}
			File destFile = new File(destPath);
			if (!destFile.exists() && !destFile.getParentFile().exists()) {
				if (FileUtil.makeFolder(destFile.getParentFile().getAbsolutePath())) {
					Log.i("sucess make folder : " + destFile.getParentFile().getAbsolutePath());
				}
			}

			boolean convAxml2Xml = false;
			String[] convStrings = null;
			if (path.equals("AndroidManifest.xml")
					|| path.startsWith("res/")) {
				convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { path });
				convAxml2Xml = RConst.AXML_VEIWER_TYPE_XML.equals(RProp.S.AXML_VIEWER_TYPE.get());
			} else if ("resources.arsc".equals(path)) {
				convStrings = apkInfo.resourcesWithValue;
				destPath += ".txt";
			} else {
				ZipFileUtil.unZip(apkInfo.filePath, path, destPath);
			}

			if (convStrings != null) {
				try (FileWriter fw = new FileWriter(new File(destPath))) {
					if(convAxml2Xml) {
						fw.write(apkInfo.a2xConvert.convertToText(convStrings));
					} else {
						for (String s : convStrings)
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
