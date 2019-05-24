package com.apkscanner;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.MainUI;
import com.apkscanner.plugin.IPlugInEventListener;
import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.PlugInConfig;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.gui.NetworkErrorDialog;
import com.apkscanner.plugin.gui.UpdateNotificationWindow;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class UIController implements Runnable, IPlugInEventListener {
	public static final String APKSCANNER_GUI_APKSCANNER = "APKSCANNER";
	public static final String APKSCANNER_GUI_EASY_APKSCANNER = "EASY_APKSCANNER";

	private static UIController instance;

	private ApkScanner apkScanner;

	private MainUI mainUI = null;
	private EasyMainUI easymainUI = null;
	private JFrame mainframe = null;

	private UIController(ApkScanner apkScanner) {
		if(apkScanner == null) {
			apkScanner = ApkScanner.getInstance();
		}
		this.apkScanner = apkScanner;

		PlugInManager.setLang(Resource.getLanguage());
		PlugInManager.addPlugInEventListener(this);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				PlugInManager.loadPlugIn();
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	public static UIController getInstance(ApkScanner apkScanner) {
		if(instance == null) {
			instance = new UIController(apkScanner);
		}
		return instance;
	}

	public static UIController getInstance() {
		return getInstance(null);
	}

	@Override
	public void run() {
		if(!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(this);
			return;
		}
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		Log.i("start UIController");

		Log.i("setLookAndFeel");
		try {
			UIManager.setLookAndFeel((String)Resource.PROP_CURRENT_THEME.getData());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		final boolean isEasyGui = (boolean) Resource.PROP_USE_EASY_UI.getData();

		Log.i("creat frame");
		if(isEasyGui) {
			mainframe = easymainUI = new EasyMainUI(apkScanner);
		} else {
			mainframe = mainUI = new MainUI(apkScanner);
		}

		mainframe.setVisible(true);

        Thread thread = new Thread(new Runnable() {
            public void run() {
            	synchronized(instance) {
	                if(isEasyGui) {
	                	if(mainUI == null) mainUI = new MainUI(null);
	                } else {
	                	if(easymainUI == null) easymainUI = new EasyMainUI(null);
	                }
            	}
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

		if(!(boolean) Resource.PROP_SKIP_STARTUP_EASY_UI_DLG.getData()) {
			if(EasyMainUI.showDlgStartupEasyMode(mainframe)) {
				changeGui(isEasyGui ? APKSCANNER_GUI_APKSCANNER : APKSCANNER_GUI_EASY_APKSCANNER);
			}
		}
	}

	public void changeGui(final String state) {
		final boolean isEasyGui = APKSCANNER_GUI_EASY_APKSCANNER.equals(state);

		final String apkPath = apkScanner.getApkInfo() != null ? apkScanner.getApkInfo().filePath : null;
		if(!isEasyGui) {
			apkScanner = ApkScanner.getInstance(ApkScanner.APKSCANNER_TYPE_AAPT);
		} else {
			apkScanner = ApkScanner.getInstance(ApkScanner.APKSCANNER_TYPE_AAPTLIGHT);
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainframe.setVisible(false);
				mainframe.getContentPane().removeAll();
				if(!isEasyGui) {
					synchronized(instance) {
						if(mainUI == null) {
							mainUI = new MainUI(apkScanner);
						} else {
							mainUI.setApkScanner(apkScanner);
						}
					}
					mainframe = mainUI;
					PlugInManager.addPlugInEventListener(mainUI);
				} else {
					synchronized(instance) {
						if(easymainUI == null) {
							easymainUI = new EasyMainUI(apkScanner);
						} else {
							easymainUI.setApkScanner(apkScanner);
						}
					}
					mainframe = easymainUI;
				}
				mainframe.setVisible(true);
				if(apkPath != null) {
					Thread thread = new Thread(new Runnable() {
						public void run() {
							apkScanner.openApk(apkPath);
						}
					});
					thread.setPriority(Thread.NORM_PRIORITY);
					thread.start();
				}
			}
		});
	}

	public static void changeToMainGui() {
		getInstance().changeGui(APKSCANNER_GUI_APKSCANNER);
	}

	public static void changeToEasyGui() {
		getInstance().changeGui(APKSCANNER_GUI_EASY_APKSCANNER);
	}

	@Override
	public void onPluginLoaded() {
		PlugInManager.checkUpdated();
	}

	@Override
	public void onUpdated(IUpdateChecker[] plugins) {
		if(!"true".equals(PlugInConfig.getGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP))) {
			UpdateNotificationWindow.show(mainframe, plugins);
		}
	}

	@Override
	public boolean onUpdateFailed(IUpdateChecker plugin) {
		int ret = NetworkErrorDialog.show(mainframe, plugin);
		switch(ret) {
		case NetworkErrorDialog.RESULT_RETRY:
			return true;
		}
		return false;
	}
}
