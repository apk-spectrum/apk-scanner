package com.ApkInfo.UI;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import com.ApkInfo.Core.*;
import com.ApkInfo.Core.ApkManager.ApkInfo;
import com.ApkInfo.Core.ApkManager.ProcessCmd;
import com.ApkInfo.Core.ApkManager.SolveType;
import com.ApkInfo.Core.ApkManager.StatusListener;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UI.DeviceUIManager.InstallButtonStatusListener;
import com.ApkInfo.UI.MyToolBarUI.ButtonId;


public class MainUI extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;
	
	private static JFrame frame;
		
	static MainUI window;
	static private MyTabUI mMyTabUI;
	static private MyToolBarUI mMyToolBarUI;
	
	//window position
	static public int nPositionX,nPositionY;
	
	//for waiting
	static public JFrame WaitingDlg;
	static public MyProgressBarDemo ProgressBarDlg;

	static private ApkManager mApkManager;
	
	public static ApkInfo GetMyApkInfo(){
		return mApkManager.getApkInfo();
	}
		
	public static void openApk(final String apkPath) {
		//System.out.println("target file :" + apkPath);
		mApkManager = new ApkManager(apkPath, new StatusListener() {
			@Override
			public void OnStart(ProcessCmd cmd) {
				System.out.println("ApkCore.OnStart()");
				switch(cmd) {
				case SOLVE_RESOURCE:
					frame.setVisible(false);

					break;
				default:
					break;
				}
			}

			@Override
			public void OnComplete(ProcessCmd cmd) {
				System.out.println("ApkCore.OnComplete()");
				switch(cmd) {
				case SOLVE_RESOURCE:
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, true);
					mMyToolBarUI.setEnabledAt(ButtonId.PACK, false);

					mMyTabUI.setData(mApkManager.getApkInfo());
					WaitingDlg.setVisible(false);

					String title = Resource.STR_APP_NAME.getString() + " - " + apkPath.substring(apkPath.lastIndexOf(File.separator)+1);
					frame.setTitle(title);
					frame.setVisible(true);
					break;
				default:
					break;
				}
			}

			@Override
			public void OnProgress(int step, String msg) {
				System.out.println("ApkCore.OnProgress() " + step + ",  " + msg);
				ProgressBarDlg.addProgress(step, msg);
			}

			@Override
			public void OnStateChange() {
				System.out.println("ApkCore.OnStateChange()");
			}
			
		});
		mApkManager.solve(SolveType.RESOURCE);
	}
	
	class ToolBarListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
	        ApkInfo apkInfo = null;

	        if(mApkManager != null) {
	        	apkInfo = mApkManager.getApkInfo();
	        }
	        
	        JButton b = (JButton) e.getSource();
	        String btn_label = b.getText();
	        
	        if (btn_label.equals(Resource.STR_BTN_OPEN.getString())) {
				JFileChooser jfc = new JFileChooser();
				//jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));
								
				jfc.showOpenDialog(null);
				File dir = jfc.getSelectedFile();

				if(dir!=null) {
					//JOptionPane.showMessageDialog(null, dir.getPath(), "Open", JOptionPane.INFORMATION_MESSAGE);
					
					ProgressBarDlg.init();
					WaitingDlg.setVisible(true);
					MainUI.openApk(dir.getPath());
					
				}
				
	        } else if(btn_label.equals(Resource.STR_BTN_MANIFEST.getString())) {
				  if(System.getProperty("os.name").indexOf("Window") >-1) {
					  try {
						new ProcessBuilder("notepad", apkInfo.WorkTempPath + File.separator + "AndroidManifest.xml").start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}	
				  } else {  //for linux
					  try {
						  new ProcessBuilder("gedit", apkInfo.WorkTempPath + File.separator + "AndroidManifest.xml").start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				  }	
	        } else if(btn_label.equals(Resource.STR_BTN_EXPLORER.getString())) { 
				  if(System.getProperty("os.name").indexOf("Window") >-1) {
					  try {
						new ProcessBuilder("explorer", apkInfo.WorkTempPath).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				  } else {  //for linux
					  try {
						  new ProcessBuilder("nautilus", apkInfo.WorkTempPath).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				  }
	        } else if(btn_label.equals(Resource.STR_BTN_UNPACK.getString())) {
	        	//JOptionPane.showMessageDialog(null, "unpack", "unpack", JOptionPane.INFORMATION_MESSAGE);
	        	if(Resource.getLanguage() == null)
	        		setLanguage("ko");
	        	else
	        		setLanguage(null);
	        } else if(btn_label.equals(Resource.STR_BTN_PACK.getString())) {
	        	JOptionPane.showMessageDialog(null, "pack", "pack", JOptionPane.INFORMATION_MESSAGE);
	        } else if(btn_label.equals(Resource.STR_BTN_INSTALL.getString())) {
	        	mMyToolBarUI.setEnabledAt(ButtonId.INSTALL, false);
			  new DeviceUIManager(apkInfo.PackageName, apkInfo.ApkPath, new InstallButtonStatusListener() {
				@Override
				public void SetInstallButtonStatus(Boolean Flag) {
					// TODO Auto-generated method stub
					mMyToolBarUI.setEnabledAt(ButtonId.INSTALL, Flag);
				}				  
			  });
			  
	        } else if(btn_label.equals(Resource.STR_BTN_ABOUT.getString())) {
	        	final ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon(100,100);
	        	String msg = "";
	        	msg += Resource.STR_APP_NAME.getString() + "\n";
	        	msg += Resource.STR_APP_VERSION.getString() + "\n\n";
	        	msg += Resource.STR_APP_MAKER.getString();
	        	JOptionPane.showMessageDialog(null, msg, Resource.STR_BTN_ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE,Appicon);
	        }
		}
	}

	public static void setLanguage(String lang)
	{
		Resource.setLanguage(lang);
		String title = Resource.STR_APP_NAME.getString() + " - " + mApkManager.getApkInfo().ApkPath.substring(mApkManager.getApkInfo().ApkPath.lastIndexOf(File.separator)+1);
		frame.setTitle(title);
		mMyToolBarUI.reloadResource();
		mMyTabUI.reloadResource();
	}

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				String Osname = System.getProperty("os.name");
				System.out.println("OS : " + Osname);
				//System.out.println("java.io.tmpdir : " + System.getProperty("java.io.tmpdir"));
				//System.out.println("user.dir : " + System.getProperty("user.dir"));
				
				String apkPath = null;
				if(args.length > 0) {
					ProgressBarDlg = new MyProgressBarDemo();
					WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
					WaitingDlg.setVisible(true);
					//Resource.setLanguage("ko");
					window = new MainUI();
					window.initialize();
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);

					apkPath = args[0];
					System.out.println("Target APK : " + args[0]);
					//frame.setVisible(false);
					openApk(apkPath);
				} else {
					window = new MainUI();
					window.initialize();
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);
					
					ProgressBarDlg = new MyProgressBarDemo();
					WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
					
					WaitingDlg.setVisible(false);
					frame.setVisible(true);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainUI() {
		//initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(this);
		frame.setBounds(100, 100, 600, 550);
		frame.setTitle(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		mMyTabUI = new MyTabUI();
		mMyToolBarUI = new MyToolBarUI(new ToolBarListener());

		frame.add(mMyTabUI, BorderLayout.CENTER);
		frame.add(mMyToolBarUI, BorderLayout.NORTH);
		
		//frame.getContentPane().add(new MyButtonPanel(), BorderLayout.SOUTH);
		frame.setResizable( true );
		
		//frame.add(new MyButtonPanel(), BorderLayout.NORTH);
		
        
        ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
        
        frame.setIconImage(Appicon.getImage());
		
		frame.setVisible(false);
		
		nPositionX = 100;
		nPositionY = 100;
        
		frame.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			@Override
			public void ancestorMoved(HierarchyEvent e) {
				if(frame.isVisible()) {
					nPositionX = frame.getLocationOnScreen().x;
					nPositionY = frame.getLocationOnScreen().y;
				}
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				
			}
		});
	}
	
	
	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		frame.setVisible(false);
		if(DeviceUIManager.dlgDialog != null && DeviceUIManager.dlgDialog.isVisible()) {
			DeviceUIManager.dlgDialog.setVisible(false);
		}
		if(mApkManager != null)
			mApkManager.clear(true);
	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

}
