package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ApkInfo.UI.MainUI;

public class MyDeviceInfo {
	
	
	//app--------------------------------------------------------------------------------------------------------
	String strPakage;
	String strVersion;
	String strVersionCode;
	String strCodeFolderPath;
	String strlegacyNativeLibraryDir;
	String strKeySet;
	
	//device--------------------------------------------------------------------------------------------------------
	String strDeviceName;
	String strkeys;
	String strBuildType;
	
	//device for adb--------------------------------------------------------------------------------------------------------
	Boolean bisDeviceConnect;
	Boolean bisPossibleRemount;
	
	public String strAppinfo;
	public String strDeviceinfo;
	
	
	public MyDeviceInfo() {
		
		String[] cmd = {"adb", "devices"};
		
		
		exc(cmd,true);
	}
	
	static String exc(String[] cmd, boolean showLog) {
		try {
			String s = "";
			//String e = "";
			
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			
			String buffer = "";
		    // 외부 프로그램 출력 읽기
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    //BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));
		    
		    
		    if(showLog) {
			    while ((s = stdOut.readLine()) != null) {
			    	System.out.println(s);
			    	buffer += s;
			    }
		    }
		    // 외부 프로그램 반환값 출력 (이 부분은 필수가 아님)
		    //System.out.println("Exit Code: " + oProcess.exitValue());
		    //System.exit(oProcess.exitValue()); // 외부 프로그램의 반환값을, 이 자바 프로그램 자체의 반환값으로 삼기
		    
		    return buffer;

		    } catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());
		      
		      System.exit(-1);
	    }
		return null;
		
	}
}
