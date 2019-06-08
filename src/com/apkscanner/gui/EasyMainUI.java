package com.apkscanner.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.easymode.EasyGuiMainPanel;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;

public class EasyMainUI extends JFrame implements WindowListener, IDeviceChangeListener {
	private static final long serialVersionUID = -1104109718930033124L;

	private ApkScanner apkScanner = null;
	private EasyGuiMainPanel mainpanel;
	//private String filepath;

	public static long UIstarttime;
	public static long corestarttime;
	public static long UIInittime;
	public static boolean isdecoframe = false;

	public EasyMainUI(ApkScanner scanner) {
		apkScanner = scanner;
		ToolEntryManager.initToolEntryManager();
		InitUI();
		setApkScanner(scanner);
	}

	public void setApkScanner(ApkScanner scanner) {
		if(scanner != null) {
			apkScanner = scanner;
			ToolEntryManager.apkScanner = apkScanner;
			mainpanel.setApkScanner(apkScanner);
		}
	}

	public void InitUI() {
		Log.d("main start");

		UIInittime = UIstarttime = System.currentTimeMillis();

		//long framestarttime = System.currentTimeMillis();
		setTitle(Resource.STR_APP_NAME.getString());
		//Log.d(""+(System.currentTimeMillis() - framestarttime) );

		Log.i("initialize() setUIFont");
		//long aaa = System.currentTimeMillis();
//		String propFont = (String) Resource.PROP_BASE_FONT.getData();
//		int propFontStyle = (int)Resource.PROP_BASE_FONT_STYLE.getInt();
//		int propFontSize = (int) Resource.PROP_BASE_FONT_SIZE.getInt();
		//setUIFont(new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));
		//20ms
		//Log.d("init setUIFont   : " + (System.currentTimeMillis() - aaa) / 1000.0);

		Log.i("initialize() setLookAndFeel");
//		try {
//			UIManager.setLookAndFeel((String)Resource.PROP_CURRENT_THEME.getData());
//		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
//				| UnsupportedLookAndFeelException e1) {
//			e1.printStackTrace();
//		} //윈도우 400ms
		Log.i("new EasyGuiMainPanel");
		mainpanel = new EasyGuiMainPanel(this);
		mainpanel.setApkScanner(apkScanner);

		if (isdecoframe) {
			setdecoframe();
		} else {
			setResizable(true);
		}
		Log.i("setIconImage");
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(300,200));

		Log.i("add(mainpanel)");
		add(mainpanel); // 100 => 60

		addWindowListener(this);

		AdbServerMonitor.startServerAndCreateBridgeAsync();
		AndroidDebugBridge.addDeviceChangeListener(this);
		Log.i("pack");
		pack();

		Log.i("setLocationRelativeTo");
		String setposition = (String)Resource.PROP_EASY_GUI_WINDOW_POSITION_X.getData();

		if (setposition == null) {
			setLocationRelativeTo(null);
			Point position = getLocation();
			Resource.PROP_EASY_GUI_WINDOW_POSITION_X.setData(position.x+"");
			Resource.PROP_EASY_GUI_WINDOW_POSITION_Y.setData(position.y+"");
			Log.d(Resource.PROP_EASY_GUI_WINDOW_POSITION_X.getData() + "");
		} else { // setLocationRelativeTo(null); 100 ms
			int x = Integer.parseInt((String)Resource.PROP_EASY_GUI_WINDOW_POSITION_X.getData());
			int y = Integer.parseInt((String)Resource.PROP_EASY_GUI_WINDOW_POSITION_Y.getData());

			setLocation(new Point(x,y));
//			Resource.PROP_EASY_GUI_WINDOW_POSITION_Y.getData()));
		}

		if (apkScanner != null &&
				(apkScanner.getLastErrorCode() != 0 || apkScanner.getApkInfo() == null)
				&& !apkScanner.isCompleted(Status.ALL_COMPLETED)) {
			Log.d("getlatestError is not 0 or args 0");
			mainpanel.showEmptyinfo();
		}

		KeyStrokeAction.registerKeyStrokeActions(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false)
			}, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int keycode = Integer.parseInt(e.getActionCommand());
					switch(keycode) {
					case KeyEvent.VK_ESCAPE:
						switch((int)Resource.PROP_ESC_ACTION.getInt()) {
						case Resource.INT_ESC_ACT_NONE: return;
						case Resource.INT_ESC_ACT_CHANG_UI_MODE: break;
						case Resource.INT_ESC_ACT_EXIT: dispose(); return;
						}
					case KeyEvent.VK_F2:
						UIController.changeToMainGui();
						break;
					case KeyEvent.VK_F12:
						LogDlg.showLogDialog(EasyMainUI.this);
						break;
					}
				}
			}
		);
		
		KeyStrokeAction.registerKeyStrokeActions(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
			new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false),
				//KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false),
				//KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, false),
				//KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, false),
				//KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
				//KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
				//KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true)
			},
			new String[] {
				Resource.STR_BTN_ABOUT.getString(),
				Resource.STR_BTN_OPEN.getString(),
				Resource.STR_BTN_OPEN_PACKAGE.getString(),
				Resource.STR_BTN_INSTALL.getString(),
				Resource.STR_BTN_EXPLORER.getString(),
				Resource.STR_BTN_MANIFEST.getString(),
			},
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ToolEntryManager.excuteEntry(e.getActionCommand());
				}
			}
		);

		Log.d("main End");
		Log.d("init UI   : " + (System.currentTimeMillis() - EasyMainUI.UIInittime) / 1000.0);

	}

//	public static void main(final String[] args) {
//		apkScanner = new EasyLightApkScanner();
//
//		new EasyMainUI(new AaptLightScanner(null));
//
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				Thread thread = new Thread(new Runnable() {
//					public void run() {
//						apkScanner.clear(false);
//						corestarttime = System.currentTimeMillis();
//						if (args.length > 0) {
//							apkScanner.setApk(args[0]);
//						} else {
//							apkScanner.setApk("");
//						}
//					}
//				});
//				thread.setPriority(Thread.NORM_PRIORITY);
//				thread.start();
//			}
//		}); //// 70ms
//
//	}

	private void setdecoframe() {
		setUndecorated(true);
		com.sun.awt.AWTUtilities.setWindowOpacity(this, 1.0f);
	}

//	private static void setUIFont(javax.swing.plaf.FontUIResource f) {
//		Enumeration<Object> keys = UIManager.getDefaults().keys();
//		while (keys.hasMoreElements()) {
//			Object key = keys.nextElement();
//			Object value = UIManager.get(key);
//			if (value instanceof javax.swing.plaf.FontUIResource) {
//				if(!"InternalFrame.titleFont".equals(key)) {
//					UIManager.put(key, f);
//				}
//			}
//		}
//	}

	private void changeDeivce() {
		mainpanel.changeDevice(AndroidDebugBridge.getBridge().getDevices());
	}

	public void finished() {
		Log.d("finished()");
		AndroidDebugBridge.removeDeviceChangeListener(this);
		removeWindowListener(this);
		apkScanner.clear(true);
		System.exit(0);
	}

	public void clear() {
		Log.d("clear()");
		AndroidDebugBridge.removeDeviceChangeListener(this);
		removeWindowListener(this);
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