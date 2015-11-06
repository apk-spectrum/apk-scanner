package com.apkscanner.gui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.AdbWrapper.AdbWrapperListener;
import com.apkscanner.core.AdbWrapper.DeviceStatus;
import com.apkscanner.core.AdbWrapper.PackageInfo;
import com.apkscanner.gui.dialog.install.InstallDlg;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;

public class ApkInstaller
{
	//static private JTextArea dialogLogArea;
	//static private JDialog dlgDialog = null;

	//static private JPanel installPanel;
	//static private JPanel uninstallPanel;
	
	static private String strPackageName;
	private static String strSourcePath;
	private static String strLibPath;
	private static String tmpApkPath;
	private static boolean checkPackage;
	private static boolean samePackage;
	static private Thread t;
	
	//window position
	//static private int nPositionX, nPositionY;
	
	public interface InstallButtonStatusListener
	{
		public void SetInstallButtonStatus(Boolean Flag);
		public void OnOpenApk(String path);
	}
	
	public interface InstallDlgFuncListener {
		public void Complete(String str);
		public int ShowQuestion(Runnable runnable, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue);
		public void AddLog(String str);
		public int getResult();
		public void SetResult(int i);
		public int ShowDeviceList(Runnable runnable);
		public void AddCheckList(String name, String t, InstallDlg.CHECKLIST_MODE mode);		
		DeviceStatus getSelectDev();
		public int getValue(String text);		
	}
	private static InstallButtonStatusListener Listener;
	private static InstallDlgFuncListener InstallDlgListener;
	
	public ApkInstaller(Boolean isOnlyInstall, String PackageName, String apkPath, String libPath, 
			final boolean samePackage, final boolean checkPackage, final InstallButtonStatusListener Listener)
	{

		strPackageName = PackageName;
		strSourcePath = apkPath;
		strLibPath = libPath;
		ApkInstaller.checkPackage = checkPackage;
		ApkInstaller.samePackage = samePackage;
		
		//ShowSetupLogDialog();
		//dialogLogArea.setText("");
		
		ApkInstaller.Listener = Listener; 
		
		
		InstallDlg dlg = new InstallDlg(isOnlyInstall);
		ApkInstaller.InstallDlgListener = dlg.getInstallDlgFuncListener();
		
		
		t = new InstallThread();
		t.start();
	}
	
	static class InstallThread extends Thread {
		
		final ImageIcon Appicon = Resource.IMG_QUESTION.getImageIcon();
        final Object[] options = {Resource.STR_BTN_PUSH.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] checkPackOptions = {Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] checkPackDelOptions = {Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_DEL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
		
		public InstallThread() {
			
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
				printlnLog(msg);
			}

			@Override
			public void OnError() {
				if(type.equals("push")) {
					printlnLog("Failure...");
					//JOptionPane.showMessageDialog(null, "Failure...", "Error",JOptionPane.ERROR_MESSAGE, WaringAppicon);
				} else if(type.equals("install")) {
					//JOptionPane.showMessageDialog(null, "Failure...", "Error", JOptionPane.ERROR_MESSAGE, WaringAppicon);
				} else if(type.equals("pull")) {
					//JOptionPane.showMessageDialog(null, "Failure...", "Error", JOptionPane.ERROR_MESSAGE, WaringAppicon);
				}
				InstallDlgListener.AddCheckList("Install", "fail" , InstallDlg.CHECKLIST_MODE.ERROR);
				
				ShowQuestion(t, Resource.STR_MSG_FAILURE_INSTALLED.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, WaringAppicon,
			    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess() {
				if(type.equals("push")) {
					final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
					InstallDlgListener.AddCheckList("Push", "Success" , InstallDlg.CHECKLIST_MODE.DONE);
					
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
					int reboot = ShowQuestion(t, Resource.STR_MSG_SUCCESS_INSTALLED.getString() + "\n" + Resource.STR_QUESTION_REBOOT_DEVICE.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE, QuestionAppicon, yesNoOptions, yesNoOptions[1]);
					
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), (reboot==0)?"true":"false" , InstallDlg.CHECKLIST_MODE.DONE);
					
					if(reboot == 0){
						printlnLog("Wait for reboot...");
						AdbWrapper.reboot(device);
						printlnLog("Reboot...");
					}
				} else if(type.equals("install")) {
					//JOptionPane.showMessageDialog(null, "Success", "Complete", JOptionPane.INFORMATION_MESSAGE, SucAppicon);
						InstallDlgListener.AddCheckList("Install", "Success" , InstallDlg.CHECKLIST_MODE.DONE);
						ShowQuestion(t, Resource.STR_MSG_SUCCESS_INSTALLED.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, SucAppicon,
				    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
				} else if(type.equals("pull")) {
					InstallDlgListener.AddCheckList("Pull success", "Done" , InstallDlg.CHECKLIST_MODE.DONE);					
					if(Listener != null) Listener.OnOpenApk(tmpApkPath);
					InstallDlgListener.AddCheckList("Open APK", "Done" , InstallDlg.CHECKLIST_MODE.ADD);
				} 
			}

			@Override
			public void OnCompleted() {
				Listener.SetInstallButtonStatus(true);
//				ShowQuestion(t, "완료", Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, SucAppicon,
//			    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
				//installPanel.setVisible(false);
			}
		}
		
		
		private int ShowQuestion(Runnable runnable, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
			Object[] temp = new Object[options.length];
			
			for(int i=0; i<options.length; i++) {
				temp[options.length-1-i] = options[i];
			}		
			@SuppressWarnings("unused")
			int result = InstallDlgListener.ShowQuestion(runnable,message,title,optionType,messageType, icon, temp, initialValue);
			
			if(runnable!=null) {
				synchronized (runnable) {
					try {
						runnable.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return InstallDlgListener.getResult();
		}
		
		private void printlnLog(String msg)
		{
			Log.i(msg);
			if(InstallDlgListener != null) {
				InstallDlgListener.AddLog(msg);
			}
		}
		
		private int showDeviceList(Runnable runnable) {
			
			@SuppressWarnings("unused")
			int result = InstallDlgListener.ShowDeviceList(runnable);
			
			synchronized (runnable) {
				try {
					runnable.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			return InstallDlgListener.getResult();
		}
		
		public void run(){
			try {
				ArrayList<DeviceStatus> DeviceList;

				
				do {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString(), "", InstallDlg.CHECKLIST_MODE.WATING);
					printlnLog("scan devices...");
					DeviceList = AdbWrapper.scanDevices();
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString(), "", InstallDlg.CHECKLIST_MODE.DONE);
					
					if(DeviceList.size() == 0) {
						printlnLog("Device not found!\nplease check device");
						Listener.SetInstallButtonStatus(true);
						final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
						
						Log.d("show Question");
						
						int n = ShowQuestion(this, Resource.STR_MSG_DEVICE_NOT_FOUND.getString(), Resource.STR_LABEL_WARNING.getString(), JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE, Appicon,
					    		new String[] {Resource.STR_BTN_REFRESH.getString(), Resource.STR_BTN_CANCEL.getString()}, Resource.STR_BTN_REFRESH.getString());
						
						//InstallDlgListener.AddCheckList(Resource.STR_MSG_DEVICE_NOT_FOUND.getString(), "-", InstallDlg.CHECKLIST_MODE.ERROR);
						
						//int n = InstallDlgListener.getResult();
						Log.d(n+"");
						
						if(n==-1 || n==1) {								
							return;
						}
					} else {
						break;
					}
				} while(true);
				DeviceStatus dev = DeviceList.get(0);
								
				if(DeviceList.size() > 1 || (DeviceList.size() == 1 && !dev.status.equals("device"))) {
					//int selectedValue = DeviceListDialog.showDialog();
					//Log.i("Seltected index : " + selectedValue);
					
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString() + " List", "", InstallDlg.CHECKLIST_MODE.QEUESTION);
					int selectedValue = showDeviceList(this);
					if(selectedValue == -1) {
						Listener.SetInstallButtonStatus(true);
						
						return;
					}
					dev = InstallDlgListener.getSelectDev();
					
//					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString() + " List", dev.name +
//							"(" + dev.device + ")", InstallDlg.CHECKLIST_MODE.DONE);
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString()+ " List", dev.name +
							"(" + dev.device + ")", InstallDlg.CHECKLIST_MODE.DONE);
				} else {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString(), dev.name +
							"(" + dev.device + ")", InstallDlg.CHECKLIST_MODE.DONE);
				}

				printlnLog(dev.getSummary());
				
				
				
				boolean alreadyCheak = false;
				printlnLog("getPackageInfo() " + strPackageName);
				PackageInfo pkgInfo = AdbWrapper.getPackageInfo(dev.name, strPackageName);
				
				if(pkgInfo==null) {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_VERSION.getString(), "not install", InstallDlg.CHECKLIST_MODE.ADD);
				} else {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_VERSION.getString(), pkgInfo.versionName + "/"+pkgInfo.versionCode , InstallDlg.CHECKLIST_MODE.ADD);
				}
				
				if(checkPackage) {
					alreadyCheak = true;
					if(pkgInfo != null) {
						String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
						boolean isDeletePossible = true;
						if(pkgInfo.isSystemApp == true && AdbWrapper.hasRootPermission(dev.name) != true) {
							isDeletePossible = false;
						}
						
						InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_ROOT.getString(), ""+isDeletePossible , InstallDlg.CHECKLIST_MODE.ADD);
						
						
						int n;
						if(isDeletePossible) {
							InstallDlgListener.AddCheckList(""+checkPackDelOptions[0] +"/"+ checkPackDelOptions[1]+"/" + checkPackDelOptions[2], "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);							
							n=ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackDelOptions, checkPackDelOptions[3]);
							InstallDlgListener.AddCheckList(""+checkPackDelOptions[n], ""+checkPackDelOptions[n] , InstallDlg.CHECKLIST_MODE.DONE);
							
							
						} else {
							InstallDlgListener.AddCheckList(""+checkPackOptions[0] +"/"+ checkPackOptions[1] , "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
							n=ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackOptions, checkPackOptions[2]);
							InstallDlgListener.AddCheckList(""+checkPackOptions[n], ""+checkPackOptions[n] , InstallDlg.CHECKLIST_MODE.DONE);
						}
						
						
						Log.i("Seltected index : " + n);
						if(n==-1 || (!isDeletePossible && n==2) || (isDeletePossible && n==3)) {
							Listener.SetInstallButtonStatus(true);
							
							return;
						}
						if(n==0) {
							String tmpPath = "/" + dev.name + pkgInfo.apkPath;
							tmpPath = tmpPath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
							tmpPath = FileUtil.makeTempPath(tmpPath)+".apk";
							tmpApkPath = tmpPath; 
							//Log.i(tmpPath);
							InstallDlgListener.AddCheckList("Pull APK", "working" , InstallDlg.CHECKLIST_MODE.WATING);
							AdbWrapper.PullApk(dev.name, pkgInfo.apkPath, tmpPath, new AdbWrapperObserver("pull", dev.name));							
							return;
						}
						if(n==2) {
							//uninstallPanel.setVisible(true);
							if(pkgInfo.isSystemApp) {
								printlnLog("adb shell rm " + pkgInfo.codePath);
								
								InstallDlgListener.AddCheckList("remove APK", "working" , InstallDlg.CHECKLIST_MODE.WATING);
								AdbWrapper.removeApk(dev.name, pkgInfo.codePath);
								InstallDlgListener.AddCheckList("remove APK", "Done" , InstallDlg.CHECKLIST_MODE.DONE);
								
								InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
								final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
								int reboot = ShowQuestion(this, Resource.STR_QUESTION_REBOOT_DEVICE.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.YES_NO_OPTION, 
										JOptionPane.QUESTION_MESSAGE, Appicon, yesNoOptions, yesNoOptions[1]);
								InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), (reboot==0)?"true":"false" , InstallDlg.CHECKLIST_MODE.DONE);
								if(reboot == 0){
									printlnLog("Wait for reboot...");									
									AdbWrapper.reboot(dev.name);
									printlnLog("Reboot...");
								}
								
							} else {
								InstallDlgListener.AddCheckList("Uninstall APK", "-" , InstallDlg.CHECKLIST_MODE.WATING);
								printlnLog("adb uninstall " + pkgInfo.pkgName);
								AdbWrapper.uninstallApk(dev.name, pkgInfo.pkgName);
								InstallDlgListener.AddCheckList("Uninstall APK", "Done" , InstallDlg.CHECKLIST_MODE.DONE);
							}
							printlnLog("compleate");
							//uninstallPanel.setVisible(false);
							Listener.SetInstallButtonStatus(true);
							return;
						}
					} else {
						//JOptionPane.showMessageDialog(null, "동일 패키지가 설치되어 있지 않습니다.", "Info", JOptionPane.INFORMATION_MESSAGE, Appicon);
						InstallDlgListener.AddCheckList("Install", "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
						int n = ShowQuestion(this, Resource.STR_MSG_NO_SUCH_PACKAGE.getString() + "\n" + Resource.STR_QUESTION_CONTINUE_INSTALL.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, Appicon,
								yesNoOptions, yesNoOptions[1]);
						InstallDlgListener.AddCheckList("Install", (n==0)?"Install":"not install" , InstallDlg.CHECKLIST_MODE.DONE);
						if(n==-1 || n==1) {
							Listener.SetInstallButtonStatus(true);
							
							return;
						}
					}
				}
				if(pkgInfo != null) {
					printlnLog(pkgInfo.toString());
					if(pkgInfo.isSystemApp == true) {
						if(AdbWrapper.hasRootPermission(dev.name) == true) {
							printlnLog("adbd is running as root");
							String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
							if(!checkPackage)InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_ROOT.getString(), ""+AdbWrapper.hasRootPermission(dev.name) , InstallDlg.CHECKLIST_MODE.ADD);
							
							InstallDlgListener.AddCheckList("" + options[0] +"/"+ options[1], "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
							int n = ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_PUSH_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
							//Log.i("Seltected index : " + n);
							
							InstallDlgListener.AddCheckList(options[n] + "", (n==0) ?"push":"install", InstallDlg.CHECKLIST_MODE.DONE);
							
							if(n==-1 || n==2) {
								Listener.SetInstallButtonStatus(true);
								
								InstallDlgListener.AddCheckList("Cancel", "cancel", InstallDlg.CHECKLIST_MODE.DONE);
								return;
							} 
							if(n==0) {
								printlnLog("Start push APK");
								//installPanel.setVisible(true);
								InstallDlgListener.AddCheckList("Push", "-" , InstallDlg.CHECKLIST_MODE.WATING);
								AdbWrapper.PushApk(dev.name, strSourcePath, pkgInfo.apkPath, strLibPath, new AdbWrapperObserver("push", dev.name));
								
								return;
							}
							alreadyCheak = true;
						} else {
							printlnLog("adbd cannot run as root in production builds");
						}
					}
					if(samePackage && !alreadyCheak) {
						String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
						InstallDlgListener.AddCheckList("Install", "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
						int n = ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_CONTINUE_INSTALL.getString(),
								Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, yesNoOptions, yesNoOptions[1]);
						//Log.i("Seltected index : " + n);
						
						InstallDlgListener.AddCheckList("Install", (n==-1 || n==1)?"Cancel":"Install" , InstallDlg.CHECKLIST_MODE.DONE);
						
						if(n==-1 || n==1) {
							Listener.SetInstallButtonStatus(true);
							
							return;
						}
					}
				}
				printlnLog("Start install APK");
				//installPanel.setVisible(true);
				InstallDlgListener.AddCheckList("Install", "Install" , InstallDlg.CHECKLIST_MODE.WATING);
				AdbWrapper.InstallApk(dev.name, strSourcePath , new AdbWrapperObserver("install", null));				
			} finally {
				//Listener.SetInstallButtonStatus(true);
				InstallDlgListener.Complete("END");				
			}		
		}
	}

	@SuppressWarnings("deprecation")
	static public void StopThead() {
		
		Listener.SetInstallButtonStatus(true);
		t.stop();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	static public void RestartThread() {
		
		Listener.SetInstallButtonStatus(false);
		//t = new InstallThread();
		if(!t.isAlive()){
			t = new InstallThread();
			t.start();
		}
	}
}


