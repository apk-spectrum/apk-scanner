package com.ApkInfo.UI;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import com.ApkInfo.Core.*;
import com.ApkInfo.Core.CoreApkTool.FSStyle;


public class MainUI extends JFrame implements WindowListener
{
	static public String VERSION = "Ver. 1.01";

	private JFrame frame;
	static MyApkInfo mApkInfo;
	private static String Title = "";
		
	static String Osname ="";
	static public String FolderDefault = "";
	static String apkFilePath ="";
	
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
				} else {
					apkFilePath = apkFile.getAbsolutePath();
					System.out.println("target file :" + apkFilePath);
				}

				Title = "APK Scanner - " + apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1);

				FolderDefault = CoreApkTool.makeTempPath(apkFilePath);
				System.out.println("Temp path : " + FolderDefault);

				//APK 풀기 
				CoreApkTool.solveAPK(apkFilePath, FolderDefault);
				
				mApkInfo = CoreXmlTool.XmlToMyApkinfo(FolderDefault);
				mApkInfo.strApkSize = CoreApkTool.getFileSize(apkFile, FSStyle.FULL);
				
				mApkInfo.ImagePathList = CoreApkTool.findFiles(new File(FolderDefault + File.separator + "res"), ".png", ".*drawable.*");
				mApkInfo.LibPathList = CoreApkTool.findFiles(new File(FolderDefault + File.separator + "lib"), ".so", null);

				mApkInfo.CertList = CoreCertTool.solveCert(FolderDefault + File.separator + "original" + File.separator + "META-INF" + File.separator);
				
				initialize();				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	class CloseThead extends Thread {
		public void run() {
			System.out.println("delete Folder : "  + FolderDefault);
			if(FolderDefault.length()>0) CoreApkTool.deleteDirectory(new File(FolderDefault));
		}
	}
	
	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {	
				//args = file path					
				apkFilePath = args[0];
				System.out.println("Target APK : " + args[0]);
				
				//CoreApkTool.makeFolder("temp");
				
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
		frame.setBounds(100, 100, 600, 500);
		frame.setTitle(Title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		frame.add(new MyTabUI(), BorderLayout.CENTER);
		
		frame.getContentPane().add(new MyButtonPanel(), BorderLayout.SOUTH);
		frame.setResizable( false );
		
		//frame.add(new MyButtonPanel(), BorderLayout.NORTH);
		
        
		String ImgPath = CoreApkTool.GetUTF8Path();
        ImageIcon Appicon = new ImageIcon(ImgPath+File.separator+"AppIcon.png");
        
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
