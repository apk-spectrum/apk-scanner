package com.apkscanner.gui.install;


import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.install.DeviceTablePanel.DeviceDO;
import com.apkscanner.gui.util.ImagePanel;
import com.apkscanner.resource.Resource;


public class ContentPanel extends JPanel
{
	private static final long serialVersionUID = -680173960208954055L;

	public static final String CONTENT_INIT = "CONTENT_INIT";
	public static final String CONTENT_LOADING = "CONTENT_LOADING";
	public static final String CONTENT_DEVICE_SCANNING = "CONTENT_DEVICE_SCANNING";
	public static final String CONTENT_SELECT_DEVICE = "CONTENT_SELECT_DEVICE";
	public static final String CONTENT_PACKAGE_SCANNING = "CONTENT_PACKAGE_SCANNING";
	public static final String CONTENT_CHECK_PACKAGES = "CONTENT_CHECK_PACKAGES";
	public static final String CONTENT_SET_INSTALL_OPTION = "CONTENT_SET_INSTALL_OPTION";
	public static final String CONTENT_INSTALLING = "CONTENT_INSTALLING";
	public static final String CONTENT_COMPLETED = "CONTENT_COMPLETED";
	

	public static final String CTT_ACT_CMD_REFRESH = "CTT_ACT_CMD_REFRESH";
	public static final String CTT_ACT_CMD_SELECT_ALL = "CTR_ACT_CMD_SELECT_ALL";
	
	
	private JButton dev_refreshButton;
	private JButton dev_selectAllButton;
	
	private DeviceTablePanel panel_select_device;
	private FindPackagePanel panel_check_package;
	private JPanel panel_set_install_option;
	
	private JLabel loadingMessageLable;
	
	public void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
			double... percentages) {
			double total = 0;
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
			}
			
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);		        
			column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
			}
		}

	
	public ContentPanel(ActionListener listener) {
		super(new CardLayout());
		
		JPanel lodingPanel = new JPanel();
		loadingMessageLable = new JLabel("");
		lodingPanel.setLayout(new BoxLayout(lodingPanel, BoxLayout.Y_AXIS));
		lodingPanel.add(new ImagePanel(Resource.IMG_APK_LOGO.getImageIcon()));
		lodingPanel.add(loadingMessageLable);
		lodingPanel.add(new ImagePanel(Resource.IMG_WAIT_BAR.getImageIcon()));
		
		panel_select_device = new DeviceTablePanel(listener);				
				
		panel_check_package = new FindPackagePanel(listener);
		panel_set_install_option = new JPanel();
		
		add(new JPanel(), CONTENT_INIT);
		add(lodingPanel, CONTENT_LOADING);
		add(new JPanel(), CONTENT_DEVICE_SCANNING);
		add(panel_select_device, CONTENT_SELECT_DEVICE);
		add(new JPanel(), CONTENT_PACKAGE_SCANNING);
		add(panel_check_package, CONTENT_CHECK_PACKAGES);
		add(panel_set_install_option, CONTENT_SET_INSTALL_OPTION);
		add(new JPanel(), CONTENT_INSTALLING);
		add(new JPanel(), CONTENT_COMPLETED);
		
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
		case ApkInstallWizard.STATUS_WAIT_FOR_DEVICE:
			loadingMessageLable.setText("WAIT-FOR-DEVICE");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_DEVICE_SCANNING:
			loadingMessageLable.setText("SCANNING DEVICES");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_DEVICE_REFRESH:
		case ApkInstallWizard.STATUS_SELECT_DEVICE:
			((CardLayout)getLayout()).show(this, CONTENT_SELECT_DEVICE);
			break;
		case ApkInstallWizard.STATUS_PACKAGE_SCANNING:
			loadingMessageLable.setText("FIND PACKAGES");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:
			//pack_textPakcInfo.setText("");
			((CardLayout)getLayout()).show(this, CONTENT_CHECK_PACKAGES);			
			break;
		case ApkInstallWizard.STATUS_SET_INSTALL_OPTION:

			((CardLayout)getLayout()).show(this, CONTENT_SET_INSTALL_OPTION);
			break;
		case ApkInstallWizard.STATUS_INSTALLING:
			loadingMessageLable.setText("INSTALLING");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		case ApkInstallWizard.STATUS_COMPLETED:
			
			((CardLayout)getLayout()).show(this, CONTENT_COMPLETED);
			break;
		default:
			loadingMessageLable.setText("UNKNOWN STEP");
			((CardLayout)getLayout()).show(this, CONTENT_LOADING);
			break;
		}
	}
	public FindPackagePanel getFindPackagePanel() {
		return panel_check_package;
	}
	
	
	public String getSelectedLauncherActivity() {		
		return (String)panel_check_package.getActivityCombo().getSelectedItem();
		//return (String)pack_comboStartActivity.getSelectedItem();		
	}
	
	public void appendLog(String msg) {
		// append to log viewer
		
	}
	
	public List<DeviceDO> getDeviceList() {
		return panel_select_device.getDeviceList();
	}
}
