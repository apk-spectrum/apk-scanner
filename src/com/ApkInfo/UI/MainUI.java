package com.ApkInfo.UI;

import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import com.ApkInfo.Core.*;


public class MainUI extends Frame implements WindowListener{

	private JFrame frame;
	static MyApkInfo mApkInfo;
	private static String Title = "";
		
	static String Osname ="";
	static String FolderDefault = "";
	static String apkFilePath ="";
	
	static MyCoreThead startCore;
	
	
	//for waiting
	static JFrame WaitingDlg;
	static MyProgressBarDemo ProgressBarDlg;
	
	/**
	 * Launch the application.
	 */
	
	class MyCoreThead extends Thread {
		public void run() {
			try {
				//OS 분기
				Osname = System.getProperty("os.name");
				System.out.println("OS : " + Osname);
				
				if(Osname.indexOf("Windows") ==-1) { //for linux
					FolderDefault = "/tmp/ApkInfo" + apkFilePath.substring(0,apkFilePath.lastIndexOf("."));
				} else { //for windows
					
				}
				
				System.out.println("DefaultFolderName : " +FolderDefault);
				//APK 풀기 
				CoreApkTool.solveAPK(apkFilePath,FolderDefault);
				
				int index = apkFilePath.lastIndexOf(File.separator);					
				Title = apkFilePath.substring(index+1);
				
				System.out.println(FolderDefault);
				mApkInfo = CoreXmlTool.XmlToMyApkinfo(FolderDefault);
				
				
				//progress 삭제 
				WaitingDlg.setVisible(false);
				
				initialize();				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
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
					
					WaitingDlg = MyProgressBarDemo.createAndShowGUI();
					
					CoreApkTool.setProgressBarDlg(ProgressBarDlg);
					CoreXmlTool.setProgressBarDlg(ProgressBarDlg);
					
					MainUI window = new MainUI();
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
		frame.setBounds(100, 100, 550, 500);
		frame.setTitle(Title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		frame.add(new MyTabUI(), BorderLayout.CENTER);
		
		frame.getContentPane().add(new MyButtonPanel(), BorderLayout.SOUTH);
		frame.setResizable( false );
		
		//frame.add(new MyButtonPanel(), BorderLayout.NORTH);
		
		frame.setVisible(true);
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub		
		System.out.println("delete Folder : "  + FolderDefault);		
		if(FolderDefault.length()>0)CoreApkTool.deleteDirectory(new File(FolderDefault));
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
