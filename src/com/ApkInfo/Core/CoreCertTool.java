package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CoreCertTool {
	
	public static void solveCert(String CertFilePath) {

		if(!(new File(CertFilePath)).exists()) {
			System.out.println("CERT.RSA 파일이 존재 하지 않습니다 :");
			return;
		}
		
		String[] cmd = {"java","-Dfile.encoding=utf8","sun.security.tools.KeyTool","-printcert","-v","-file", CertFilePath};
		
		exc(cmd);
	}
	
	
	static String exc(String[] cmd) {
		try {
			String s = "";
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			
			String buffer = "";
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    
		    while ((s = stdOut.readLine()) != null) {
		    	System.out.println(s);
		    	buffer += s;
		    }
		    return buffer;
		} catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());
		      //System.exit(-1);
	    }
		return null;
		
	}
}
