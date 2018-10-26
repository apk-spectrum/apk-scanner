package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
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

public class EasyGuiMain{
	public static JFrame frame;
	private static final EasyLightApkScanner apkScanner = new EasyLightApkScanner();
	
	public EasyGuiMain() {
	}
	public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
//						apkScanner.setApk("/media/leejinhyeong/Perforce/DCM_APP_DEV_LJH_DEV/PEACE/Cinnamon/applications/provisional/JPN/DCM/apps/DCMAccountAuthenticator/generic/DCMAccountAuthenticator.apk");
					
						if(args.length > 0) {
							apkScanner.setApk(args[0]);
						}
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
            	
//            	apkScanner.openApk("/media/leejinhyeong/Perforce/DCM_APP_DEV_LJH_DEV/OHIO81/Cinnamon/applications/provisional/JPN/DCM/apps/DCMContacts/starqltedcm/DCMContacts_eng.apk");
//            	
//            	ApkInfo apkinfo = apkScanner.getApkInfo();
            	
//            	Log.d(apkinfo.manifest.packageName);
            	
            	frame = new JFrame("APKScanner - DCMHome.apk");
                frame.setUndecorated(true);                
                frame.add(new EasyGuiMainPanel(frame, apkScanner));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
                frame.setVisible(true);         
                
				//apkScanner.clear(false);				
				
            }
        });
	}
}