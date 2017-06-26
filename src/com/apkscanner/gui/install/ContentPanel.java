package com.apkscanner.gui.install;


import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.gui.util.ImagePanel;
import com.apkscanner.resource.Resource;


public class ContentPanel extends JPanel
{
	private static final long serialVersionUID = -680173960208954055L;

	public static final String CONTENT_INIT = "CONTENT_INIT";
	public static final String CONTENT_LOADING = "CONTENT_LOADING";
	public static final String CONTENT_VERIFY_ERROR = "CONTENT_VERIFY_ERROR";
	
	public static final String CONTENT_PACKAGE_SCANNING = "CONTENT_PACKAGE_SCANNING";
	public static final String CONTENT_CHECK_PACKAGES = "CONTENT_CHECK_PACKAGES";
	public static final String CONTENT_INSTALLING = "CONTENT_INSTALLING";
	public static final String CONTENT_COMPLETED = "CONTENT_COMPLETED";
	
	

	public static final String CTT_ACT_CMD_REFRESH = "CTT_ACT_CMD_REFRESH";
	public static final String CTT_ACT_CMD_SELECT_ALL = "CTR_ACT_CMD_SELECT_ALL";

	//private DeviceTablePanel panel_select_device;
	private FindPackagePanel panel_check_package;
	//private JPanel panel_set_install_option;
	
	private JLabel loadingMessageLable;
	private JLabel ErrorMessageLable;
	
	private JPanel lodingPanel;
	private String loadingtext;
	
	public ContentPanel(ActionListener listener) {
		super(new CardLayout());
		
		lodingPanel = new JPanel();
		loadingMessageLable = new JLabel("");
		lodingPanel.setLayout(new BoxLayout(lodingPanel, BoxLayout.Y_AXIS));
		lodingPanel.add(new ImagePanel(Resource.IMG_APK_LOGO.getImageIcon(340,220)));
		lodingPanel.add(loadingMessageLable);
		lodingPanel.add(new ImagePanel(Resource.IMG_WAIT_BAR.getImageIcon()));
		
		//panel_select_device = new DeviceTablePanel(listener);				
				
		panel_check_package = new FindPackagePanel(listener);
		//panel_set_install_option = new JPanel();
		ErrorMessageLable = new JLabel("Please Check this APK file!", SwingConstants.CENTER);
		ErrorMessageLable.setFont(new Font("Serif", Font.PLAIN, 30)); 
		
		add(new JPanel(), CONTENT_INIT);
		add(lodingPanel, CONTENT_LOADING);		
		add(new JPanel(), CONTENT_PACKAGE_SCANNING);
		add(ErrorMessageLable, CONTENT_VERIFY_ERROR);
		
		add(panel_check_package, CONTENT_CHECK_PACKAGES);
		add(new JPanel(), CONTENT_INSTALLING);
		add(new JLabel("result"), CONTENT_COMPLETED);
		
		this.setBorder(new EmptyBorder(10,10,10,10));
		
		
		//init_Panel_set_install_option();
		// set status
		setStatus(ApkInstallWizard.STATUS_INIT);
	}
	
	public void setStatus(int status) {
		switch(status) {
		case ApkInstallWizard.STATUS_INIT:
			loadingMessageLable.setText("INIT");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
			
		case ApkInstallWizard.STATUS_PACKAGE_SCANNING:
			loadingMessageLable.setText("VERIFY APK");
			loadingtext = "VERIFY APK";
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
			
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:
			//pack_textPakcInfo.setText("");
			panel_check_package.setStatus(ApkInstallWizard.STATUS_CHECK_PACKAGES);
			((CardLayout)getLayout()).show(this, CONTENT_CHECK_PACKAGES);
			panel_check_package.refreshDeviceInfo();
			break;
		case ApkInstallWizard.STATUS_INSTALLING:
			//loadingMessageLable.setText("INSTALLING");
			//loadingtext = "INSTALLING";
			//((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			//lodingPanel.revalidate();
			panel_check_package.setStatus(ApkInstallWizard.STATUS_INSTALLING);
			
			break;
		case ApkInstallWizard.STATUS_COMPLETED:			
			//((CardLayout)getLayout()).show(this, CONTENT_COMPLETED);
			panel_check_package.setStatus(ApkInstallWizard.STATUS_COMPLETED);
						
			break;
		case ApkInstallWizard.STATUS_APK_VERTIFY_ERROR:
			((CardLayout)getLayout()).show(this, CONTENT_VERIFY_ERROR);
			break;

		case ApkInstallWizard.STATUS_DESTROY_DIALOG:
			panel_check_package.destroy();
			break;			
		default:
			loadingMessageLable.setText("UNKNOWN STEP");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		}
	}
	public void setLoadingTextStep(int current, int all) {
		loadingMessageLable.setText(loadingtext + "(" + current + "/" + all + ")");		
	}
	public void appendLog(String msg) {
		// append to log viewer
		
	}
	
	public ListModel<DeviceListData> getDeviceListData() {
		return panel_check_package.getListModelData();
	}
}
