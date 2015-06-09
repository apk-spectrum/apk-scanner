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
	String workingDir;
	public MyDeviceInfo() {
		DeviceList = new ArrayList<Device>();	
		
		workingDir = CoreApkTool.GetUTF8Path() + File.separator + "adb";
		System.out.println(workingDir);
		
		Refresh();		
		//newDivceInfo.strVersion = TargetInfo(TargetInfo.("versionName=")
		//adb shell getprop ro.build.version.release
		
	}
	
	public Boolean Refresh()
	{
		String strDeviceList;
		String[] cmd = {workingDir, "devices"};
		strDeviceList = exc(cmd,true);
		//adb shell dumpsys package my.package | grep versionName
		
		strDeviceList = strDeviceList.substring("List of devices attached ".length());
		
		DeviceList.clear();
		
		for(; strDeviceList.indexOf("device") !=-1;) {
			Device temp = new Device();
			String strTemp;
			
			strTemp = strDeviceList.substring(0, strDeviceList.indexOf("device"));
			
			strTemp.replaceAll(" ", "");
			strTemp.replaceAll("\t", "");
			
			temp.strADBDeviceNumber =strTemp;
			
			System.out.println(temp.strADBDeviceNumber);
			
			strDeviceList = strDeviceList.substring( strDeviceList.indexOf("device")+ "device".length());
			
			SetTarget(temp, "a7614df3");
			
			DeviceList.add(temp);
		}
		
		return true;
	}
	
	public void SetTarget(Device Devicetemp, String DeviceADBNumber)
	{
		String TargetInfo = new String();
		
		String[] cmd1 = {workingDir,"-s",DeviceADBNumber, "shell", "dumpsys","package","com.nttdocomo.android.ictrw"};
		
		TargetInfo = exc(cmd1,true);
		
		System.out.println(selectString(TargetInfo,"versionName="));
		System.out.println(selectString(TargetInfo,"versionCode="));
		System.out.println(selectString(TargetInfo,"codePath="));
		System.out.println(selectString(TargetInfo,"legacyNativeLibraryDir="));		
		
		
		Devicetemp.strVersion = selectString(TargetInfo,"versionName=");
		Devicetemp.strVersionCode = selectString(TargetInfo,"versionCode=");
		Devicetemp.strCodeFolderPath = selectString(TargetInfo,"codePath=");
		//Devicetemp.str = selectString(TargetInfo,"legacyNativeLibraryDir=");
		
		
		
		
		
		String[] cmd2 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.factory.model"};
		TargetInfo = exc(cmd2,true);
		
		Devicetemp.strDeviceName = TargetInfo;
		
		String[] cmd3 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.PDA"};
		TargetInfo = exc(cmd3,true);
		
		
		String[] cmd4 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.tags"};
		TargetInfo = exc(cmd4,true);
		
		String[] cmd5 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.type"};
		TargetInfo = exc(cmd5,true);			
	}
	
	public void InstallApk(String sourcePath, String DeviceADBNumber) {
		String TargetInfo = new String();
		String[] cmd6 = {workingDir, "-s", DeviceADBNumber,"install", "-d","-r", "/home/leejinhyeong/Desktop/dhome_phone_test_signed_on.apk"};
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
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			String buffer = "";
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    
		    while ((s = stdOut.readLine()) != null) {
		    	if(showLog) System.out.println(s);
		    	buffer += s;
		    }
		    return buffer;

		    } catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());		      
		      System.exit(-1);
	    }
		return null;
		
	}
	public class Device {
		//app--------------------------------------------------------------------------------------------------------
		public String strPakage;
		public String strVersion;
		public String strVersionCode;
		public String strCodeFolderPath;
				
		//device--------------------------------------------------------------------------------------------------------
		public String strADBDeviceNumber;
		public String strDeviceName;
		public String strkeys;
		public String strBuildType;
				
	}
}
