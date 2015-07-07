package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;

public class CoreCertTool {
	
	public static ArrayList<String> CertList = new ArrayList<String>();
	public static String CertSummary = "";
	
	public static ArrayList<String> solveCert(String CertPath) {
		Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
		String keytoolPackage;
		if(javaVersion >= 1.8) {
			keytoolPackage = "sun.security.tools.keytool.Main";
		} else {
			keytoolPackage = "sun.security.tools.KeyTool";
		}

		CertList.clear();
		if(!(new File(CertPath)).exists()) {
			System.out.println("META-INFO 폴더가 존재 하지 않습니다 :");
			return CertList;
		}
		
		for (String s : (new File(CertPath)).list()) {
			if(!s.matches(".*\\.RSA") && !s.matches(".*\\.DSA") ) continue;

			File rsaFile = new File(CertPath + s);
			if(!rsaFile.exists()) continue;

			String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()};
			String[] result = MyConsolCmd.exc(cmd, false, null);

		    String certContent = "";
		    CertSummary = "<certificate[1]>\n";
		    for(int i=0; i < result.length; i++){
	    		if(!certContent.isEmpty() && result[i].matches("^.*\\[[0-9]*\\]:$")){
    				CertList.add(certContent);
    				CertSummary += "<certificate[" + (CertList.size() + 1) + "]>\n";
			    	certContent = "";
	    		}
	    		if(result[i].matches("^.*:( [^ ,]+=(\".*\")?[^,]*,?)+$")) {
	    			CertSummary += result[i] + "\n";
	    		}
	    		certContent += (certContent.isEmpty() ? "" : "\n") + result[i];
		    }
			CertList.add(certContent);
		}
		return CertList;
	}
	
	public static String getCertSummary() {
		return CertSummary;
	}
}
