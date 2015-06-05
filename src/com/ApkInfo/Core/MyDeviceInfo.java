package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.ApkInfo.UI.MainUI;

public class MyDeviceInfo {
	public static ArrayList<Device> DeviceList;
	
	public String strAppinfo;
	public String strDeviceinfo;
	public int DeviceCount;
	
	public MyDeviceInfo() {
		DeviceList = new ArrayList<Device>();	
		String TargetInfo = new String();
		String workingDir = CoreApkTool.GetUTF8Path() + File.separator + "adb";
		System.out.println(workingDir);
		
		String[] cmd = {workingDir, "devices"};
		exc(cmd,true);
		
		//adb shell dumpsys package my.package | grep versionName
		String[] cmd1 = {workingDir, "shell", "dumpsys","package","com.nttdocomo.android.ictrw"};
				
		TargetInfo = exc(cmd1,true);
		
		Device newDivceInfo = new Device();
		
		
		//newDivceInfo.strVersion = TargetInfo(TargetInfo.("versionName=")
		
		System.out.println(selectString(TargetInfo,"versionName="));
		System.out.println(selectString(TargetInfo,"versionCode="));
		System.out.println(selectString(TargetInfo,"codePath="));
		System.out.println(selectString(TargetInfo,"legacyNativeLibraryDir="));
		
		
		
		//adb shell getprop ro.build.version.release
		
		String[] cmd2 = {workingDir, "shell", "getprop","ro.factory.model"};
		TargetInfo = exc(cmd2,true);
		
		String[] cmd3 = {workingDir, "shell", "getprop","ro.build.PDA"};
		TargetInfo = exc(cmd3,true);
	
		String[] cmd4 = {workingDir, "shell", "getprop","ro.build.tags"};
		TargetInfo = exc(cmd4,true);
		
		String[] cmd5 = {workingDir, "shell", "getprop","ro.build.type"};
		TargetInfo = exc(cmd5,true);
		
		String[] cmd6 = {workingDir, "-s", "97920989","install", "-d","-r", "/home/leejinhyeong/Desktop/dhome_phone_test_signed_on.apk"};
		TargetInfo = exc(cmd6,true);		
		
	}
	
	public String selectString(String source, String key)
	{
		String temp = null;
		
		int startindex = source.indexOf(key);
		temp = source.substring(startindex);

		int endindex = temp.indexOf(" ");
		temp = temp.substring(key.length(),endindex);
		return temp; 
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
	public class Device {
		//app--------------------------------------------------------------------------------------------------------
		String strPakage;
		String strVersion;
		String strVersionCode;
		String strCodeFolderPath;
				
		//device--------------------------------------------------------------------------------------------------------
		String strADBDeviceNumber;
		String strDeviceName;
		String strkeys;
		String strBuildType;
				
	}
}
