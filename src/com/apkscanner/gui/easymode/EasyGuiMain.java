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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class EasyGuiMain implements WindowListener, IDeviceChangeListener {
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
		ToolEntryManager.initToolEntryManager();
		InitUI();
	}

	public void InitUI() {
		Log.d("main start");

		UIInittime = UIstarttime = System.currentTimeMillis();
		frame = new JFrame(Resource.STR_APP_NAME.getString()); // 200
		mainpanel = new EasyGuiMainPanel(frame, apkScanner);

		if (isdecoframe) {
			setdecoframe();
		} else {
			frame.setResizable(false);
		}
		frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.add(mainpanel); // 100 => 60
		frame.addWindowListener(this);

		AdbServerMonitor.startServerAndCreateBridgeAsync();
		AndroidDebugBridge.addDeviceChangeListener(this);

		// frame.setResizable(true);
		frame.pack();

		Point position = (Point) Resource.PROP_EASY_GUI_WINDOW_POSITION.getData();

		if (position == null) {
			frame.setLocationRelativeTo(null);
		} else {
			frame.setLocation(position);
		}

		if (apkScanner.getlatestError() != 0 || apkScanner.getApkFilePath() == null) {
			Log.d("getlatestError is not 0 or args 0");
			mainpanel.showEmptyinfo();
			frame.setVisible(true);
		}

		Log.d("main End");
		Log.d("init UI   : " + (System.currentTimeMillis() - EasyGuiMain.UIInittime) / 1000.0);
	}

	public static void main(final String[] args) {
		apkScanner = new EasyLightApkScanner();

		EasyGuiMain mainFrame = new EasyGuiMain(new AaptLightScanner());

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

	private static void setdecoframe() {
		frame.setUndecorated(true);
		com.sun.awt.AWTUtilities.setWindowOpacity(frame, 1.0f);
	}

	private void changeDeivce() {
		mainpanel.changeDevice(AndroidDebugBridge.getBridge().getDevices());
	}

	public void finished() {
		Log.d("finished()");
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
		// TODO Auto-generated method stub
		Log.d("deviceChanged");
		changeDeivce();

	}

	@Override
	public void deviceConnected(IDevice arg0) {
		// TODO Auto-generated method stub

		Log.d("deviceConnected");
	}

	@Override
	public void deviceDisconnected(IDevice arg0) {
		// TODO Auto-generated method stub
		Log.d("deviceDisconnected");
		changeDeivce();

	}
}