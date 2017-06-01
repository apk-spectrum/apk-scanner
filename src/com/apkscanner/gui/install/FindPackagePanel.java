package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.util.ArrayList;


import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.MainUI;

import com.apkscanner.gui.dialog.PackageInfoDlg;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.ApkInstallWizard.UIEventHandler;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;
import com.sun.jna.platform.win32.DBT.DEV_BROADCAST_DEVICEINTERFACE;

public class FindPackagePanel extends JPanel implements IDeviceChangeListener, ListSelectionListener, ActionListener{
	
	private static final String NO_DEVICE_LAYOUT = "NO_DEVICE_LAYOUT";
	private static final String DEVICE_LAYOUT = "DEVICE_LAYOUT";
	
	public static final String REQ_REFRESH_DETAIL_PANEL = "REQ_REFRESH_DETAIL_PANEL";
	
	private ActionListener mainlistener;
	private DeviceCustomList devicelist;
	private JPanel pacakgeinfopanel;	
	AndroidDebugBridge adb;
    public FindPackagePanel(ActionListener listener) {
    	AndroidDebugBridge.addDeviceChangeListener(this);
		this.setLayout(new CardLayout());
		mainlistener = listener;
		JPanel mainpanel = new JPanel(new BorderLayout());
		JLabel textSelectDevice = new JLabel("no device");
		textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
	    mainpanel.add(textSelectDevice,BorderLayout.NORTH);
		pacakgeinfopanel = new JPanel(new CardLayout());
		
        //pacakgeinfopanel = slider.getBasePanel();
		
        pacakgeinfopanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        
        
	    mainpanel.add(pacakgeinfopanel,BorderLayout.CENTER);
	    devicelist = new DeviceCustomList(this);
	    devicelist.addListSelectionListener(this);
	    devicelist.setBorder(new EtchedBorder(EtchedBorder.RAISED));
	    
	    mainpanel.add(devicelist, BorderLayout.WEST);

	    this.add(mainpanel, DEVICE_LAYOUT);
	    this.add(textSelectDevice, NO_DEVICE_LAYOUT);
	    
	    ((CardLayout)getLayout()).show(this, NO_DEVICE_LAYOUT);
	}

	private void setmargin(JPanel c, int size) {
    	c.setBorder(BorderFactory.createEmptyBorder(size, size, size, size));
    }
    
	private JButton getButton(String text, String actCmd, ActionListener listener) {
		JButton btn = new JButton(text);
		btn.setActionCommand(actCmd);		
		btn.addActionListener(listener);
		return btn;
	}
	
	
    private GridBagConstraints addGrid(GridBagConstraints gbc, 
            int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
      gbc.gridx = gridx;
      gbc.gridy = gridy;
      gbc.gridwidth = gridwidth;
      gbc.gridheight = gridheight;
      gbc.weightx = weightx;
      gbc.weighty = weighty;
      return gbc;
    }

    private Container getPackageInfopanel()
	{
        String packageName = ApkScanner.getPackageName(ApkInstallWizard.pakcageFilePath);
        
        adb = AdbServerMonitor.getAndroidDebugBridge();
        IDevice[] devices = adb.getDevices();
        
        Log.d(devices.length + "         " + ApkInstallWizard.pakcageFilePath);
        
        ArrayList<PackageInfo> packageList = new ArrayList<PackageInfo>();
        for(IDevice dev: devices) {
            PackageInfo info = PackageManager.getPackageInfo(dev, packageName);
            if(info != null) {
                packageList.add(info);
                PackageInfoDlg packageInfoDlg = new PackageInfoDlg(null);
    			packageInfoDlg.setPackageInfo(info);
    			//packageInfoDlg.setVisible(true);
    			return packageInfoDlg.getContentPane();
            }
        }
		return new JPanel();
	}

	@Override
	public void deviceChanged(IDevice arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.d("change device state : " + arg0.getSerialNumber() + " : " + arg0.getState());
		
		if(devicelist!=null) devicelist.deviceChanged(arg0, arg1);
		
	}

	@Override
	public void deviceConnected(IDevice arg0) {
		Log.d("connect device state : " + arg0.getSerialNumber() + " : " + arg0.getState());
		// TODO Auto-generated method stub
		if(devicelist!=null) devicelist.deviceConnected(arg0);
		
		((CardLayout)getLayout()).show(this, DEVICE_LAYOUT);
	}

	@Override
	public void deviceDisconnected(IDevice arg0) {
		// TODO Auto-generated method stub
		if(devicelist!=null) devicelist.deviceDisconnected(arg0);
		
		if(devicelist.getModel().getSize() == 0) {
			((CardLayout)getLayout()).show(this, NO_DEVICE_LAYOUT);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(e.getValueIsAdjusting()) {		
			refreshDetailPanel();
		}
	}
	
	public void refreshDetailPanel() {
		DeviceListData data = (DeviceListData)devicelist.getModel().getElementAt(devicelist.getSelectedIndex());
		
		pacakgeinfopanel.removeAll();
		
		if(data.showstate == DeviceListData.SHOW_INSTALL_DETAL) {
			
			pacakgeinfopanel.add(data.AppDetailpanel);
		} else if(data.showstate == DeviceListData.SHOW_INSTALL_OPTION) {
			pacakgeinfopanel.add(data.installoptionpanel);
			
			//slidePanelInFromRight(data.AppDetailpanel, data.installoptionpanel, pacakgeinfopanel);
			
		}		
		//pacakgeinfopanel.add(new JLabel("aa"));
		this.repaint();
		this.revalidate();		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(REQ_REFRESH_DETAIL_PANEL)) {
			refreshDetailPanel();			
		}
	}
	
}
