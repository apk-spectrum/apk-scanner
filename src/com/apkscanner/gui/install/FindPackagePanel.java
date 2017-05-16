package com.apkscanner.gui.install;

import java.awt.BorderLayout;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.MainUI;

import com.apkscanner.gui.dialog.PackageInfoDlg;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.ApkInstallWizard.UIEventHandler;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.DeviceMonitor;
import com.apkscanner.util.Log;
import com.sun.jna.platform.win32.DBT.DEV_BROADCAST_DEVICEINTERFACE;

public class FindPackagePanel extends JPanel{
	
	private ActionListener mainlistener;
	private DeviceCustomList devicelist;	
	AndroidDebugBridge adb;
    public FindPackagePanel(ActionListener listener) {
		this.setLayout(new GridBagLayout());		
		mainlistener = listener;
		
		JPanel mainpanel = new JPanel(new BorderLayout());
		JPanel Listpanel = new JPanel(new BorderLayout());
		JPanel packagepanel = new JPanel(new BorderLayout());
		JPanel appstartpanel = new JPanel(new BorderLayout());
		JPanel buttonpanel = new JPanel();
		
		JLabel textSelectDevice = new JLabel("installed same package!");
		textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
	    
	    packagepanel.add(getPackageInfopanel(), BorderLayout.CENTER);
	    packagepanel.add(appstartpanel, BorderLayout.SOUTH);		    
	    
	    Listpanel.add(packagepanel, BorderLayout.CENTER);
	    Listpanel.add(buttonpanel, BorderLayout.SOUTH);
	    
	    //mainpanel.add(textSelectDevice,BorderLayout.NORTH);
	    
	    devicelist = new DeviceCustomList();
	    
	    mainpanel.add(Listpanel,BorderLayout.CENTER);
	    mainpanel.add(devicelist, BorderLayout.WEST);
	    
	    GridBagConstraints gbc = new GridBagConstraints();            
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.anchor = GridBagConstraints.NORTH;
	    //this.add(textSelectDevice,addGrid(gbc, 0, 0, 1, 1, 1, 1));
	    gbc.fill = GridBagConstraints.BOTH;
	    this.add(mainpanel,addGrid(gbc, 0, 1, 1, 1, 1, 7));
	    this.add(new JPanel(),addGrid(gbc, 0, 2, 1, 1, 1, 3));
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
		
		return null;	
	}
}
