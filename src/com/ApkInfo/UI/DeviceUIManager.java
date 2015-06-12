package com.ApkInfo.UI;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.Core.MyDeviceInfo.Device;

public class DeviceUIManager {
	
	MyDeviceInfo mMyDeviceInfo;
	ArrayList<Device> DeviceList;
	String strPackageName;
	
	
	public DeviceUIManager(String PackageName) {
		// TODO Auto-generated constructor stub
		
		strPackageName = PackageName;
		
		mMyDeviceInfo = new MyDeviceInfo();
		
		DeviceList = mMyDeviceInfo.scanDevices();
		
		System.out.println("Device count : " + DeviceList.size());
		
		if(DeviceList.size() ==0) {
			JOptionPane.showMessageDialog(null,"Device Not found!\nplease check Connected","Warning",JOptionPane.WARNING_MESSAGE);
		} else if(DeviceList.size() ==1) {
			
		} else if(DeviceList.size() >1) {
			
		}
		
	}

public static void main(final String[] args) {
	
	EventQueue.invokeLater(new Runnable() {
		public void run() {	
			//args = file path					
			DeviceUIManager mMyDeviceManager = new DeviceUIManager("com.nextbit.app");
		}
	});
}
}


