package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CoreCertTool {
	
	public static void solveCert(String CertFilePath) {

		if(!(new File(CertFilePath)).exists()) {
			System.out.println("CERT.RSA 파일이 존재 하지 않습니다 :");
			return;
		}
		
		String keyToolPath = CoreCertTool.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		keyToolPath = (new File(keyToolPath)).getParentFile().getPath();
		keyToolPath += File.separator + "tool" + File.separator + "keytool";
		try {
			keyToolPath = URLDecoder.decode(keyToolPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("keyToolPath : " + keyToolPath);

		String[] cmd = {keyToolPath,"-printcert","-v","-file", CertFilePath};
		
		exc(cmd);
	}
	
	
	static String exc(String[] cmd) {
		try {
			String s = "";
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			
			String buffer = "";
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream(),"x-windows-949"));
		    
		    System.err.println("defaultCharset() " + java.nio.charset.Charset.defaultCharset().displayName());
		    
		    while ((s = stdOut.readLine()) != null) {
		    	System.out.println(s);
		    	System.out.println(URLDecoder.decode(s, "UTF-8"));
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
