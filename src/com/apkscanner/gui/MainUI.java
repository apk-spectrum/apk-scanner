package com.apkscanner.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.LanguageChangeListener;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.plugin.IPlugInEventListener;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.swing.WindowSizeMemorizer;
import com.apkspectrum.util.Log;

public class MainUI extends JFrame implements IPlugInEventListener, LanguageChangeListener
{
	private static final long serialVersionUID = -623259597186280485L;

	private ApkScanner apkScanner;
	private int infoHashCode;
	private ToolBarManagement toolbarManager;

	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;
	private MessageBoxPool messagePool;
	private PlugInDropTargetChooser dropTargetChooser;

	public MainUI(ApkScanner scanner, UiEventHandler eventHandler) {
		initialize(eventHandler);
		setApkScanner(scanner);
		PlugInManager.addPlugInEventListener(this);
	}

	public void setApkScanner(ApkScanner scanner) {
		apkScanner = scanner;
		if(apkScanner != null) {
			boolean changed = apkScanner.getApkInfo() != null
					&& apkScanner.getApkInfo().hashCode() != infoHashCode;
			apkScanner.setStatusListener(new ApkScannerListener(), changed);
		}
	}

	public void initialize(UiEventHandler eventHandler) {
		Log.i("UI Init start");
		messagePool = new MessageBoxPool(this);

		Log.i("initialize() setUIFont");
		String propFont = RProp.S.BASE_FONT.get();
		int propFontStyle = RProp.I.BASE_FONT_STYLE.get();
		int propFontSize = RProp.I.BASE_FONT_SIZE.get();
		setUIFont(new javax.swing.plaf.FontUIResource(propFont, propFontStyle, propFontSize));

		Log.i("initialize() set title & icon");
		setTitle(RStr.APP_NAME.get());
		setIconImage(RImg.APP_ICON.getImage());

		Log.i("initialize() set bound & size ");
		WindowSizeMemorizer.apply(this, new Dimension(RConst.INT_WINDOW_SIZE_WIDTH_MIN, RConst.INT_WINDOW_SIZE_HEIGHT_MIN));
		setResizable(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Log.i("initialize() toolbar init");
		// ToolBar initialize and add
		toolBar = new ToolBar(eventHandler);
		toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
		toolBar.setEnabledAt(ButtonSet.NEED_DEVICE, false);
		add(toolBar, BorderLayout.NORTH);

		toolbarManager = new ToolBarManagement(toolBar);

		Log.i("initialize() tabbedpanel init");
		// TabPanel initialize and add
		String tabbedStyle = RProp.S.TABBED_UI_THEME.get();
		tabbedPanel = new TabbedPanel(tabbedStyle, eventHandler);
		add(tabbedPanel, BorderLayout.CENTER);

		Log.i("initialize() register event handler");
		// Closing event of window be delete tempFile
		addWindowListener(eventHandler);

		// Drag & Drop event processing panel
		dropTargetChooser = new PlugInDropTargetChooser(eventHandler);
		setGlassPane(dropTargetChooser);
		dropTargetChooser.setVisible(true);

		RStr.addLanguageChangeListener(this);
		Log.i("UI Init end");
	}

	public void uiLoadBooster() {
		tabbedPanel.uiLoadBooster();
	}

	@Override
	public void onPluginLoaded() {
		toolBar.onLoadPlugin();
		tabbedPanel.onLoadPlugin(apkScanner.getApkInfo(), apkScanner.getStatus());
	}

	@Override
	public void languageChange(String oldLang, String newLang) {
		ApkInfo apkInfo = apkScanner.getApkInfo();
		String title = RStr.APP_NAME.get();
		if(apkInfo != null) {
			title += " - " + apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)+1);
		}
		setTitle(title);
	}

	public void setUpdatedBadgeCount(int count) {
		toolBar.setBadgeCount(count);
	}

	private static void setUIFont(javax.swing.plaf.FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				if(!"InternalFrame.titleFont".equals(key)) {
					UIManager.put(key, f);
				}
			}
		}
	}

	private class ApkScannerListener implements ApkScanner.StatusListener
	{
		@Override
		public void onStart(final long estimatedTime) {
			Log.i("onStart()");
			toolbarManager.setApkInfo(null);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
					tabbedPanel.setLodingLabel();
				}
			});
		}

		@Override
		public void onSuccess() {
			Log.v("ApkCore.onSuccess()");
		}

		@Override
		public void onError(int error) {
			Log.e("ApkCore.onError() " + error);
			toolbarManager.setApkInfo(null);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					setTitle(RStr.APP_NAME.get());
					tabbedPanel.setData(null, ApkScanner.STATUS_STANBY);
					messagePool.show(MessageBoxPool.MSG_FAILURE_OPEN_APK);
				}
			});
		}

		@Override
		public void onCompleted() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					Log.v("ApkCore.onComplete()");
					toolBar.setEnabledAt(ButtonSet.OPEN, true);
				}
			});
		}

		@Override
		public void onProgress(final int step, final String message) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					switch(step) {
					case 0:
						tabbedPanel.onProgress(message);
						break;
					default:
						Log.i(message);
					}
				}
			});
		}

		@Override
		public void onStateChanged(final int status) {
			//Log.v("onStateChanged() "+ status);

			ApkInfo apkInfo = apkScanner.getApkInfo();

			if(status == ApkScanner.STATUS_STANBY) {
				Log.v("STANBY: does not UI update");
				PlugInManager.setApkInfo(apkInfo);
				return;
			}

			if(!EventQueue.isDispatchThread()) {
				Log.v("onStateChanged() This task is not EDT. Invoke to EDT for " + status);
				try {
					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							onStateChanged(status);
						}
					});
					return;
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}
			}

			switch(status) {
			case ApkScanner.STATUS_ACTIVITY_COMPLETED: case ApkScanner.STATUS_CERT_COMPLETED:
				toolbarManager.setApkInfo(apkInfo);
			default: break;
			}

			Log.i("onStateChanged() ui sync start for " + status);
			switch(status) {
			case ApkScanner.STATUS_BASIC_INFO_COMPLETED:
				PlugInManager.setApkInfo(apkInfo);

				String apkFilePath = apkInfo.filePath;
				String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + RStr.APP_NAME.get();
				setTitle(title);

				if(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX) {
					toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);
				} else {
					toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APEX, true);
				}
				dropTargetChooser.setExternalToolsVisible(true);

				infoHashCode = apkInfo.hashCode();
			default:
				tabbedPanel.setData(apkInfo, status);
				break;
			}
			Log.i("onStateChanged() ui sync end " + status);
		}
	}
}
