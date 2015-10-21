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
import java.io.FileWriter;
import java.io.IOException;

import com.apkscanner.Launcher;
import com.apkscanner.core.AaptToolManager;
import com.apkscanner.core.ApkScannerStub;
import com.apkscanner.core.ApkScannerStub.Status;
import com.apkscanner.data.ApkInfo;
import com.apkscanner.gui.ApkInstaller.InstallButtonStatusListener;
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

	private ApkScannerStub apkScanner = null;
	
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
				
				apkScanner = new AaptToolManager(new ApkScannerListener());
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
				
				apkScanner = new AaptToolManager(new ApkScannerListener());
				apkScanner.openApk(apkFilePath);
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

				apkScanner = new AaptToolManager(new ApkScannerListener());
				apkScanner.openPackage(devSerialNumber, packageName, resources);
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
		
		setBounds(0, 0, 650, 490);
		setMinimumSize(new Dimension(650, 490));
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
	
	private class ApkScannerListener implements ApkScannerStub.StatusListener
	{
		@Override
		public void OnStart() {
			Log.v("ApkCore.OnStart()");
			setVisible(false);
			tabbedPanel.initLabel();
		}

		@Override
		public void OnSuccess() {
			Log.v("ApkCore.OnSuccess()");
			if(exiting) return;
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
			Log.i(msg);
			progressBarDlg.addProgress(step, msg+"\n");
		}

		@Override
		public void OnStateChanged(Status status)
		{
			Log.i("OnStateChanged() "+ status);
			switch(status) {
			case BASIC_INFO_COMPLETED:
				String apkFilePath = apkScanner.getApkInfo().ApkPath;
				String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + Resource.STR_APP_NAME.getString();
				setTitle(title);
				
				if(apkScanner.getApkInfo().PermGroupMap.keySet().size() > 30) {
					setSize(new Dimension(650, 530));
				} else {
					setSize(new Dimension(650, 490));
				}

				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);
				tabbedPanel.setData(apkScanner.getApkInfo(), 0);
				
				progressBarDlg.setVisible(false);
				setVisible(true);
				break;
			case WIDGET_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 1);
				break;
			case LIB_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 2);
				break;
			case IMAGE_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 3);
				break;
			case ACTIVITY_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 4);
				break;
			case CERT_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 5);
				if(isVisible()) {
					tabbedPanel.setData(apkScanner.getApkInfo(), 0);
				}
				break;
			default:
				break;
			}
		}
	}

	class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener, FileDrop.Listener, HierarchyBoundsListener
	{
		private void evtOpenApkFile(boolean newWindow)
		{
			String apkFilePath = ApkFileChooser.openApkFilePath(MainUI.this);
			if(apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}

			if(!newWindow) {
				progressBarDlg.init();
				progressBarDlg.setVisible(true);
				apkScanner.openApk(apkFilePath);
			} else {
				Launcher.run(apkFilePath);
			}
		}
		
		private void evtOpenPackage(boolean newWindow)
		{
			PackageTreeDlg Dlg = new PackageTreeDlg();
			if(Dlg.showTreeDlg(MainUI.this) != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}
			
			String device = Dlg.getSelectedDevice();
			String apkFilePath = Dlg.getSelectedApkPath();
			String frameworkRes = Dlg.getSelectedFrameworkRes();

			if(!newWindow) {
				progressBarDlg.init();
				progressBarDlg.setVisible(true);
				setVisible(false);
				
				apkScanner.openPackage(device, apkFilePath, frameworkRes);
			} else {
				Launcher.run(device, apkFilePath, frameworkRes);
			}
		}
		
		private void evtInstallApk(boolean checkPackage)
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtInstallApk() apkInfo is null");
				return;
			}

			toolBar.setEnabledAt(ButtonSet.INSTALL, false);
			String libPath = apkInfo.WorkTempPath + File.separator + "lib" + File.separator;
			new ApkInstaller(apkInfo.PackageName, apkInfo.ApkPath, libPath , 
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
			ApkInfo apkInfo = apkScanner.getApkInfo();
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
				String manifestPath = apkInfo.WorkTempPath + File.separator + "AndroidManifest.xml";
				File manifestFile = new File(manifestPath); 
				if(!manifestFile.exists()) {
					if(!manifestFile.getParentFile().exists()) {
						if(FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
							Log.d("sucess make folder");
						}
					}
					FileWriter fw = new FileWriter(new File(manifestPath));
					
					fw.write(((AaptToolManager)apkScanner).makeAndroidManifestXml());
					//for(String line: ((AaptToolManager)apkScanner).getAndroidManifest()) {
						//fw.write(line);
						//fw.write("\r\n");
					//}
					fw.close();
				}
				new ProcessBuilder(editor, manifestPath).start();
			} catch (IOException e1) {
			}
		}
		
		private void evtShowExplorer()
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtShowExplorer() apkInfo is null");
				return;
			}
			try {
				if(System.getProperty("os.name").indexOf("Window") >-1) {
					new ProcessBuilder("explorer", apkInfo.ApkPath).start();
				} else {  //for linux
					new ProcessBuilder("file-roller", apkInfo.ApkPath).start();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		private void evtSettings()
		{
			(new SettingDlg()).makeDialog(MainUI.this);

			String lang = (String)Resource.PROP_LANGUAGE.getData();
			if(lang != null && Resource.getLanguage() != null 
					&& !Resource.getLanguage().equals(lang)) {
				setLanguage(lang);
			}
		}

		private void setLanguage(String lang)
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
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
				AboutDlg.showAboutDialog(MainUI.this);
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
					case KeyEvent.VK_F1 : AboutDlg.showAboutDialog(MainUI.this);break;
					case KeyEvent.VK_F12: LogDlg.showLogDialog(MainUI.this);	break;
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
				apkScanner.openApk(files[0].getCanonicalPath(), (String)Resource.PROP_FRAMEWORK_RES.getData());
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
			}
		}
		@Override public void ancestorResized(HierarchyEvent e) { ancestorMoved(e); }

		// Closing event of window be delete tempFile
		@Override
		public void windowClosing(WindowEvent e)
		{
			exiting = true;
			setVisible(false);
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
