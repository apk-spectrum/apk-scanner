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
		Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
		String keytoolPackage = "sun.security.tools.KeyTool";
		if(javaVersion >= 1.8) {
			keytoolPackage = "sun.security.tools.keytool.Main";
		}

/*
		String keyToolPath = CoreApkTool.GetUTF8Path()+File.separator+"tool"+File.separator+"keytool";
		if(keyToolPath.matches("^C:.*")) {
			keyToolPath += ".exe";
		}
		if(!(new File(keyToolPath)).exists()) {
			System.out.println("keytool이 존재 하지 않습니다 :" + keyToolPath);
			keyToolPath = "";
		}
*/
		if(!(new File(CertPath)).exists()) {
			System.out.println("META-INFO 폴더가 존재 하지 않습니다 :");
			return CertList;
		}
		
		for (String s : (new File(CertPath)).list()) {
			if(!s.matches(".*\\.RSA")) continue;

			File rsaFile = new File(CertPath + s);
			if(!rsaFile.exists()) continue;

			exc(new String[]{"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", rsaFile.getAbsolutePath()});
/*
			if(keyToolPath.isEmpty()) {
				exc(new String[]{"java","-Dfile.encoding=utf8","sun.security.tools.KeyTool","-printcert","-v","-file", rsaFile.getAbsolutePath()});
			} else {
				exc(new String[]{keyToolPath,"-printcert","-v","-file", rsaFile.getAbsolutePath()});
			}
*/
		}
		return CertList;
	}
	
	public static String getCertSummary() {
		return CertSummary;
	}
	
	static String exc(String[] cmd) {
		try {
			String s = "";
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			/*
			String encoding = "UTF-8";
			if(cmd[0].matches("^C:.*")) {
				encoding = "EUC-KR";
			}
			*/
			String buffer = "";
		    BufferedReader stdOut = new BufferedReader(new InputStreamReader(oProcess.getInputStream()/*, encoding*/));
		    
		    String certContent = "";
		    CertSummary = "<certificate[1]>\n";
		    int certCnt = 0;
		    while ((s = stdOut.readLine()) != null) {
	    		if(!certContent.isEmpty() && s.matches("^.*\\[[0-9]*\\]:$")){
	    			if((cmd[0].equals("java") && cmd[4].equals("-v"))/* || (!cmd[0].equals("java") && cmd[2].equals("-v"))*/) {
	    				CertList.add(new Object[] { "Certificate[" + (CertList.size() + 1) + "]", certContent });
	    				CertSummary += "<certificate[" + (CertList.size() + 1) + "]>\n";
				    	certContent = "";
	    			} else {
	    				CertList.get(certCnt++)[1] += "\n\n[RFC output]\n" + certContent;
				    	certContent = "";
				    	continue;
	    			}
	    		}
	    		if(s.matches("^.*:( [^ ,]+=(\".*\")?[^,]*,?)+$")) {
	    			CertSummary += s + "\n";
	    		}
	    		certContent += (certContent.isEmpty() ? "" : "\n") + s;
		    	buffer += s;
		    }
		    if((cmd[0].equals("java") && cmd[4].equals("-v"))/* || (!cmd[0].equals("java") && cmd[2].equals("-v"))*/) {
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
