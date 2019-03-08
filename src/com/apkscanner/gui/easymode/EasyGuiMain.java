package com.apkscanner.gui.easymode;

import java.awt.Color;
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
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;

public class EasyGuiMain extends JFrame implements WindowListener, IDeviceChangeListener {
	private static final long serialVersionUID = -9199974173720756974L;

	private static EasyLightApkScanner apkScanner = null;
	private static EasyGuiMainPanel mainpanel;
	//private String filepath;

	public static long UIstarttime;
	public static long corestarttime;
	public static long UIInittime;
	public static boolean isdecoframe = false;

	public EasyGuiMain(ApkScanner aaptapkScanner) {
		apkScanner = new EasyLightApkScanner(aaptapkScanner);
		ToolEntryManager.initToolEntryManager();
		InitUI();
	}

	public void InitUI() {
		Log.d("main start");

		UIInittime = UIstarttime = System.currentTimeMillis();
		
		//long framestarttime = System.currentTimeMillis();
		setTitle(Resource.STR_APP_NAME.getString());
		//Log.d(""+(System.currentTimeMillis() - framestarttime) );
		
		
		mainpanel = new EasyGuiMainPanel(this, apkScanner);

		if (isdecoframe) {
			setdecoframe();
		} else {
			setResizable(false);
		}
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);		
		add(mainpanel); // 100 => 60
		addWindowListener(this);

		AdbServerMonitor.startServerAndCreateBridgeAsync();
		AndroidDebugBridge.addDeviceChangeListener(this);

		setResizable(true);
		pack();

		Point position = (Point) Resource.PROP_EASY_GUI_WINDOW_POSITION.getData();

		if (position == null) {
			setLocationRelativeTo(null);
		} else {
			setLocation(position);
		}

		if (apkScanner.getlatestError() != 0 || apkScanner.getApkFilePath() == null) {
			Log.d("getlatestError is not 0 or args 0");
			mainpanel.showEmptyinfo();
			setVisible(true);
		}

		Log.d("main End");
		Log.d("init UI   : " + (System.currentTimeMillis() - EasyGuiMain.UIInittime) / 1000.0);
	}

	public static void main(final String[] args) {
		apkScanner = new EasyLightApkScanner();

		new EasyGuiMain(new AaptLightScanner());

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
		setUndecorated(true);
		com.sun.awt.AWTUtilities.setWindowOpacity(this, 1.0f);
	}

	private void changeDeivce() {
		mainpanel.changeDevice(AndroidDebugBridge.getBridge().getDevices());
	}

	public void finished() {
		Log.d("finished()");
		setVisible(false);
		AndroidDebugBridge.removeDeviceChangeListener(this);
		apkScanner.clear(true);
		System.exit(0);
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
	}

	@Override
	public void deviceDisconnected(IDevice arg0) {
		Log.d("deviceDisconnected");
		changeDeivce();

	}
}