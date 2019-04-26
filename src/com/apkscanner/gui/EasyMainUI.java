package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.easymode.*;
import com.apkscanner.gui.easymode.EasyLightApkScanner;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;

import delight.nashornsandbox.internal.RemoveComments;

public class EasyMainUI implements WindowListener, IDeviceChangeListener {
	private static final long serialVersionUID = -9199974173720756974L;

	private static EasyLightApkScanner apkScanner = null;
	private static EasyGuiMainPanel mainpanel;
	//private String filepath;

	public static long UIstarttime;
	public static long corestarttime;
	public static long UIInittime;
	public static boolean isdecoframe = false;

	private JFrame mainframe = null;
	
	public EasyMainUI(ApkScanner aaptapkScanner) {
		mainframe = new JFrame();
		apkScanner = new EasyLightApkScanner(aaptapkScanner);
		ToolEntryManager.initToolEntryManager();
		InitUI();
	}
	
	public EasyMainUI(ApkScanner aaptapkScanner, JFrame mainFrame) {
		this.mainframe = mainFrame;
		apkScanner = new EasyLightApkScanner(aaptapkScanner);
		ToolEntryManager.initToolEntryManager();
		InitUI();
	}

	public void InitUI() {
		Log.d("main start");

		UIInittime = UIstarttime = System.currentTimeMillis();
		
		//long framestarttime = System.currentTimeMillis();
		mainframe.setTitle(Resource.STR_APP_NAME.getString());
		//Log.d(""+(System.currentTimeMillis() - framestarttime) );
		
		
		mainpanel = new EasyGuiMainPanel(mainframe, apkScanner);

		if (isdecoframe) {
			setdecoframe();
		} else {
			mainframe.setResizable(false);
		}
		mainframe.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		mainframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainframe.setMinimumSize(new Dimension(300,200));
		
		mainframe.add(mainpanel); // 100 => 60
		mainframe.addWindowListener(this);

		AdbServerMonitor.startServerAndCreateBridgeAsync();
		AndroidDebugBridge.addDeviceChangeListener(this);

		mainframe.setResizable(true);
		mainframe.pack();

		Point position = (Point) Resource.PROP_EASY_GUI_WINDOW_POSITION.getData();

		if (position == null) {
			mainframe.setLocationRelativeTo(null);
		} else {
			mainframe.setLocation(position);
		}

		if (apkScanner.getlatestError() != 0 || apkScanner.getApkFilePath() == null) {
			Log.d("getlatestError is not 0 or args 0");
			mainpanel.showEmptyinfo();
			mainframe.setVisible(true);
		}

		Log.d("main End");
		Log.d("init UI   : " + (System.currentTimeMillis() - EasyMainUI.UIInittime) / 1000.0);
	}

	public static void main(final String[] args) {
		apkScanner = new EasyLightApkScanner();

		new EasyMainUI(new AaptLightScanner(null));

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

	}

	private void setdecoframe() {
		mainframe.setUndecorated(true);
		com.sun.awt.AWTUtilities.setWindowOpacity(mainframe, 1.0f);
	}

	private void changeDeivce() {
		mainpanel.changeDevice(AndroidDebugBridge.getBridge().getDevices());
	}

	public void finished() {
		Log.d("finished()");
		mainframe.setVisible(false);
		AndroidDebugBridge.removeDeviceChangeListener(this);
		mainframe.removeWindowListener(this);
		apkScanner.clear(true);
		System.exit(0);
	}

	public void clear() {
		Log.d("clear()");
		AndroidDebugBridge.removeDeviceChangeListener(this);
		mainframe.removeWindowListener(this);		
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Log.d("window closing");
		finished();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		Log.d("window closed");
		finished();
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

	@Override
	public void deviceChanged(IDevice arg0, int arg1) {
		Log.d("deviceChanged");
		changeDeivce();

	}

	@Override
	public void deviceConnected(IDevice arg0) {
		Log.d("deviceConnected");
		changeDeivce();
	}

	@Override
	public void deviceDisconnected(IDevice arg0) {
		Log.d("deviceDisconnected");
		changeDeivce();

	}
}