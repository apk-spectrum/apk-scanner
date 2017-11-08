package com.apkscanner.gui.dialog;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.DefaultOptionsFactory;
import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.core.installer.OptionsBundle.IOptionsChangedListener;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.gui.install.ContentPanel;
import com.apkscanner.gui.install.ControlPanel;
import com.apkscanner.gui.install.DeviceCustomList;
import com.apkscanner.gui.install.DeviceListData;
import com.apkscanner.gui.install.InstallOptionPanel;
import com.apkscanner.gui.install.InstallProgressPanel;
import com.apkscanner.gui.install.ToggleButtonBar;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class ApkInstallWizard implements IDeviceChangeListener
{
	public static final int STATUS_INIT = 0;
	public static final int STATUS_APK_VERIFY = 1;
	public static final int STATUS_WAIT_FOR_DEVICE = 2;
	public static final int STATUS_SET_OPTIONS = 3;
	public static final int STATUS_INSTALLING = 4;
	public static final int STATUS_COMPLETED = 5;

	public static final int STATUS_APK_VERTIFY_ERROR = 101;

	// UI components
	private Window wizard;
	private InstallProgressPanel progressPanel;
	private ContentPanel contentPanel;
	private ControlPanel controlPanel;
	private DeviceCustomList deviceList;
	private DefaultListModel<DeviceListData> deviceListModel;
	private UIEventHandler uiEventHandler = new UIEventHandler();

	private InstallOptionPanel installOptionPanel;
	private PackageInfoPanel pacakgeInfoPanel;
	private JLabel errorMessageLable;

	private String pakcageFilePath;
	private CompactApkInfo apkInfo;
	private SignatureReport signatureReport;
	private DefaultOptionsFactory optFactory;
	private HashMap<IDevice, DeviceListData> deviceDataMap = new HashMap<IDevice, DeviceListData>();

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
			MessageBoxPool.show(null, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}
		wizard = new ApkInstallWizardFrame();
	}

	public ApkInstallWizard(String FilePath, JFrame owner) {
		pakcageFilePath = FilePath;
		if(FilePath == null || !(new File(FilePath).isFile())) {
			Log.e("No such apk file... : " + FilePath);
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
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
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
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
		deviceList = new DeviceCustomList(uiEventHandler);
		deviceList.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		deviceList.addListSelectionListener(uiEventHandler);
		deviceListModel = (DefaultListModel<DeviceListData>) deviceList.getModel();

		installOptionPanel = new InstallOptionPanel();
		pacakgeInfoPanel = new PackageInfoPanel();
		errorMessageLable = new JLabel("Please Check this APK file!", SwingConstants.CENTER);
		errorMessageLable.setFont(new Font("Serif", Font.PLAIN, 30));

		contentPanel.add(installOptionPanel, ContentPanel.CONTENT_SET_OPTIONS);
		contentPanel.add(pacakgeInfoPanel, ContentPanel.CONTENT_PACKAGE_INFO);
		contentPanel.add(errorMessageLable, ContentPanel.CONTENT_VERIFY_ERROR);

		JPanel panelDummy = new JPanel();
		//progressPanel.setPreferredSize(new Dimension(700, 200));
		panelDummy.setBackground(Color.WHITE);
		panelDummy.setOpaque(true);
		panelDummy.setPreferredSize(new Dimension(600, 80));
		panelDummy.add(progressPanel);

		window.add(panelDummy, BorderLayout.NORTH);


		window.add(deviceList, BorderLayout.WEST);
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
		Log.v("changeState() " + status);
		if(!EventQueue.isDispatchThread()) {
			Log.w("changeState() isDispatchThread " + EventQueue.isDispatchThread());
		}

		if(this.status == status) return;
		this.status = status;

		progressPanel.setStatus(status);
		contentPanel.setStatus(status);
		controlPanel.setStatus(status);

		switch(status) {
		case STATUS_SET_OPTIONS:
		case STATUS_INSTALLING:
		case STATUS_COMPLETED:
			deviceList.setVisible(true);
			break;
		default:
			deviceList.setVisible(false);
			break;
		}

		execute(status);
	}

	private void execute(int status) {
		switch(status) {
		case STATUS_INIT:
			next();
			break;
		case STATUS_APK_VERIFY:
			new SwingWorker<Boolean, Void>() {
				protected Boolean doInBackground() throws Exception {
					String apkFilePath = pakcageFilePath;

					ApkScanner scanner = ApkScanner.getInstance("AAPTLIGHT");
					scanner.openApk(apkFilePath);
					if(scanner.getLastErrorCode() != ApkScanner.NO_ERR) {
						Log.e("Fail open APK: errcode " + scanner.getLastErrorCode());
						return false;
					}
					apkInfo = new CompactApkInfo(scanner.getApkInfo());

					signatureReport = null;
					try {
						signatureReport = new SignatureReport(new JarFile(apkFilePath, true));
					} catch (Exception e) { }
					if(signatureReport == null || signatureReport.getSize() == 0) {
						Log.e("Fail APK Virify");
						return false;
					}
					optFactory = new DefaultOptionsFactory(apkInfo, signatureReport);
					return true;
				}

				@Override
				protected void done() {
					boolean pass = false;
					try {
						pass = get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
						pass = false;
					}
					if(pass) {
						installOptionPanel.setApkInfo(apkInfo);
						next();
					} else {
						changeState(STATUS_APK_VERTIFY_ERROR);
					}
				}
			}.execute();
			break;
		case STATUS_WAIT_FOR_DEVICE:
			revaluationDeviceState(null);
			break;
		case STATUS_SET_OPTIONS:
			break;
		case STATUS_INSTALLING:
			AndroidDebugBridge.removeDeviceChangeListener(this);
			synchronized (deviceDataMap) {
				for(Entry<IDevice, DeviceListData> entry: deviceDataMap.entrySet()) {
					OptionsBundle bundle = entry.getValue().getOptionsBundle();
					if(bundle.isInstallOptions() || bundle.isPushOptions()) {
						entry.getValue().setState(DeviceListData.STATUS_INSTALLING);
					} else {
						entry.getValue().setState(DeviceListData.STATUS_NO_ACTION);
					}
				}
				deviceList.repaint();
			}
			installApk();
			break;
		case STATUS_COMPLETED:
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
		AndroidDebugBridge.addDeviceChangeListener(this);
		setVisible(true);
		changeState(STATUS_APK_VERIFY);
	}

	private void next() {
		synchronized(this) {
			switch(status) {
			case STATUS_INIT:
				changeState(STATUS_APK_VERIFY);
				break;
			case STATUS_APK_VERIFY:
				changeState(STATUS_WAIT_FOR_DEVICE);
				break;
			case STATUS_WAIT_FOR_DEVICE:
				changeState(STATUS_SET_OPTIONS);
				break;
			case STATUS_SET_OPTIONS:
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

	private void installApk() {
		new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				Set<Entry<IDevice, DeviceListData>> entrySet = null;
				synchronized (deviceDataMap) {
					entrySet = deviceDataMap.entrySet();
				}

				for(Entry<IDevice, DeviceListData> entry: entrySet) {
					OptionsBundle bundle = entry.getValue().getOptionsBundle();
					if(!bundle.isInstallOptions() && !bundle.isPushOptions()) {
						 continue;
					}

					String errMsg = ApkInstaller.install(entry.getValue().getDevice(), apkInfo, bundle);
					if(errMsg == null || errMsg.isEmpty()) {
						entry.getValue().setState(DeviceListData.STATUS_SUCESSED);
					} else {
						Log.e(errMsg);
						entry.getValue().setState(DeviceListData.STATUS_FAILED);
					}
					publish(errMsg);
				}
				return null;
			}

			@Override
			protected void process(List<Object> chunks) {
				deviceList.repaint();
			}
			
			@Override
			protected void done() {
				next();
			}
		}.execute();
	}

	private void revaluationDeviceState(final IDevice device) {
		new SwingWorker<Boolean, Object>() {
			@Override
			protected Boolean doInBackground() throws Exception {

				AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
				if(adb == null) {
					Log.w("revaluationDeviceState() adb is null");
					return false;
				}

				IDevice[] devices = adb.getDevices();
				if(devices == null || devices.length == 0) {
					if(status == STATUS_SET_OPTIONS) {
						publish(STATUS_WAIT_FOR_DEVICE);
					}
				} else {
					if(device != null) {
						devices = new IDevice[] { device };
					}

					for(IDevice dev: devices) {
						synchronized (deviceDataMap) {
							DeviceListData data = deviceDataMap.get(dev);
							if(data == null) {
								final OptionsBundle bundle = optFactory.createOptions(dev);
								bundle.addOptionsChangedListener(new IOptionsChangedListener() {
									@Override
									public void changeOptions(int changedFlag, String... extraData) {
										switch(changedFlag) {
										case OptionsBundle.FLAG_OPT_DISSEMINATE:
											synchronized(deviceDataMap) {
												for(Entry<IDevice, DeviceListData> entry: deviceDataMap.entrySet()) {
													entry.getValue().getOptionsBundle().copyFrom(bundle);
												}
											}
										case OptionsBundle.FLAG_OPT_INSTALL:
										case OptionsBundle.FLAG_OPT_PUSH:
										case OptionsBundle.FLAG_OPT_NO_INSTALL:
											deviceList.repaint();
											break;
										default:
											break;
										}
										synchronized(deviceDataMap) {
											boolean posibleInstall = false;
											for(Entry<IDevice, DeviceListData> entry: deviceDataMap.entrySet()) {
												OptionsBundle bundle = entry.getValue().getOptionsBundle();
												if(bundle.isInstallOptions() || bundle.isPushOptions()) {
													posibleInstall = true;
													break;
												}
											}
											controlPanel.setNextButtonEnable(posibleInstall);
										}
									}
								});
								data = new DeviceListData(dev, bundle);
								//Log.v("deviceDataMap.put " + dev.getSerialNumber());
								deviceDataMap.put(dev, data);
								publish(data);
							}
						}
					}

					if(status == STATUS_WAIT_FOR_DEVICE) {
						publish(STATUS_SET_OPTIONS);
					}
				}
				return true;
			}

			@Override
			protected void process(List<Object> chunks) {
				for(Object data: chunks) {
					if(data instanceof Integer) {
						changeState((Integer)data);
					} else if(data instanceof DeviceListData) {
						synchronized (deviceDataMap) {
							if(deviceDataMap.containsValue(data)) {
								//Log.v("deviceListModel.addElement ");
								deviceListModel.addElement((DeviceListData)data);
							}
						}
					}
				}
			}

			@Override
			protected void done() {
				if(deviceListModel.getSize() == 0) {
					//Log.v("DONE() BUT LIST IS EMPTY");
					changeState(STATUS_WAIT_FOR_DEVICE);
				} else {
					if(deviceList.isSelectionEmpty()) {
						deviceList.setSelectedIndex(0);
					}
					installOptionPanel.setVisibleDisseminate(deviceListModel.getSize() > 1);
				}
			};
		}.execute();
	}

	@Override
	public void deviceChanged(IDevice device, int changeMask) {
		if((changeMask & IDevice.CHANGE_STATE) != 0) {
			Log.i("deviceChanged() " + device.getName());
			revaluationDeviceState(device);
		}
	}

	@Override
	public void deviceConnected(IDevice device) {
		Log.i("deviceConnected() " + device.getName() + ", isOnline " + device.isOnline());
		if(device.isOnline()) {
			revaluationDeviceState(device);
		}
	}

	@Override
	public void deviceDisconnected(IDevice device) {
		Log.i("deviceDisconnected() " + device.getName() + ", isOnline " + device.isOnline());
		synchronized(deviceDataMap) {
			Log.i("deviceDisconnected() 2 " + device.getName() + ", isOnline " + device.isOnline());
			final DeviceListData data = deviceDataMap.get(device);
			if(data != null) {
				try {
					EventQueue.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							deviceListModel.removeElement(data);
							if(deviceListModel.size() == 0) {
								changeState(STATUS_WAIT_FOR_DEVICE);
							} else {
								installOptionPanel.setVisibleDisseminate(deviceListModel.getSize() > 1);
							}
						}
					});
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}
				//Log.v("deviceDataMap.remove " + device.getSerialNumber());
				deviceDataMap.remove(device);
			}
		}
	}

	public class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener, ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getSource() instanceof DeviceCustomList) {
				DeviceCustomList list = (DeviceCustomList) e.getSource();
				if(list == null) return;

				DeviceListData data = list.getSelectedValue();
				if(data == null) return;

				installOptionPanel.setOptions(data.getOptionsBundle());
				contentPanel.show(ContentPanel.CONTENT_SET_OPTIONS);
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String actCommand = arg0.getActionCommand();
			if(ControlPanel.CTR_ACT_CMD_NEXT.equals(actCommand)) {
				next();
			} else if(ControlPanel.CTR_ACT_CMD_PREVIOUS.equals(actCommand)) {
				//previous();
			} else if(ControlPanel.CTR_ACT_CMD_CANCEL.equals(actCommand) ||
					ControlPanel.CTR_ACT_CMD_OK.equals(actCommand)) {
				if(wizard instanceof JFrame &&
						((JFrame)wizard).getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
					System.exit(0);
				} else {
					wizard.dispose();
				}
			} else if(ControlPanel.CTR_ACT_CMD_RESTART.equals(actCommand)) {
				changeState(STATUS_INIT);
			} else if(ToggleButtonBar.ACT_CMD_BUILD_OPTTIONS.equals(actCommand)) {
				if(arg0.getSource() instanceof DeviceListData) {
					DeviceListData data = (DeviceListData) arg0.getSource();
					installOptionPanel.setOptions(data.getOptionsBundle());
					contentPanel.show(ContentPanel.CONTENT_SET_OPTIONS);
				}
			} else if(ToggleButtonBar.ACT_CMD_PACKAGE_INFO.equals(actCommand)) {
				if(arg0.getSource() instanceof DeviceListData) {
					DeviceListData data = (DeviceListData) arg0.getSource();
					if(data != null && data.getDevice() != null && apkInfo != null && apkInfo.packageName != null) {
						PackageInfo info = PackageManager.getPackageInfo(data.getDevice(), apkInfo.packageName);
						if(info != null) {
							pacakgeInfoPanel.setPackageInfo(info);
							contentPanel.show(ContentPanel.CONTENT_PACKAGE_INFO);
						}
					} else {
						Log.v("no have device or apk package");
					}
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
						//next();
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
		public void windowClosing(WindowEvent e){
			Log.v("closing....ApkInstallWizard");
			AndroidDebugBridge.removeDeviceChangeListener(ApkInstallWizard.this);
		}

		@Override
		public void windowClosed(WindowEvent e) {
			Log.v("closed....ApkInstallWizard");
			AndroidDebugBridge.removeDeviceChangeListener(ApkInstallWizard.this);
		}

		@Override public void windowOpened(WindowEvent e) { }
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
