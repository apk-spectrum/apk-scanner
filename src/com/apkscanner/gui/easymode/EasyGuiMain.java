package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
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

import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class EasyGuiMain{
	public static JFrame frame;
	private static final EasyLightApkScanner apkScanner = new EasyLightApkScanner();
	
	public static long UIstarttime;
	public static long corestarttime;
	public static long UIInittime;
	
	public EasyGuiMain() {
	}
	public static void main(final String[] args) {
		Log.d("main start");
		UIInittime = UIstarttime = System.currentTimeMillis();
   	
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
						if(args.length > 0) {
							corestarttime = System.currentTimeMillis();
							apkScanner.setApk(args[0]);
						}
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
            }
        });    //// 70ms        
        
    	frame = new JFrame(Resource.STR_APP_NAME.getString()); //200
    	Resource.setLanguage("ko");
    	//frame.setUndecorated(true);
    	frame.setResizable(false);
    	frame.add(new EasyGuiMainPanel(frame, apkScanner)); //100  => 60
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setResizable(true);
        frame.pack();
        
//        long UIsetlocationttime = System.currentTimeMillis();
        //remove 
        //frame.setLocationRelativeTo(null);  //30 slow
        
//        int lebar = frame.getWidth()/2;
//        int tinggi = frame.getHeight()/2;
//        int x = (Toolkit.getDefaultToolkit().getScreenSize().width/2)-lebar;
//        int y = (Toolkit.getDefaultToolkit().getScreenSize().height/2)-tinggi;
//        Log.d( " setLocationRelativeTo  : " + ( System.currentTimeMillis() - UIsetlocationttime )/1000.0 );
         
        frame.setLocation(500, 500);
        frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage()); //20
        
        if(apkScanner.getlatestError() != 0 || args.length == 0) {
        	Log.d("getlatestError is not 0 or args 0");
        	frame.setVisible(true);
        }
		Log.d("main End");
		Log.d( " init UI   : " + ( System.currentTimeMillis() - EasyGuiMain.UIInittime )/1000.0 );
		

	}
}