package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class EasyGuiMain {
	public static JFrame frame;
	private static EasyLightApkScanner apkScanner = null;
	private static EasyGuiMainPanel mainpanel;
	private String filepath;
	
	public static long UIstarttime;
	public static long corestarttime;
	public static long UIInittime;
	public static boolean isdecoframe = true;
	
	
	public EasyGuiMain(AaptLightScanner aaptapkScanner) {
		this.apkScanner = new EasyLightApkScanner(aaptapkScanner);
		InitUI();
	}

	public void InitUI() {
		Log.d("main start");
		ToolEntryManager.initToolEntryManager();
		UIInittime = UIstarttime = System.currentTimeMillis();
		frame = new JFrame(Resource.STR_APP_NAME.getString()); // 200
		mainpanel = new EasyGuiMainPanel(frame, apkScanner);

		if (isdecoframe) {
			setdecoframe();
		} else {
			frame.setResizable(false);
		}
		frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainpanel); // 100 => 60
		// frame.setResizable(true);
		frame.pack();

		frame.setLocation(500, 500);
		// frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		// //20

		if (apkScanner.getlatestError() != 0 || apkScanner.getApkFilePath() == null) {
			Log.d("getlatestError is not 0 or args 0");
			 mainpanel.showEmptyinfo();
			frame.setVisible(true);
		}
		
		Log.d("main End");
		Log.d("init UI   : " + (System.currentTimeMillis() - EasyGuiMain.UIInittime) / 1000.0);
	}
	
	public static void main(final String[] args) {
		Log.d("main start");
		Resource.setLanguage((String) Resource.PROP_LANGUAGE.getData(SystemUtil.getUserLanguage()));
		ToolEntryManager.initToolEntryManager();
		
		apkScanner = new EasyLightApkScanner();	

		UIInittime = UIstarttime = System.currentTimeMillis();
		frame = new JFrame(Resource.STR_APP_NAME.getString()); // 200
		mainpanel = new EasyGuiMainPanel(frame, apkScanner);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						apkScanner.clear(false);
						corestarttime = System.currentTimeMillis();
						if (args.length > 0) {
							apkScanner.setApk(args[0]);
						} else {
							apkScanner.setApk("");
						}
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
			}
		}); //// 70ms

		if (isdecoframe) {
			setdecoframe();
		} else {
			frame.setResizable(false);
		}
		frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainpanel); // 100 => 60
		// frame.setResizable(true);
		frame.pack();

		// long UIsetlocationttime = System.currentTimeMillis();
		// remove
		// frame.setLocationRelativeTo(null); //30 slow

		// int lebar = frame.getWidth()/2;
		// int tinggi = frame.getHeight()/2;
		// int x = (Toolkit.getDefaultToolkit().getScreenSize().width/2)-lebar;
		// int y =
		// (Toolkit.getDefaultToolkit().getScreenSize().height/2)-tinggi;
		// Log.d( " setLocationRelativeTo : " + ( System.currentTimeMillis() -
		// UIsetlocationttime )/1000.0 );

		frame.setLocation(500, 500);
		// frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		// //20

		Log.d(args.length + "");
		if (apkScanner.getlatestError() != 0 || args.length == 0) {
			Log.d("getlatestError is not 0 or args 0");
			//mainpanel.showEmptyinfo();
			frame.setVisible(true);
		}

		Log.d("main End");
		Log.d("init UI   : " + (System.currentTimeMillis() - EasyGuiMain.UIInittime) / 1000.0);
	}

	private static void setdecoframe() {
		frame.setUndecorated(true);
		com.sun.awt.AWTUtilities.setWindowOpacity(frame, 1.0f);
	}
}