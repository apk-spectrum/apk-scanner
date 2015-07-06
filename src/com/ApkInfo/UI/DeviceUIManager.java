package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.Rectangle;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.Core.MyConsolCmd;
import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.Core.MyDeviceInfo.Device;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;

public class DeviceUIManager {
	
	public static MyDeviceInfo mMyDeviceInfo;
	ArrayList<Device> DeviceList;
	String strPackageName;
	static public JTextArea dialogLogArea;
	String strSourcePath;
	String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
	public static JDialog dlgDialog = null;
	
	
	public DeviceUIManager(String PackageName, String sourcePath) {
		// TODO Auto-generated constructor stub

		final ImageIcon Appicon = Resource.IMG_QUESTION.getImageIcon();
        
		
		final Object[] options = {"Push", "Install"};
		strPackageName = PackageName;
		strSourcePath = sourcePath;
		
		ShowSetupLogDialog();
		
		mMyDeviceInfo = new MyDeviceInfo();
		
		  Thread t = new Thread(new Runnable(){
		        public void run(){
		    		DeviceList = mMyDeviceInfo.scanDevices();

		    	    String[] names = new String[DeviceList.size()];
		    	    
		    	    for(int i=0; i<DeviceList.size(); i++) {
		    	    	names[i] = DeviceList.get(i).strADBDeviceNumber + "(" + DeviceList.get(i).strDeviceName + ")";
		    	    }
		    	    
		    		System.out.println("Device count : " + DeviceList.size());
		    		if(DeviceList.size() ==0) {
		    			//MyButtonPanel.btnInstall.setEnabled(true);
		    			final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();		    			
		    			JOptionPane.showMessageDialog(null,"Device not found!\nplease check Connected","Warning",JOptionPane.WARNING_MESSAGE, Appicon);		    			
		    		} else if(DeviceList.size() ==1) {
		    			if(DeviceList.get(0).ckeckPackage(strPackageName)) {
		    				if(DeviceList.get(0).isSystemApp == true && DeviceList.get(0).hasRootPermission == true){
		    					int n = JOptionPane.showOptionDialog(null, "동일 package가 설치되어있습니다.\n"  +  strLine+ DeviceList.get(0).strLabelText +strLine+"Push or Install?",
		    						    "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
		    							System.out.println("Seltected index : " + n);				
		    		    				if(n==0) {
		    		    					ShowSetupDialog(0, false);
		    		    				} else if(n==1){
		    		    					ShowSetupDialog(0,true);
		    		    				} else {
		    		    		            if(n==-1) {
		    		    		            	MyButtonPanel.btnInstall.setEnabled(true);
		    		    		            	return;
		    		    		            } 
		    		    				}
		    				} else {
		    					ShowSetupDialog(0, true);
		    				}
		    			} else {
		    				ShowSetupDialog(0, true);
		    			}			
		    		} else if(DeviceList.size() >1) {
		                int selectedValue = MyListDialog.showDialog(null, null, "Select Device", "Device List", names, 0, "Cosmo  ");
		                System.out.println("Seltected index : " + selectedValue);
		                
		                if(selectedValue==-1) {
		                	MyButtonPanel.btnInstall.setEnabled(true);
		                	return;
		                }            
		    			if(DeviceList.get(selectedValue).ckeckPackage(strPackageName)) {
		    				if(DeviceList.get(selectedValue).isSystemApp == true && DeviceList.get(selectedValue).hasRootPermission == true){
		    					int n = JOptionPane.showOptionDialog(null, "동일 package가 설치되어있습니다.\n"  +  strLine+ DeviceList.get(selectedValue).strLabelText +strLine+"Push or Install?",
		    						    "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
		    							System.out.println("Seltected index : " + n);				
		    		    				if(n==0) {
		    		    					ShowSetupDialog(selectedValue, false);
		    		    				} else if(n==1){
		    		    					ShowSetupDialog(selectedValue,true);
		    		    				} else {
		    		    		            if(n==-1) {
		    		    		            	MyButtonPanel.btnInstall.setEnabled(true);
		    		    		            	return;
		    		    		            } 
		    		    				}						
		    				} else {
		    					ShowSetupDialog(selectedValue, true);
		    				}
		    			} else {
		    				ShowSetupDialog(selectedValue, true);
		    			}
		    		}
		        	
		        }
		    });
		  t.start();
	}
	public void ShowSetupDialog(int selected, Boolean isInstall) {
		ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
		
		//JPanel DialogPanel = makeLodingDialog();
		if(isInstall) {
			mMyDeviceInfo.InstallApk(DeviceList.get(selected), strSourcePath , dialogLogArea);
		} else {
			mMyDeviceInfo.PushApk(DeviceList.get(selected), strSourcePath , dialogLogArea);
		}
		
		//JOptionPane.showMessageDialog(null, DialogPanel,"설치중...", JOptionPane.DEFAULT_OPTION,Appicon);
	}
	
	public void ShowSetupLogDialog() {
		System.out.println("aaaaaaaaaaaaa" + MainUI.nPositionX +600 + "      " + MainUI.nPositionY);
		
		if(dlgDialog ==null) {
			final JPanel DialogPanel = makeLodingDialog();
			  Thread t = new Thread(new Runnable(){
			        public void run(){
			        	//JOptionPane.showMessageDialog(null, DialogPanel,"Log", JOptionPane.CANCEL_OPTION,Appicon);
			        	dlgDialog = new JDialog();
			        	
			        	dlgDialog.setTitle("Log");
			        	dlgDialog.setModal(false);
			        	
			        	dlgDialog.setDefaultCloseOperation(
		                        JDialog.DISPOSE_ON_CLOSE);
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
	

	public JPanel makeLodingDialog() {
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
	
	
	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {	
				//args = file path
	
			 
				DeviceUIManager mMyDeviceManager = new DeviceUIManager("com.nextbit.app", "/home/leejinhyeong/workspace/APKInfoDlgv2/CloudMailer_sign_zipaligned.apk");
				
			}
		});
	}
}


