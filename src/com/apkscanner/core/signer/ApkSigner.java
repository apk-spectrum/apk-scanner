package com.apkscanner.core.signer;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;

public class ApkSigner {

	public static String signApk(String pemFilePath, String pk8FilePath, String apkFilePath, String newSignedApkPath) {
		String[] result = ConsolCmd.exc(new String[] {"java", "-Dfile.encoding=utf-8", "-jar", Resource.BIN_SIGNAPK.getPath(), 
				pemFilePath, pk8FilePath, apkFilePath, newSignedApkPath}, true, null);
		//com.apkscanner.resource.Resource.PROP_PEM_FILE_PATH.setData("");
		StringBuilder errMessage = new StringBuilder();
		for(String line: result) {
			errMessage.append(line);
			errMessage.append("\n");
		}
		return errMessage.toString();
	}
}
