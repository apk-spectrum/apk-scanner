package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.ApkInfo.Core.AdbWrapper;
import com.ApkInfo.Core.AdbWrapper.AdbWrapperListener;
import com.ApkInfo.Core.AdbWrapper.DeviceStatus;
import com.ApkInfo.Core.AdbWrapper.PackageInfo;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;

public class DeviceUIManager
{
	static private JTextArea dialogLogArea;
	static private JDialog dlgDialog = null;

	private String strPackageName;
	private String strSourcePath;
	private String strLibPath;
	
	public interface InstallButtonStatusListener
	{
		public void SetInstallButtonStatus(Boolean Flag);
	}
	private InstallButtonStatusListener Listener;
	
	public DeviceUIManager(String PackageName, String apkPath, String libPath, final InstallButtonStatusListener Listener)
	{
		final ImageIcon Appicon = Resource.IMG_QUESTION.getImageIcon();
        final Object[] options = {"Push", "Install"};
		strPackageName = PackageName;
		strSourcePath = apkPath;
		strLibPath = libPath;
		
		ShowSetupLogDialog();
		
		this.Listener = Listener; 
		
		Thread t = new Thread(new Runnable() {
			public void run(){
				printlnLog("scan devices...");
				ArrayList<DeviceStatus> DeviceList = AdbWrapper.scanDevices();

				String[] names = new String[DeviceList.size()];

				int i = 0, activeDevice = 0;
				for(DeviceStatus dev: DeviceList) {
					if(dev.status.equals("device")) {
						printlnLog("getDeviceInfo() " + dev.name);
						names[i++] = dev.name + "(" + dev.device + ")";
						activeDevice++;
					} else {
						names[i++] = dev.name + "(Unknown) - " + dev.status; 
					}
					printlnLog(names[i-1]);
				}

				printlnLog("Device count : " + DeviceList.size());
				if(DeviceList.size() == 0) {
					Listener.SetInstallButtonStatus(true);
					final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
					JOptionPane.showMessageDialog(null, "Device not found!\nplease check Connected","Warning", JOptionPane.WARNING_MESSAGE, Appicon);
					return;
				}
				
				String deviceName = null;
				if(DeviceList.size() > 1 || (DeviceList.size() == 1 && activeDevice == 0)) {
					int selectedValue = MyListDialog.showDialog(null, null, "Select Device", "Device List", names, 0, "Cosmo  ");
					System.out.println("Seltected index : " + selectedValue);
					
					if(selectedValue == -1) {
						Listener.SetInstallButtonStatus(true);
						return;
					}
					deviceName = DeviceList.get(selectedValue).name;
				} else {
					deviceName = DeviceList.get(0).name;
				}

				printlnLog("getPackageInfo() " + strPackageName);
				PackageInfo pkgInfo = AdbWrapper.getPackageInfo(deviceName, strPackageName);
				if(pkgInfo != null) {
					if(pkgInfo.isSystemApp == true && AdbWrapper.hasRootPermission(deviceName)){
						String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
						int n = JOptionPane.showOptionDialog(null, "동일 package가 설치되어있습니다.\n"  +  strLine + pkgInfo.getSummary() + strLine + "Push or Install?",
								"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
						System.out.println("Seltected index : " + n);
						if(n==-1) {
							Listener.SetInstallButtonStatus(true);
							return;
						} 
						if(n==0) {
							AdbWrapper.PushApk(deviceName, strSourcePath, pkgInfo.apkPath, strLibPath, new AdbWrapperObserver("push", deviceName));
							return;
						}
					}
				}
				AdbWrapper.InstallApk(deviceName, strSourcePath , new AdbWrapperObserver("install", null));
			}
		});
		t.start();
	}
	
	static public void setVisible(boolean bool)
	{
		if(dlgDialog != null) {
			dlgDialog.setVisible(bool);
		}
	}
	
	private void printlnLog(String msg)
	{
		dialogLogArea.append(msg+"\n");
	}
	
	private class AdbWrapperObserver implements AdbWrapperListener
	{
		private String type;
		private String device;
		
		private final ImageIcon QuestionAppicon;
		private final ImageIcon WaringAppicon;
		private final ImageIcon SucAppicon;
		
		public AdbWrapperObserver(String type, String device)
		{
			this.type = type;
			this.device = device;

			QuestionAppicon = Resource.IMG_QUESTION.getImageIcon();
			WaringAppicon = Resource.IMG_WARNING.getImageIcon();
			SucAppicon = Resource.IMG_SUCCESS.getImageIcon();
		}
		
		@Override
		public void OnMessage(String msg) {
			printlnLog(msg+"\n");
		}

		@Override
		public void OnError() {
			if(type.equals("push")) {
				printlnLog("Failure...\n");
				JOptionPane.showMessageDialog(null, "Failure...", "Waring",JOptionPane.QUESTION_MESSAGE, WaringAppicon);
			} else {
				JOptionPane.showMessageDialog(null, "Failure...", "Complete", JOptionPane.INFORMATION_MESSAGE, SucAppicon);
			}
		}

		@Override
		public void OnSuccess() {
			if(type.equals("push")) {
				printlnLog("Success...\n");
				int reboot = JOptionPane.showConfirmDialog(null, "Success download..\nRestart device now?", "APK Push", JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, QuestionAppicon);
				if(reboot == 0){
					printlnLog("Wait for reboot...");
					AdbWrapper.reboot(device);
					printlnLog("Reboot...");
				}
			} else {
				JOptionPane.showMessageDialog(null, "Success", "Complete", JOptionPane.INFORMATION_MESSAGE, SucAppicon);
			}
		}

		@Override
		public void OnCompleted() {
			Listener.SetInstallButtonStatus(true);
		}
	}
	
	private void ShowSetupLogDialog()
	{
		if(dlgDialog ==null) {
			final JPanel DialogPanel = makeLodingDialog();
			Thread t = new Thread(new Runnable(){
				public void run(){
					//JOptionPane.showMessageDialog(null, DialogPanel,"Log", JOptionPane.CANCEL_OPTION,Appicon);
					dlgDialog = new JDialog();
			        	
					dlgDialog.setTitle("Log");
					dlgDialog.setModal(false);
			        	
					dlgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			        	
					JLabel GifLabel;
					ImageIcon icon = Resource.IMG_INSTALL_WAIT.getImageIcon();
					GifLabel = new JLabel(icon);
			            
					DialogPanel.add(GifLabel);
			            
			            
					StandardButton btnOK;
					btnOK = new StandardButton("닫기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);
					btnOK.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {		                    		                    		                    
							dlgDialog.setVisible(false);		                    
						}
					});

					DialogPanel.add(btnOK);
			            
					dlgDialog.setSize(new Dimension(480,250));
					dlgDialog.setResizable( false );
					dlgDialog.add(DialogPanel);
					//dlgDialog.setLocationRelativeTo(null);
					dlgDialog.setVisible(true);
			        	
					if(MainUI.window != null) {
						dlgDialog.setLocation(MainUI.nPositionX +600, MainUI.nPositionY);
			        		
					}
				}
			});
			t.start();
		}
		else if(dlgDialog.isVisible() == false) {
			dlgDialog.setVisible(true);
		}
	}

	private JPanel makeLodingDialog()
	{
		//JPanel DiaPanel = new JPanel(new BorderLayout(3,3));
		
		JPanel DiaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		DiaPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
		
		
		dialogLogArea = new JTextArea(10,30); 
		
		DefaultCaret caret = (DefaultCaret) dialogLogArea.getCaret(); // ←
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
		
		dialogLogArea.setWrapStyleWord(true);
		dialogLogArea.setLineWrap(true);
		dialogLogArea.setEditable(false);
		
		DiaPanel.add(new JScrollPane(dialogLogArea));
		
		return DiaPanel;
	}
}


