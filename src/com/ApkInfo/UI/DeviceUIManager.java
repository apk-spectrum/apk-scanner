package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.Core.MyConsolCmd;
import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.Core.MyDeviceInfo.Device;

public class DeviceUIManager {
	
	public static MyDeviceInfo mMyDeviceInfo;
	ArrayList<Device> DeviceList;
	String strPackageName;
	JTextArea dialogLogArea;
	String strSourcePath;
	String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
	public DeviceUIManager(String PackageName, String sourcePath) {
		// TODO Auto-generated constructor stub

		String ImgPath = CoreApkTool.GetUTF8Path();
		ImageIcon Appicon = new ImageIcon(ImgPath+File.separator+"AppIcon.png");
        
		
		Object[] options = {"Push", "Install"};
		strPackageName = PackageName;
		strSourcePath = sourcePath;
		
		mMyDeviceInfo = new MyDeviceInfo();
		DeviceList = mMyDeviceInfo.scanDevices();

	    String[] names = new String[DeviceList.size()];
	    
	    for(int i=0; i<DeviceList.size(); i++) {
	    	names[i] = DeviceList.get(i).strADBDeviceNumber + "(" + DeviceList.get(i).strDeviceName + ")";
	    }
	    
		System.out.println("Device count : " + DeviceList.size());
		if(DeviceList.size() ==0) {
			MyButtonPanel.btnInstall.setEnabled(true);
			JOptionPane.showMessageDialog(null,"Device not found!\nplease check Connected","Warning",JOptionPane.WARNING_MESSAGE, Appicon);			
		} else if(DeviceList.size() ==1) {
			DeviceList.get(0).ckeckPackage(strPackageName);			
			if(DeviceList.get(0).isSystemApp == true){
				int n = JOptionPane.showOptionDialog(null, "동일 package가 설치되어있습니다.\n"  +  strLine+ DeviceList.get(0).strLabelText +strLine+"Push or Install?",
					    "warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
						System.out.println("Seltected index : " + n);				
	    				if(n==0) {
	    					JPanel DialogPanel = makeLodingDialog();						
	    					mMyDeviceInfo.PushApk(DeviceList.get(0),strSourcePath , dialogLogArea);
	    					JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION, Appicon);
	    				} else if(n==1){
	    					JPanel DialogPanel = makeLodingDialog();						
	    					mMyDeviceInfo.InstallApk(DeviceList.get(0),strSourcePath , dialogLogArea);
	    					JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION,Appicon);
	    				} 
						
			} else {
				JPanel DialogPanel = makeLodingDialog();						
				mMyDeviceInfo.InstallApk(DeviceList.get(0),strSourcePath , dialogLogArea);
				JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION, Appicon);
			}
		} else if(DeviceList.size() >1) {
            int selectedValue = MyListDialog.showDialog(null, null, "Select Device", "Device List", names, 0, "Cosmo  ");
            System.out.println("Seltected index : " + selectedValue);
            
            if(selectedValue==-1) {
            	MyButtonPanel.btnInstall.setEnabled(true);
            	return;
            }
            
        	if(DeviceList.get(selectedValue).ckeckPackage(strPackageName)) {
        		int n = JOptionPane.showOptionDialog(null, "동일 package가 설치되어있습니다.\n"  + strLine+ DeviceList.get(selectedValue).strLabelText +strLine+ "Push or Install?",
					    "warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
						System.out.println("Seltected index : " + n);
				if(n==0) {
					JPanel DialogPanel = makeLodingDialog();
					mMyDeviceInfo.PushApk(DeviceList.get(selectedValue),strSourcePath , dialogLogArea);
					JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION, Appicon);
				} else if(n==1){
					JPanel DialogPanel = makeLodingDialog();						
					mMyDeviceInfo.InstallApk(DeviceList.get(selectedValue),strSourcePath , dialogLogArea);
					JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION, Appicon);
				} else {
					MyButtonPanel.btnInstall.setEnabled(true);
					return ;
				}

        	} else {
        		JPanel DialogPanel = makeLodingDialog();						
				mMyDeviceInfo.InstallApk(DeviceList.get(selectedValue), strSourcePath , dialogLogArea);
				JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION, Appicon);
        	}
		}
	}
	public void ShowSetupDialog(int selected) {
		JPanel DialogPanel = makeLodingDialog();
		mMyDeviceInfo.InstallApk(DeviceList.get(selected), strSourcePath , dialogLogArea);
		JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION);
	}
	
	public JPanel makeLodingDialog() {
		JPanel DiaPanel = new JPanel(new BorderLayout(3,3));
		
		dialogLogArea = new JTextArea(5,30); 
		
		dialogLogArea.setWrapStyleWord(true);
		dialogLogArea.setLineWrap(true);
		dialogLogArea.setEditable(false);
		dialogLogArea.setFocusable(false);
		
		DiaPanel.add(new JScrollPane(dialogLogArea), BorderLayout.CENTER);

		
		return DiaPanel;
	}
	
	
	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {	
				//args = file path
	
			 
				DeviceUIManager mMyDeviceManager = new DeviceUIManager("com.nextbit.app", "/home/leejinhyeong/workspace/APKInfoDlgv2/CloudMailer_sign_zipaligned.apk");
				
			}
		});
	}
}


