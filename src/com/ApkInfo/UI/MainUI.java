package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

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


public class MainUI extends JFrame implements WindowListener, KeyEventDispatcher, FileDrop.Listener
{
	private static final long serialVersionUID = 1L;
	
	private static JFrame frame;
		
	static MainUI window;
	static private MyTabUI mMyTabUI;
	static private MyToolBarUI mMyToolBarUI;
	
	//window position
	static public int nPositionX,nPositionY;
	
	//for waiting
	static public JFrame WaitingDlg;
	static public MyProgressBarDemo ProgressBarDlg;

	static private ApkManager mApkManager;
	
	static private boolean exiting = false;
	
	public static void openApk(final String apkPath, boolean isPackage) {
		//System.out.println("target file :" + apkPath);
		if(mApkManager != null)
			mApkManager.clear(false, null);

		mApkManager = new ApkManager(apkPath, isPackage);
		mApkManager.addFameworkRes((String)Resource.PROP_FRAMEWORK_RES.getData());
		mApkManager.solve(SolveType.RESOURCE, new StatusListener(){
			@Override
			public void OnStart() {
				//System.out.println("ApkCore.OnStart()");
				frame.setVisible(false);
			}

			@Override
			public void OnSuccess() {
				//System.out.println("ApkCore.OnSuccess()");
				if(exiting) return;
				
				mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, true);
				mMyToolBarUI.setEnabledAt(ButtonId.PACK, false);

				mMyTabUI.setData(mApkManager.getApkInfo());
				WaitingDlg.setVisible(false);

				String title = Resource.STR_APP_NAME.getString() + " - " + apkPath.substring(apkPath.lastIndexOf(File.separator)+1);
				frame.setTitle(title);
				frame.setVisible(true);
				
				if(mApkManager.getApkInfo().PermGroupMap.keySet().size() > 30) {
					frame.setSize(new Dimension(650, 570));
				} else {
					frame.setSize(new Dimension(650, 520));
				}
			}

			@Override
			public void OnError() {
				if(exiting) return;
				
				WaitingDlg.setVisible(false);

				frame.setTitle(Resource.STR_APP_NAME.getString());
				mMyTabUI.setData(null);
				frame.setVisible(true);
				final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
				//JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
			    JOptionPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_OPEN_APK.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, Appicon,
			    		new String[] {Resource.STR_BTN_CLOSE.getString()}, Resource.STR_BTN_CLOSE.getString());
			}

			@Override
			public void OnComplete() {
				//System.out.println("ApkCore.OnComplete()");
			}

			@Override
			public void OnProgress(int step, String msg) {
				//System.out.println("ApkCore.OnProgress() " + step + ",  " + msg);
				if(exiting) return;
				
				ProgressBarDlg.addProgress(step, msg);
			}

			@Override
			public void OnStateChange() {
				//System.out.println("ApkCore.OnStateChange()");
			}
		});
	}

	static private class PackageOpen implements AdbWrapperListener
	{
		private String tempApkPath;
		
		public PackageOpen(String device, String packageName)
		{
			String apkPath = AdbWrapper.getPackageInfo(device, packageName).apkPath;
			if(apkPath == null) {
				if(exiting) return;
				
				WaitingDlg.setVisible(false);

				frame.setTitle(Resource.STR_APP_NAME.getString());
				mMyTabUI.setData(null);
				frame.setVisible(true);
				final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
				//JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
			    JOptionPane.showOptionDialog(null, Resource.STR_MSG_FAILURE_OPEN_APK.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, Appicon,
			    		new String[] {Resource.STR_BTN_CLOSE.getString()}, Resource.STR_BTN_CLOSE.getString());
			    return;
			}

			ProgressBarDlg.addProgress(1, "I: open package\n");
			ProgressBarDlg.addProgress(1, "I: apk path in device : " + apkPath + "\n");
			
			String tmpPath = "/" + device + apkPath;
			tmpPath = tmpPath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
			tmpPath = CoreApkTool.makeTempPath(tmpPath)+".apk";
			tempApkPath = tmpPath; 
			//System.out.println(tmpPath);
			ProgressBarDlg.addProgress(1, "I: start to pull apk " + apkPath + "\n");
			AdbWrapper.PullApk(device, apkPath, tmpPath, this);
		}

		@Override public void OnCompleted() {
			if(!(new File(tempApkPath)).exists())
				tempApkPath = null;
			//System.out.println("Target APK : " + tempApkPath);
			//frame.setVisible(false);
			openApk(tempApkPath, true);
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
			String classPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			classPath = URLDecoder.decode(classPath, "UTF-8");
			Runtime.getRuntime().exec(new String[] {"java", "-Dfile.encoding=utf-8", "-cp", classPath, MainUI.class.getName(), apkFile});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void newWindow(String device, String packageName)
	{
		try {
			String classPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			classPath = URLDecoder.decode(classPath, "UTF-8");
			Runtime.getRuntime().exec(new String[] {"java", "-Dfile.encoding=utf-8", "-cp", classPath, MainUI.class.getName(), "@package", device, packageName});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void openApkFile(String apkFile)
	{
		if(apkFile == null) return;

		ProgressBarDlg.init();
		WaitingDlg.setVisible(true);
		openApk(apkFile, false);
	}
	
	private void openPackage(String device, String packageName)
	{
		ProgressBarDlg.init();
		WaitingDlg.setVisible(true);
		frame.setVisible(false);
		
		new PackageOpen(device, packageName);
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
					
					if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedPackage().isEmpty())
						newWindow(Dlg.getSelectedDevice(), Dlg.getSelectedPackage());
				} else if(cmd.equals(Resource.STR_MENU_APK_FILE.getString())) {
					String file = selectApkFile();
					openApkFile(file);
				} else if(cmd.equals(Resource.STR_MENU_PACKAGE.getString())) {
					PackageTreeDlg Dlg = new PackageTreeDlg();
					Dlg.showTreeDlg();

					if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedPackage().isEmpty())
						openPackage(Dlg.getSelectedDevice(), Dlg.getSelectedPackage());
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
		if(!frame.isFocused()) return false;
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

					if(Dlg.getSelectedDevice() != null && !Dlg.getSelectedDevice().isEmpty() && !Dlg.getSelectedPackage().isEmpty())
						openPackage(Dlg.getSelectedDevice(), Dlg.getSelectedPackage());
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
    		//System.out.println(files[0].getCanonicalPath() + "\n");

    		ProgressBarDlg.init();
			WaitingDlg.setVisible(true);
			openApk(files[0].getCanonicalPath(), false);
        }   // end try
        catch( java.io.IOException e ) {}
	}
	
	public static void setLanguage(String lang)
	{
		Resource.setLanguage(lang);
		String title = Resource.STR_APP_NAME.getString();
		if(mApkManager != null) {
			title += " - " + mApkManager.getApkInfo().ApkPath.substring(mApkManager.getApkInfo().ApkPath.lastIndexOf(File.separator)+1);
		}
		frame.setTitle(title);
		mMyToolBarUI.reloadResource();
		mMyTabUI.reloadResource();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		exiting = true;
		frame.setVisible(false);
		DeviceUIManager.setVisible(false);
		WaitingDlg.setVisible(false);
		if(mApkManager != null)
			mApkManager.clear(true, null);
	}

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				String Osname = System.getProperty("os.name");
				System.out.println("OS : " + Osname);
				//System.out.println("java.io.tmpdir : " + System.getProperty("java.io.tmpdir"));
				//System.out.println("user.dir : " + System.getProperty("user.dir"));
				
				String apkPath = null;
				if(args.length > 0)
					apkPath = args[0];

				Resource.setLanguage((String)Resource.PROP_LANGUAGE.getData(System.getProperty("user.language")));
				if((args.length > 0 && !apkPath.equals("@package") && (new File(apkPath)).exists())
						|| (args.length > 2 && apkPath.equals("@package") && !args[1].isEmpty() && !args[2].isEmpty())) {
					ProgressBarDlg = new MyProgressBarDemo();
					WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
					WaitingDlg.setVisible(true);
					window = new MainUI();
					window.initialize();
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);
					
					WaitingDlg.addWindowListener(window);
					
					if(apkPath.equals("@package")) {
						new PackageOpen(args[1], args[2]);
					} else {
						System.out.println("Target APK : " + apkPath);
						//frame.setVisible(false);
						openApk(apkPath, false);
					}
				} else {
					window = new MainUI();
					window.initialize();
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);
					
					ProgressBarDlg = new MyProgressBarDemo();
					WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
					WaitingDlg.addWindowListener(window);
					WaitingDlg.setVisible(false);
					
					frame.setTitle(Resource.STR_APP_NAME.getString());
					mMyTabUI.setData(null);
					frame.setVisible(true);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainUI() {
		//initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(this);
		frame.setBounds(100, 100, 650, 520);
		frame.setMinimumSize(new Dimension(650, 520));
		frame.setTitle(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
        
        frame.setLocationRelativeTo(null);
		
		mMyTabUI = new MyTabUI();
		mMyToolBarUI = new MyToolBarUI(new ToolBarListener());
		
		frame.add(mMyTabUI, BorderLayout.CENTER);
		frame.add(mMyToolBarUI, BorderLayout.NORTH);
		
		//frame.getContentPane().add(new MyButtonPanel(), BorderLayout.SOUTH);
		frame.setResizable( true );
		
		//frame.add(new MyButtonPanel(), BorderLayout.NORTH);
		
        new FileDrop(frame, /*dragBorder,*/ this); // end FileDrop.Listener
		
        KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
        ky.addKeyEventDispatcher(this);
        
        ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
        
        frame.setIconImage(Appicon.getImage());
		
		frame.setVisible(false);
		
		nPositionX = 100;
		nPositionY = 100;
        
		frame.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			@Override
			public void ancestorMoved(HierarchyEvent e) {
				if(frame.isVisible()) {
					nPositionX = frame.getLocationOnScreen().x;
					nPositionY = frame.getLocationOnScreen().y;
										
					DeviceUIManager.setLogWindowPosition(nPositionX,nPositionY);
					DeviceUIManager.setLogWindowToFront();
				}
			}

			@Override public void ancestorResized(HierarchyEvent e) { }
		});
	}

	@Override public void windowOpened(WindowEvent e) { }
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
}
