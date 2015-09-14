package com.apkscanner.gui;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
import com.apkscanner.core.ApktoolManager;
import com.apkscanner.core.DeviceUIManager;
import com.apkscanner.core.ApktoolManager.ApkInfo;
import com.apkscanner.core.ApktoolManager.SolveType;
import com.apkscanner.core.ApktoolManager.StatusListener;
import com.apkscanner.core.DeviceUIManager.InstallButtonStatusListener;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkscanner.gui.dialog.ProgressBarDlg;
import com.apkscanner.gui.dialog.SettingDlg;
import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.AdbWrapper;
import com.apkscanner.util.CoreApkTool;
import com.apkscanner.util.Log;
import com.apkscanner.util.AdbWrapper.AdbWrapperListener;


public class MainUI extends JFrame
{
	private static final long serialVersionUID = 1L;

	private ApktoolManager mApkManager;
	
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
				progressBarDlg = new ProgressBarDlg(new UIEventHandler());
			}
		}).start();
	}
	
	public MainUI(final String apkFilePath)
	{
		new Thread(new Runnable() {
			public void run()
			{
				progressBarDlg = new ProgressBarDlg(new UIEventHandler());
				progressBarDlg.setVisible(true);

				initialize(false);
				openApk(apkFilePath);
			}
		}).start();
	}
	
	public MainUI(final String devSerialNumber, final String packageName, final String resources)
	{
		new Thread(new Runnable() {
			public void run()
			{
				progressBarDlg = new ProgressBarDlg(new UIEventHandler());
				progressBarDlg.setVisible(true);
				initialize(false);
				new PackageOpen(devSerialNumber, packageName, resources);
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

		//if(visible) setVisible(true);
        toolBar = new ToolBar(new UIEventHandler());
        toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, false);
		add(toolBar, BorderLayout.NORTH);
		//if(visible) revalidate();
		
		tabbedPanel = new TabbedPanel();
		tabbedPanel.setData(null);
		add(tabbedPanel, BorderLayout.CENTER);
		//if(visible) revalidate();
		
		if(visible) setVisible(true);

		addWindowListener(new UIEventHandler());

		new FileDrop(this, /*dragBorder,*/ new UIEventHandler()); // end FileDrop.Listener

		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
        ky.addKeyEventDispatcher(new UIEventHandler());
        
		getContentPane().addHierarchyBoundsListener(new UIEventHandler());
		new UIEventHandler().ancestorMoved(null);

		Log.i("initialize() end");
	}
	
	public void openApk(final String apkPath) {
		openApk(apkPath, null, false);
	}
	
	public void openApk(final String apkPath, String frameworkRes, boolean isPackage) {
		//Log.i("target file :" + apkPath);
		if(mApkManager != null)
			mApkManager.clear(false, null);
		
		if(frameworkRes == null) {
			frameworkRes = (String)Resource.PROP_FRAMEWORK_RES.getData();
		}

		mApkManager = new ApktoolManager(apkPath, frameworkRes, isPackage);
		//mApkManager.addFameworkRes((String)Resource.PROP_FRAMEWORK_RES.getData());
		mApkManager.solve(SolveType.RESOURCE, new StatusListener() {
			@Override
			public void OnStart() {
				//Log.i("ApkCore.OnStart()");
				setVisible(false);
				Log.i("openApk start");
			}

			@Override
			public void OnSuccess() {
				//Log.i("ApkCore.OnSuccess()");
				if(exiting) return;
				
				toolBar.setEnabledAt(ButtonSet.NEED_TARGET_APK, true);

				tabbedPanel.setData(mApkManager.getApkInfo());
				progressBarDlg.setVisible(false);

				String title = Resource.STR_APP_NAME.getString() + " - " + apkPath.substring(apkPath.lastIndexOf(File.separator)+1);
				setTitle(title);
				setVisible(true);
				
				if(mApkManager.getApkInfo().PermGroupMap.keySet().size() > 30) {
					setSize(new Dimension(650, 570));
				} else {
					setSize(new Dimension(650, 520));
				}
			}

			@Override
			public void OnError() {
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
				//Log.i("ApkCore.OnComplete()");
			}

			@Override
			public void OnProgress(int step, String msg) {
				//Log.i("ApkCore.OnProgress() " + step + ",  " + msg);
				if(exiting) return;
				
				progressBarDlg.addProgress(step, msg);
			}

			@Override
			public void OnStateChange() {
				//Log.i("ApkCore.OnStateChange()");
			}
		});
	}

	private class PackageOpen implements AdbWrapperListener
	{
		private String tempApkPath;
		private String frameworkRes;
		
		public PackageOpen(String device, String apkPath, String frameworkRes)
		{
			if(mApkManager != null) {
				mApkManager.clear(false, null);
				mApkManager = null;
			}
			
			if(apkPath == null) {
				if(exiting) return;
				
				progressBarDlg.setVisible(false);

				setTitle(Resource.STR_APP_NAME.getString());
				tabbedPanel.setData(null);
				setVisible(true);
				final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
				//JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
			    JOptionPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_OPEN_APK.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, Appicon,
			    		new String[] {Resource.STR_BTN_CLOSE.getString()}, Resource.STR_BTN_CLOSE.getString());
			    return;
			}

			progressBarDlg.addProgress(1, "I: open package\n");
			progressBarDlg.addProgress(1, "I: apk path in device : " + apkPath + "\n");
			
			String tmpPath = "/" + device + apkPath;
			tmpPath = tmpPath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
			tmpPath = CoreApkTool.makeTempPath(tmpPath)+".apk";
			tempApkPath = tmpPath;

			if(frameworkRes == null) {
				frameworkRes = (String)Resource.PROP_FRAMEWORK_RES.getData();
			}
			this.frameworkRes = "";
			if(frameworkRes != null && !frameworkRes.isEmpty()) {
				for(String s: frameworkRes.split(";")) {
					if(s.startsWith("@")) {
						String name = s.replaceAll("^@([^/]*)/.*", "$1");
						String path = s.replaceAll("^@[^/]*", "");
						String dest = (new File(tmpPath).getParent()) + File.separator + path.replaceAll(".*/", "");
						progressBarDlg.addProgress(1, "I: start to pull resource apk " + path + "\n");
						AdbWrapper.PullApk_sync(name, path, dest);
						this.frameworkRes += dest + ";"; 
					} else {
						this.frameworkRes += s + ";"; 
					}
				}
			}

			//Log.i(tmpPath);
			progressBarDlg.addProgress(1, "I: start to pull apk " + apkPath + "\n");
			AdbWrapper.PullApk(device, apkPath, tmpPath, this);
		}

		@Override public void OnCompleted() {
			if(!(new File(tempApkPath)).exists()) {
				tempApkPath = null;
				return;
			}
			//Log.i("Target APK : " + tempApkPath);
			//setVisible(false);
			openApk(tempApkPath, frameworkRes, true);
		}
		
		@Override public void OnMessage(String msg) { }
		@Override public void OnError() { }
		@Override public void OnSuccess() { }
	}

	
	class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener, FileDrop.Listener, HierarchyBoundsListener
	{
		private String selectApkFile()
		{
			JFileChooser jfc = new JFileChooser((String)Resource.PROP_LAST_FILE_OPEN_PATH.getData(""));
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));

			if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return null;
			
			File dir = jfc.getSelectedFile();
			if(dir == null) return null;
			Resource.PROP_LAST_FILE_OPEN_PATH.setData(dir.getParentFile().getAbsolutePath());

			return dir.getPath();
		}

		private void openApkFile(String apkFile)
		{
			if(apkFile == null) return;

			progressBarDlg.init();
			progressBarDlg.setVisible(true);
			openApk(apkFile);
		}
		
		private void openPackage(String device, String apkPath, String frameworkRes)
		{
			progressBarDlg.init();
			progressBarDlg.setVisible(true);
			setVisible(false);
			
			new PackageOpen(device, apkPath, frameworkRes);
		}
		
		private void installApk(boolean checkPackage)
		{
			ApkInfo apkInfo = null;
			if(mApkManager != null) {
				apkInfo = mApkManager.getApkInfo();
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

		public void setLanguage(String lang)
		{
			Resource.setLanguage(lang);
			String title = Resource.STR_APP_NAME.getString();
			if(mApkManager != null) {
				title += " - " + mApkManager.getApkInfo().ApkPath.substring(mApkManager.getApkInfo().ApkPath.lastIndexOf(File.separator)+1);
			}
			setTitle(title);
			toolBar.reloadResource();
			tabbedPanel.reloadResource();
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (ToolBar.ButtonSet.OPEN.matchActionEvent(e) || ToolBar.MenuItemSet.OPEN_APK.matchActionEvent(e)) {
				String file = selectApkFile();
				openApkFile(file);
			} else if(ToolBar.ButtonSet.MANIFEST.matchActionEvent(e)) {
				String editor = (String)Resource.PROP_EDITOR.getData();
				ApkInfo apkInfo = mApkManager.getApkInfo();
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
			} else if(ToolBar.ButtonSet.EXPLORER.matchActionEvent(e)) {
				ApkInfo apkInfo = mApkManager.getApkInfo();
				try {
					if(System.getProperty("os.name").indexOf("Window") >-1) {
						new ProcessBuilder("explorer", apkInfo.WorkTempPath).start();
					} else {  //for linux
						new ProcessBuilder("nautilus", apkInfo.WorkTempPath).start();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if(ToolBar.ButtonSet.INSTALL.matchActionEvent(e) || ToolBar.MenuItemSet.INSTALL_APK.matchActionEvent(e)) {
				installApk(false);
			} else if(ToolBar.ButtonSet.SETTING.matchActionEvent(e)) {
				(new SettingDlg()).makeDialog();

				String lang = (String)Resource.PROP_LANGUAGE.getData();
				if(lang != null && Resource.getLanguage() != null 
						&& !Resource.getLanguage().equals(lang)) {
					setLanguage(lang);
				}
			} else if(ToolBar.ButtonSet.ABOUT.matchActionEvent(e)) {
				AboutDlg.showAboutDialog();
			} else if(ToolBar.MenuItemSet.NEW_EMPTY.matchActionEvent(e)) {
				Launcher.run();
			} else if(ToolBar.MenuItemSet.NEW_APK.matchActionEvent(e)) {
				String file = selectApkFile();
				if(file != null && (new File(file)).exists())
					Launcher.run(file);
			} else if(ToolBar.MenuItemSet.NEW_PACKAGE.matchActionEvent(e)) {
				PackageTreeDlg Dlg = new PackageTreeDlg();
				Dlg.showTreeDlg();
				
				if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedApkPath().isEmpty())
					Launcher.run(Dlg.getSelectedDevice(), Dlg.getSelectedApkPath(), Dlg.getSelectedFrameworkRes());
			} else if(ToolBar.MenuItemSet.OPEN_PACKAGE.matchActionEvent(e)) {
				PackageTreeDlg Dlg = new PackageTreeDlg();
				Dlg.showTreeDlg();

				if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedApkPath().isEmpty())
					openPackage(Dlg.getSelectedDevice(), Dlg.getSelectedApkPath(), Dlg.getSelectedFrameworkRes());
			} else if(ToolBar.MenuItemSet.INSTALLED_CHECK.matchActionEvent(e)) {
				installApk(true);
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			ApkInfo apkInfo = null;
			if(!isFocused()) return false;
			if (e.getID()==KeyEvent.KEY_RELEASED) {
				if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_V:
						break;
					case KeyEvent.VK_O:
						openApkFile(selectApkFile());
						break;
					case KeyEvent.VK_P:
						PackageTreeDlg Dlg = new PackageTreeDlg();
						Dlg.showTreeDlg();

						if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedApkPath().isEmpty())
							openPackage(Dlg.getSelectedDevice(), Dlg.getSelectedApkPath(), Dlg.getSelectedFrameworkRes());
						break;
					case KeyEvent.VK_N:
						Launcher.run();
						break;
					case KeyEvent.VK_I:
						if(mApkManager != null) {
							apkInfo = mApkManager.getApkInfo();
						}
						if(apkInfo != null) {
							installApk(false);
						}
						break;
					case KeyEvent.VK_T:
						if(mApkManager != null) {
							apkInfo = mApkManager.getApkInfo();
						}
						if(apkInfo != null) {
							installApk(true);
						}
						break;
					case KeyEvent.VK_E:
						if(mApkManager != null) {
							apkInfo = mApkManager.getApkInfo();
						}
						if(apkInfo != null) {
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
						break;
					case KeyEvent.VK_M:
						if(mApkManager != null) {
							apkInfo = mApkManager.getApkInfo();
						}
						if(apkInfo != null) {
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
						break;
					case KeyEvent.VK_S:
						(new SettingDlg()).makeDialog();

						String lang = (String)Resource.PROP_LANGUAGE.getData();
						if(lang != null && Resource.getLanguage() != null 
								&& !Resource.getLanguage().equals(lang)) {
							setLanguage(lang);
						}
						break;
					default:
						return false;
					}
					return true;
				} else if(e.getModifiers() == 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_F1:
						AboutDlg.showAboutDialog();
						break;
					case KeyEvent.VK_F12:
						JTextArea taskOutput = new JTextArea();
						taskOutput.setText(Log.getLog());
						taskOutput.setEditable(false);
						taskOutput.setCaretPosition(0);
						
						JScrollPane scrollPane = new JScrollPane(taskOutput);
						scrollPane.setPreferredSize(new Dimension(600, 400));

						JOptionPane.showOptionDialog(null, scrollPane, Resource.STR_LABEL_LOG.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null,
					    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
						break;
					default:
						return false;
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public void filesDropped(File[] files)
		{
			try {   
	    		progressBarDlg.init();
				progressBarDlg.setVisible(true);
				openApk(files[0].getCanonicalPath(), (String)Resource.PROP_FRAMEWORK_RES.getData(), false);
	        } catch( java.io.IOException e ) {}
		}
		
		@Override
		public void ancestorMoved(HierarchyEvent e) {
			if(isVisible()) {
				int nPositionX = getLocationOnScreen().x;
				int nPositionY = getLocationOnScreen().y;
				int nPositionWidth = getWidth();
				DeviceUIManager.setLogWindowPosition(nPositionX + nPositionWidth, nPositionY);
				DeviceUIManager.setLogWindowToFront();
			}
		}
		@Override public void ancestorResized(HierarchyEvent e) { ancestorMoved(e); }

		@Override
		public void windowClosing(WindowEvent e) {
			exiting = true;
			setVisible(false);
			DeviceUIManager.setVisible(false);
			progressBarDlg.setVisible(false);
			if(mApkManager != null)
				mApkManager.clear(true, null);
		}
		
		@Override public void windowOpened(WindowEvent e) { }
		@Override public void windowClosed(WindowEvent e) { }
		@Override public void windowIconified(WindowEvent e) { }
		@Override public void windowDeiconified(WindowEvent e) { }
		@Override public void windowActivated(WindowEvent e) { }
		@Override public void windowDeactivated(WindowEvent e) { }
	}
}
