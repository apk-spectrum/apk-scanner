package com.apkscanner.gui.install;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.util.ImagePanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class ContentPanel extends JPanel
{
	private static final long serialVersionUID = -680173960208954055L;

	public static final String CONTENT_INIT = "CONTENT_INIT";
	public static final String CONTENT_LOADING = "CONTENT_LOADING";
	public static final String CONTENT_VERIFY_ERROR = "CONTENT_VERIFY_ERROR";
	public static final String CONTENT_WAIT_FOR_DEVICE = "CONTENT_WAIT_FOR_DEVICE";
	public static final String CONTENT_SET_OPTIONS = "CONTENT_SET_OPTIONS";
	public static final String CONTENT_PACKAGE_INFO = "CONTENT_PACKAGE_INFO";
	public static final String CONTENT_INSTALLING = "CONTENT_INSTALLING";
	public static final String CONTENT_COMPLETED = "CONTENT_COMPLETED";

	private JLabel loadingMessageLable;

	private JPanel lodingPanel;

	public ContentPanel(ActionListener listener) {
		super(new CardLayout());

		lodingPanel = new JPanel();
		loadingMessageLable = new JLabel("");
		loadingMessageLable.setAlignmentX(0.5f);
		lodingPanel.setLayout(new BoxLayout(lodingPanel, BoxLayout.Y_AXIS));
		lodingPanel.add(new ImagePanel(Resource.IMG_APK_LOGO.getImageIcon(340,220)));
		lodingPanel.add(loadingMessageLable);
		lodingPanel.add(new ImagePanel(Resource.IMG_WAIT_BAR.getImageIcon()));

		JLabel txtWaitForDevice = new JLabel("Wait for device!", SwingConstants.CENTER);
		txtWaitForDevice.setFont(new Font(txtWaitForDevice.getFont().getName(), Font.PLAIN, 30));

		add(new JPanel(), CONTENT_INIT);
		add(lodingPanel, CONTENT_LOADING);
		add(txtWaitForDevice, CONTENT_WAIT_FOR_DEVICE);
		add(new JPanel(), CONTENT_SET_OPTIONS);
		add(new JPanel(), CONTENT_INSTALLING);
		add(new JPanel(), CONTENT_COMPLETED);

		this.setBorder(new EmptyBorder(10,10,10,10));

		// set status
		setStatus(ApkInstallWizard.STATUS_INIT);
	}

	public void setStatus(int status) {
		switch(status) {
		case ApkInstallWizard.STATUS_INIT:
			loadingMessageLable.setText("INIT");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_APK_VERIFY:
			loadingMessageLable.setText("VERIFY APK");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_WAIT_FOR_DEVICE:
			((CardLayout)getLayout()).show(this, CONTENT_WAIT_FOR_DEVICE);
			break;
		case ApkInstallWizard.STATUS_SET_OPTIONS:
			((CardLayout)getLayout()).show(this, CONTENT_SET_OPTIONS);
			break;
		case ApkInstallWizard.STATUS_INSTALLING:
			loadingMessageLable.setText("INSTALLING");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_COMPLETED:
			((CardLayout)getLayout()).show(this, CONTENT_COMPLETED);
			break;
		case ApkInstallWizard.STATUS_APK_VERTIFY_ERROR:
			((CardLayout)getLayout()).show(this, CONTENT_VERIFY_ERROR);
			break;
		default:
			Log.w("Unknown status : " + status);
			//loadingMessageLable.setText("UNKNOWN STEP : " + status);
			//((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		}
	}

	public void setLoadingMessage(String message) {
		loadingMessageLable.setText(message != null ? message : "");
	}

	public void show(String name) {
		((CardLayout)getLayout()).show(this, name);
	}
}
