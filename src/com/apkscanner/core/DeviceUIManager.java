package com.apkscanner.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultCaret;

import com.apkscanner.gui.dialog.DeviceListDialog;
import com.apkscanner.gui.util.ArrowTraversalPane;
import com.apkscanner.gui.util.ButtonType;
import com.apkscanner.gui.util.StandardButton;
import com.apkscanner.gui.util.Theme;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.AdbWrapper;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.AdbWrapper.AdbWrapperListener;
import com.apkscanner.util.AdbWrapper.DeviceStatus;
import com.apkscanner.util.AdbWrapper.PackageInfo;

public class DeviceUIManager
{
	static private JTextArea dialogLogArea;
	static private JDialog dlgDialog = null;

	static private JPanel installPanel;
	static private JPanel uninstallPanel;
	
	private String strPackageName;
	private String strSourcePath;
	private String strLibPath;
	private String tmpApkPath;
	
	//window position
	static private int nPositionX, nPositionY;
	
	public interface InstallButtonStatusListener
	{
		public void SetInstallButtonStatus(Boolean Flag);
		public void OnOpenApk(String path);
	}
	private InstallButtonStatusListener Listener;
	
	public DeviceUIManager(String PackageName, String apkPath, String libPath, 
			final boolean samePackage, final boolean checkPackage, final InstallButtonStatusListener Listener)
	{
		final ImageIcon Appicon = Resource.IMG_QUESTION.getImageIcon();
        final Object[] options = {Resource.STR_BTN_PUSH.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] checkPackOptions = {Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] checkPackDelOptions = {Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_DEL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
		strPackageName = PackageName;
		strSourcePath = apkPath;
		strLibPath = libPath;
		
		ShowSetupLogDialog();
		dialogLogArea.setText("");
		
		this.Listener = Listener; 
		
		Thread t = new Thread(new Runnable() {
			public void run(){
				ArrayList<DeviceStatus> DeviceList;
				do {
					printlnLog("scan devices...");
					DeviceList = AdbWrapper.scanDevices();
	
					if(DeviceList.size() == 0) {
						printlnLog("Device not found!\nplease check device");
						Listener.SetInstallButtonStatus(true);
						final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
						//JOptionPane.showMessageDialog(null, "Device not found!\nplease check Connected","Warning", JOptionPane.WARNING_MESSAGE, Appicon);
						int n = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_DEVICE_NOT_FOUND.getString(), Resource.STR_LABEL_WARNING.getString(), JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE, Appicon,
					    		new String[] {Resource.STR_BTN_REFRESH.getString(), Resource.STR_BTN_CANCEL.getString()}, Resource.STR_BTN_REFRESH.getString());
						if(n==-1 || n==1) {
							setVisible(false);
							return;
						}
					} else {
						break;
					}
				} while(true);

				DeviceStatus dev = DeviceList.get(0);
				if(DeviceList.size() > 1 || (DeviceList.size() == 1 && !dev.status.equals("device"))) {
					int selectedValue = DeviceListDialog.showDialog();
					//Log.i("Seltected index : " + selectedValue);
					
					if(selectedValue == -1) {
						Listener.SetInstallButtonStatus(true);
						setVisible(false);
						return;
					}
					dev = DeviceListDialog.getSelectedData();
				}
				printlnLog(dev.getSummary());
				
				boolean alreadyCheak = false;
				printlnLog("getPackageInfo() " + strPackageName);
				PackageInfo pkgInfo = AdbWrapper.getPackageInfo(dev.name, strPackageName);
				
				if(checkPackage) {
					alreadyCheak = true;
					if(pkgInfo != null) {
						String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
						boolean isDeletePossible = true;
						if(pkgInfo.isSystemApp == true && AdbWrapper.hasRootPermission(dev.name) != true) {
							isDeletePossible = false;
						}
						int n;
						if(isDeletePossible) {
							n = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackDelOptions, checkPackDelOptions[3]);
						} else {
							n = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackOptions, checkPackOptions[2]);							
						}
						//Log.i("Seltected index : " + n);
						if(n==-1 || (!isDeletePossible && n==2) || (isDeletePossible && n==3)) {
							Listener.SetInstallButtonStatus(true);
							setVisible(false);
							return;
						}
						if(n==0) {
							String tmpPath = "/" + dev.name + pkgInfo.apkPath;
							tmpPath = tmpPath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
							tmpPath = FileUtil.makeTempPath(tmpPath)+".apk";
							tmpApkPath = tmpPath; 
							//Log.i(tmpPath);
							AdbWrapper.PullApk(dev.name, pkgInfo.apkPath, tmpPath, new AdbWrapperObserver("pull", dev.name));
							return;
						}
						if(n==2) {
							uninstallPanel.setVisible(true);
							if(pkgInfo.isSystemApp) {
								printlnLog("adb shell rm " + pkgInfo.codePath);
								AdbWrapper.removeApk(dev.name, pkgInfo.codePath);
								
								final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
								int reboot = ArrowTraversalPane.showOptionDialog(null, Resource.STR_QUESTION_REBOOT_DEVICE.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.YES_NO_OPTION, 
										JOptionPane.QUESTION_MESSAGE, Appicon, yesNoOptions, yesNoOptions[1]);
								if(reboot == 0){
									printlnLog("Wait for reboot...");
									AdbWrapper.reboot(dev.name);
									printlnLog("Reboot...");
								}
							} else {
								printlnLog("adb uninstall " + pkgInfo.pkgName);
								AdbWrapper.uninstallApk(dev.name, pkgInfo.pkgName);
							}
							printlnLog("compleate");
							uninstallPanel.setVisible(false);
							Listener.SetInstallButtonStatus(true);
							return;
						}
					} else {
						//JOptionPane.showMessageDialog(null, "동일 패키지가 설치되어 있지 않습니다.", "Info", JOptionPane.INFORMATION_MESSAGE, Appicon);
						int n = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_NO_SUCH_PACKAGE.getString() + "\n" + Resource.STR_QUESTION_CONTINUE_INSTALL.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, Appicon,
								yesNoOptions, yesNoOptions[1]);
						if(n==-1 || n==1) {
							Listener.SetInstallButtonStatus(true);
							setVisible(false);
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
							int n = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_PUSH_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
							//Log.i("Seltected index : " + n);
							if(n==-1 || n==2) {
								Listener.SetInstallButtonStatus(true);
								setVisible(false);
								return;
							} 
							if(n==0) {
								printlnLog("Start push APK");
								installPanel.setVisible(true);
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
						int n = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_CONTINUE_INSTALL.getString(),
								Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, yesNoOptions, yesNoOptions[1]);
						//Log.i("Seltected index : " + n);
						if(n==-1 || n==1) {
							Listener.SetInstallButtonStatus(true);
							setVisible(false);
							return;
						}
					}
				}
				printlnLog("Start install APK");
				installPanel.setVisible(true);
				AdbWrapper.InstallApk(dev.name, strSourcePath , new AdbWrapperObserver("install", null));
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
		if(dialogLogArea != null) {
			dialogLogArea.append(msg+"\n");
		}
	}
	
	public static void setLogWindowPosition(int x, int y) {
		
		if(dlgDialog != null) {
			dlgDialog.setLocation(x, y);
		} else {
			nPositionX = x;
			nPositionY = y;
		}
	}
	
	public static void setLogWindowToFront() {
		if(dlgDialog != null) {
			dlgDialog.toFront();
			dlgDialog.repaint();
		}
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
			ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_INSTALLED.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, WaringAppicon,
		    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
		}

		@Override
		public void OnSuccess() {
			if(type.equals("push")) {
				final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
				int reboot = ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_SUCCESS_INSTALLED.getString() + "\n" + Resource.STR_QUESTION_REBOOT_DEVICE.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, QuestionAppicon, yesNoOptions, yesNoOptions[1]);
				if(reboot == 0){
					printlnLog("Wait for reboot...");
					AdbWrapper.reboot(device);
					printlnLog("Reboot...");
				}
			} else if(type.equals("install")) {
				//JOptionPane.showMessageDialog(null, "Success", "Complete", JOptionPane.INFORMATION_MESSAGE, SucAppicon);
				ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_SUCCESS_INSTALLED.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, SucAppicon,
			    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
			} else if(type.equals("pull")) {
				setVisible(false);
				if(Listener != null) Listener.OnOpenApk(tmpApkPath);
			} 
		}

		@Override
		public void OnCompleted() {
			Listener.SetInstallButtonStatus(true);
			installPanel.setVisible(false);
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
			        	
					dlgDialog.setTitle(Resource.STR_LABEL_LOG.getString());
					dlgDialog.setModal(false);
					dlgDialog.setLocation(nPositionX, nPositionY);
					
					dlgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
					dlgDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
					dlgDialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
						private static final long serialVersionUID = -1499175497935529270L;
						public void actionPerformed(ActionEvent e) {
							dlgDialog.dispose();
					    }
					});
			        	
					JLabel GifLabel, waitbar, installlabel;
					ImageIcon icon = Resource.IMG_INSTALL_WAIT.getImageIcon();
					ImageIcon waitbaricon = Resource.IMG_WAIT_BAR.getImageIcon();
					installPanel = new JPanel();
					
					GifLabel = new JLabel(icon);
					waitbar = new JLabel(waitbaricon);
					installlabel = new JLabel(Resource.STR_LABEL_INSTALLING.getString());
					
					installPanel.add(installlabel);
					installPanel.add(waitbar);
					
					installPanel.setVisible(false);
					installPanel.setOpaque(true);

					uninstallPanel = new JPanel();
					uninstallPanel.add(new JLabel(Resource.STR_LABEL_UNINSTALLING.getString()));
					uninstallPanel.add(new JLabel(waitbaricon));
					uninstallPanel.setVisible(false);
					uninstallPanel.setOpaque(true);
					
					DialogPanel.add(GifLabel);
					
					
			        JPanel containerPanel = new JPanel();
			        containerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 46, 150));
			        containerPanel.setLayout(new BorderLayout());
			        
					StandardButton btnOK;
					btnOK = new StandardButton(Resource.STR_BTN_CLOSE.getString(),Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);
					btnOK.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {		                    		                    		                    
							dlgDialog.setVisible(false);
						}
					});

					DialogPanel.add(btnOK);
					DialogPanel.add(containerPanel);
					DialogPanel.add(installPanel);
					DialogPanel.add(uninstallPanel);
					//DialogPanel.add(installlabel);
					//DialogPanel.add(waitbar);
					
					
					dlgDialog.setSize(new Dimension(480,210));
					dlgDialog.setResizable( false );
					dlgDialog.add(DialogPanel);
					
					//dlgDialog.setLocationRelativeTo(null);
					dlgDialog.setVisible(true);
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
		
		
		dialogLogArea = new JTextArea(7,30); 
		
		DefaultCaret caret = (DefaultCaret) dialogLogArea.getCaret(); // ←
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
		
		dialogLogArea.setWrapStyleWord(true);
		dialogLogArea.setLineWrap(true);
		dialogLogArea.setEditable(false);
		
		DiaPanel.add(new JScrollPane(dialogLogArea));
		
		return DiaPanel;
	}
}


