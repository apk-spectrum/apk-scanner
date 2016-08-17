package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.Launcher;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.ApkInstaller.ApkInstallerListener;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScannerStub;
import com.apkscanner.core.scanner.ApkScannerStub.Status;
import com.apkscanner.data.apkinfo.ActivityAliasInfo;
import com.apkscanner.data.apkinfo.ActivityInfo;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.dialog.install.InstallDlg;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.ArrowTraversalPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceManager;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.tool.adb.AdbPackageManager;
import com.apkscanner.tool.adb.AdbPackageManager.PackageInfo;
import com.apkscanner.tool.adb.AdbWrapper;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class ApkInstallWizard
{
	public static final int STATUS_INIT = 0;
	public static final int STATUS_DEVICE_SCANNING = 1;
	public static final int STATUS_DEVICE_REFRESH = 2;
	public static final int STATUS_SELECT_DEVICE = 3;
	public static final int STATUS_PACKAGE_SCANNING = 4;
	public static final int STATUS_CHECK_PACKAGES = 5;
	public static final int STATUS_SET_INSTALL_OPTION = 6;
	public static final int STATUS_INSTALLING = 7;
	public static final int STATUS_COMPLETED = 8;
	
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
	private ProgressPanel progressPanel;
	private ContentPanel contentPanel;
	private UIEventHandler uiEventHandler = new UIEventHandler();
	
	private int status;
	private int flag;
	private ArrayList<InstalledReport> installReports;

	private DeviceStatus[] targetDevices;
	private PackageInfo[] installedPackage;
	private ApkInfo apkInfo;
	
	private ApkScannerStub apkScanner;

	public class ApkInstallWizardDialog  extends JDialog
	{
		private static final long serialVersionUID = 2018466680871932348L;

		public ApkInstallWizardDialog() {
			dialog_init(null);
		}
		
		public ApkInstallWizardDialog(JFrame owner) {
			super(owner);
			dialog_init(owner);
		}
		
		public ApkInstallWizardDialog(JDialog owner) {
			super(owner);
			dialog_init(owner);
		}
		
		private void dialog_init(Component owner) {
			setTitle(Resource.STR_TITLE_INSTALL_WIZARD.getString());
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setResizable(false);
			setModal(false);

			initialize(this);
			setLocationRelativeTo(owner);
		}
	}
	
	public class ApkInstallWizardFrame extends JFrame
	{
		private static final long serialVersionUID = -5642057585041759436L;
		
		public ApkInstallWizardFrame() {
			frame_init();
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
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setResizable(false);

			initialize(this);
			setLocationRelativeTo(null);
			
			// Closing event of window be delete tempFile
			addWindowListener(uiEventHandler);
		}
	}
	
	private class ProgressPanel extends JPanel
	{
		private static final long serialVersionUID = 6145481552592676895L;


		public ProgressPanel() {
			super(new BorderLayout());
			
			// add progress image components...

			
			// set status
			setStatus(STATUS_INIT);
		}

		public void setStatus(int status) {
			switch(status) {
			case STATUS_INIT:
			case STATUS_DEVICE_SCANNING:
			case STATUS_SELECT_DEVICE:
			case STATUS_PACKAGE_SCANNING:
			case STATUS_CHECK_PACKAGES:
			case STATUS_SET_INSTALL_OPTION:
			case STATUS_INSTALLING:
			case STATUS_COMPLETED:
			default:
				break;
			}
		}
	}
	
	private class ContentPanel extends JPanel
	{
		private static final long serialVersionUID = -680173960208954055L;

		public static final String CONTENT_INIT = "DEVICE_SCANNING";
		public static final String CONTENT_DEVICE_SCANNING = "DEVICE_SCANNING";
		public static final String CONTENT_SELECT_DEVICE = "SELECT_DEVICE";
		public static final String CONTENT_PACKAGE_SCANNING = "PACKAGE_SCANNING";
		public static final String CONTENT_CHECK_PACKAGES = "CHECK_PACKAGES";
		public static final String CONTENT_SET_INSTALL_OPTION = "SET_INSTALL_OPTION";
		public static final String CONTENT_INSTALLING = "INSTALLING";
		public static final String CONTENT_COMPLETED = "COMPLETED";
		
		public ContentPanel(ActionListener listener) {
			super(new CardLayout());
			
			add(new JPanel(), CONTENT_INIT);
			add(new JPanel(), CONTENT_DEVICE_SCANNING);
			add(new JPanel(), CONTENT_SELECT_DEVICE);
			add(new JPanel(), CONTENT_PACKAGE_SCANNING);
			add(new JPanel(), CONTENT_CHECK_PACKAGES);
			add(new JPanel(), CONTENT_SET_INSTALL_OPTION);
			add(new JPanel(), CONTENT_INSTALLING);
			add(new JPanel(), CONTENT_COMPLETED);

			// set status
			setStatus(STATUS_INIT);
		}
		
		public void setStatus(int status) {
			switch(status) {
			case STATUS_INIT:
				((CardLayout)getLayout()).show(this, CONTENT_INIT);
				break;
			case STATUS_DEVICE_SCANNING:
				((CardLayout)getLayout()).show(this, CONTENT_DEVICE_SCANNING);
				break;
			case STATUS_DEVICE_REFRESH:
			case STATUS_SELECT_DEVICE:
				// set UI Data of device list 
				if(targetDevices.length == 0) {
					// disable select_all & next button 
				} else {
					// enable select_all & next button
				}
				
				if(status == STATUS_DEVICE_REFRESH) {
					// clear listview
				}
				
				// if() listview was not empty
				{
					boolean isAllSelected = true;
					//for(DeviceStatus dev: targetDevices)
					{
						// such dev.name in listview
						// isAllSelected = false;
					}
					if(isAllSelected) {
						// edit label to unselect_all 
					} else {
						// edit label to select_all
					}
				} 
				//else
				{
					// add devise to listview
					for(DeviceStatus dev: targetDevices) {
						if(dev.status.equals("device")) {
							// default check;
							Log.e(">>>>>> add listview " + dev.name + "(" + dev.device + ")");
						} else {
							// uncheck;
							Log.e(">>>>>> add listview " + dev.name + "(Unknown) - " + dev.status);
						}
					}
				}
				
				if(status == STATUS_DEVICE_REFRESH) {
					break;
				}

				((CardLayout)getLayout()).show(this, CONTENT_SELECT_DEVICE);
				break;
			case STATUS_PACKAGE_SCANNING:
				((CardLayout)getLayout()).show(this, CONTENT_PACKAGE_SCANNING);
				break;
			case STATUS_CHECK_PACKAGES:
				// set UI Data of package list
				for(int i = 0; i < targetDevices.length; i++) {
					if(installedPackage[i] != null) {
						Log.e(">>>>>> add listview " + targetDevices[i].name + " - " + installedPackage[i].apkPath);
					}
				}
				
				if((apkInfo.featureFlags & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
					// enable run app button
				}

				((CardLayout)getLayout()).show(this, CONTENT_CHECK_PACKAGES);
				break;
			case STATUS_SET_INSTALL_OPTION:
				// set state of component
				
				if((apkInfo.featureFlags & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
					// enable run app button
				} else {
					
				}
				if((apkInfo.manifest.installLocation.indexOf("internalOnly")) > -1) {
					// disable external
				} else {
					
				}

				if(apkInfo.certificates == null || apkInfo.certificates.length == 0) {
					// disable next
				} else {
					
				}
				
				boolean isAllRootDevice = true;
				for(DeviceStatus dev: targetDevices) {
					// such dev.name in listview
					if(!AdbWrapper.root(dev.name, null)) {
						isAllRootDevice = false;
						break;
					}
				}
				if(isAllRootDevice) {
					// enable push group
					boolean haveSystemApp = false;
					if(installedPackage != null) {
						for(PackageInfo pack: installedPackage) {
							if(pack.isSystemApp) {
								haveSystemApp = true;
								break;
							}
						}
					}
					if(haveSystemApp) {
						// enable overwrite
					} else {
						// disable overwrite
					}
					
					if(apkInfo.librarys == null || apkInfo.librarys.length == 0) {
						// disable with libs
					} else {
						
					}
				} else {
					// disable push group
				}
				
				((CardLayout)getLayout()).show(this, CONTENT_SET_INSTALL_OPTION);
				break;
			case STATUS_INSTALLING:
				((CardLayout)getLayout()).show(this, CONTENT_INSTALLING);
				break;
			case STATUS_COMPLETED:
				int successCount = 0;
				StringBuilder sb = new StringBuilder("-- Installation Reports ----------------\n");
				for(InstalledReport report: installReports) {
					sb.append(report);
					if(report.successed) {
						successCount++;
					}
				}
				sb.append("----------------------------------------");
				
				if(installReports.size() == successCount) {
					printLog("Installation succeeded.");
				} else if(installReports.size() == 1) {
					printLog("Installation failed.");
				} else {
					printLog(String.format("Installation succeeded %1$d of %2$d.", successCount, installReports.size()));
				}
				printLog(sb.toString());
				
				((CardLayout)getLayout()).show(this, CONTENT_COMPLETED);
				break;
			default:
				break;
			}
		}
		
		private String getSelectedLauncherActivity() {
			// get selected activity from combo box
			
			String[] list = getLauncherActivityList();
			return list.length > 0 ? list[0] : null;
		}
		
		private void appendLog(String msg) {
			// append to log viewer
			
		}
	}

	public ApkInstallWizard() {
		this((JFrame)null);
	}

	public ApkInstallWizard(JFrame owner) {
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame();
	}
	
	public ApkInstallWizard(JDialog owner) {
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame();
	}
	
	private void setVisible(boolean visible) {
		if(wizard != null) wizard.setVisible(visible);
	}

	private void initialize(Window window)
	{
		if(window == null) return;

		window.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		window.setSize(new Dimension(500,350));
		
		progressPanel = new ProgressPanel();
		contentPanel = new ContentPanel(uiEventHandler);
		
		window.add(progressPanel, BorderLayout.NORTH);
		window.add(contentPanel, BorderLayout.CENTER);
		
		//Log.i("initialize() register event handler");
		//window.addWindowListener(new UIEventHandler());
		
		// Shortcut key event processing
		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(uiEventHandler);
	}
	
	private void changeState(int status) {
		Log.e(">>>>>>>>>>>>> changeState() " + status);
		if(this.status == status) return;
		this.status = status;
		progressPanel.setStatus(status);
		contentPanel.setStatus(status);
		
		execute(status);
	}
	
	private void execute(int status) {
		switch(status) {
		case STATUS_DEVICE_SCANNING:
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(ApkInstallWizard.this) {
						targetDevices = AdbDeviceManager.scanDevices();
						next();
					}
				}
			}).start();
			break;
		case STATUS_PACKAGE_SCANNING:
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(ApkInstallWizard.this) {
						if(targetDevices != null && targetDevices.length > 0) {
							boolean existed = false;
							installedPackage = new PackageInfo[targetDevices.length];
							for(int i = 0; i < targetDevices.length; i++) {
								installedPackage[i] = AdbPackageManager.getPackageInfo(targetDevices[i].name, apkInfo.manifest.packageName);
								if(installedPackage[i] != null) existed = true;
							}
							if(!existed) {
								installedPackage = null;
							}
						}
						next();
					}
				}
			}).start();
			break;
		case STATUS_INSTALLING:
			installReports = new ArrayList<InstalledReport>(targetDevices.length);
			for(int i = 0; i < targetDevices.length; i++) {
				installApk(targetDevices[i], installedPackage != null ? installedPackage[i] : null);
			}
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
		if(apkInfo == null || apkInfo.filePath == null || 
				!(new File(apkInfo.filePath).isFile())) {
			Log.e("No such apk file...");
		    JOptionPane.showOptionDialog(null,
		    		Resource.STR_MSG_NO_SUCH_APK_FILE.getString(), 
		    		Resource.STR_LABEL_ERROR.getString(),
		    		JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, 
		    		Resource.IMG_WARNING.getImageIcon(),
		    		new String[] {Resource.STR_BTN_CLOSE.getString()},
		    		Resource.STR_BTN_CLOSE.getString());
			return;
		}
		setVisible(true);
		changeState(STATUS_DEVICE_SCANNING);
	}
	
	private void next() {
		synchronized(this) {
			switch(status) {
			case STATUS_INIT:
				changeState(STATUS_DEVICE_SCANNING);
				break;
			case STATUS_DEVICE_SCANNING:
				if(targetDevices == null || targetDevices.length != 1) {
					changeState(STATUS_SELECT_DEVICE);
					break;
				}
			case STATUS_SELECT_DEVICE:
				if(targetDevices != null) {
					boolean isAllOnline = true;
					for(DeviceStatus dev: targetDevices) {
						// such dev.name in listview
						if("".equals(dev.status)) {
							isAllOnline = false;
							break;
						}
					}
					if(isAllOnline) {
						changeState(STATUS_PACKAGE_SCANNING);
					} else if(status == STATUS_DEVICE_SCANNING) {
						changeState(STATUS_SELECT_DEVICE);
					} else {
						// show warring message, offline device selected...
					}
				}
				break;
			case STATUS_PACKAGE_SCANNING:
				if(installedPackage != null) {
					changeState(STATUS_CHECK_PACKAGES);
					break;
				}
			case STATUS_CHECK_PACKAGES:
				changeState(STATUS_SET_INSTALL_OPTION);
				break;
			case STATUS_SET_INSTALL_OPTION:
				if(flag == 0) break;
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
	
	private void previous() {
		synchronized(this) {
			switch(status) {
			case STATUS_CHECK_PACKAGES:
				changeState(STATUS_SELECT_DEVICE);
				break;
			case STATUS_SET_INSTALL_OPTION:
				changeState(STATUS_CHECK_PACKAGES);
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
		status = STATUS_INIT;
		start();
	}
	
	public void setApk(ApkInfo apkInfo) {
		this.apkInfo = apkInfo;
		if(apkScanner != null) {
			apkScanner.clear(false);
			apkScanner = null;
		}
	}

	public void setApk(String apkFilePath) {
		if(apkFilePath == null || !(new File(apkFilePath).isFile())) {
			Log.e("No such apk file... : " + apkFilePath);
		    JOptionPane.showOptionDialog(null,
		    		Resource.STR_MSG_NO_SUCH_APK_FILE.getString(), 
		    		Resource.STR_LABEL_ERROR.getString(),
		    		JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, 
		    		Resource.IMG_WARNING.getImageIcon(),
		    		new String[] {Resource.STR_BTN_CLOSE.getString()},
		    		Resource.STR_BTN_CLOSE.getString());
			return;
		}

		if(apkScanner != null) {
			apkScanner.clear(false);
			apkScanner = null;
		}
		apkScanner = new AaptScanner(new ApkScannerStub.StatusListener() {
			@Override
			public void OnStateChanged(Status status) {
				Log.i("OnStateChanged() "+ status);
				switch(status) {
				case BASIC_INFO_COMPLETED:
				case LIB_COMPLETED:
				case CERT_COMPLETED:
				case ACTIVITY_COMPLETED:
					break;
				default:
					break;
				}
			}

			@Override public void OnSuccess() { }
			@Override public void OnStart(long estimatedTime) { }
			@Override public void OnProgress(int step, String msg) { }
			@Override public void OnError() { }
			@Override public void OnComplete() { }
		});
		apkScanner.openApk(apkFilePath);
		apkInfo = apkScanner.getApkInfo();
	}
	
	private String[] getLauncherActivityList() {
		ArrayList<String> launcherList = new ArrayList<String>();
		ArrayList<String> mainList = new ArrayList<String>(); 
		if(apkInfo != null &&
				apkInfo.manifest != null &&
				apkInfo.manifest.application != null) {
			if(apkInfo.manifest.application.activity != null) {
				for(ActivityInfo info: apkInfo.manifest.application.activity) {
					if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
						if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0)
							launcherList.add(info.name);
						else
							mainList.add(info.name);
					}
				}
			}
			if(apkInfo.manifest.application.activityAlias != null) {
				for(ActivityAliasInfo info: apkInfo.manifest.application.activityAlias) {
					if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
						if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0)
							launcherList.add(info.name);
						else
							mainList.add(info.name);
					}
				}
			}
			launcherList.addAll(mainList);
		}
		return launcherList.toArray(new String[0]);
	}
	
	private void launchApp(final String device) {
		final String selectedActivity = contentPanel.getSelectedLauncherActivity();
		if(selectedActivity == null) {
			Log.w("No such launch activity");
			ArrowTraversalPane.showOptionDialog(null,
					Resource.STR_MSG_NO_SUCH_LAUNCHER.getString(),
					Resource.STR_LABEL_WARNING.getString(),
					JOptionPane.OK_OPTION, 
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new String[] {Resource.STR_BTN_OK.getString()},
					Resource.STR_BTN_OK.getString());
			return;
		}

		new Thread(new Runnable() {
			public void run()
			{
				String launcherActivity = apkInfo.manifest.packageName + "/" + selectedActivity;
				String[] cmdResult = AdbWrapper.shell(device, new String[] {"am", "start", "-n", launcherActivity}, null);
				if(cmdResult == null || (cmdResult.length > 2 && cmdResult[1].startsWith("Error")) ||
						(cmdResult.length > 1 && cmdResult[0].startsWith("error"))) {
					Log.e("activity start faile : " + launcherActivity);
					Log.e(String.join("\n", cmdResult));
					ArrowTraversalPane.showOptionDialog(null,
							Resource.STR_MSG_FAILURE_LAUNCH_APP.getString(),
							Resource.STR_LABEL_WARNING.getString(),
							JOptionPane.OK_OPTION, 
							JOptionPane.INFORMATION_MESSAGE,
							null,
							new String[] {Resource.STR_BTN_OK.getString()},
							Resource.STR_BTN_OK.getString());
				}
			}
		}).start();
	}
	
	public void openApk(DeviceStatus dev, PackageInfo pkgInfo) {
		String tmpPath = "/" + dev.name + pkgInfo.apkPath;
		tmpPath = tmpPath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
		tmpPath = FileUtil.makeTempPath(tmpPath)+".apk";
		final String openApkPath = tmpPath; 

		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			@Override
			public void OnError(int cmdType, String device) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_FAILURE_PULL_APK.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				Launcher.run(openApkPath);
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) { printLog(msg); }
		});
		apkInstaller.pullApk(pkgInfo.apkPath, tmpPath);
	}
	
	private void saveApk(DeviceStatus dev, PackageInfo pkgInfo) {
		String saveFileName;
		if(pkgInfo.apkPath.endsWith("base.apk")) {
			saveFileName = pkgInfo.apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
		} else {
			saveFileName = pkgInfo.apkPath.replaceAll(".*/", "");
		}

		final File destFile = ApkFileChooser.saveApkFile(wizard, saveFileName);
		if(destFile == null) return;

		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			@Override
			public void OnError(int cmdType, String device) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_FAILURE_PULL_APK.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				int n = ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_SUCCESS_PULL_APK.getString() + "\n" + destFile.getAbsolutePath(),
						Resource.STR_LABEL_QUESTION.getString(),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				switch(n) {
				case 0: // explorer
					String openner = (System.getProperty("os.name").indexOf("Window") > -1) ? "explorer" : "xdg-open";
					try {
						new ProcessBuilder(openner, destFile.getParent()).start();
					} catch (IOException e1) { }
					break;
				case 1: // open
					Launcher.run(destFile.getAbsolutePath());
					break;
				default:
					break;
				}
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) { printLog(msg); }
		});		
		apkInstaller.pullApk(pkgInfo.apkPath, destFile.getAbsolutePath());
	}
	
	private void uninstallApk(final DeviceStatus dev, final PackageInfo pkgInfo) {
		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			@Override
			public void OnError(int cmdType, String device) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_FAILURE_UNINSTALLED.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				Log.i("Success APK uninstall or remove " + cmdType);
				if(cmdType == CMD_REMOVE) {
					final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
					int reboot = ArrowTraversalPane.showOptionDialog(null,
							Resource.STR_MSG_SUCCESS_REMOVED.getString() + "\n" + Resource.STR_QUESTION_REBOOT_DEVICE.getString(),
							Resource.STR_LABEL_INFO.getString(),
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE,
							null,
							yesNoOptions, yesNoOptions[1]);
					if(reboot == 0){							
						AdbWrapper.reboot(dev.name, null);
					}
				} else {
					ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_SUCCESS_REMOVED.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				}
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) { printLog(msg); }
		});

		if(pkgInfo.isSystemApp) {
			if(!AdbWrapper.root(dev.name, null)) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_DEVICE_HAS_NOT_ROOT.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				return;
			}
			apkInstaller.removeApk(pkgInfo.codePath);
		} else {
			apkInstaller.uninstallApk(pkgInfo.pkgName);
		}
	}
	
	private void installReport(final DeviceStatus dev, boolean sucess, String errorMsg) {
		installReports.add(new InstalledReport(dev, sucess, errorMsg));
		if(installReports.size() >= targetDevices.length) {
			Log.e("Installation completed");
			changeState(STATUS_COMPLETED);
		}
	}
	
	private void installApk(final DeviceStatus dev, final PackageInfo pkgInfo) {
		// install
		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			String ErrorMsg = "";
			@Override
			public void OnError(int cmdType, String device) {
				if(cmdType == CMD_REMOVE) {
					 return;
				}
				printLog("Error installed " + dev.name + "-" + dev.device + ", " + ErrorMsg);
				if(ErrorMsg.indexOf("INSTALL_FAILED_INSUFFICIENT_STORAGE") > -1) {
					// 
				} else {
					
				}
				installReport(dev, false, ErrorMsg);
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				if(cmdType == CMD_REMOVE) {
					return;
				}
				printLog("Succeed install into device(" + device + ")");
				if(cmdType == CMD_INSTALL) {
					if((flag & FLAG_OPT_EXTRA_RUN) == FLAG_OPT_EXTRA_RUN) {
						printLog("Launch app in device(" + device + ")");
						launchApp(dev.name);
					}
				} else if(cmdType == CMD_PUSH) {
					if((flag & FLAG_OPT_EXTRA_REBOOT) == FLAG_OPT_EXTRA_REBOOT) {
						printLog("reboot device(" + device + ")");
						AdbWrapper.reboot(dev.name, null);
					}
				}
				installReport(dev, true, null);
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) {
				String errmsg = msg.toUpperCase();
				if(errmsg.indexOf("ERROR") > -1 || 
						errmsg.indexOf("FAILURE") > -1 ||
						errmsg.indexOf("FAILED") > -1) {
					ErrorMsg = msg;
				}
				printLog(msg); 
			}
		});

		if((flag & FLAG_OPT_INSTALL) == FLAG_OPT_INSTALL) {
			printLog("Install APK ...");
			apkInstaller.installApk(apkInfo.filePath, (flag & FLAG_OPT_INSTALL_EXTERNAL) != 0);
		} else if((flag & FLAG_OPT_PUSH) == FLAG_OPT_PUSH) {
			printLog("Install APK by push ...");
			String destPath = null;
			if((flag & FLAG_OPT_PUSH_OVERWRITE) == FLAG_OPT_PUSH_OVERWRITE &&
					pkgInfo != null) {
				printLog("Overwrite APK ...");
				printLog("Existing path : " + pkgInfo.apkPath);
				destPath = pkgInfo.apkPath;
			} else {
				if(pkgInfo != null && pkgInfo.codePath != null) {
					printLog("Delete existing APK ...");
					printLog("code path : " + pkgInfo.codePath);
					apkInstaller.removeApk(pkgInfo.codePath);
				}
				if((flag & FLAG_OPT_PUSH_SYSTEM) == FLAG_OPT_PUSH_SYSTEM) {
					destPath = "/system/app/";
				} else if((flag & FLAG_OPT_PUSH_PRIVAPP) == FLAG_OPT_PUSH_PRIVAPP) {
					destPath = "/system/priv-app/";
				} else if((flag & FLAG_OPT_PUSH_DATA) == FLAG_OPT_PUSH_DATA) {
					destPath = "/data/app/";
				} else {
					destPath = "/system/app/";
				}
				destPath += apkInfo.manifest.packageName + "-1/base.apk";
				printLog("New APK Path : " + destPath);
			}
			
			String libPath = null;
			if((flag & FLAG_OPT_EXTRA_WITH_LIB) == FLAG_OPT_EXTRA_WITH_LIB &&
					(flag & FLAG_OPT_PUSH_DATA) != FLAG_OPT_PUSH_DATA &&
					apkInfo.librarys != null && apkInfo.librarys.length > 0) {
				printLog("With libraries ...");
				// unzip libs..
				if(ZipFileUtil.unZip(apkInfo.filePath, "lib/", apkInfo.tempWorkPath+File.separator+"lib")) {
					libPath = apkInfo.tempWorkPath + File.separator + "lib" + File.separator;
					printLog("Success unzip lib ... " + libPath);
				} else {
					printLog("Fail unzip lib ...");	
				}
			}
			apkInstaller.pushApk(apkInfo.filePath, destPath, libPath);
		}

	}
	
	private void printLog(String msg) {
		Log.v(msg);	
		// append to log viewer
		contentPanel.appendLog(msg);
	}
	
	private class InstalledReport {
		public DeviceStatus dev;
		public boolean successed;
		public String errMessage;
		
		public InstalledReport(DeviceStatus dev, boolean successed, String errMessage) {
			this.dev = dev;
			this.successed = successed;
			this.errMessage = errMessage;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(successed) {
				sb.append("SUCCESS: " + dev.device + "(" + dev.name + ")\n");
			} else {
				sb.append("FAILURE: " + dev.device + "(" + dev.name + ")\n");
				sb.append("\tERROR : " + errMessage + "\n");
			}
			return sb.toString();
		}
	}
	
	private class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if("NEXT".equals(arg0.getActionCommand())) {
				next();
			} else if("PREVIOUS".equals(arg0.getActionCommand())) {
				previous();
			} else if("REFRESH".equals(arg0.getActionCommand())) {
				new Thread(new Runnable() {
					public void run()
					{
						synchronized(ApkInstallWizard.this) {
							targetDevices = AdbDeviceManager.scanDevices();
							contentPanel.setStatus(STATUS_DEVICE_REFRESH);
						}
					}
				}).start();
			} else if("SELECT_ALL".equals(arg0.getActionCommand())) {
				
			} else if("RUN".equals(arg0.getActionCommand())) {
				
			} else if("OPEN".equals(arg0.getActionCommand())) {
				
			} else if("SAVE".equals(arg0.getActionCommand())) {

			} else if("UNINSTALL".equals(arg0.getActionCommand())) {
				
			} else if("CHANG_SIGN".equals(arg0.getActionCommand())) {
				
			} else if("RESTART".equals(arg0.getActionCommand())) {
				restart();
			} else if("CANCEL".equals(arg0.getActionCommand())) {
				wizard.dispose();
			} else if("OK".equals(arg0.getActionCommand())) {
				wizard.dispose();
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent arg0) {
			return false;
		}
		
		// Closing event of window be delete tempFile
		@Override
		public void windowClosing(WindowEvent e)
		{
			if(apkScanner != null) {
				apkScanner.clear(false);
				apkScanner = null;
			}
		}
		
		@Override public void windowOpened(WindowEvent e) { }
		@Override public void windowClosed(WindowEvent e) { }
		@Override public void windowIconified(WindowEvent e) { }
		@Override public void windowDeiconified(WindowEvent e) { }
		@Override public void windowActivated(WindowEvent e) { }
		@Override public void windowDeactivated(WindowEvent e) { }
	};
	// ----------------------------------------------------------------------------------------
	
	
	
	
	
	
	
	
	
	
	
	//static private JTextArea dialogLogArea;
	//static private JDialog dlgDialog = null;

	//static private JPanel installPanel;
	//static private JPanel uninstallPanel;
	
	static private String strPackageName;
	private static String strSourcePath;
	private static String strLibPath;
	private static String tmpApkPath;
	private static boolean checkPackage;
	private static boolean samePackage;
	static private Thread t;
	
	//window position
	//static private int nPositionX, nPositionY;
	
	public interface InstallButtonStatusListener
	{
		public void SetInstallButtonStatus(Boolean Flag);
		public void OnOpenApk(String path);
	}
	
	public interface InstallDlgFuncListener {
		public void Complete(String str);
		public int ShowQuestion(Runnable runnable, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue);
		public void AddLog(String str);
		public int getResult();
		public void SetResult(int i);
		public int ShowDeviceList(Runnable runnable);
		public void AddCheckList(String name, String t, InstallDlg.CHECKLIST_MODE mode);		
		DeviceStatus getSelectDev();
		public int getValue(String text);
	}
	private static InstallButtonStatusListener Listener;
	private static InstallDlgFuncListener InstallDlgListener;
	
	
	public ApkInstallWizard(Frame owner, Boolean isOnlyInstall, String PackageName, String apkPath, String libPath, 
			final boolean samePackage, final boolean checkPackage, final InstallButtonStatusListener Listener)
	{

		strPackageName = PackageName;
		strSourcePath = apkPath;
		strLibPath = libPath;
		ApkInstallWizard.checkPackage = checkPackage;
		ApkInstallWizard.samePackage = samePackage;
		
		//ShowSetupLogDialog();
		//dialogLogArea.setText("");
		
		ApkInstallWizard.Listener = Listener; 
		
		
		InstallDlg dlg = new InstallDlg(owner, isOnlyInstall);
		ApkInstallWizard.InstallDlgListener = dlg.getInstallDlgFuncListener();
		
		
		
		t = new InstallThread();
		t.start();
	}
	
	static class InstallThread extends Thread {
		
		final ImageIcon Appicon = Resource.IMG_QUESTION.getImageIcon();
        final Object[] options = {Resource.STR_BTN_PUSH.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] checkPackOptions = {Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] checkPackDelOptions = {Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_DEL.getString(), Resource.STR_BTN_CANCEL.getString()};
        final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
		
		public InstallThread() {
			
		}
		
		private class AdbWrapperObserver implements ApkInstallerListener
		{
			
			private final ImageIcon QuestionAppicon;
			private final ImageIcon WaringAppicon;
			private final ImageIcon SucAppicon;
			
			public AdbWrapperObserver()
			{
				QuestionAppicon = Resource.IMG_QUESTION.getImageIcon();
				WaringAppicon = Resource.IMG_WARNING.getImageIcon();
				SucAppicon = Resource.IMG_SUCCESS.getImageIcon();
			}
			
			@Override
			public void OnMessage(String msg) {
				printlnLog(msg);
			}

			@Override
			public void OnError(int cmdType, String device) {
				if(cmdType == ApkInstallerListener.CMD_PUSH) {
					printlnLog("Failure...");
					//JOptionPane.showMessageDialog(null, "Failure...", "Error",JOptionPane.ERROR_MESSAGE, WaringAppicon);
				} else if(cmdType == ApkInstallerListener.CMD_INSTALL) {
					//JOptionPane.showMessageDialog(null, "Failure...", "Error", JOptionPane.ERROR_MESSAGE, WaringAppicon);
				} else if(cmdType == ApkInstallerListener.CMD_PULL) {
					//JOptionPane.showMessageDialog(null, "Failure...", "Error", JOptionPane.ERROR_MESSAGE, WaringAppicon);
				}
				InstallDlgListener.AddCheckList("Install", "fail" , InstallDlg.CHECKLIST_MODE.ERROR);
				
				ShowQuestion(t, Resource.STR_MSG_FAILURE_INSTALLED.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, WaringAppicon,
			    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				if(cmdType == ApkInstallerListener.CMD_PUSH) {
					final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
					InstallDlgListener.AddCheckList("Push", "Success" , InstallDlg.CHECKLIST_MODE.DONE);
					
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
					int reboot = ShowQuestion(t, Resource.STR_MSG_SUCCESS_INSTALLED.getString() + "\n" + Resource.STR_QUESTION_REBOOT_DEVICE.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE, QuestionAppicon, yesNoOptions, yesNoOptions[1]);
					
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), (reboot==0)?"true":"false" , InstallDlg.CHECKLIST_MODE.DONE);
					
					if(reboot == 0){
						printlnLog("Wait for reboot...");
						AdbWrapper.reboot(device, null);
						printlnLog("Reboot...");
					}
				} else if(cmdType == ApkInstallerListener.CMD_INSTALL) {
					//JOptionPane.showMessageDialog(null, "Success", "Complete", JOptionPane.INFORMATION_MESSAGE, SucAppicon);
						InstallDlgListener.AddCheckList("Install", "Success" , InstallDlg.CHECKLIST_MODE.DONE);
						ShowQuestion(t, Resource.STR_MSG_SUCCESS_INSTALLED.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, SucAppicon,
				    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
				} else if(cmdType == ApkInstallerListener.CMD_PULL) {
					InstallDlgListener.AddCheckList("Pull success", "Done" , InstallDlg.CHECKLIST_MODE.DONE);					
					if(Listener != null) Listener.OnOpenApk(tmpApkPath);
					InstallDlgListener.AddCheckList("Open APK", "Done" , InstallDlg.CHECKLIST_MODE.ADD);
				} 
			}

			@Override
			public void OnCompleted(int cmdType, String device) {
				Listener.SetInstallButtonStatus(true);
//				ShowQuestion(t, "완료", Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, SucAppicon,
//			    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
				//installPanel.setVisible(false);
			}
		}
		
		
		private int ShowQuestion(Runnable runnable, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
			Object[] temp = new Object[options.length];
			
			for(int i=0; i<options.length; i++) {
				temp[options.length-1-i] = options[i];
			}		
			@SuppressWarnings("unused")
			int result = InstallDlgListener.ShowQuestion(runnable,message,title,optionType,messageType, icon, temp, initialValue);
			
			if(runnable!=null) {
				synchronized (runnable) {
					try {
						runnable.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return InstallDlgListener.getResult();
		}
		
		private void printlnLog(String msg)
		{
			Log.i(msg);
			if(InstallDlgListener != null) {
				InstallDlgListener.AddLog(msg);
			}
		}
		
		private int showDeviceList(Runnable runnable) {
			
			@SuppressWarnings("unused")
			int result = InstallDlgListener.ShowDeviceList(runnable);
			
			synchronized (runnable) {
				try {
					runnable.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			return InstallDlgListener.getResult();
		}
		
		public void run(){
			try {
				DeviceStatus[] DeviceList;
				
				do {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString(), "", InstallDlg.CHECKLIST_MODE.WATING);
					printlnLog("scan devices...");
					DeviceList = AdbDeviceManager.scanDevices();
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString(), "", InstallDlg.CHECKLIST_MODE.DONE);
					
					if(DeviceList.length == 0) {
						printlnLog("Device not found!\nplease check device");
						Listener.SetInstallButtonStatus(true);
						final ImageIcon Appicon = Resource.IMG_WARNING.getImageIcon();
						
						Log.d("show Question");
						
						int n = ShowQuestion(this, Resource.STR_MSG_DEVICE_NOT_FOUND.getString(), Resource.STR_LABEL_WARNING.getString(), JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE, Appicon,
					    		new String[] {Resource.STR_BTN_REFRESH.getString(), Resource.STR_BTN_CANCEL.getString()}, Resource.STR_BTN_REFRESH.getString());
						
						//InstallDlgListener.AddCheckList(Resource.STR_MSG_DEVICE_NOT_FOUND.getString(), "-", InstallDlg.CHECKLIST_MODE.ERROR);
						
						//int n = InstallDlgListener.getResult();
						Log.d(n+"");
						
						if(n==-1 || n==1) {								
							return;
						}
					} else {
						break;
					}
				} while(true);
				DeviceStatus dev = DeviceList[0];
								
				if(DeviceList.length > 1 || (DeviceList.length == 1 && !dev.status.equals("device"))) {
					//int selectedValue = DeviceListDialog.showDialog();
					//Log.i("Seltected index : " + selectedValue);
					
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString() + " List", "", InstallDlg.CHECKLIST_MODE.QEUESTION);
					int selectedValue = showDeviceList(this);
					if(selectedValue == -1) {
						Listener.SetInstallButtonStatus(true);
						
						return;
					}
					dev = InstallDlgListener.getSelectDev();
					
//					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString() + " List", dev.name +
//							"(" + dev.device + ")", InstallDlg.CHECKLIST_MODE.DONE);
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString()+ " List", dev.name +
							"(" + dev.device + ")", InstallDlg.CHECKLIST_MODE.DONE);
				} else {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_DEVICE.getString(), dev.name +
							"(" + dev.device + ")", InstallDlg.CHECKLIST_MODE.DONE);
				}

				printlnLog(dev.getSummary());
				
				
				
				boolean alreadyCheak = false;
				printlnLog("getPackageInfo() " + strPackageName);
				PackageInfo pkgInfo = AdbPackageManager.getPackageInfo(dev.name, strPackageName);
				
				if(pkgInfo==null) {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_VERSION.getString(), "not install", InstallDlg.CHECKLIST_MODE.ADD);
				} else {
					InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_VERSION.getString(), pkgInfo.versionName + "/"+pkgInfo.versionCode , InstallDlg.CHECKLIST_MODE.ADD);
				}
				
				if(checkPackage) {
					alreadyCheak = true;
					if(pkgInfo != null) {
						String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
						boolean isDeletePossible = true;
						if(pkgInfo.isSystemApp == true && AdbWrapper.root(dev.name, null) != true) {
							isDeletePossible = false;
						}
						
						InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_ROOT.getString(), ""+isDeletePossible , InstallDlg.CHECKLIST_MODE.ADD);
						
						
						int n;
						if(isDeletePossible) {
							InstallDlgListener.AddCheckList(""+checkPackDelOptions[0] +"/"+ checkPackDelOptions[1]+"/" + checkPackDelOptions[2], "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);							
							n=ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackDelOptions, checkPackDelOptions[3]);
							InstallDlgListener.AddCheckList(""+checkPackDelOptions[n], ""+checkPackDelOptions[n] , InstallDlg.CHECKLIST_MODE.DONE);
							
							
						} else {
							InstallDlgListener.AddCheckList(""+checkPackOptions[0] +"/"+ checkPackOptions[1] , "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
							n=ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackOptions, checkPackOptions[2]);
							InstallDlgListener.AddCheckList(""+checkPackOptions[n], ""+checkPackOptions[n] , InstallDlg.CHECKLIST_MODE.DONE);
						}
						
						
						Log.i("Seltected index : " + n);
						if(n==-1 || (!isDeletePossible && n==2) || (isDeletePossible && n==3)) {
							Listener.SetInstallButtonStatus(true);
							
							return;
						}
						ApkInstaller apkInstaller = new ApkInstaller(dev.name, new AdbWrapperObserver());
						if(n==0) {
							String tmpPath = "/" + dev.name + pkgInfo.apkPath;
							tmpPath = tmpPath.replaceAll("/", File.separator+File.separator).replaceAll("//", "/");
							tmpPath = FileUtil.makeTempPath(tmpPath)+".apk";
							tmpApkPath = tmpPath; 
							//Log.i(tmpPath);
							InstallDlgListener.AddCheckList("Pull APK", "working" , InstallDlg.CHECKLIST_MODE.WATING);
							apkInstaller.pullApk(pkgInfo.apkPath, tmpPath);							
							return;
						}
						if(n==2) {
							//uninstallPanel.setVisible(true);
							if(pkgInfo.isSystemApp) {
								printlnLog("adb shell rm " + pkgInfo.codePath);
								
								InstallDlgListener.AddCheckList("remove APK", "working" , InstallDlg.CHECKLIST_MODE.WATING);
								apkInstaller.removeApk(pkgInfo.codePath);
								InstallDlgListener.AddCheckList("remove APK", "Done" , InstallDlg.CHECKLIST_MODE.DONE);
								
								InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
								final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
								int reboot = ShowQuestion(this, Resource.STR_QUESTION_REBOOT_DEVICE.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.YES_NO_OPTION, 
										JOptionPane.QUESTION_MESSAGE, Appicon, yesNoOptions, yesNoOptions[1]);
								InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_REBOOT.getString(), (reboot==0)?"true":"false" , InstallDlg.CHECKLIST_MODE.DONE);
								if(reboot == 0){
									printlnLog("Wait for reboot...");									
									AdbWrapper.reboot(dev.name, null);
									printlnLog("Reboot...");
								}
								
							} else {
								InstallDlgListener.AddCheckList("Uninstall APK", "-" , InstallDlg.CHECKLIST_MODE.WATING);
								printlnLog("adb uninstall " + pkgInfo.pkgName);
								apkInstaller.uninstallApk(pkgInfo.pkgName);
								InstallDlgListener.AddCheckList("Uninstall APK", "Done" , InstallDlg.CHECKLIST_MODE.DONE);
							}
							printlnLog("compleate");
							//uninstallPanel.setVisible(false);
							Listener.SetInstallButtonStatus(true);
							return;
						}
					} else {
						//JOptionPane.showMessageDialog(null, "동일 패키지가 설치되어 있지 않습니다.", "Info", JOptionPane.INFORMATION_MESSAGE, Appicon);
						InstallDlgListener.AddCheckList("Install", "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
						int n = ShowQuestion(this, Resource.STR_MSG_NO_SUCH_PACKAGE.getString() + "\n" + Resource.STR_QUESTION_CONTINUE_INSTALL.getString(), Resource.STR_LABEL_INFO.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, Appicon,
								yesNoOptions, yesNoOptions[1]);
						InstallDlgListener.AddCheckList("Install", (n==0)?"Install":"not install" , InstallDlg.CHECKLIST_MODE.DONE);
						if(n==-1 || n==1) {
							Listener.SetInstallButtonStatus(true);
							
							return;
						}
					}
				}
				if(pkgInfo != null) {
					printlnLog(pkgInfo.toString());
					if(pkgInfo.isSystemApp == true) {
						if(AdbWrapper.root(dev.name, null) == true) {
							printlnLog("adbd is running as root");
							String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
							if(!checkPackage)InstallDlgListener.AddCheckList(Resource.STR_TREE_MESSAGE_ROOT.getString(), ""+AdbWrapper.root(dev.name, null) , InstallDlg.CHECKLIST_MODE.ADD);
							
							InstallDlgListener.AddCheckList("" + options[0] +"/"+ options[1], "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
							int n = ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_PUSH_OR_INSTALL.getString(),
									Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, options, options[1]);
							//Log.i("Seltected index : " + n);
							
							InstallDlgListener.AddCheckList(options[n] + "", (n==0) ?"push":"install", InstallDlg.CHECKLIST_MODE.DONE);
							
							if(n==-1 || n==2) {
								Listener.SetInstallButtonStatus(true);
								
								InstallDlgListener.AddCheckList("Cancel", "cancel", InstallDlg.CHECKLIST_MODE.DONE);
								return;
							} 
							if(n==0) {
								printlnLog("Start push APK");
								//installPanel.setVisible(true);
								InstallDlgListener.AddCheckList("Push", "-" , InstallDlg.CHECKLIST_MODE.WATING);
								new ApkInstaller(dev.name, new AdbWrapperObserver()).pushApk(strSourcePath, pkgInfo.apkPath, strLibPath);
								
								return;
							}
							alreadyCheak = true;
						} else {
							printlnLog("adbd cannot run as root in production builds");
						}
					}
					if(samePackage && !alreadyCheak) {
						String strLine = "━━━━━━━━━━━━━━━━━━━━━━\n";
						InstallDlgListener.AddCheckList("Install", "-" , InstallDlg.CHECKLIST_MODE.QEUESTION);
						int n = ShowQuestion(this, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_CONTINUE_INSTALL.getString(),
								Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, yesNoOptions, yesNoOptions[1]);
						//Log.i("Seltected index : " + n);
						
						InstallDlgListener.AddCheckList("Install", (n==-1 || n==1)?"Cancel":"Install" , InstallDlg.CHECKLIST_MODE.DONE);
						
						if(n==-1 || n==1) {
							Listener.SetInstallButtonStatus(true);
							
							return;
						}
					}
				}
				printlnLog("Start install APK");
				//installPanel.setVisible(true);
				InstallDlgListener.AddCheckList("Install", "Install" , InstallDlg.CHECKLIST_MODE.WATING);
				new ApkInstaller(dev.name, new AdbWrapperObserver()).installApk(strSourcePath, false);				
			} finally {
				//Listener.SetInstallButtonStatus(true);
				InstallDlgListener.Complete("END");				
			}		
		}
	}

	@SuppressWarnings("deprecation")
	static public void StopThead() {
		
		Listener.SetInstallButtonStatus(true);
		t.stop();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	static public void RestartThread() {
		
		Listener.SetInstallButtonStatus(false);
		//t = new InstallThread();
		if(!t.isAlive()){
			t = new InstallThread();
			t.start();
		}
	}
}


