package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CoreCertTool {
	
	public static ArrayList<Object[]> CertList = new ArrayList<Object[]>();
	public static String CertSummary = "";
	
	public static ArrayList<Object[]> solveCert(String CertPath) {

		if(!(new File(CertPath)).exists()) {
			System.out.println("META-INFO 폴더가 존재 하지 않습니다 :");
			return CertList;
		}
		
		for (String s : (new File(CertPath)).list()) {
			if(!s.matches(".*\\.RSA")) continue;

			File rsaFile = new File(CertPath + s);
			if(!rsaFile.exists()) continue;

			String[] cmd = {"java","-Dfile.encoding=utf8","sun.security.tools.KeyTool","-printcert","-v","-file", rsaFile.getAbsolutePath()};
			exc(cmd);
		}

		//String[] cmd = {"java","-Dfile.encoding=utf8","sun.security.tools.KeyTool","-printcert","-v","-file", CertFilePath};
		//String[] cmd2 = {"java","-Dfile.encoding=utf8","sun.security.tools.KeyTool","-printcert","-rfc","-file", CertFilePath};
		//exc(cmd);
		//exc(cmd2);
		return CertList;
	}
	
	public static String getCertSummary() {
		return CertSummary;
	}
	
	static String exc(String[] cmd) {
		try {
			String s = "";
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			
			String buffer = "";
		    BufferedReader stdOut = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    
		    String certContent = "";
		    CertSummary = "<certificate[1]>\n";
		    int certCnt = 0;
		    while ((s = stdOut.readLine()) != null) {
	    		if(!certContent.isEmpty() && s.matches("^.*\\[[0-9]*\\]:$")){
	    			if(cmd[4].equals("-v")) {
	    				CertList.add(new Object[] { "Certificate[" + (CertList.size() + 1) + "]", certContent });
	    				CertSummary += "<certificate[" + (CertList.size() + 1) + "]>\n";
				    	certContent = "";
	    			} else {
	    				CertList.get(certCnt++)[1] += "\n\n[RFC output]\n" + certContent;
				    	certContent = "";
				    	continue;
	    			}
	    		}
	    		if(s.matches("^.*: CN=.*$")) {
	    			CertSummary += s + "\n";
	    		}
	    		certContent += (certContent.isEmpty() ? "" : "\n") + s;
		    	buffer += s;
		    }
			if(cmd[4].equals("-v")) {
				CertList.add(new Object[] { "Certificate[" + (CertList.size() + 1) + "]", certContent });
			} else {
				CertList.get(certCnt++)[1] += "\n\n[RFC]\n" + certContent;
			}
			

		    return buffer;
		} catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());
		      //System.exit(-1);
	    }
		return null;
		
	}
}
