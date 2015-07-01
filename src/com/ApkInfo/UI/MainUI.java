package com.ApkInfo.UI;

import java.awt.EventQueue;
import java.awt.FileDialog;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import com.ApkInfo.Core.*;
import com.ApkInfo.Core.CoreApkTool.FSStyle;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UITEST.Example;


public class MainUI extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;
	
	private JFrame frame;
	static MyApkInfo mApkInfo;
	private static String Title = "";
		
	static String Osname = "";
	static private String apkFilePath = null;
	
	static MyCoreThead startCore;
	
	static MainUI window;
	
	//window position
	static public int nPositionX,nPositionY;
	
	//for waiting
	static public JFrame WaitingDlg;
	static public MyProgressBarDemo ProgressBarDlg;
	/**
	 * Launch the application.
	 */
	public static MyApkInfo GetMyApkInfo(){
		return mApkInfo;
	}	
	
	class MyCoreThead extends Thread {
		public void run() {
			try {
				//OS 분기
				Osname = System.getProperty("os.name");
				System.out.println("OS : " + Osname);

				File apkFile = new File(apkFilePath);

				if(!apkFile.exists()) {
					System.out.println("No Such APK file");
					Title = "APK Scanner - None";
					
					mApkInfo = new MyApkInfo();
				} else {
					apkFilePath = apkFile.getAbsolutePath();

					System.out.println("target file :" + apkFilePath);
					Title = "APK Scanner - " + apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1);

					mApkInfo = new MyApkInfo(apkFilePath, new MyApkInfo.ProgressingMoniter() {
						@Override
						public void progress(int step, String msg) {
							ProgressBarDlg.addProgress(step, msg);
						}
					});
					//mApkInfo.dump();
				}
				
				initialize();				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	class CloseThead extends Thread {
		public void run() {
			System.out.println("delete Folder : "  + mApkInfo.strWorkAPKPath);
			if(mApkInfo.strWorkAPKPath.length()>0) CoreApkTool.deleteDirectory(new File(mApkInfo.strWorkAPKPath));
		}
	}
	
	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {	
				//args = file path
				if(args.length > 0) {
					apkFilePath = args[0];
					System.out.println("Target APK : " + args[0]);
				} else {
					// open file dialog
				}

				if(apkFilePath == null) return;
				
				ProgressBarDlg = new MyProgressBarDemo();
				
				WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
				
				window = new MainUI();
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainUI() {
		//initialize();
		startCore = new MyCoreThead();
		startCore.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(this);
		frame.setBounds(100, 100, 600, 550);
		frame.setTitle(Title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		frame.add(new MyTabUI(), BorderLayout.CENTER);
		frame.add(new Example(), BorderLayout.NORTH);
		
		//frame.getContentPane().add(new MyButtonPanel(), BorderLayout.SOUTH);
		frame.setResizable( false );
		
		//frame.add(new MyButtonPanel(), BorderLayout.NORTH);
		
        
        ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
        
        frame.setIconImage(Appicon.getImage());
		
		frame.setVisible(true);
		
		nPositionX = frame.getLocationOnScreen().x;
		nPositionY = frame.getLocationOnScreen().y;
        
		frame.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			@Override
			public void ancestorMoved(HierarchyEvent e) {
				// TODO Auto-generated method stub
				
				nPositionX = frame.getLocationOnScreen().x;
				nPositionY = frame.getLocationOnScreen().y;
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub		
		frame.setVisible(false);
		if(DeviceUIManager.dlgDialog != null && DeviceUIManager.dlgDialog.isVisible()) {
			DeviceUIManager.dlgDialog.setVisible(false);
		}
		CloseThead temp = new CloseThead();
		temp.start();		
		try {
			temp.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//if(FolderDefault.length()>0) CoreApkTool.deleteDirectory(new File(FolderDefault));
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub		
	}

}
