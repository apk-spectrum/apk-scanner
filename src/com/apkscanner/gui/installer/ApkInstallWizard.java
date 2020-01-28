package com.apkscanner.gui.installer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.WindowSizeMemorizer;
import com.apkspectrum.util.Log;

public class ApkInstallWizard implements IDeviceChangeListener
{
	public static final int STATUS_INIT = 0;
	public static final int STATUS_APK_VERIFY = 1;
	public static final int STATUS_WAIT_FOR_DEVICE = 2;
	public static final int STATUS_SIMPLE_OPTION = 3;
	public static final int STATUS_SET_OPTIONS = 4;
	public static final int STATUS_INSTALLING = 5;
	public static final int STATUS_COMPLETED = 6;

	public static final int STATUS_APK_VERTIFY_ERROR = 101;

	// UI components
	private Window wizard;
	private InstallProgressPanel progressPanel;
	private ContentPanel contentPanel;
	private ControlPanel controlPanel;
	private DeviceCustomList deviceList;
	private DefaultListModel<DeviceListData> deviceListModel;
	private UIEventHandler uiEventHandler = new UIEventHandler();

	private SimpleOptionPanel simpleOptionPanel;
	private InstallOptionPanel installOptionPanel;
	private PackageInfoPanel pacakgeInfoPanel;
	private JLabel errorMessageLable;

	private String packageFilePath;
	private CompactApkInfo apkInfo;
	private SignatureReport signatureReport;
	private DefaultOptionsFactory optFactory;
	private HashMap<IDevice, DeviceListData> deviceDataMap = new HashMap<IDevice, DeviceListData>();

	private int status;

	public class ApkInstallWizardDialog extends JDialog
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
			setTitle(RStr.TITLE_INSTALL_WIZARD.get());
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setResizable(true);
			setModal(false);
			addWindowListener(uiEventHandler);

			initialize(this, getRootPane());
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
				UIManager.setLookAndFeel(RProp.S.CURRENT_THEME.get());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}

			setTitle(RStr.TITLE_INSTALL_WIZARD.get());
			setResizable(true);

			initialize(this, getRootPane());
			setLocationRelativeTo(null);

			addWindowListener(uiEventHandler);
		}
	}

	public ApkInstallWizard() {
		packageFilePath = null;
		wizard = new ApkInstallWizardFrame();
	}

	public ApkInstallWizard(String filePath) {
		packageFilePath = filePath;
		wizard = new ApkInstallWizardFrame();
	}

	public ApkInstallWizard(String filePath, JFrame owner) {
		packageFilePath = filePath;
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else
			wizard = new ApkInstallWizardFrame(owner);
	}

	public ApkInstallWizard(String filePath, JDialog owner) {
		packageFilePath = filePath;
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else
			wizard = new ApkInstallWizardFrame(owner);
	}

	private void setVisible(boolean visible) {
		if(wizard != null) wizard.setVisible(visible);
	}

	private void initialize(Window window, JComponent compoent)
	{
		if(window == null) {
			Log.e("Error: window is null");
			return;
		}

		AdbServerMonitor.startServerAndCreateBridgeAsync();

		window.setIconImage(RImg.APP_ICON.getImage());

		WindowSizeMemorizer.apply(window, new Dimension(600, 450));

		progressPanel = new InstallProgressPanel();
		controlPanel = new ControlPanel(uiEventHandler);
		contentPanel = new ContentPanel(uiEventHandler);
		deviceList = new DeviceCustomList(uiEventHandler);
		deviceListModel = (DefaultListModel<DeviceListData>) deviceList.getModel();

		simpleOptionPanel = new SimpleOptionPanel(uiEventHandler);
		installOptionPanel = new InstallOptionPanel();
		pacakgeInfoPanel = new PackageInfoPanel(uiEventHandler);
		errorMessageLable = new JLabel("Please Check this APK file!", SwingConstants.CENTER);
		errorMessageLable.setFont(new Font("Serif", Font.PLAIN, 24));

		contentPanel.add(simpleOptionPanel, ContentPanel.CONTENT_SIMPLE_OPTIONS);
		contentPanel.add(installOptionPanel, ContentPanel.CONTENT_SET_OPTIONS);
		contentPanel.add(pacakgeInfoPanel, ContentPanel.CONTENT_PACKAGE_INFO);
		contentPanel.add(errorMessageLable, ContentPanel.CONTENT_VERIFY_ERROR);

		window.add(progressPanel, BorderLayout.NORTH);
		window.add(deviceList, BorderLayout.WEST);
		window.add(contentPanel, BorderLayout.CENTER);
		window.add(controlPanel, BorderLayout.SOUTH);

		// Shortcut key event processing
		KeyStrokeAction.registerKeyStrokeActions(compoent, JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK, false)
			}, uiEventHandler);
	}

	private void changeState(final int status) {
		Log.v("changeState() " + status);
		if(this.status == status) {
			Log.v("No action, because does not changed state.");
			return;
		}

		if(!EventQueue.isDispatchThread()) {
			Log.v("changeState() isDispatchThread " + EventQueue.isDispatchThread());
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					changeState(status);
				}
			});
			return;
		}

		this.status = status;

		progressPanel.setStatus(status);
		contentPanel.setStatus(status);
		controlPanel.setStatus(status);

		switch(status) {
		case STATUS_SIMPLE_OPTION:
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
			verifyApk();
			break;
		case STATUS_WAIT_FOR_DEVICE:
			revaluationDeviceState(null);
			break;
		case STATUS_SIMPLE_OPTION:
		case STATUS_SET_OPTIONS:
			break;
		case STATUS_INSTALLING:
			installApk();
			break;
		case STATUS_COMPLETED:
			break;
		default:
			break;
		}
	}

	public boolean start() {
		if(status != STATUS_INIT) {
			Log.w("No init state : " + status);
			return false;
		}

		if(packageFilePath == null || !(new File(packageFilePath).isFile())) {
			Log.e("No such apk file... : " + packageFilePath);
			MessageBoxPool.show(wizard != null ? wizard.getParent() : null, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return false;
		}

		AndroidDebugBridge.addDeviceChangeListener(this);
		setVisible(true);
		next();

		return true;
	}

	private void next() {
		changeState(status + 1);
	}

	private void previous() {
		changeState(status - 1);
	}

	private void verifyApk() {
		new SwingWorker<String, Void>() {
			protected String doInBackground() throws Exception {
				String apkFilePath = packageFilePath;

				ApkScanner scanner = ApkScanner.getInstance("AAPTLIGHT");
				scanner.openApk(apkFilePath);
				int errCode = scanner.getLastErrorCode();
				if(errCode != ApkScanner.NO_ERR) {
					return scanner.getLastErrorMessage();
				}
				apkInfo = new CompactApkInfo(scanner.getApkInfo());

				signatureReport = null;
				try {
					signatureReport = new SignatureReport(new File(apkFilePath));
				} catch (Exception e) { }
				if(signatureReport == null || signatureReport.getSize() == 0) {
					Log.e("Fail APK Virify");
					if(apkInfo.certificates == null || apkInfo.certificates.length == 0) {
						Log.e("certificates is null or 0");
						return "APK was not signed.";
					}
				}
				optFactory = new DefaultOptionsFactory(apkInfo, signatureReport);
				return null;
			}

			@Override
			protected void done() {
				String errMessage = null;
				try {
					errMessage = get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					errMessage = e.getMessage();
				}
				if(errMessage == null) {
					installOptionPanel.setApkInfo(apkInfo);
					next();
				} else {
					errorMessageLable.setText(errMessage);
					changeState(STATUS_APK_VERTIFY_ERROR);
				}
			}
		}.execute();
	}

	private void installApk() {
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

		final ArrayList<DeviceListData> remainderList = new ArrayList<DeviceListData>();
		synchronized (deviceDataMap) {
			for(DeviceListData data: deviceDataMap.values()) {
				final OptionsBundle bundle = data.getOptionsBundle();
				if(!bundle.isInstallOptions() && !bundle.isPushOptions()) {
					 continue;
				}
				remainderList.add(data);
			}
		}

		final DeviceListData[] targets = remainderList.toArray(new DeviceListData[remainderList.size()]);
		for(final DeviceListData data: targets) {
			new SwingWorker<DeviceListData, Void>() {
				@Override
				protected DeviceListData doInBackground() throws Exception {
					String errMsg = ApkInstaller.install(data.getDevice(), apkInfo, data.getOptionsBundle());
					if(errMsg == null || errMsg.isEmpty()) {
						data.setState(DeviceListData.STATUS_SUCESSED);
					} else {
						Log.e(errMsg);
						data.setState(DeviceListData.STATUS_FAILED);
						data.setErrorMessage(errMsg);
					}
					return data;
				}

				@Override
				protected void done() {
					DeviceListData data = null;
					try {
						data = get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
					if(data != null && data.equals(deviceList.getSelectedValue())) {
						switch(((DeviceListData) data).getState()) {
						case DeviceListData.STATUS_SUCESSED:
							contentPanel.show(ContentPanel.CONTENT_SUCCESSED);
							break;
						case DeviceListData.STATUS_FAILED:
							contentPanel.setErrorMessage(((DeviceListData) data).getErrorMessage());
							contentPanel.show(ContentPanel.CONTENT_FAILED);
							break;
						default: break;
						}
					}
					deviceList.repaint();
					synchronized (remainderList) {
						if(remainderList.contains(data)) {
							remainderList.remove(data);
						}
						if(remainderList.isEmpty()) {
							DeviceListData curData = deviceList.getSelectedValue();
							if(curData != null && curData.getState() == DeviceListData.STATUS_NO_ACTION){
								contentPanel.show(ContentPanel.CONTENT_NO_ACTION);
							}
							next();
						}
					}
				}
			}.execute();
		}
	}

	private void addDeviceToList(final IDevice device) {
		new SwingWorker<Boolean, Object>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				DeviceListData data = null;
				synchronized (deviceDataMap) {
					data = deviceDataMap.get(device);
					if(data == null) {
						OptionsBundle bundle = new OptionsBundle();
						data = new DeviceListData(device, bundle);
						data.setState(DeviceListData.STATUS_CONNECTING_DEVICE);
						deviceDataMap.put(device, data);
						publish(data);
					}
				}
				if(device.isOnline() && data.getState() == DeviceListData.STATUS_CONNECTING_DEVICE) {
					final OptionsBundle bundle = data.getOptionsBundle();
					optFactory.createOptions(device, bundle);
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
							case OptionsBundle.FLAG_OPT_NOT_INSTALL:
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
					data.setState(DeviceListData.STATUS_SETTING);
					publish(data);
				}
				simpleOptionPanel.setDeviceListData(deviceDataMap.values());
				return true;
			}

			@Override
			protected void process(List<Object> chunks) {
				for(Object data: chunks) {
					if(!(data instanceof DeviceListData)) {
						return;
					}
					if(!deviceListModel.contains(data)) {
						deviceListModel.addElement((DeviceListData)data);
					} else {
						deviceList.repaint();
					}
					if(deviceList.isSelectionEmpty()) {
						deviceList.setSelectedIndex(0);
					}
					if(data != null && data.equals(deviceList.getSelectedValue())) {
						installOptionPanel.setOptions(((DeviceListData) data).getOptionsBundle());
						if(((DeviceListData) data).getState() != DeviceListData.STATUS_CONNECTING_DEVICE) {
							contentPanel.show(status == STATUS_SIMPLE_OPTION ?
									ContentPanel.CONTENT_SIMPLE_OPTIONS : ContentPanel.CONTENT_SET_OPTIONS);
						} else {
							contentPanel.setLoadingMessage("Reading information of device...");
							contentPanel.show(ContentPanel.CONTENT_LOADING);
						}
					}
				}
			}

			@Override
			protected void done() {
				if(deviceListModel.getSize() == 0) {
					changeState(STATUS_WAIT_FOR_DEVICE);
				} else {
					if(deviceList.isSelectionEmpty()) {
						deviceList.setSelectedIndex(0);
					}
					installOptionPanel.setVisibleDisseminate(deviceListModel.getSize() > 1);

					synchronized(deviceDataMap) {
						boolean posibleInstall = false;
						for(Entry<IDevice, DeviceListData> entry: deviceDataMap.entrySet()) {
							if(entry.getValue().getState() != DeviceListData.STATUS_CONNECTING_DEVICE) {
								posibleInstall = true;
								break;
							}
						}
						controlPanel.setNextButtonEnable(posibleInstall);
					}
				}
			};
		}.execute();
	}

	private void revaluationDeviceState(final IDevice device) {
		final AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
		if(adb == null) {
			Log.w("revaluationDeviceState() adb is null");
			return;
		}

		IDevice[] devices = adb.getDevices();
		if(devices == null || devices.length == 0) {
			if(status == STATUS_SET_OPTIONS || status == STATUS_SIMPLE_OPTION) {
				changeState(STATUS_WAIT_FOR_DEVICE);
			}
			return;
		}

		if(status == STATUS_WAIT_FOR_DEVICE) {
			changeState(STATUS_SIMPLE_OPTION);
		}

		if(device != null) {
			devices = new IDevice[] { device };
		}

		controlPanel.setNextButtonEnable(false);
		for(final IDevice dev: devices) {
			addDeviceToList(dev);
		}
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
		Log.e("deviceConnected() " + device.getName() + ", isOnline " + device.isOnline() + ", state " + device.getState());
		//if(device.isOnline()) {
		revaluationDeviceState(device);
		//}
	}

	@Override
	public void deviceDisconnected(IDevice device) {
		Log.i("deviceDisconnected() " + device.getName() + ", isOnline " + device.isOnline());
		synchronized(deviceDataMap) {
			final DeviceListData data = deviceDataMap.get(device);
			if(data != null) {
				deviceDataMap.remove(device);
				simpleOptionPanel.setDeviceListData(deviceDataMap.values());

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						deviceListModel.removeElement(data);
						if(deviceListModel.size() <= 0) {
							changeState(STATUS_WAIT_FOR_DEVICE);
						} else {
							if(deviceList.isSelectionEmpty()) {
								deviceList.setSelectedIndex(0);
							}
							installOptionPanel.setVisibleDisseminate(deviceListModel.size() > 1);
						}
					}
				});
			}
		}
	}

	public class UIEventHandler implements ActionListener, WindowListener, ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getSource() instanceof DeviceCustomList) {
				DeviceCustomList list = (DeviceCustomList) e.getSource();
				if(list == null) return;

				DeviceListData data = list.getSelectedValue();
				if(data == null) return;

				switch(data.getState()) {
				case DeviceListData.STATUS_SETTING:
					installOptionPanel.setOptions(data.getOptionsBundle());
					contentPanel.show(status == STATUS_SIMPLE_OPTION ?
							ContentPanel.CONTENT_SIMPLE_OPTIONS : ContentPanel.CONTENT_SET_OPTIONS);
					break;
				case DeviceListData.STATUS_CONNECTING_DEVICE:
					contentPanel.show(ContentPanel.CONTENT_CONNECTING_DEVICE);
					break;
				case DeviceListData.STATUS_INSTALLING:
					contentPanel.show(ContentPanel.CONTENT_INSTALLING);
					break;
				case DeviceListData.STATUS_SUCESSED:
					contentPanel.show(ContentPanel.CONTENT_SUCCESSED);
					break;
				case DeviceListData.STATUS_FAILED:
					contentPanel.setErrorMessage(((DeviceListData) data).getErrorMessage());
					contentPanel.show(ContentPanel.CONTENT_FAILED);
					break;
				case DeviceListData.STATUS_NO_ACTION:
					contentPanel.show(ContentPanel.CONTENT_NO_ACTION);
				default:
					break;
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String actCmd = e.getActionCommand();
			if(actCmd == null || actCmd.isEmpty()) return;
			switch(actCmd) {
			case ControlPanel.CTR_ACT_CMD_NEXT:
				next();
				break;
			case ControlPanel.CTR_ACT_CMD_PREVIOUS:
				previous();
				break;
			case ControlPanel.CTR_ACT_CMD_CANCEL:
			case ControlPanel.CTR_ACT_CMD_OK:
				if(wizard instanceof JFrame &&
						((JFrame)wizard).getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
					System.exit(0);
				} else {
					wizard.dispose();
				}
				break;
			case ControlPanel.CTR_ACT_CMD_RESTART:
				changeState(STATUS_INIT);
				break;
			case PackageInfoPanel.ACT_CMD_BACK:
				contentPanel.show(status == STATUS_SIMPLE_OPTION ?
						ContentPanel.CONTENT_SIMPLE_OPTIONS : ContentPanel.CONTENT_SET_OPTIONS);
				break;
			case ToggleButtonBar.ACT_CMD_BUILD_OPTTIONS:
				if(e.getSource() instanceof DeviceListData) {
					DeviceListData data = (DeviceListData) e.getSource();
					installOptionPanel.setOptions(data.getOptionsBundle());
					if(data.getState() != DeviceListData.STATUS_CONNECTING_DEVICE) {
						contentPanel.show(status == STATUS_SIMPLE_OPTION ?
								ContentPanel.CONTENT_SIMPLE_OPTIONS : ContentPanel.CONTENT_SET_OPTIONS);
					} else {
						contentPanel.setLoadingMessage("Reading information of device...");
						contentPanel.show(ContentPanel.CONTENT_LOADING);
					}
				}
				break;
			case ToggleButtonBar.ACT_CMD_PACKAGE_INFO:
				if(e.getSource() instanceof DeviceListData) {
					DeviceListData data = (DeviceListData) e.getSource();
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
				break;
			case SimpleOptionPanel.ACT_CMD_SIMPLE_INSTALL:
			case SimpleOptionPanel.ACT_CMD_SIMPLE_PUSH:
				changeState(STATUS_INSTALLING);
				break;
			case SimpleOptionPanel.ACT_CMD_SET_ADVANCED_OPT:
				changeState(STATUS_SET_OPTIONS);
				break;
			case "alt pressed N":
				//next();
				break;
			case "alt pressed P":
				//previous();
				break;
			case "pressed F5":
				//contentPanel.refreshDeviceList();
				break;
			default:
				break;
			}
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
}
