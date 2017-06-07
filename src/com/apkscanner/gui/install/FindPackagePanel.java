package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.util.Log;


public class FindPackagePanel extends JPanel implements IDeviceChangeListener, ListSelectionListener, ActionListener{
	
	private static final long serialVersionUID = 3234890834569931496L;
	
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
		JLabel textSelectDevice = new JLabel("connect device........... and wait");
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

/*
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
*/
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
		mainlistener.actionPerformed(new ActionEvent(this, 0, DEVICE_LAYOUT));
	}

	@Override
	public void deviceDisconnected(IDevice arg0) {
		// TODO Auto-generated method stub
		if(devicelist!=null) devicelist.deviceDisconnected(arg0);
		
		if(devicelist.getModel().getSize() == 0) {
			((CardLayout)getLayout()).show(this, NO_DEVICE_LAYOUT);
			mainlistener.actionPerformed(new ActionEvent(this, 0, NO_DEVICE_LAYOUT));
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
		
		if(data.showstate == DeviceListData.SHOW_INSTALL_DETAL || data.pacakgeLoadingstatus == DeviceListData.WAITING) {
			
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
