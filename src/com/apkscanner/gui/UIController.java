package com.apkscanner.gui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.easymode.dlg.EasyStartupDlg;
import com.apkscanner.plugin.IPlugInEventListener;
import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.NetworkException;
import com.apkscanner.plugin.PlugInConfig;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.gui.NetworkErrorDialog;
import com.apkscanner.plugin.gui.UpdateNotificationWindow;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class UIController implements Runnable {
	public static final String APKSCANNER_GUI_APKSCANNER = "APKSCANNER";
	public static final String APKSCANNER_GUI_EASY_APKSCANNER = "EASY_APKSCANNER";

	private static UIController instance;

	private ApkScanner apkScanner;

	private MainUI mainUI = null;
	private EasyMainUI easymainUI = null;
	private JFrame mainframe = null;

	private int updatedBadgeCount;

	private UIController(ApkScanner apkScanner) {
		if(apkScanner == null) {
			apkScanner = ApkScanner.getInstance();
		}
		this.apkScanner = apkScanner;

		loadPlugIn();
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

		final boolean isEasyGui = (boolean) Resource.PROP_USE_EASY_UI.getData();

		Log.i("setLookAndFeel");
		setLookAndFeel(isEasyGui);

		Log.i("creat frame");
		if(isEasyGui) {
			mainframe = easymainUI = new EasyMainUI(apkScanner);
		} else {
			mainframe = mainUI = new MainUI(apkScanner);
		}
		mainframe.setVisible(true);

		if((boolean)Resource.PROP_USE_UI_BOOSTER.getData()) {
			uiLoaderBooster(isEasyGui);
		}

		if(!(boolean) Resource.PROP_SKIP_STARTUP_EASY_UI_DLG.getData()) {
			if(EasyStartupDlg.showAboutDialog(mainframe)) {
				changeGui(isEasyGui ? APKSCANNER_GUI_APKSCANNER : APKSCANNER_GUI_EASY_APKSCANNER);
			}
		}
	}

	private void setLookAndFeel(boolean useDelay) {
		if(useDelay) {
	        new Timer().schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	EventQueue.invokeLater(new Runnable() {
	        			@Override
	        			public void run() {
	        				setLookAndFeel(false);
	        				synchronized (instance) {
		        				instance.notify();
							}
	        			}
	            	});
	            }
	        }, 500);
	        return;
		}
		try {
			UIManager.setLookAndFeel((String)Resource.PROP_CURRENT_THEME.getData());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	}

	private void uiLoaderBooster(final boolean isEasyGui) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
            	synchronized(instance) {
	                if(isEasyGui) {
	            		try {
	        				instance.wait();
	        			} catch (InterruptedException e) { }
	                	if(mainUI == null) mainUI = new MainUI(null);
	                	mainUI.uiLoadBooster();
	                	mainUI.setUpdatedBadgeCount(updatedBadgeCount);
	                } else {
	                	if(easymainUI == null) easymainUI = new EasyMainUI(null);
	                }
            	}
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
	}

	public void changeGui(final String state) {
		final boolean isEasyGui = APKSCANNER_GUI_EASY_APKSCANNER.equals(state);

		final String apkPath = apkScanner.getApkInfo() != null ? apkScanner.getApkInfo().filePath : null;

		if(apkScanner instanceof AaptLightScanner) {
			apkScanner.setStatusListener(null);
			((AaptLightScanner) apkScanner).setLightMode(isEasyGui);
		} else {
			apkScanner = ApkScanner.getInstance(ApkScanner.APKSCANNER_TYPE_AAPTLIGHT);
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

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainframe.setVisible(false);
				if(!isEasyGui) {
					synchronized(instance) {
						if(mainUI == null) {
							mainUI = new MainUI(apkScanner);
							mainUI.setUpdatedBadgeCount(updatedBadgeCount);
						} else {
							mainUI.setApkScanner(apkScanner);
						}
					}
					mainframe = mainUI;
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
			}
		});
	}

	public static void changeToMainGui() {
		getInstance().changeGui(APKSCANNER_GUI_APKSCANNER);
	}

	public static void changeToEasyGui() {
		getInstance().changeGui(APKSCANNER_GUI_EASY_APKSCANNER);
	}

	private void loadPlugIn() {
		PlugInManager.setLang(Resource.getLanguage());
		PlugInManager.addPlugInEventListener(new IPlugInEventListener() {
			@Override
			public void onPluginLoaded() {
		        new Timer().schedule(new TimerTask() {
		            @Override
		            public void run() {
		            	checkUpdated(PlugInManager.getUpdateChecker());
		            }
		        }, 1000);
			}
		});

		Thread thread = new Thread(new Runnable() {
            public void run() {
				PlugInManager.loadPlugIn();
            }
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	private void checkUpdated(final IUpdateChecker[] updater) {
        new SwingWorker<IUpdateChecker[], IUpdateChecker>() {
			@Override
			protected IUpdateChecker[] doInBackground() throws Exception {
				ArrayList<IUpdateChecker> newUpdates = new ArrayList<>();
				for(IUpdateChecker uc: updater) {
					if(!uc.wasPeriodPassed()) {
						if(uc.hasNewVersion()) {
							newUpdates.add(uc);
						}
						continue;
					}
					try {
						if(uc.checkNewVersion()) {
							newUpdates.add(uc);
						};
					} catch (NetworkException e) {
						publish(uc);
						if(e.isNetworkNotFoundException()) {
							Log.d("isNetworkNotFoundException");
							break;
						}
					}
				}
				return newUpdates.toArray(new IUpdateChecker[newUpdates.size()]);
			}

			@Override
			protected void process(List<IUpdateChecker> updater) {
				ArrayList<IUpdateChecker> retryUpdates = new ArrayList<>();
				for(IUpdateChecker uc: updater) {
					int ret = NetworkErrorDialog.show(mainframe, uc);
					switch(ret) {
					case NetworkErrorDialog.RESULT_RETRY:
						retryUpdates.add(uc);
					}
				}
				if(!retryUpdates.isEmpty()) {
					checkUpdated(retryUpdates.toArray(new IUpdateChecker[retryUpdates.size()]));
				}
			}

			@Override
			protected void done() {
				IUpdateChecker[] updaters = null;
				try {
					updaters = get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				if(updaters != null && updaters.length > 0) {
					updatedBadgeCount = updaters.length;
					if(mainUI != null) mainUI.setUpdatedBadgeCount(updatedBadgeCount);
					if(!"true".equals(PlugInConfig.getGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP))) {
						UpdateNotificationWindow.show(mainframe, updaters);
					}
				}
				PlugInManager.saveProperty();
			}
		}.execute();
	}
}
