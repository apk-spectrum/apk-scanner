package com.apkscanner.gui.action;

import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.tabpanels.ResourceObject;
import com.apkscanner.gui.tabpanels.ResourceType;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

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

	protected String uncompressRes(ResourceObject resObj) {
		return uncompressRes(resObj, null);
	}

	protected String uncompressRes(ResourceObject resObj, String destPath) {
		if(resObj == null || resObj.isFolder) return null;

		if(resObj.type == ResourceType.LOCAL) {
			destPath = resObj.path;
		} else {
			ApkInfo apkInfo = getApkInfo();
			if(apkInfo == null) {
				Log.e("OpenResFileAction() apkInfo is null");
				return null;
			}

			if(destPath == null) {
				destPath = apkInfo.tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
			}
			File destFile = new File(destPath);
			if (!destFile.exists() && !destFile.getParentFile().exists()) {
				if (FileUtil.makeFolder(destFile.getParentFile().getAbsolutePath())) {
					Log.i("sucess make folder : " + destFile.getParentFile().getAbsolutePath());
				}
			}

			boolean convAxml2Xml = false;
			String[] convStrings = null;
			if (resObj.attr == ResourceObject.ATTR_AXML) {
				convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] { resObj.path });
				convAxml2Xml = RConst.AXML_VEIWER_TYPE_XML.equals(RProp.S.AXML_VIEWER_TYPE.get());
			} else if ("resources.arsc".equals(resObj.path)) {
				convStrings = apkInfo.resourcesWithValue;
				destPath += ".txt";
			} else {
				ZipFileUtil.unZip(apkInfo.filePath, resObj.path, destPath);
			}

			if (convStrings != null) {
				try (FileWriter fw = new FileWriter(new File(destPath))) {
					if(convAxml2Xml) {
						AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
						a2x.setMultiLinePrint(RProp.B.PRINT_MULTILINE_ATTR.get());
						fw.write(a2x.toString());
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
