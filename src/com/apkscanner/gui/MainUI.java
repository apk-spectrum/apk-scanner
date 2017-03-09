package com.apkscanner.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScannerStub;
import com.apkscanner.core.scanner.ApkScannerStub.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.SearchDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.dex2jar.Dex2JarWrapper;
import com.apkscanner.tool.jd_gui.JDGuiLauncher;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;


public class MainUI extends JFrame
{
	private static final long serialVersionUID = 1L;

	private ApkScannerStub apkScanner = null;
	
	private TabbedPanel tabbedPanel;
	private ToolBar toolBar;
	
	private boolean exiting = false;
	
	private Object uiInitSync = new Object();
	private Object labelInitSync = new Object();
	
	static private JFrame frame;
	
	public MainUI()
	{
		new Thread(new Runnable() {
			public void run()
			{
				initialize(false);
				tabbedPanel.setData(null);
				apkScanner = new AaptScanner(new ApkScannerListener());
			}
		}).start();
	}
	
	public MainUI(final String apkFilePath)
	{
		synchronized(uiInitSync) {
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(uiInitSync) {
						uiInitSync.notify();
						try {
							uiInitSync.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.i("UI Init start");
						initialize(true);
						tabbedPanel.setLodingLabel();
						Log.i("UI Init end");
					}
				}
			}).start();
			try {
				uiInitSync.wait();
				uiInitSync.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		new Thread(new Runnable() {
			public void run()
			{
				apkScanner = new AaptScanner(new ApkScannerListener());
				apkScanner.openApk(apkFilePath);
			}
		}).start();
	}
	
	public MainUI(final String devSerialNumber, final String packageName, final String resources)
	{
		synchronized(uiInitSync) {
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(uiInitSync) {
						uiInitSync.notify();
						try {
							uiInitSync.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.i("UI Init start");
						initialize(true);
						tabbedPanel.setLodingLabel();
						tabbedPanel.setProgress(-1);
						Log.i("UI Init end");
					}
				}
			}).start();
			try {
				uiInitSync.wait();
				uiInitSync.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		new Thread(new Runnable() {
			public void run()
			{
				apkScanner = new AaptScanner(new ApkScannerListener());
				apkScanner.openPackage(devSerialNumber, packageName, resources);
			}
		}).start();
	}

	private void initialize(boolean opening)
	{
		Log.i("initialize() setLookAndFeel");
		try {
			
			if(Resource.PROP_CURRENT_THEME.getData()==null) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(Resource.PROP_CURRENT_THEME.getData().toString());
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		Log.i("initialize() set title/icon");
		setTitle(Resource.STR_APP_NAME.getString());
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		
		Log.i("initialize() set bound");
		setBounds(0, 0, 650, 490);
		setMinimumSize(new Dimension(650, 490));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(null);

		Log.i("initialize() toolbar init");
		// ToolBar initialize and add
		toolBar = new ToolBar(new UIEventHandler());
		toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
		add(toolBar, BorderLayout.NORTH);
		
		Log.i("initialize() tabbedpanel init");
		// TabPanel initialize and add
		tabbedPanel = new TabbedPanel(opening);
		add(tabbedPanel, BorderLayout.CENTER);
		
		Log.i("initialize() visible");
		setVisible(true);
		
		Log.i("initialize() register event handler");
		// Closing event of window be delete tempFile
		addWindowListener(new UIEventHandler());
		
		// Drag & Drop event processing
		new FileDrop(this, /*dragBorder,*/ new UIEventHandler()); // end FileDrop.Listener
		
		// Shortcut key event processing
		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(new UIEventHandler());
		
		frame = this;
	}
	
	public static JFrame getCurrentParentsFrame() {
		return frame;
	}
	
	private class ApkScannerListener implements ApkScannerStub.StatusListener
	{
		@Override
		public void onStart(final long estimatedTime) {
			synchronized(labelInitSync) {
				new Thread(new Runnable() {
					public void run()
					{
						synchronized(labelInitSync) {
							labelInitSync.notify();
							try {
								labelInitSync.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							synchronized(uiInitSync) {
								Log.i("OnStart() uiInitSync");	
							}
						}
						if(toolBar != null) {
							toolBar.setEnabledAt(ButtonSet.OPEN, false);
							toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
						}
					}
				}).start();
				try {
					labelInitSync.wait();
					labelInitSync.notify();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onSuccess() {
			Log.v("ApkCore.onSuccess()");
			if(exiting) return;
		}

		@Override
		public void onError(int error) {
			Log.e("ApkCore.onError() " + error);
			if(exiting) return;

			setTitle(Resource.STR_APP_NAME.getString());
			if(tabbedPanel != null) {
				tabbedPanel.setData(null);
			}
			
			final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
			//JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
		    JOptionPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_OPEN_APK.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, Appicon,
		    		new String[] {Resource.STR_BTN_CLOSE.getString()}, Resource.STR_BTN_CLOSE.getString());
		}

		@Override
		public void onComplete() {
			Log.v("ApkCore.onComplete()");
			toolBar.setEnabledAt(ButtonSet.OPEN, true);
		}

		@Override
		public void onProgress(int step, String msg) {
			if(exiting) return;
			switch(step) {
			case 0:
				tabbedPanel.setProgress(Integer.valueOf(msg));
				break;
			default:
				Log.i(msg);
			}
		}

		@Override
		public void onStateChanged(Status status)
		{
			Log.i("onStateChanged() "+ status);
			synchronized(labelInitSync) {
				synchronized(uiInitSync) {
					Log.i("OnStateChanged() sync "+ status);	
				}
			}
			switch(status) {
			case BASIC_INFO_COMPLETED:
				String apkFilePath = apkScanner.getApkInfo().filePath;
				String title = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1) + " - " + Resource.STR_APP_NAME.getString();
				setTitle(title);
				
				//if(apkScanner.getApkInfo().PermGroupMap.keySet().size() > 30) {
				//	setSize(new Dimension(650, 530));
				//} else {
					setSize(new Dimension(650, 490));
				//}

				Log.i(status + " ui sync start");
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);
				if(tabbedPanel!=null) tabbedPanel.setData(apkScanner.getApkInfo(), 0);
				
				Log.i(status + " ui sync end");
				break;
			case WIDGET_COMPLETED:
				Log.i(status + " ui sync start");
				tabbedPanel.setData(apkScanner.getApkInfo(), 1);
				Log.i(status + " ui sync end");
				break;
			case LIB_COMPLETED:
				Log.i(status + " ui sync start");
				tabbedPanel.setData(apkScanner.getApkInfo(), 2);
				Log.i(status + " ui sync end");
				break;
			case RESOURCE_COMPLETED:
				Log.i(status + " ui sync start");
				tabbedPanel.setData(apkScanner.getApkInfo(), 3);
				Log.i(status + " ui sync end");
				break;
			case RES_DUMP_COMPLETED:
				tabbedPanel.setData(apkScanner.getApkInfo(), 3 + TabbedPanel.CMD_EXTRA_DATA);
				break;
			case ACTIVITY_COMPLETED:
				Log.i(status + " ui sync start");
				tabbedPanel.setData(apkScanner.getApkInfo(), 4);
				Log.i(status + " ui sync end");
				break;
			case CERT_COMPLETED:
				Log.i(status + " ui sync start");
				tabbedPanel.setData(apkScanner.getApkInfo(), 0 + TabbedPanel.CMD_EXTRA_DATA);
				tabbedPanel.setData(apkScanner.getApkInfo(), 5);
				Log.i(status + " ui sync end");
				break;
			default:
				break;
			}
		}
	}

	class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener, FileDrop.Listener
	{
		private void evtOpenApkFile(boolean newWindow)
		{
			String apkFilePath = ApkFileChooser.openApkFilePath(MainUI.this);
			if(apkFilePath == null) {
				Log.v("Not choose apk file");
				return;
			}
			if(!newWindow) {
				if(tabbedPanel != null) {
					tabbedPanel.setLodingLabel();
				}
				apkScanner.clear(false);
				apkScanner.openApk(apkFilePath);
			} else {
				Launcher.run(apkFilePath);
			}
		}
		
		private void evtOpenSearchPopup() {
        	SearchDlg dialog = new SearchDlg();
        	dialog.setApkInfo(apkScanner.getApkInfo());
        	
			dialog.setModal(false);
			dialog.setVisible(true);
			
			Log.d(dialog.sName);
			
		
		    // (?i) <- "찾을 문자열"에 대소문자 구분을 없애고
		    // .*   <- 문자열이 행의 어디에 있든지 찾을 수 있게
			
		    //String findStr = "(?i).*" + dialog.sName + ".*";

		    
		}
		
		private void evtOpenPackage(boolean newWindow)
		{
			PackageTreeDlg Dlg = new PackageTreeDlg();
			if(Dlg.showTreeDlg(MainUI.this) != PackageTreeDlg.APPROVE_OPTION) {
				Log.v("Not choose package");
				return;
			}
			
			final String device = Dlg.getSelectedDevice();
			final String apkFilePath = Dlg.getSelectedApkPath();
			final String frameworkRes = Dlg.getSelectedFrameworkRes();

			if(!newWindow) {
				if(tabbedPanel != null) {
					tabbedPanel.setLodingLabel();
					tabbedPanel.setProgress(-1);
				}
				if(toolBar != null) {
					toolBar.setEnabledAt(ButtonSet.OPEN, false);
					toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
				}
				new Thread(new Runnable() {
					public void run()
					{
						apkScanner.clear(false);
						apkScanner.openPackage(device, apkFilePath, frameworkRes);
					}
				}).start();
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
			
			ApkInstallWizard wizard = new ApkInstallWizard(MainUI.this);
			wizard.setApk(apkInfo);
			wizard.start();

			toolBar.setEnabledAt(ButtonSet.INSTALL, true);
		}

		private void evtOpenJDGUI()
		{
			ApkInfo apkInfo = apkScanner.getApkInfo();
			if(apkInfo == null) {
				Log.e("evtOpenJDGUI() apkInfo is null");
				return;
			}

			toolBar.setEnabledAt(ButtonSet.OPEN_CODE, false);
			Dex2JarWrapper.openDex(apkInfo.filePath, new Dex2JarWrapper.DexWrapperListener() {
				@Override
				public void OnError() {
					toolBar.setEnabledAt(ButtonSet.OPEN_CODE, true);
				}
				@Override
				public void OnSuccess(String jarFilePath) {
					toolBar.setEnabledAt(ButtonSet.OPEN_CODE, true);
					JDGuiLauncher.run(jarFilePath);
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
					editor = "explorer";
				} else {  //for linux
					editor = "gedit";
				}
			}
			
			try {
				String manifestPath = apkInfo.tempWorkPath + File.separator + "AndroidManifest.xml";
				File manifestFile = new File(manifestPath); 
				if(!manifestFile.exists()) {
					if(!manifestFile.getParentFile().exists()) {
						if(FileUtil.makeFolder(manifestFile.getParentFile().getAbsolutePath())) {
							Log.d("sucess make folder");
						}
					}

					String[] convStrings = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] {"AndroidManifest.xml"});
					AxmlToXml a2x = new AxmlToXml(convStrings, (apkInfo != null) ? apkInfo.resourceScanner : null);
					a2x.setMultiLinePrint(true);
					
					FileWriter fw = new FileWriter(new File(manifestPath));
					fw.write(a2x.toString());
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
					new ProcessBuilder("explorer", apkInfo.filePath).start();
				} else {  //for linux
					new ProcessBuilder("file-roller", apkInfo.filePath).start();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		private void restart() {
	        
			String filePath = apkScanner.getApkInfo().filePath;
			String cmd = new String();			
			if(System.getProperty("os.name").indexOf("Window") >-1) {
				cmd += "C:\\Program Files\\APKScanner\\ApkScanner.exe";
			} else {  //for linux
				cmd += "/opt/APKScanner/APKScanner.sh";
			}
			cmd += " " + filePath;
			
			try {
	              Runtime.getRuntime().exec(cmd.toString());
	          } catch (IOException e) {
	              e.printStackTrace();
	          }
	          System.exit(0);
	  }
		
		private void evtSettings()
		{
			int value = (new SettingDlg()).makeDialog(MainUI.this);
			
			//changed theme
			if(value == 1) {
				restart();
			}
			
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
				title += " - " + apkInfo.filePath.substring(apkInfo.filePath.lastIndexOf(File.separator)+1);
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
			} else if(ToolBar.ButtonSet.OPEN_CODE.matchActionEvent(e)) {
				evtOpenJDGUI();
			} 
			else if(ToolBar.ButtonSet.SEARCH.matchActionEvent(e)) {
				evtOpenSearchPopup();
			}
		}

		// Shortcut key event processing
		@Override
		public boolean dispatchKeyEvent(KeyEvent e)
		{
			if(!isFocused()) return false;
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				if(e.getModifiers() == KeyEvent.CTRL_MASK) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_O: evtOpenApkFile(false);	break;
					case KeyEvent.VK_P: evtOpenPackage(false);	break;
					case KeyEvent.VK_N: Launcher.run();			break;
					case KeyEvent.VK_I: evtInstallApk(false);	break;
					case KeyEvent.VK_T: evtInstallApk(true);	break;
					case KeyEvent.VK_E: evtShowExplorer();		break;
					case KeyEvent.VK_M: evtShowManifest();		break;
					//case KeyEvent.VK_S: evtSettings();			break;
					default: return false;
					}
					return true;
				} else if(e.getModifiers() == (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_O: evtOpenApkFile(true);	break;
					case KeyEvent.VK_P: evtOpenPackage(true);	break;
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
				if(tabbedPanel != null) {
					tabbedPanel.setLodingLabel();
				}
				apkScanner.clear(false);
				apkScanner.openApk(files[0].getCanonicalPath(), (String)Resource.PROP_FRAMEWORK_RES.getData());
	        } catch( java.io.IOException e ) {}
		}
		
		// Closing event of window be delete tempFile
		@Override
		public void windowClosing(WindowEvent e)
		{
			exiting = true;
			setVisible(false);
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
