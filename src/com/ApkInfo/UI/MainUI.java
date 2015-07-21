package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;

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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import com.ApkInfo.Core.*;
import com.ApkInfo.Core.ApkManager.ApkInfo;
import com.ApkInfo.Core.ApkManager.SolveType;
import com.ApkInfo.Core.ApkManager.StatusListener;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UI.DeviceUIManager.InstallButtonStatusListener;
import com.ApkInfo.UI.MyToolBarUI.ButtonId;
import com.ApkInfo.UIUtil.JHtmlEditorPane;


public class MainUI extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;
	
	private static JFrame frame;
		
	static MainUI window;
	static private MyTabUI mMyTabUI;
	static private MyToolBarUI mMyToolBarUI;
	static SettingDlg SettingDlg;
	
	//window position
	static public int nPositionX,nPositionY;
	
	//for waiting
	static public JFrame WaitingDlg;
	static public MyProgressBarDemo ProgressBarDlg;

	static private ApkManager mApkManager;
	
	public static void openApk(final String apkPath) {
		//System.out.println("target file :" + apkPath);
		mApkManager = new ApkManager(apkPath);
		mApkManager.solve(SolveType.RESOURCE, new StatusListener(){
			@Override
			public void OnStart() {
				System.out.println("ApkCore.OnStart()");
				frame.setVisible(false);
			}

			@Override
			public void OnSuccess() {
				System.out.println("ApkCore.OnSuccess()");
				mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, true);
				mMyToolBarUI.setEnabledAt(ButtonId.PACK, false);

				mMyTabUI.setData(mApkManager.getApkInfo());
				WaitingDlg.setVisible(false);

				String title = Resource.STR_APP_NAME.getString() + " - " + apkPath.substring(apkPath.lastIndexOf(File.separator)+1);
				frame.setTitle(title);
				frame.setVisible(true);
			}

			@Override
			public void OnError() {
				WaitingDlg.setVisible(false);
				frame.setVisible(true);
				final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
				JOptionPane.showMessageDialog(null, "Sorry, Can not open the APK", "Error", JOptionPane.ERROR_MESSAGE, Appicon);
			}

			@Override
			public void OnComplete() {
				System.out.println("ApkCore.OnComplete()");
			}

			@Override
			public void OnProgress(int step, String msg) {
				//System.out.println("ApkCore.OnProgress() " + step + ",  " + msg);
				ProgressBarDlg.addProgress(step, msg);
			}

			@Override
			public void OnStateChange() {
				System.out.println("ApkCore.OnStateChange()");
			}
		});
	}
		
	class ToolBarListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			ApkInfo apkInfo = null;

			if(mApkManager != null) {
				apkInfo = mApkManager.getApkInfo();
			}
	        
			JButton b = (JButton) e.getSource();
			String btn_label = b.getText();
	        
			if (btn_label.equals(Resource.STR_BTN_OPEN.getString())) {
				JFileChooser jfc = new JFileChooser();
				//jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));
								
				jfc.showOpenDialog(null);
				File dir = jfc.getSelectedFile();

				if(dir!=null) {
					ProgressBarDlg.init();
					WaitingDlg.setVisible(true);
					openApk(dir.getPath());
				}
			} else if(btn_label.equals(Resource.STR_BTN_MANIFEST.getString())) {
				try {
					if(System.getProperty("os.name").indexOf("Window") >-1) {
						new ProcessBuilder("notepad", apkInfo.WorkTempPath + File.separator + "AndroidManifest.xml").start();
					} else {  //for linux
						new ProcessBuilder("gedit", apkInfo.WorkTempPath + File.separator + "AndroidManifest.xml").start();
					}	
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
				if(Resource.getLanguage() == null)
					setLanguage("ko");
				else
					setLanguage(null);
			} else if(btn_label.equals(Resource.STR_BTN_PACK.getString())) {
				JOptionPane.showMessageDialog(null, "pack", "pack", JOptionPane.INFORMATION_MESSAGE);
			} else if(btn_label.equals(Resource.STR_BTN_INSTALL.getString())) {
				mMyToolBarUI.setEnabledAt(ButtonId.INSTALL, false);
				String libPath = apkInfo.WorkTempPath + File.separator + "lib" + File.separator;
				new DeviceUIManager(apkInfo.PackageName, apkInfo.ApkPath, libPath , new InstallButtonStatusListener() {
					@Override
					public void SetInstallButtonStatus(Boolean Flag) {
						mMyToolBarUI.setEnabledAt(ButtonId.INSTALL, Flag);
					}
				});
			} else if(btn_label.equals(Resource.STR_BTN_SETTING.getString())) {
				
				SettingDlg= new SettingDlg();
				SettingDlg.makeDialog();
				
				//JOptionPane.showMessageDialog(null, "Setting", "Setting", JOptionPane.INFORMATION_MESSAGE, null);
			} else if(btn_label.equals(Resource.STR_BTN_ABOUT.getString())) {
				final ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon(100,100);
				StringBuilder body = new StringBuilder();
				body.append("<div id=\"about\">");
				body.append("<H1>" + Resource.STR_APP_NAME.getString() + " ");
				body.append(Resource.STR_APP_VERSION.getString() + "</H1>");
				body.append("With following tools,<br/>");
				body.append("Apktool " + ApkManager.getApkToolVersion() + "<br/>");
				body.append("  - <a href=\"http://ibotpeaches.github.io/Apktool/\" title=\"Apktool Project Site\">http://ibotpeaches.github.io/Apktool/</a><br/>");
				body.append("" + AdbWrapper.getVersion() + "<br/>");
				body.append("  - <a href=\"http://developer.android.com/tools/help/adb.html\" title=\"Android Developer Site\">http://developer.android.com/tools/help/adb.html</a><br/>");
				body.append("<br/><hr/>");
				body.append("Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
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
			    JOptionPane.showMessageDialog(null, hep, Resource.STR_BTN_ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE, Appicon);
			}
		}
	}

	public static void setLanguage(String lang)
	{
		Resource.setLanguage(lang);
		String title = Resource.STR_APP_NAME.getString() + " - " + mApkManager.getApkInfo().ApkPath.substring(mApkManager.getApkInfo().ApkPath.lastIndexOf(File.separator)+1);
		frame.setTitle(title);
		mMyToolBarUI.reloadResource();
		mMyTabUI.reloadResource();
	}

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				String Osname = System.getProperty("os.name");
				System.out.println("OS : " + Osname);
				//System.out.println("java.io.tmpdir : " + System.getProperty("java.io.tmpdir"));
				//System.out.println("user.dir : " + System.getProperty("user.dir"));
				
				String apkPath = null;
				SettingDlg = new SettingDlg();
				if(args.length > 0) {
					ProgressBarDlg = new MyProgressBarDemo();
					WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
					WaitingDlg.setVisible(true);
					//Resource.setLanguage("ko");
					window = new MainUI();
					window.initialize();
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);

					apkPath = args[0];
					System.out.println("Target APK : " + args[0]);
					//frame.setVisible(false);
					openApk(apkPath);
				} else {					
					window = new MainUI();
					window.initialize();
					mMyToolBarUI.setEnabledAt(ButtonId.NEED_TARGET_APK, false);
					
					ProgressBarDlg = new MyProgressBarDemo();
					WaitingDlg = MyProgressBarDemo.createAndShowGUI(ProgressBarDlg);
					
					WaitingDlg.setVisible(false);
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
		frame.setBounds(100, 100, 650, 510);
		frame.setMinimumSize(new Dimension(650, 510));
		frame.setTitle(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		mMyTabUI = new MyTabUI();
		mMyToolBarUI = new MyToolBarUI(new ToolBarListener());
		
		frame.add(mMyTabUI, BorderLayout.CENTER);
		frame.add(mMyToolBarUI, BorderLayout.NORTH);
		
		//frame.getContentPane().add(new MyButtonPanel(), BorderLayout.SOUTH);
		frame.setResizable( true );
		
		//frame.add(new MyButtonPanel(), BorderLayout.NORTH);
		
        
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

			@Override
			public void ancestorResized(HierarchyEvent e) {
				
			}
		});
		
		
	}
	
	
	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		frame.setVisible(false);
		DeviceUIManager.setVisible(false);
		if(mApkManager != null)
			mApkManager.clear(true, null);
	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

}
