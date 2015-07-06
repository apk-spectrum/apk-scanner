package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UI.DeviceUIManager;
import com.ApkInfo.UI.MainUI;
import com.ApkInfo.UI.MyButtonPanel;

public class MyDeviceInfo
{
	public String strAppinfo;
	public String strDeviceinfo;
	public JTextArea LogTextArea;
	String adbCmd;
	final ImageIcon QuestionAppicon;
	final ImageIcon WaringAppicon;
	final ImageIcon SucAppicon;
	
	enum INSTALL_TYPE {
		INSTALL,
		PUSH
	}

	static MyCoreThead startCore;
	
	public MyDeviceInfo()
	{
		QuestionAppicon = Resource.IMG_QUESTION.getImageIcon();
		WaringAppicon = Resource.IMG_WARNING.getImageIcon();
		SucAppicon = Resource.IMG_SUCCESS.getImageIcon();
		
		adbCmd = Resource.BIN_ADB_LNX.getPath();

		if(adbCmd.matches("^[A-Z]:.*")) {
			adbCmd = Resource.BIN_ADB_WIN.getPath();
		}

		if(!(new File(adbCmd)).exists()) {
			System.out.println("no such adb tool" + adbCmd);
			adbCmd = null;
		}
		
		System.out.println(adbCmd);
	}

	public boolean ckeckAdbTool() {
		System.out.println("ckeckAdbTool()");
		if(adbCmd == null || !(new File(adbCmd)).exists())
			return false;

		MyConsolCmd.exc(new String[] {adbCmd, "kill-server"}, false, null);
		String[] result = MyConsolCmd.exc(new String[] {adbCmd, "start-server"}, false, null);
		return result[1].matches(".*daemon started successfully.*");
	}

	public ArrayList<Device> scanDevices()
	{
		ArrayList<Device> DeviceList = new ArrayList<Device>();
		String[] strDeviceList;

		DeviceList.clear();
		
		if(adbCmd == null) {
			return DeviceList;
		}

		String[] cmd = {adbCmd, "devices"};
		strDeviceList = MyConsolCmd.exc(cmd,true,null);
		
		for(int i=0; i<strDeviceList.length; i++) {
			if(strDeviceList[i].matches("^.*\\s*device\\s*$")){
				String name = strDeviceList[i].replaceAll("^\\s*([^\\s]*)\\s*device\\s*$", "$1");
				addLogText(name);
				Device dev = new Device(name);
				dev.dump();
				DeviceList.add(dev);
			}
		}
		return DeviceList;
	}
	
	public boolean PushApk(Device device, String sourcePath, JTextArea LogTextArea)
	{
		System.out.println("sourcePath : " + sourcePath + ", DeviceADBNumber: " + device.strADBDeviceNumber);
		if(adbCmd == null || adbCmd.isEmpty() || device == null
				|| device.strADBDeviceNumber == null || device.strADBDeviceNumber.isEmpty()
				|| sourcePath == null || sourcePath.isEmpty()
				|| device.strApkPath == null || device.strApkPath.isEmpty()) {
			return false;
		}
		
		startCore = new MyCoreThead(INSTALL_TYPE.PUSH, device, sourcePath, LogTextArea);
		startCore.start();

		return true;
	}
	
	public boolean InstallApk(Device device, String sourcePath, JTextArea LogTextArea)
	{
		System.out.println("sourcePath : " + sourcePath + ", DeviceADBNumber: " + device.strADBDeviceNumber);
		if(adbCmd == null || adbCmd.isEmpty() || device == null
			|| device.strADBDeviceNumber == null || device.strADBDeviceNumber.isEmpty()
			|| sourcePath == null || sourcePath.isEmpty()) {
			return false;
		}
		
		startCore = new MyCoreThead(INSTALL_TYPE.INSTALL, device, sourcePath, LogTextArea);
		startCore.start();
		
		return true;
	}
	
	private void addLogText(String str) {
		DeviceUIManager.dialogLogArea.append(str+"\n");
	}
	
	class MyCoreThead extends Thread
	{
		INSTALL_TYPE type;
		String DeviceADBNumber;
		String sourcePath;
		Device device;
		JTextArea LogTextArea;
		
		MyCoreThead(INSTALL_TYPE type, Device device, String sourcePath, JTextArea LogTextArea)
		{
			this.type = type;
			this.device = device;
			this.DeviceADBNumber = device.strADBDeviceNumber;
			this.sourcePath = sourcePath;
			this.LogTextArea = LogTextArea;
		}
		
		public void run()
		{			
			
			if(type == INSTALL_TYPE.INSTALL) {
				String[] result;
				String[] cmd = {adbCmd, "-s", "\"" + this.DeviceADBNumber + "\"", "install", "-d","-r", this.sourcePath};
				
				//LogTextArea.append(adbCmd + "\n");
				result = MyConsolCmd.exc(cmd,true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						addLogText(output.replaceAll("^.*adb(\\.exe)?", "adb") + "\n");
						return true;
					}
				});
				if(MyButtonPanel.btnInstall != null) {
				MyButtonPanel.btnInstall.setEnabled(true);
				JOptionPane.showMessageDialog(null, result[2], "Success", JOptionPane.INFORMATION_MESSAGE, SucAppicon);
				}
			} else {
				String[][] result;
				List<String[]> cmd = new ArrayList<String[]>();
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "root"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "remount"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "shell", "su", "-c", "setenforce", "0"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "push", this.sourcePath, device.strApkPath});
				System.out.println(this.sourcePath + " to " + device.strApkPath);

				String LibSourcePath = MainUI.GetMyApkInfo().WorkTempPath + File.separator + "lib" + File.separator;
				Iterator<String> libPaths = MainUI.GetMyApkInfo().LibList.iterator();
				while(libPaths.hasNext()) {
					String path = libPaths.next();
					if(!(new File(path)).exists()) {
						System.out.println("no such file : " + path);
						continue;
					}
					if(path.matches(LibSourcePath.replace("\\", "\\\\")+"arm64.*")) {
						if(device.isAbi64) {
							cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "push", path, "/system/lib64/"});
							System.out.println("push " + path + " " + "/system/lib64/");
						} else {
							System.out.println("ignored lib64 : " + path);
						}
					} else {
						if(!device.isAbi64) {
							cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "push", path, "/system/lib/"});
							System.out.println("push " + path + " " + "/system/lib/");
						} else {
							System.out.println("ignored lib32 path : " + path);
						}
					}
				}
				//cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "reboot"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "shell", "echo", "Compleated..."});
				
				result = MyConsolCmd.exc(cmd.toArray(new String[0][0]),true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						LogTextArea.append(output.replaceAll("^.*adb(\\.exe)?", "adb") + "\n");
				    	if(output.equals("* failed to start daemon *")
				    		|| output.equals("error: device not found")
				    		|| output.equals("adbd cannot run as root in production builds")
				    		|| output.matches(".*Permission denied.*")
				    	) {
				    		System.out.println(">>>>>>>>>>>> fail : " + output);
				    		return false;
				    	}
				    	return true;
					}
				});

				MyButtonPanel.btnInstall.setEnabled(true);				
				//JOptionPane.showMessageDialog(null, result);

				System.out.println("cmd.size() " + cmd.size() + ", result.length " + result.length);
				if(cmd.size() == result.length) {
					LogTextArea.append("Success...\n");
					int reboot = JOptionPane.showConfirmDialog(null, "Success download..\nRestart device now?", "APK Push", JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE, QuestionAppicon);
					if(reboot == 0){
						LogTextArea.append("Wait for reboot...\n");
						MyConsolCmd.exc(new String[] {adbCmd, "-s", this.DeviceADBNumber, "reboot"});
						LogTextArea.append("Reboot\n");
					}
				} else {
					LogTextArea.append("Failure...\n");
					JOptionPane.showMessageDialog(null, "Failure...", "Waring",JOptionPane.QUESTION_MESSAGE, WaringAppicon);
				}
			}
		}
	}

	public class Device
	{
		//app--------------------------------------------------------------------------------------------------------
		public String strPakage;
		public String strVersion;
		public String strVersionCode;
		public String strCodeFolderPath;
		public String strApkPath;
		public boolean isSystemApp;

		//device--------------------------------------------------------------------------------------------------------
		public String strADBDeviceNumber;
		public String strDeviceName;
		public String strModelName;
		public String strOsVersion;
		public String strBuildVersion;
		public String strSdkVersion;
		public String strkeys;
		public String strBuildType;
		public String strLabelText;
		public boolean isAbi64;
		public boolean hasRootPermission;

		public Device() { }
		
		public Device(String deviceName)
		{
			setDeviceInfo(deviceName);
			makeLabel();
		}
		
		public Device(String deviceName, String packageName)
		{
			setDeviceInfo(deviceName);
			ckeckPackage(packageName);
			makeLabel();
		}
		
		public void setDeviceInfo(String deviceName)
		{
			strADBDeviceNumber = deviceName;
			strDeviceName = getSystemProp(deviceName, "ro.product.device");
			strModelName = getSystemProp(deviceName, "ro.product.model");
			strOsVersion = getSystemProp(deviceName, "ro.build.version.release");
			strBuildVersion = getSystemProp(deviceName, "ro.build.version.incremental");
			strSdkVersion = getSystemProp(deviceName, "ro.build.version.sdk");
			strBuildType = getSystemProp(deviceName, "ro.build.type");
			isAbi64 = !getSystemProp(deviceName, "ro.product.cpu.abilist64").isEmpty();
			
			hasRootPermission = true;
			String[] cmd = {adbCmd,"-s",strADBDeviceNumber, "root"};
			String[] result = MyConsolCmd.exc(cmd, false, null);
			if(result[0].equals("adbd cannot run as root in production builds")) {
				hasRootPermission = false;
			}
		}
		
		public boolean ckeckPackage(String packageName)
		{
			String[] TargetInfo;

			strPakage = null;
			strVersion = null;
			strVersionCode = null;
			strCodeFolderPath = null;
			isSystemApp = false;
			if(packageName == null) return false;
			
			System.out.println("ckeckPackage() " + packageName);

			String[] cmd = {adbCmd,"-s",strADBDeviceNumber, "shell", "dumpsys","package",packageName};
			TargetInfo = MyConsolCmd.exc(cmd,false,null);
			
			if(TargetInfo.length > 1) {
				strPakage = packageName;
				strVersion = selectString(TargetInfo,"versionName=");
				strVersionCode = selectString(TargetInfo,"versionCode=");
				strCodeFolderPath = selectString(TargetInfo,"codePath=");
				
				if(strCodeFolderPath != null && strCodeFolderPath.matches("^/system/.*")) {
					isSystemApp = true;
				}
				
				strApkPath = null;
				if(strCodeFolderPath != null && !strCodeFolderPath.isEmpty()) {
					String[] cmd2 = {adbCmd,"-s",strADBDeviceNumber, "shell", "ls",strCodeFolderPath};
					MyConsolCmd.exc(cmd2,false,new MyConsolCmd.OutputObserver() {
						@Override
						public boolean ConsolOutput(String output) {
					    	if(output.matches("^.*apk")) {
					    		strApkPath = strCodeFolderPath + "/" + output;
					    	}
					    	return true;
						}
					});
				}
				makeLabel();
				return true;
			}
			return false;
		}
		
		public void makeLabel()
		{
			//Devicetemp.strLabelText = "-Device Info\n";
			strLabelText = "Model : " + strModelName + " / " + strDeviceName + "\n";
			strLabelText += "Version : " + strBuildVersion + "(" + strBuildType + ") / ";
			strLabelText += "" + strOsVersion + "(" + strSdkVersion + ")\n";
			if(strPakage != null) {
				strLabelText += "\n";
				strLabelText += "-Installed APK info\n";
				strLabelText += "Pakage : " + strPakage +"\n";
				strLabelText += "Version : " + strVersion + " / " + strVersionCode +"\n";
				strLabelText += "CodePath : " + strCodeFolderPath +"\n";
			}
		}
		
		public void dump()
		{
			System.out.println("VersionName : " + strVersion);
			System.out.println("VersionCode : " + strVersionCode);
			System.out.println("CodePath : " + strCodeFolderPath);
			System.out.println("Device : " + strDeviceName);
			System.out.println("Model : " + strModelName);
			System.out.println("BuildType : " + strBuildType);
		}
		
		private String selectString(String[] source, String key)
		{
			String temp = null;
			
			for(int i=0; i < source.length; i++) {
				if(source[i].matches("^\\s*"+key+".*$")) {
					temp = source[i].replaceAll("^\\s*"+key+"\\s*([^\\s]*).*$", "$1");
					break;
				}
			}
			return temp;
		}
		
		private String getSystemProp(String device, String tag)
		{
			String[] TargetInfo;
			String[] cmd = {adbCmd, "-s", device, "shell", "getprop", tag};

			TargetInfo = MyConsolCmd.exc(cmd,false,null);
			
			addLogText(TargetInfo[0]);
			
			return TargetInfo[0];
		}

		private void addLogText(String str) {
			DeviceUIManager.dialogLogArea.append(str+"\n");
		}
		
	}
}
