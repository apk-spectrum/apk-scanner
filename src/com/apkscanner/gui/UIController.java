package com.apkscanner.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.Launcher;
import com.apkscanner.gui.easymode.dlg.EasyStartupDlg;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.scanner.AaptLightScanner;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.plugin.PlugInEventAdapter;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.plugin.UpdateChecker;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

public class UIController implements Runnable, InvocationHandler {
	public static final String APKSCANNER_GUI_APKSCANNER = "APKSCANNER";
	public static final String APKSCANNER_GUI_EASY_APKSCANNER = "EASY_APKSCANNER";

	private static UIController instance;

	private ApkScanner apkScanner;

	private MainUI mainUI;
	private EasyMainUI easymainUI;
	private JFrame mainframe;
	private JDialog lodingDlg;

	private UiEventHandler eventHandler;

	private int updatedBadgeCount;

	private UIController(ApkScanner apkScanner) {
		if(apkScanner == null) {
			apkScanner = ApkScanner.getInstance();
		}
		this.apkScanner = apkScanner;
		eventHandler = new UiEventHandler(apkScanner);
		PlugInManager.setActionEventHandler(eventHandler);

		initMacApplication();
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
		loadPlugIn();
	}

	private void createAndShowGUI() {
		Log.i("start UIController");

		final boolean isEasyGui = RProp.B.USE_EASY_UI.get();

		Log.i("setLookAndFeel");
		setLookAndFeel(isEasyGui);

		Log.i("creat frame");
		if(isEasyGui) {
			mainframe = easymainUI = new EasyMainUI(apkScanner, eventHandler);
		} else {
			mainframe = mainUI = new MainUI(apkScanner, eventHandler);
		}
		mainframe.setVisible(true);
		eventHandler.registerKeyStrokeAction(mainframe.getRootPane());

		if(RProp.B.USE_UI_BOOSTER.get()) {
			uiLoaderBooster(isEasyGui);
		}

		if(!RProp.B.SKIP_STARTUP_EASY_UI_DLG.get()) {
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
			UIManager.setLookAndFeel(RProp.S.CURRENT_THEME.get());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	}

	private void uiLoaderBooster(final boolean isEasyGui) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
            	synchronized(instance) {
            		try {
        				instance.wait();
        			} catch (InterruptedException e) { }
	                if(isEasyGui) {
	                	if(mainUI == null) {
	                		mainUI = new MainUI(null, eventHandler);
		                	mainUI.uiLoadBooster();
		                	mainUI.setUpdatedBadgeCount(updatedBadgeCount);
		                	eventHandler.registerKeyStrokeAction(mainUI.getRootPane());
	                	}
	                } else {
	                	if(easymainUI == null) {
	                		easymainUI = new EasyMainUI(null, eventHandler);
		                	eventHandler.registerKeyStrokeAction(easymainUI.getRootPane());
	                	}
	                }
            	}
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
	}

	public void showAdbServerLodingDlg(final boolean visible) {
		Log.v("showAdbServerLodingDlg() " + visible);

    	EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(lodingDlg == null && visible) {
					lodingDlg = new JDialog(mainframe, true);
					lodingDlg.setSize(mainframe.getSize());
					lodingDlg.setEnabled(false);
					lodingDlg.setUndecorated(true);
					lodingDlg.setLocationRelativeTo(mainframe);
					lodingDlg.setBackground(new Color(255,255,255,192));
					JLabel lodingLable = new JLabel("Waiting for ADB Server.", RImg.RESOURCE_TREE_OPEN_JD_LOADING.getImageIcon(), SwingConstants.CENTER);
					Font font = lodingLable.getFont();
					lodingLable.setFont(font.deriveFont(Font.BOLD, 20.0f));
					lodingLable.setHorizontalTextPosition(JLabel.CENTER);
					lodingLable.setVerticalTextPosition(JLabel.BOTTOM);
					lodingDlg.add(lodingLable);
				}

				if(visible) {
			        new Timer().schedule(new TimerTask() {
			            @Override
			            public void run() {
			            	EventQueue.invokeLater(new Runnable() {
			        			@Override
			        			public void run() {
			        				if(lodingDlg == null) return;
			        				lodingDlg.setVisible(true);
			        			}
			            	});
			            }
			        }, 100);
				} else if(lodingDlg != null) {
					lodingDlg.dispose();
					lodingDlg = null;
				}
			}
    	});
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
							mainUI = new MainUI(apkScanner, eventHandler);
							mainUI.setUpdatedBadgeCount(updatedBadgeCount);
		                	eventHandler.registerKeyStrokeAction(mainUI.getRootPane());
						} else {
							mainUI.setApkScanner(apkScanner);
						}
					}
					mainframe = mainUI;
				} else {
					synchronized(instance) {
						if(easymainUI == null) {
							easymainUI = new EasyMainUI(apkScanner, eventHandler);
		                	eventHandler.registerKeyStrokeAction(easymainUI.getRootPane());
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
		PlugInManager.setAppPackage("com.apkscanner", RStr.APP_VERSION.get(), RStr.APP_NAME, RImg.APP_ICON);
		PlugInManager.setLang(RStr.getLanguage());
		PlugInManager.addPlugInEventListener(new PlugInEventAdapter() {
			@Override
			public void onPluginLoaded() {
		        PlugInManager.checkForUpdatesWithUI(mainframe, 1000);
			}

			@Override
			public void onPluginUpdated(UpdateChecker[] list) {
				if(list != null && list.length > 0) {
					updatedBadgeCount = list.length;
					if(mainUI != null) mainUI.setUpdatedBadgeCount(updatedBadgeCount);
				}
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

	public static void sendEvent(String actionCommand) {
		UIController thiz = getInstance();
		thiz.eventHandler.sendEvent(thiz.mainframe, actionCommand);
	}

	private void initMacApplication() {
		if(!SystemUtil.isMac()) return;
		boolean isJdk9 = SystemUtil.checkJvmVersion("9");
	    try {
	        final Class<?> applicationClass = Class.forName(isJdk9 ? "java.awt.Desktop" : "com.apple.eawt.Application");
	        final Class<?> openFilesHandlerClass = Class.forName(isJdk9 ? "java.awt.desktop.OpenFilesHandler" : "com.apple.eawt.OpenFilesHandler");
	        final Method getApplication = applicationClass.getMethod(isJdk9 ? "getDesktop" : "getApplication");
	        final Object application = getApplication.invoke(null);
	        final Method setOpenFileHandler = applicationClass.getMethod("setOpenFileHandler", openFilesHandlerClass);
	        final ClassLoader openFilesHandlerClassLoader = openFilesHandlerClass.getClassLoader();
	        final Object openFilesHandlerObject = Proxy.newProxyInstance(openFilesHandlerClassLoader,
	        		new Class<?>[] { openFilesHandlerClass }, this);
	        setOpenFileHandler.invoke(application, openFilesHandlerObject);
	    } catch (Exception e) {
	        Log.e("Exception adding OS X file open handler");
	    }
	    try {
	        final Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
	        final Method getApplication = applicationClass.getMethod("getApplication");
	        final Object application = getApplication.invoke(null);
	        final Method setDockIconImage = applicationClass.getMethod("setDockIconImage", Image.class);
	        setDockIconImage.invoke(application, RImg.APP_ICON.getImage());
	    } catch (Exception e) {
	        Log.e("Exception change OS X icon");
	    }
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("openFiles")) {
			boolean isJdk9 = SystemUtil.checkJvmVersion("9");
			final Class<?> openFilesEventClass = Class.forName(isJdk9 ? "java.awt.desktop.FilesEvent" : "com.apple.eawt.AppEvent$OpenFilesEvent");
			final Method getFiles = openFilesEventClass.getMethod("getFiles");
			Object e = args[0];
			try {
				@SuppressWarnings("unchecked")
				final List<File> ff = (List<File>) getFiles.invoke(e);
				boolean isEmpty = apkScanner.getStatus() == 0;
                for (final File file : ff){
                    if(isEmpty) {
        				Thread thread = new Thread(new Runnable() {
        					public void run() {
        						apkScanner.openApk(file.getAbsolutePath());
        					}
        				});
        				thread.setPriority(Thread.NORM_PRIORITY);
        				thread.start();
        				isEmpty = false;
                    } else {
                    	Launcher.run(file.getAbsolutePath());
                    }
                }
			} catch (RuntimeException ee) {
				throw ee;
			} catch (Exception ee) {
				throw new RuntimeException(ee);
			}
		}
		return null;
	}
}
