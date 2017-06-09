package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.gui.util.ImagePanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.util.Log;


public class FindPackagePanel extends JPanel implements IDeviceChangeListener, ListSelectionListener, ActionListener{
	
	private static final long serialVersionUID = 3234890834569931496L;
	
	private static final String NO_DEVICE_LAYOUT = "NO_DEVICE_LAYOUT";
	private static final String DEVICE_LAYOUT = "DEVICE_LAYOUT";
	
	public static final String REQ_REFRESH_DETAIL_PANEL = "REQ_REFRESH_DETAIL_PANEL";
	public static final String REQ_FINISHED_INSTALL = "REQ_FINISHED_INSTALL";
	
	
	private ActionListener mainlistener;
	private DeviceCustomList devicelist;
	private JPanel pacakgeinfopanel;
	private JPanel lodingPanel;
	private int status;
	AndroidDebugBridge adb;
    public FindPackagePanel(ActionListener listener) {
		this.setLayout(new CardLayout());
		AndroidDebugBridge.addDeviceChangeListener(this);
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
	    
		lodingPanel = new JPanel();
		JLabel loadingMessageLable = new JLabel("INSTALLING");
		lodingPanel.setLayout(new BoxLayout(lodingPanel, BoxLayout.Y_AXIS));
		lodingPanel.add(new ImagePanel(Resource.IMG_APK_LOGO.getImageIcon(340,220)));
		lodingPanel.add(loadingMessageLable);
		lodingPanel.add(new ImagePanel(Resource.IMG_WAIT_BAR.getImageIcon()));
	    
	    //refreshDeviceInfo();
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
		//if(devicelist!=null) devicelist.deviceConnected(arg0);
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
	public void refreshDeviceInfo() {
	    int count = 0;
        while (AdbServerMonitor.getAndroidDebugBridge().getBridge() == null) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException e) {
                // pass
            }
            
            // let's not wait > 10 sec.
            if (count > 100) {
                Log.d("Timeout getting device list!");
            }
        }
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	    IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
	    
	    if(devices.length >0) {
	    	Log.d("device is " + devices.length);
			((CardLayout)getLayout()).show(this, DEVICE_LAYOUT);
			mainlistener.actionPerformed(new ActionEvent(this, 0, DEVICE_LAYOUT));
	    }else {
	    	Log.d("device is 0");
	    }
	    
	    for(IDevice device: devices) {
	    	devicelist.deviceConnected(device);
	    }	    
	}
	public void refreshDetailPanel() {
		DeviceListData data = (DeviceListData)devicelist.getModel().getElementAt(devicelist.getSelectedIndex());
		
		pacakgeinfopanel.removeAll();
		//pacakgeinfopanel.add(lodingPanel);
		switch(status) {
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:		
			if(data.showstate == DeviceListData.SHOW_INSTALL_DETAL) {
				pacakgeinfopanel.add(data.AppDetailpanel);
			} else if(data.showstate == DeviceListData.SHOW_INSTALL_OPTION || data.pacakgeLoadingstatus == DeviceListData.WAITING) {
				pacakgeinfopanel.add(data.installoptionpanel);
			}
			break;
		case ApkInstallWizard.STATUS_INSTALLING:			
			if(data.showstate == DeviceListData.SHOW_LOADING_INSTALL) {				
				pacakgeinfopanel.add(lodingPanel);
			} else if(data.showstate == DeviceListData.SHOW_COMPLETE_INSTALL) {
				pacakgeinfopanel.add(new JLabel(data.installErrorCuase));
			}
			break;
		}
		
		this.repaint();
		this.revalidate();		
	}

	public void setStatus(int status) {
		this.status = status;
		devicelist.setStatus(status);
		switch(status) {
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:
			
			break;
		case ApkInstallWizard.STATUS_INSTALLING:
			
			refreshDetailPanel();
			break;
		case ApkInstallWizard.STATUS_COMPLETED:
			
			break;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(REQ_REFRESH_DETAIL_PANEL)) {
			refreshDetailPanel();
		} else if(e.getActionCommand().equals(REQ_FINISHED_INSTALL)) {
			mainlistener.actionPerformed(new ActionEvent(this, 0, ControlPanel.CTR_ACT_CMD_NEXT));
		}
	}
	@SuppressWarnings("unchecked")
	public ListModel<DeviceListData> getListModelData() {
		return devicelist.getModel();
	}
	public void destroy() {
		AndroidDebugBridge.removeDeviceChangeListener(this);		
	}
	
}
