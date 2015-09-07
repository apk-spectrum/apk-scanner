package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
import java.net.URLDecoder;

import com.ApkInfo.Core.*;
import com.ApkInfo.Core.AdbWrapper.AdbWrapperListener;
import com.ApkInfo.Core.ApkManager.ApkInfo;
import com.ApkInfo.Core.ApkManager.SolveType;
import com.ApkInfo.Core.ApkManager.StatusListener;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UI.DeviceUIManager.InstallButtonStatusListener;
import com.ApkInfo.UI.MyToolBarUI.ButtonId;
import com.ApkInfo.UIUtil.FileDrop;
import com.ApkInfo.UIUtil.JHtmlEditorPane;
import com.apkscanner.Main;


public class MainUI extends JFrame implements WindowListener, KeyEventDispatcher, FileDrop.Listener
{
	private static final long serialVersionUID = 1L;

	private ApkManager mApkManager;
	
	private MyTabUI mMyTabUI;
	private MyToolBarUI mMyToolBarUI;
	private ProgressBarDlg progressBarDlg;
	
	//window position
	public int nPositionX, nPositionY, nPositionWidth;
	
	private boolean exiting = false;
	
	public MainUI()
	{
		new Thread(new Runnable() {
			public void run()
			{
				initialize(true);
				progressBarDlg = new ProgressBarDlg(MainUI.this);
			}
		}).start();
	}
	
	public MainUI(final String apkFilePath)
	{
		new Thread(new Runnable() {
			public void run()
			{
				progressBarDlg = new ProgressBarDlg(MainUI.this);
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
				progressBarDlg = new ProgressBarDlg(MainUI.this);
				progressBarDlg.setVisible(true);
				initialize(false);
				new PackageOpen(devSerialNumber, packageName, resources);
			}
		}).start();
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

		mApkManager = new ApkManager(apkPath, frameworkRes, isPackage);
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
				
				mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, true);
				mMyToolBarUI.setEnabledAt(ButtonId.PACK, false);

				mMyTabUI.setData(mApkManager.getApkInfo());
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
				mMyTabUI.setData(null);
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
				mMyTabUI.setData(null);
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
					if(s.matches("^@.*")) {
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
	
	private void newWindow(String apkFile)
	{
		if(apkFile == null) apkFile = "";
		
		try {
			String classPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String libPath = Resource.LIB_JSON_JAR.getPath();
			libPath += File.pathSeparator + Resource.LIB_CLI_JAR.getPath();
			classPath = URLDecoder.decode(classPath, "UTF-8");
			Runtime.getRuntime().exec(new String[] {"java", "-Dfile.encoding=utf-8", "-cp", classPath + File.pathSeparator + libPath, Main.class.getName(),
													apkFile});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void newWindow(String device, String apkPath, String frameworkRes)
	{
		try {
			String classPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String libPath = Resource.LIB_JSON_JAR.getPath();
			libPath += File.pathSeparator + Resource.LIB_CLI_JAR.getPath();
			classPath = URLDecoder.decode(classPath, "UTF-8");
			if(frameworkRes == null || frameworkRes.isEmpty()) frameworkRes = "null";
			Runtime.getRuntime().exec(new String[] {"java", "-Dfile.encoding=utf-8", "-cp", classPath + File.pathSeparator + libPath, Main.class.getName(),
													"package", "-d", device, "-f", frameworkRes, apkPath});
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		mMyToolBarUI.setEnabledAt(ButtonId.INSTALL, false);
		String libPath = apkInfo.WorkTempPath + File.separator + "lib" + File.separator;
		new DeviceUIManager(apkInfo.PackageName, apkInfo.ApkPath, libPath , 
				(boolean)Resource.PROP_CHECK_INSTALLED.getData(false), checkPackage, new InstallButtonStatusListener() {
			@Override
			public void SetInstallButtonStatus(Boolean Flag) {
				mMyToolBarUI.setEnabledAt(ButtonId.INSTALL, Flag);
			}

			@Override
			public void OnOpenApk(String path) {
				if((new File(path)).exists())
					newWindow(path);
			}
		});
	}
	
	private void showAbout()
	{
		final ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon(100,100);
		StringBuilder body = new StringBuilder();
		body.append("<div id=\"about\">");
		body.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		body.append("  Using following tools,<br/>");
		body.append("  Apktool " + ApkManager.getApkToolVersion() + "<br/>");
		body.append("  - <a href=\"http://ibotpeaches.github.io/Apktool/\" title=\"Apktool Project Site\">http://ibotpeaches.github.io/Apktool/</a><br/>");
		body.append("  " + AdbWrapper.getVersion() + "<br/>");
		body.append("  - <a href=\"http://developer.android.com/tools/help/adb.html\" title=\"Android Developer Site\">http://developer.android.com/tools/help/adb.html</a><br/>");
		body.append("  <br/><hr/>");
		body.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		body.append("</div>");

		JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuilder style = new StringBuilder("#about {");
	    style.append("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;}");
	    style.append("#about a {text-decoration:none;}");

	    // html content
	    JHtmlEditorPane hep = new JHtmlEditorPane("", "", body.toString());
	    hep.setStyle(style.toString());

	    hep.setEditable(false);
	    hep.setBackground(label.getBackground());

	    // show
	    //JOptionPane.showMessageDialog(null, hep, Resource.STR_BTN_ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE, Appicon);
	    JOptionPane.showOptionDialog(null, hep, Resource.STR_BTN_ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, Appicon,
	    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
	}
	
	class ToolBarListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			ApkInfo apkInfo = null;

			if(mApkManager != null) {
				apkInfo = mApkManager.getApkInfo();
			}
	        
			if(e.getSource().getClass().getSimpleName().equals("ToolBarButton")) {
				JButton b = (JButton) e.getSource();
				String btn_label = b.getText();
		        
				if (btn_label.equals(Resource.STR_BTN_OPEN.getString())) {
					String file = selectApkFile();
					openApkFile(file);
				} else if(btn_label.equals(Resource.STR_BTN_MANIFEST.getString())) {
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
				} else if(btn_label.equals(Resource.STR_BTN_EXPLORER.getString())) {
					try {
						if(System.getProperty("os.name").indexOf("Window") >-1) {
							new ProcessBuilder("explorer", apkInfo.WorkTempPath).start();
						} else {  //for linux
							new ProcessBuilder("nautilus", apkInfo.WorkTempPath).start();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if(btn_label.equals(Resource.STR_BTN_UNPACK.getString())) {
					//JOptionPane.showMessageDialog(null, "unpack", "unpack", JOptionPane.INFORMATION_MESSAGE);
				} else if(btn_label.equals(Resource.STR_BTN_PACK.getString())) {
					//JOptionPane.showMessageDialog(null, "pack", "pack", JOptionPane.INFORMATION_MESSAGE);
				} else if(btn_label.equals(Resource.STR_BTN_INSTALL.getString())) {
					installApk(false);
				} else if(btn_label.equals(Resource.STR_BTN_SETTING.getString())) {
					(new SettingDlg()).makeDialog();

					String lang = (String)Resource.PROP_LANGUAGE.getData();
					if(lang != null && Resource.getLanguage() != null 
							&& !Resource.getLanguage().equals(lang)) {
						setLanguage(lang);
					}
				} else if(btn_label.equals(Resource.STR_BTN_ABOUT.getString())) {
					showAbout();
				}
			} if(e.getSource().getClass().getSimpleName().equals("JMenuItem")) {
				String cmd = e.getActionCommand();
				if(cmd.equals(Resource.STR_MENU_NEW_WINDOW.getString())) {
					newWindow(null);
				} else if(cmd.equals(Resource.STR_MENU_NEW_APK_FILE.getString())) {
					String file = selectApkFile();
					if(file != null && (new File(file)).exists())
						newWindow(file);
				} else if(cmd.equals(Resource.STR_MENU_NEW_PACKAGE.getString())) {
					PackageTreeDlg Dlg = new PackageTreeDlg();
					Dlg.showTreeDlg();
					
					if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedApkPath().isEmpty())
						newWindow(Dlg.getSelectedDevice(), Dlg.getSelectedApkPath(), Dlg.getSelectedFrameworkRes());
				} else if(cmd.equals(Resource.STR_MENU_APK_FILE.getString())) {
					String file = selectApkFile();
					openApkFile(file);
				} else if(cmd.equals(Resource.STR_MENU_PACKAGE.getString())) {
					PackageTreeDlg Dlg = new PackageTreeDlg();
					Dlg.showTreeDlg();

					if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedApkPath().isEmpty())
						openPackage(Dlg.getSelectedDevice(), Dlg.getSelectedApkPath(), Dlg.getSelectedFrameworkRes());
				} else if(cmd.equals(Resource.STR_MENU_INSTALL.getString())) {
					installApk(false);
				} else if(cmd.equals(Resource.STR_MENU_CHECK_INSTALLED.getString())) {
					installApk(true);
				}
			}
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
					newWindow(null);
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
					showAbout();
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
    		//text.append( files[i].getCanonicalPath() + "\n" );
    		//Log.i(files[0].getCanonicalPath() + "\n");

    		progressBarDlg.init();
			progressBarDlg.setVisible(true);
			openApk(files[0].getCanonicalPath(), (String)Resource.PROP_FRAMEWORK_RES.getData(), false);
        }   // end try
        catch( java.io.IOException e ) {}
	}

	public void setLanguage(String lang)
	{
		Resource.setLanguage(lang);
		String title = Resource.STR_APP_NAME.getString();
		if(mApkManager != null) {
			title += " - " + mApkManager.getApkInfo().ApkPath.substring(mApkManager.getApkInfo().ApkPath.lastIndexOf(File.separator)+1);
		}
		setTitle(title);
		mMyToolBarUI.reloadResource();
		mMyTabUI.reloadResource();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(boolean visible)
	{
		setBounds(100, 100, 650, 520);
		setMinimumSize(new Dimension(650, 520));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(Resource.STR_APP_NAME.getString());
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		setResizable(true);
        setLocationRelativeTo(null);

		//if(visible) setVisible(true);
		
        mMyToolBarUI = new MyToolBarUI(new ToolBarListener());
		mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);
		add(mMyToolBarUI, BorderLayout.NORTH);
		//if(visible) revalidate();
		
		mMyTabUI = new MyTabUI();
		mMyTabUI.setData(null);
		add(mMyTabUI, BorderLayout.CENTER);
		//if(visible) revalidate();
		
		if(visible) setVisible(true);
		
		nPositionX = 100;
		nPositionY = 100;

		addWindowListener(this);
		
		new FileDrop(this, /*dragBorder,*/ this); // end FileDrop.Listener

		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
        ky.addKeyEventDispatcher(this);
        
		getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			@Override
			public void ancestorMoved(HierarchyEvent e) {
				if(isVisible()) {
					nPositionX = getLocationOnScreen().x;
					nPositionY = getLocationOnScreen().y;
					nPositionWidth = getContentPane().getWidth();
					DeviceUIManager.setLogWindowPosition(nPositionX,nPositionY,nPositionWidth);
					DeviceUIManager.setLogWindowToFront();
				}
			}

			@Override public void ancestorResized(HierarchyEvent e) { }
		});
		Log.i("initialize() end");
	}

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
