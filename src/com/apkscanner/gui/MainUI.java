package com.apkscanner.gui;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import com.apkscanner.Launcher;
import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.ApktoolManager;
import com.apkscanner.core.DeviceUIManager;
import com.apkscanner.core.ApktoolManager.ApkInfo;
import com.apkscanner.core.ApktoolManager.SolveType;
import com.apkscanner.core.DeviceUIManager.InstallButtonStatusListener;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.ProgressBarDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;


public class MainUI extends JFrame
{
	private static final long serialVersionUID = 1L;

	private ApkScanner apkScanner = null;
	private ApkInfo apkInfo = null;
	
	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;

	private ProgressBarDlg progressBarDlg;
	
	private boolean exiting = false;
	
	public MainUI()
	{
		new Thread(new Runnable() {
			public void run()
			{
				initialize(true);
				progressBarDlg = new ProgressBarDlg(MainUI.this, new UIEventHandler());
				
				apkScanner = new ApkScanner();
			}
		}).start();
	}
	
	public MainUI(final String apkFilePath)
	{
		new Thread(new Runnable() {
			public void run()
			{
				progressBarDlg = new ProgressBarDlg(MainUI.this, new UIEventHandler());
				progressBarDlg.setVisible(true);
				initialize(false);
				
				apkScanner = new ApkScanner();
				apkInfo = apkScanner.openApk(apkFilePath);
			}
		}).start();
	}
	
	public MainUI(final String devSerialNumber, final String packageName, final String resources)
	{
		new Thread(new Runnable() {
			public void run()
			{
				progressBarDlg = new ProgressBarDlg(MainUI.this, new UIEventHandler());
				progressBarDlg.setVisible(true);
				initialize(false);

				apkScanner = new ApkScanner();
				apkInfo = apkScanner.openPackage(devSerialNumber, packageName, resources);
			}
		}).start();
	}

	private void initialize(boolean visible)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		setTitle(Resource.STR_APP_NAME.getString());
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		
		setBounds(0, 0, 650, 520);
		setMinimumSize(new Dimension(650, 520));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(null);

		// ToolBar initialize and add
		toolBar = new ToolBar(new UIEventHandler());
		toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
		add(toolBar, BorderLayout.NORTH);
		
		// TabPanel initialize and add
		tabbedPanel = new TabbedPanel();
		tabbedPanel.setData(null);
		add(tabbedPanel, BorderLayout.CENTER);
		
		if(visible) setVisible(true);
		
		// Closing event of window be delete tempFile
		addWindowListener(new UIEventHandler());
		
		// Drag & Drop event processing
		new FileDrop(this, /*dragBorder,*/ new UIEventHandler()); // end FileDrop.Listener
		
		// Shortcut key event processing
		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(new UIEventHandler());
		
		// MainUI window move event
		getContentPane().addHierarchyBoundsListener(new UIEventHandler());
		new UIEventHandler().ancestorMoved(null);
	}
	
	private class ApkScanner implements ApktoolManager.StatusListener
	{
		private ApktoolManager apkManager;
		
		public ApkInfo openApk(final String apkFilePath)
		{
			return openApk(apkFilePath, null, false);
		}
		
		public ApkInfo openApk(final String apkFilePath, String frameworkRes, boolean isPackage)
		{
			Log.v("openApk() target file :" + apkFilePath);
			//this.apkFilePath = apkFilePath;
			
			if(frameworkRes == null) {
				frameworkRes = (String)Resource.PROP_FRAMEWORK_RES.getData();
			}

			if(apkManager != null) {
				apkManager.clear(false, null);
			}

			apkManager = new ApktoolManager(apkFilePath, frameworkRes, isPackage);
			apkManager.solve(SolveType.RESOURCE, this);

			return apkManager.getApkInfo();
		}

		public ApkInfo openPackage(String devSerialNumber, String devApkFilePath, String framework)
		{
			progressBarDlg.addProgress(1, "I: Open package\n");
			progressBarDlg.addProgress(1, "I: apk path in device : " + devApkFilePath + "\n");
			
			String tempApkFilePath = "/" + devSerialNumber + devApkFilePath;
			tempApkFilePath = tempApkFilePath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
			tempApkFilePath = FileUtil.makeTempPath(tempApkFilePath)+".apk";

			if(framework == null) {
				framework = (String)Resource.PROP_FRAMEWORK_RES.getData();
			}

			String frameworkRes = "";
			if(framework != null && !framework.isEmpty()) {
				for(String s: framework.split(";")) {
					if(s.startsWith("@")) {
						String devNum = s.replaceAll("^@([^/]*)/.*", "$1");
						String path = s.replaceAll("^@[^/]*", "");
						String dest = (new File(tempApkFilePath).getParent()) + File.separator + path.replaceAll(".*/", "");
						progressBarDlg.addProgress(1, "I: start to pull resource apk " + path + "\n");
						AdbWrapper.PullApk_sync(devNum, path, dest);
						frameworkRes += dest + ";"; 
					} else {
						frameworkRes += s + ";"; 
					}
				}
			}

			progressBarDlg.addProgress(1, "I: start to pull apk " + devApkFilePath + "\n");
			AdbWrapper.PullApk_sync(devSerialNumber, devApkFilePath, tempApkFilePath);
			
			if(!(new File(tempApkFilePath)).exists()) {
				Log.e("openPackage() failure : apk pull - " + tempApkFilePath);
				return null;
			}

			return openApk(tempApkFilePath, frameworkRes, true);
		}
		
		public void clear(boolean sync)
		{
			if(apkManager != null)
				apkManager.clear(sync, null);
		}
		
		@Override
		public void OnStart() {
			Log.v("ApkCore.OnStart()");
			setVisible(false);
		}

		@Override
		public void OnSuccess() {
			Log.v("ApkCore.OnSuccess()");
			if(exiting) return;
			
			toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);
			tabbedPanel.setData(apkManager.getApkInfo());
			
			progressBarDlg.setVisible(false);
			
			String apkFilePath = apkManager.getApkInfo().ApkPath;
			String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + Resource.STR_APP_NAME.getString();
			setTitle(title);
			setVisible(true);
			
			if(apkManager.getApkInfo().PermGroupMap.keySet().size() > 30) {
				setSize(new Dimension(650, 570));
			} else {
				setSize(new Dimension(650, 520));
			}
		}

		@Override
		public void OnError() {
			Log.v("ApkCore.OnError()");
			if(exiting) return;
			
			progressBarDlg.setVisible(false);

			setTitle(Resource.STR_APP_NAME.getString());
			tabbedPanel.setData(null);
			setVisible(true);
			
			final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
			//JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
		    JOptionPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_OPEN_APK.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, Appicon,
		    		new String[] {Resource.STR_BTN_CLOSE.getString()}, Resource.STR_BTN_CLOSE.getString());
		}

		@Override
		public void OnComplete() {
			Log.v("ApkCore.OnComplete()");
		}

		@Override
		public void OnProgress(int step, String msg) {
			if(exiting) return;
			
			progressBarDlg.addProgress(step, msg);
		}

		@Override
		public void OnStateChange() {

		}
	}

	class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener, FileDrop.Listener, HierarchyBoundsListener
	{
		private void evtOpenApkFile(boolean newWindow)
		{
			String apkFilePath = ApkFileChooser.openApkFilePath(MainUI.this);
			if(apkFilePath == null) return;

			if(!newWindow) {
				progressBarDlg.init();
				progressBarDlg.setVisible(true);
				apkInfo = apkScanner.openApk(apkFilePath);
			} else {
				Launcher.run(apkFilePath);
			}
		}
		
		private void evtOpenPackage(boolean newWindow)
		{
			PackageTreeDlg Dlg = new PackageTreeDlg();
			Dlg.showTreeDlg();
			
			String device = Dlg.getSelectedDevice();
			String apkFilePath = Dlg.getSelectedApkPath();
			String frameworkRes = Dlg.getSelectedFrameworkRes();

			if(!newWindow) {
				progressBarDlg.init();
				progressBarDlg.setVisible(true);
				setVisible(false);
				
				apkInfo = apkScanner.openPackage(device, apkFilePath, frameworkRes);
			} else {
				Launcher.run(device, apkFilePath, frameworkRes);
			}
		}
		
		private void evtInstallApk(boolean checkPackage)
		{
			if(apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				return;
			}

			toolBar.setEnabledAt(ButtonSet.INSTALL, false);
			String libPath = apkInfo.WorkTempPath + File.separator + "lib" + File.separator;
			new DeviceUIManager(apkInfo.PackageName, apkInfo.ApkPath, libPath , 
					(boolean)Resource.PROP_CHECK_INSTALLED.getData(false), checkPackage, new InstallButtonStatusListener() {
				@Override
				public void SetInstallButtonStatus(Boolean Flag) {
					toolBar.setEnabledAt(ButtonSet.INSTALL, Flag);
				}

				@Override
				public void OnOpenApk(String path) {
					if((new File(path)).exists())
						Launcher.run(path);
				}
			});
		}
		
		private void evtShowManifest()
		{
			if(apkInfo == null) {
				Log.e("evtShowManifest() apkInfo is null");
				return;
			}
			String editor = (String)Resource.PROP_EDITOR.getData();
			if(editor == null) {
				if(System.getProperty("os.name").indexOf("Window") >-1) {
					editor = "notepad";
				} else {  //for linux
					editor = "gedit";
				}
			}
			try {
				new ProcessBuilder(editor, apkInfo.WorkTempPath + File.separator + "AndroidManifest.xml").start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		private void evtShowExplorer()
		{
			if(apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				return;
			}
			try {
				if(System.getProperty("os.name").indexOf("Window") >-1) {
					new ProcessBuilder("explorer", apkInfo.WorkTempPath).start();
				} else {  //for linux
					new ProcessBuilder("nautilus", apkInfo.WorkTempPath).start();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		private void evtSettings()
		{
			(new SettingDlg()).makeDialog();

			String lang = (String)Resource.PROP_LANGUAGE.getData();
			if(lang != null && Resource.getLanguage() != null 
					&& !Resource.getLanguage().equals(lang)) {
				setLanguage(lang);
			}
		}

		private void setLanguage(String lang)
		{
			Resource.setLanguage(lang);
			String title = Resource.STR_APP_NAME.getString();
			if(apkInfo != null) {
				title += " - " + apkInfo.ApkPath.substring(apkInfo.ApkPath.lastIndexOf(File.separator)+1);
			}
			setTitle(title);
			toolBar.reloadResource();
			tabbedPanel.reloadResource();
		}
		
		// ToolBar event processing
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (ToolBar.ButtonSet.OPEN.matchActionEvent(e) || ToolBar.MenuItemSet.OPEN_APK.matchActionEvent(e)) {
				evtOpenApkFile(false);
			} else if(ToolBar.ButtonSet.MANIFEST.matchActionEvent(e)) {
				evtShowManifest();
			} else if(ToolBar.ButtonSet.EXPLORER.matchActionEvent(e)) {
				evtShowExplorer();
			} else if(ToolBar.ButtonSet.INSTALL.matchActionEvent(e) || ToolBar.MenuItemSet.INSTALL_APK.matchActionEvent(e)) {
				evtInstallApk(false);
			} else if(ToolBar.ButtonSet.SETTING.matchActionEvent(e)) {
				evtSettings();
			} else if(ToolBar.ButtonSet.ABOUT.matchActionEvent(e)) {
				AboutDlg.showAboutDialog();
			} else if(ToolBar.MenuItemSet.NEW_EMPTY.matchActionEvent(e)) {
				Launcher.run();
			} else if(ToolBar.MenuItemSet.NEW_APK.matchActionEvent(e)) {
				evtOpenApkFile(true);
			} else if(ToolBar.MenuItemSet.NEW_PACKAGE.matchActionEvent(e)) {
				evtOpenPackage(true);
			} else if(ToolBar.MenuItemSet.OPEN_PACKAGE.matchActionEvent(e)) {
				evtOpenPackage(false);
			} else if(ToolBar.MenuItemSet.INSTALLED_CHECK.matchActionEvent(e)) {
				evtInstallApk(true);
			}
		}

		// Shortcut key event processing
		@Override
		public boolean dispatchKeyEvent(KeyEvent e)
		{
			if(!isFocused()) return false;
			if (e.getID()==KeyEvent.KEY_RELEASED) {
				if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_O: evtOpenApkFile(false);	break;
					case KeyEvent.VK_P: evtOpenPackage(false);	break;
					case KeyEvent.VK_N: Launcher.run();		break;
					case KeyEvent.VK_I: evtInstallApk(false);	break;
					case KeyEvent.VK_T: evtInstallApk(true);	break;
					case KeyEvent.VK_E: evtShowExplorer();		break;
					case KeyEvent.VK_M: evtShowManifest();		break;
					case KeyEvent.VK_S: evtSettings();			break;
					default: return false;
					}
					return true;
				} else if(e.getModifiers() == 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_F1 : AboutDlg.showAboutDialog();	break;
					case KeyEvent.VK_F12: LogDlg.showLogDialog();		break;
					default: return false;
					}
					return true;
				}
			}
			return false;
		}

		// Drag & Drop event processing
		@Override
		public void filesDropped(File[] files)
		{
			try {   
	    		progressBarDlg.init();
				progressBarDlg.setVisible(true);
				apkInfo = apkScanner.openApk(files[0].getCanonicalPath(), (String)Resource.PROP_FRAMEWORK_RES.getData(), false);
	        } catch( java.io.IOException e ) {}
		}
		
		// MainUI window move event 
		@Override
		public void ancestorMoved(HierarchyEvent e)
		{
			if(isVisible()) {
				int nPositionX = getLocationOnScreen().x;
				int nPositionY = getLocationOnScreen().y;
				int nPositionWidth = getWidth();
				DeviceUIManager.setLogWindowPosition(nPositionX + nPositionWidth, nPositionY);
				DeviceUIManager.setLogWindowToFront();
			}
		}
		@Override public void ancestorResized(HierarchyEvent e) { ancestorMoved(e); }

		// Closing event of window be delete tempFile
		@Override
		public void windowClosing(WindowEvent e)
		{
			exiting = true;
			setVisible(false);
			DeviceUIManager.setVisible(false);
			progressBarDlg.setVisible(false);
			apkScanner.clear(true);
		}
		
		@Override public void windowOpened(WindowEvent e) { }
		@Override public void windowClosed(WindowEvent e) { }
		@Override public void windowIconified(WindowEvent e) { }
		@Override public void windowDeiconified(WindowEvent e) { }
		@Override public void windowActivated(WindowEvent e) { }
		@Override public void windowDeactivated(WindowEvent e) { }
	}
}
