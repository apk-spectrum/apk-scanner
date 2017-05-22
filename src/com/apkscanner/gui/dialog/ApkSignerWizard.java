package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.core.signer.ApkSigner;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class ApkSignerWizard implements ActionListener {

	private static final String ACT_CMD_PEM_EXPLOERE = "ACT_CMD_PEM_EXPLOERE";
	private static final String ACT_CMD_PK8_EXPLOERE = "ACT_CMD_PK8_EXPLOERE";
	private static final String ACT_CMD_SIGN_APK = "ACT_CMD_SIGN_APK";
	private static final String ACT_CMD_CANCEL = "ACT_CMD_CANCEL";

	private Window wizard;
	private JTextField txtPemFilePath;
	private JTextField txtPk8FilePath;

	private String apkFilePath;

	public class ApkInstallWizardDialog  extends JDialog
	{
		private static final long serialVersionUID = 8992419627027530170L;

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
			setTitle(Resource.STR_TITLE_APK_SIGNER.getString());
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setResizable(true);
			setModal(false);

			initialize(this);
			setLocationRelativeTo(owner);
		}
	}

	public class ApkInstallWizardFrame extends JFrame
	{
		private static final long serialVersionUID = -6037824872351044585L;

		public ApkInstallWizardFrame() {
			frame_init();
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}

		public ApkInstallWizardFrame(JFrame owner) {
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

			setTitle(Resource.STR_TITLE_APK_SIGNER.getString());
			setResizable(true);

			initialize(this);
			setLocationRelativeTo(null);

			// Closing event of window be delete tempFile
			//addWindowListener(uiEventHandler);
		}
	}


	public ApkSignerWizard() {
		wizard = new ApkInstallWizardFrame();
	}

	public ApkSignerWizard(JFrame owner) {
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame(owner);
	}

	public ApkSignerWizard(JDialog owner) {
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame(owner);
	}

	private void initialize(Window window)
	{
		if(window == null) return;

		AdbServerMonitor.startServerAndCreateBridgeAsync();

		window.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		window.setSize(new Dimension(400,150));

		window.setLayout(new GridBagLayout());

		//GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) 
		GridBagConstraints gridHeadConst = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,5,5,5),0,0);
		GridBagConstraints gridDataConst = new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0);
		GridBagConstraints gridButtonConst = new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,5,5,5),0,0);

		window.add(new JLabel("Certificate(.pem)"), gridHeadConst);

		txtPemFilePath = new JTextField();
		txtPemFilePath.setEditable(false);
		txtPemFilePath.setText((String)Resource.PROP_PEM_FILE_PATH.getData());
		window.add(txtPemFilePath, gridDataConst);

		JButton btnExplorer = new JButton(Resource.STR_BTN_SELF_SEARCH.getString());
		btnExplorer.setToolTipText(Resource.STR_BTN_SELF_SEARCH_LAB.getString());
		btnExplorer.setActionCommand(ACT_CMD_PEM_EXPLOERE);
		btnExplorer.addActionListener(this);

		window.add(btnExplorer, gridButtonConst);

		gridHeadConst.gridy++;
		gridDataConst.gridy++;
		gridButtonConst.gridy++;

		window.add(new JLabel("Private key(.pk8)"), gridHeadConst);

		txtPk8FilePath = new JTextField();
		txtPk8FilePath.setEditable(false);
		txtPk8FilePath.setText((String)Resource.PROP_PK8_FILE_PATH.getData());
		window.add(txtPk8FilePath, gridDataConst);

		btnExplorer = new JButton(Resource.STR_BTN_SELF_SEARCH.getString());
		btnExplorer.setToolTipText(Resource.STR_BTN_SELF_SEARCH_LAB.getString());
		btnExplorer.setActionCommand(ACT_CMD_PK8_EXPLOERE);
		btnExplorer.addActionListener(this);
		window.add(btnExplorer, gridButtonConst);

		gridHeadConst.gridy++;
		gridDataConst.gridy++;
		gridButtonConst.gridy++;

		gridHeadConst.gridwidth = 3;
		gridHeadConst.anchor = GridBagConstraints.EAST;

		JPanel ctrBtnsPanel = new JPanel(new FlowLayout());


		JButton btnCancel = new JButton(Resource.STR_BTN_CANCEL.getString());
		btnCancel.setActionCommand(ACT_CMD_CANCEL);
		btnCancel.addActionListener(this);
		ctrBtnsPanel.add(btnCancel);

		JButton btnSign = new JButton(Resource.STR_BTN_SIGN.getString());
		btnSign.setActionCommand(ACT_CMD_SIGN_APK);
		btnSign.addActionListener(this);
		ctrBtnsPanel.add(btnSign);

		window.add(ctrBtnsPanel, gridHeadConst);

		//Log.i("initialize() register event handler");
		//window.addWindowListener(new UIEventHandler());

		//window.setMinimumSize(new Dimension(600, 450));

		// Shortcut key event processing
		//KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		//ky.addKeyEventDispatcher(uiEventHandler);
	}

	public void setVisible(boolean visible) {
		if(wizard != null) wizard.setVisible(visible);
	}

	public void setApk(String apkFilePath) {
		if(apkFilePath == null || !(new File(apkFilePath).isFile())) {
			Log.e("No such apk file... : " + apkFilePath);
			MessageBoxPool.show(MessageBoxPool.MSG_ERR_NO_SUCH_FILE);
			return;
		}
		this.apkFilePath = apkFilePath;
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				ApkSignerWizard wizard = new ApkSignerWizard();
				if(SystemUtil.isWindows()) {
					wizard.setApk("C:\\Melon.apk");
				} else {  //for linux
					wizard.setApk("/home/leejinhyeong/Desktop/reco.apk");
				}
				wizard.setVisible(true);
			}
		});
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
		String actCmd = arg0.getActionCommand();
		if(ACT_CMD_SIGN_APK.equals(actCmd)) {
			File pemFile = new File(txtPemFilePath.getText());
			if(!pemFile.isFile()) {
				return;
			}
			File pk8File = new File(txtPk8FilePath.getText());
			if(!pk8File.isFile()) {
				return;
			}

			String saveFileName = apkFilePath.replaceAll("\\.apk$", "_signed.apk");
			final File destFile = ApkFileChooser.saveApkFile(wizard, saveFileName);
			if(destFile == null) return;

			((JButton)arg0.getSource()).setEnabled(false);
			new SwingWorker<String, Void>(){
				@Override
				protected String doInBackground() throws Exception {

					return ApkSigner.signApk(pemFile.getAbsolutePath(), pk8File.getAbsolutePath(), apkFilePath, destFile.getAbsolutePath());
				}

				@Override
				protected void done() {
					String errMessage = null;
					try {
						errMessage = get();
					} catch (InterruptedException | ExecutionException e) {
						errMessage = e.getMessage();
						e.printStackTrace();
					}
					if(errMessage == null || errMessage.isEmpty()) {
						Resource.PROP_PEM_FILE_PATH.setData(pemFile.getAbsolutePath());
						Resource.PROP_PK8_FILE_PATH.setData(pk8File.getAbsolutePath());
						exitOrClose();
					} else {
						((JButton)arg0.getSource()).setEnabled(true);
						Log.e("Failure: cannot sign apk :\n" + errMessage);
					}
				}
			}.execute();
		} else if(ACT_CMD_CANCEL.equals(actCmd)) {
			exitOrClose();
		} else {
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setDialogType(JFileChooser.OPEN_DIALOG);
			if(ACT_CMD_PEM_EXPLOERE.equals(actCmd)) {
				jfc.setSelectedFile(new File(txtPemFilePath.getText()));
				jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Privacy Enhanced Mail Security Certificate(.pem)","pem"));
			} else if(ACT_CMD_PK8_EXPLOERE.equals(actCmd)) {
				jfc.setSelectedFile(new File(txtPk8FilePath.getText()));
				jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Private key(.pk8)","pk8"));
			}
			if(jfc.showOpenDialog(wizard) != JFileChooser.APPROVE_OPTION)
				return;
			File file = jfc.getSelectedFile();
			if(ACT_CMD_PEM_EXPLOERE.equals(actCmd)) {
				txtPemFilePath.setText(file.getAbsolutePath());
			} else if(ACT_CMD_PK8_EXPLOERE.equals(actCmd)) {
				txtPk8FilePath.setText(file.getAbsolutePath());
			}
		}
	}

	private void exitOrClose() {
		if(wizard instanceof JFrame &&
				((JFrame)wizard).getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
			System.exit(0);
		} else {
			wizard.dispose();
		}
	}
}
