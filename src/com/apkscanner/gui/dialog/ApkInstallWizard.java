package com.apkscanner.gui.dialog;


import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.android.ddmlib.IDevice;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.DefaultOptionsFactory;
import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.gui.install.ContentPanel;
import com.apkscanner.gui.install.ControlPanel;
import com.apkscanner.gui.install.FindPackagePanel;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.gui.install.InstallProgressPanel;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.Resource;

import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class ApkInstallWizard
{
	public static final int STATUS_INIT = 0;
	public static final int STATUS_PACKAGE_SCANNING = 1;
	public static final int STATUS_CHECK_PACKAGES = 2;
	public static final int STATUS_INSTALLING = 3;
	public static final int STATUS_COMPLETED = 4;
	
	public static final int STATUS_DESTROY_DIALOG = 100;
	public static final int STATUS_APK_VERTIFY_ERROR = 101;
	
	public static final int FLAG_OPT_INSTALL	 	= 0x0100;
	public static final int FLAG_OPT_PUSH			= 0x0200;
	public static final int FLAG_OPT_PUSH_OVERWRITE = 0x0400;
	
	public static final int FLAG_OPT_INSTALL_INTERNAL = 0x0001;
	public static final int FLAG_OPT_INSTALL_EXTERNAL = 0x0002;
	
	public static final int FLAG_OPT_PUSH_SYSTEM	= 0x0001;
	public static final int FLAG_OPT_PUSH_PRIVAPP	= 0x0002;
	public static final int FLAG_OPT_PUSH_DATA		= 0x0004;
	
	public static final int FLAG_OPT_EXTRA_RUN		= 0x0010;
	public static final int FLAG_OPT_EXTRA_REBOOT	= 0x0020;
	public static final int FLAG_OPT_EXTRA_WITH_LIB	= 0x0040;
	public static final int FLAG_OPT_EXTRA_DELETE_EXISTING_APK = 0x0080;

	// UI components
	private Window wizard;
	private InstallProgressPanel progressPanel;
	private ContentPanel contentPanel;
	private ControlPanel controlPanel;
	private UIEventHandler uiEventHandler = new UIEventHandler();
	
	public static String pakcageFilePath;	
	public static CompactApkInfo apkInfo;
	public static SignatureReport signatureReport;
	public static DefaultOptionsFactory optFactory;
	
	private int status;
	
	public class ApkInstallWizardDialog  extends JDialog
	{
		private static final long serialVersionUID = 2018466680871932348L;

		public ApkInstallWizardDialog() {
			dialog_init(null);
		}
		
		public ApkInstallWizardDialog(Frame owner) {
			super(owner);
			dialog_init(owner);
		}
		
		public ApkInstallWizardDialog(JDialog owner) {
			super(owner);
			dialog_init(owner);
		}
		
		private void dialog_init(Component owner) {
			setTitle(Resource.STR_TITLE_INSTALL_WIZARD.getString());
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setResizable(true);
			setModal(false);
			addWindowListener(uiEventHandler);
			
			initialize(this);
			setLocationRelativeTo(owner);
		}
	}
	
	public class ApkInstallWizardFrame extends JFrame
	{
		private static final long serialVersionUID = -5642057585041759436L;
		
		public ApkInstallWizardFrame() {
			frame_init();
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}

		public ApkInstallWizardFrame(Frame owner) {
			frame_init();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}

		public ApkInstallWizardFrame(JDialog owner) {
			frame_init();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
		
		private void frame_init()
		{
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
			
			setTitle(Resource.STR_TITLE_INSTALL_WIZARD.getString());
			setResizable(true);

			initialize(this);
			setLocationRelativeTo(null);
			
			// Closing event of window be delete tempFile
			addWindowListener(uiEventHandler);
			
		}
	}

	public ApkInstallWizard() {
		wizard = new ApkInstallWizardFrame();
	}

	public ApkInstallWizard(String FilePath) {
		pakcageFilePath = FilePath;		
		if(FilePath == null || !(new File(FilePath).isFile())) {
			Log.e("No such apk file... : " + FilePath);
			MessageBoxPool.show(wizard, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}		
		wizard = new ApkInstallWizardFrame();		
	}
	
	public ApkInstallWizard(String FilePath, JFrame owner) {
		pakcageFilePath = FilePath;		
		if(FilePath == null || !(new File(FilePath).isFile())) {
			Log.e("No such apk file... : " + FilePath);
			MessageBoxPool.show(wizard, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}		
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame(owner);
	}
	
	public ApkInstallWizard(String FilePath, JDialog owner) {
		pakcageFilePath = FilePath;		
		if(FilePath == null || !(new File(FilePath).isFile())) {
			Log.e("No such apk file... : " + FilePath);
			MessageBoxPool.show(wizard, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}		
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame(owner);
	}

	private void setVisible(boolean visible) {
		if(wizard != null) wizard.setVisible(visible);
	}

	private void initialize(Window window)
	{
		if(window == null) return;

		AdbServerMonitor.startServerAndCreateBridgeAsync();
		window.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		window.setSize(new Dimension(550,450));
		
		progressPanel = new InstallProgressPanel();
		controlPanel = new ControlPanel(uiEventHandler);
		contentPanel = new ContentPanel(uiEventHandler);
		
		
		JPanel PanelDummy = new JPanel();
		//progressPanel.setPreferredSize(new Dimension(700, 200));
		PanelDummy.setBackground(Color.WHITE);
		PanelDummy.setOpaque(true);
		PanelDummy.setPreferredSize(new Dimension(600, 80));
		PanelDummy.add(progressPanel);
		
		window.add(PanelDummy, BorderLayout.NORTH);
		window.add(contentPanel, BorderLayout.CENTER);
		window.add(controlPanel, BorderLayout.SOUTH);
		
		//Log.i("initialize() register event handler");
		//window.addWindowListener(new UIEventHandler());
		
		window.setMinimumSize(new Dimension(600, 450));
		
		// Shortcut key event processing
		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(uiEventHandler);
	}
	
	private void changeState(int status) {
		Log.d(">>>>>>>>>>>>> changeState() " + status);
		if(this.status == status) return;
		this.status = status;
		progressPanel.setStatus(status);
		contentPanel.setStatus(status);
		controlPanel.setStatus(status);
		
		execute(status);
	}
	
	private void execute(int status) {
		switch(status) {
		case STATUS_INIT:
			next();
			break;
		case STATUS_PACKAGE_SCANNING:			
			new Thread(new Runnable() {
				@Override
				public void run() {
			        String apkFilePath = pakcageFilePath;
			        
			        ApkScanner scanner = ApkScanner.getInstance("AAPTLIGHT");
			        scanner.openApk(apkFilePath);
			        if(scanner.getLastErrorCode() != ApkScanner.NO_ERR) {
			            Log.e("Fail open APK");
			            changeState(STATUS_APK_VERTIFY_ERROR);
			        }
			        apkInfo = new CompactApkInfo(scanner.getApkInfo());
			        
			        signatureReport = null;
			        try {
			            signatureReport = new SignatureReport(new JarFile(apkFilePath, true));
			        } catch (Exception e) { }
			        if(signatureReport == null) {
			            Log.e("Fail APK Virify");
			            changeState(STATUS_APK_VERTIFY_ERROR);
			        }
			        try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			        
			        next();
				}			
			}).start();			
			break;
		case STATUS_INSTALLING:
				
			break;
		default:
			break;
		}
	}
	
	public void start() {
		if(status != STATUS_INIT) {
			Log.w("No init state : " + status);
			return;
		}

		setVisible(true);
		changeState(STATUS_PACKAGE_SCANNING);
	}
	
	private void next() {
		synchronized(this) {
			switch(status) {
			case STATUS_INIT:
				changeState(STATUS_PACKAGE_SCANNING);
				break;
			case STATUS_PACKAGE_SCANNING:				
				changeState(STATUS_CHECK_PACKAGES);
				//changeState(STATUS_WATING_CHECK_PACKAGES);
				break;
			case STATUS_CHECK_PACKAGES:
				changeState(STATUS_INSTALLING);
				break;						
			case STATUS_INSTALLING:
				changeState(STATUS_COMPLETED);
				break;
			default:
				break;
			}
		}
	}	
	
	public void stop() {
		
	}
	
	private void restart() {
		if(status != STATUS_COMPLETED) return;
		status = STATUS_PACKAGE_SCANNING;
		start();
	}
	
	private void printLog(String msg) {
		Log.v(msg);	
		// append to log viewer
		contentPanel.appendLog(msg);
	}
	
	public class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(ControlPanel.CTR_ACT_CMD_NEXT.equals(arg0.getActionCommand())) {
				next();
			} else if(ControlPanel.CTR_ACT_CMD_PREVIOUS.equals(arg0.getActionCommand())) {
				//previous();
			} else if(ControlPanel.CTR_ACT_CMD_CANCEL.equals(arg0.getActionCommand()) ||
					ControlPanel.CTR_ACT_CMD_OK.equals(arg0.getActionCommand())) {
				if(wizard instanceof JFrame &&
						((JFrame)wizard).getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
					System.exit(0);
				} else {
					wizard.dispose();
				}
			} else if(ControlPanel.CTR_ACT_CMD_RESTART.equals(arg0.getActionCommand())) {
				changeState(STATUS_INIT);				
			} else if(ContentPanel.CTT_ACT_CMD_REFRESH.equals(arg0.getActionCommand())) {

			} else if(ContentPanel.CTT_ACT_CMD_SELECT_ALL.equals(arg0.getActionCommand())) {

			} else if("SELECT_ALL".equals(arg0.getActionCommand())) {
				
			} else if("CHANG_SIGN".equals(arg0.getActionCommand())) {
				
			} else if(FindPackagePanel.NO_DEVICE_LAYOUT.equals(arg0.getActionCommand())) {
				if(status == ApkInstallWizard.STATUS_CHECK_PACKAGES) {
					controlPanel.setStatus(ControlPanel.STATUS_NO_DEVICE);
				}
			} else if(FindPackagePanel.DEVICE_LAYOUT.equals(arg0.getActionCommand())) {
				if(status == ApkInstallWizard.STATUS_CHECK_PACKAGES) {
					controlPanel.setStatus(ApkInstallWizard.STATUS_CHECK_PACKAGES);					
				}
			} else if(FindPackagePanel.DEVICE_LAYOUT_WAIT_INSTALL_BUTTON.equals(arg0.getActionCommand())) {
				if(status == ApkInstallWizard.STATUS_CHECK_PACKAGES) {
					controlPanel.setNextButtonEnable(false);
				}
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if(!wizard.isFocused()) return false;
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				if(e.getModifiers() == KeyEvent.ALT_MASK) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_N: 
						next(); 
						break;
					case KeyEvent.VK_P:	
						//previous();
						break;
					default: 
						return false;
					}
					return true;
				} else if(e.getModifiers() == 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_F5 :
						//contentPanel.refreshDeviceList();
						break;
					default:
						return false;
					}
					return true;
				}
			}
			return false;
		}
		
		// Closing event of window be delete tempFile
		@Override
		public void windowClosing(WindowEvent e)
		{	
			Log.d("closing....ApkInstallWizard");
			changeState(STATUS_DESTROY_DIALOG);			
		}
		
		@Override public void windowOpened(WindowEvent e) { }
		@Override public void windowClosed(WindowEvent e) { }
		@Override public void windowIconified(WindowEvent e) { }
		@Override public void windowDeiconified(WindowEvent e) { }
		@Override public void windowActivated(WindowEvent e) { }
		@Override public void windowDeactivated(WindowEvent e) { }
	};
	
	
    public static void main(String args[]) {
    	Resource.setLanguage((String)Resource.PROP_LANGUAGE.getData(SystemUtil.getUserLanguage()));
        EventQueue.invokeLater(new Runnable() {
            public void run() {
            	if(SystemUtil.isWindows()) {
            		ApkInstallWizard wizard = new ApkInstallWizard("C:\\Melon.apk");
            		wizard.start();
            	} else {
            		ApkInstallWizard wizard = new ApkInstallWizard("/home/leejinhyeong/Desktop/reco.apk");
            		wizard.start();
            	}
            }
        });
    }
}


