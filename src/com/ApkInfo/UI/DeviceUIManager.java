package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.Core.MyDeviceInfo.Device;

public class DeviceUIManager {
	
	public static MyDeviceInfo mMyDeviceInfo;
	ArrayList<Device> DeviceList;
	String strPackageName;
	
	public DeviceUIManager(String PackageName) {
		// TODO Auto-generated constructor stub
		
		strPackageName = PackageName;
		
		mMyDeviceInfo = new MyDeviceInfo();
		
		DeviceList = mMyDeviceInfo.scanDevices();

	    String[] names = new String[DeviceList.size()];
	    
	    for(int i=0; i<DeviceList.size(); i++) {
	    	names[i] = DeviceList.get(i).strADBDeviceNumber + "(" + DeviceList.get(i).strDeviceName + ")";
	    }
	    
		System.out.println("Device count : " + DeviceList.size());
		
		if(DeviceList.size() ==0) {
			JOptionPane.showMessageDialog(null,"Device not found!\nplease check Connected","Warning",JOptionPane.WARNING_MESSAGE);			
		} else if(DeviceList.size() ==1) {
			Object[] options = {"Push",
                    "Install"};
			int n = JOptionPane.showOptionDialog(null, "동일 package가 설치되어있습니다.\n"  + "Push or Install?",
			    "warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				System.out.println("Seltected index : " + n);
				
		} else if(DeviceList.size() >1) {
            int selectedValue = MyListDialog.showDialog(null, null, "Select Device", "Device List", names, 0, "Cosmo  ");
            System.out.println("Seltected index : " + selectedValue);            
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


