package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.PackageInfoDlg;
import com.apkscanner.gui.install.DeviceTablePanel.DeviceDO;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;

public class DeviceCustomList extends JList{

	DefaultListModel listmodel;
    public DeviceCustomList() {
		// TODO Auto-generated constructor stub
    	setLayout(new BorderLayout());    	
        listmodel = new DefaultListModel ();
		//AndroidDebugBridge.init(true);
        IDevice[] devices = AdbServerMonitor.getAndroidDebugBridge().getDevices();        
        Log.d(devices.length + "         " + ApkInstallWizard.pakcageFilePath);        
        //AndroidDebugBridge.addDeviceChangeListener(this);
        
        this.setModel(listmodel);
        
        this.setCellRenderer ( new Listrenderer ( ) );
        this.setBorder ( BorderFactory.createEmptyBorder ( 5, 5, 5, 5 ) );
	}

    private void setModeldata(DefaultListModel listmodel, IDevice device) {
    	for(int i=0; i < listmodel.size(); i++) {
    		DeviceListData temp = (DeviceListData) listmodel.getElementAt(i);
    		if(temp.serialnumber.equals(device.getSerialNumber())) {
    			temp.status = device.getState().toString();
    			setDeviceProperty(device, temp, IDevice.PROP_DEVICE_MODEL);
    			setDeviceProperty(device,temp,IDevice.PROP_BUILD_API_LEVEL);
    			this.repaint();
    			return;
    		}
    	}
    	
		DeviceListData data = new DeviceListData();
		data.circleColor = new Color( 209, 52, 23 );
		data.serialnumber = device.getSerialNumber();
		//data.SDKVersion = device.getProperty(IDevice.PROP_BUILD_VERSION_NUMBER);
		//data.name = device.getName();
		setDeviceProperty(device,data,IDevice.PROP_DEVICE_MODEL);
		setDeviceProperty(device,data,IDevice.PROP_BUILD_API_LEVEL);
		
		data.status = device.getState().toString();
		
		//data.AppDetailpanel = getPackageInfopanel(device);
		
		data.AppDetailpanel = new JPanel();
		
		listmodel.addElement (data);
		
		if(listmodel.size() ==1) {
			this.setSelectedIndex(0);
		}
		
    }
    
    private Container getPackageInfopanel(IDevice dev)
	{
        String packageName = ApkScanner.getPackageName(ApkInstallWizard.pakcageFilePath);
        
        PackageInfo info = PackageManager.getPackageInfo(dev, packageName);
        if(info != null) {
            PackageInfoDlg packageInfoDlg = new PackageInfoDlg(null);
			packageInfoDlg.setPackageInfo(info);
			//packageInfoDlg.setVisible(true);
			return packageInfoDlg.getContentPane();
        }
        
		return new JPanel();
	}
    
	public void setDeviceProperty(IDevice device, final DeviceListData DO, final String propertyname) {
		try {
		final String DeviceName = null;
		if("ONLINE".equals(device.getState().toString())) {
				device.executeShellCommand("getprop "+propertyname, new MultiLineReceiver() {
					String temp;
					    @Override
					    public void processNewLines(String[] lines) {					        
					        	if(lines[0].length() >0) {
					        		if(propertyname.indexOf(IDevice.PROP_DEVICE_MODEL) > -1) {
					        			DO.name = lines[0];					        		
					        		} else if(propertyname.indexOf(IDevice.PROP_BUILD_API_LEVEL) > -1) {
					        			DO.SDKVersion = lines[0];
					        		}
					        		return ;
					        	}
					    }
					    @Override
					    public boolean isCancelled() {
					        return false;
					    }
					});
				
			} else {				
				DO.name = device.getName();
			}
			return;
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		return;
	}
    
	private class Listrenderer extends DefaultListCellRenderer {
    	private CustomLabel renderer;
        @Override
        public Component getListCellRendererComponent ( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            renderer.setSelected ( isSelected );
            renderer.setData ( ( DeviceListData ) value );
            return renderer;
        }
        
        private Listrenderer () {
            super ();
            renderer = new CustomLabel ();
        }
    }
    
    
    public class DeviceListData
    {
        public Color circleColor;
        public String status;
        public String name;
        public String serialnumber;
        public String SDKVersion;
        public Container AppDetailpanel;
        public DeviceListData ( Color circleColor, String status, String name, String sdkVersion, String serialnumber )
        {
            super ();
            this.circleColor = circleColor;
            this.status = status;
            this.name = name;
            this.SDKVersion = sdkVersion;
        }
        
        public DeviceListData ()
        {

        }

        private Color getCircleColor () {
            return circleColor;
        }

        private String getStatus() {
            return status;
        }

        private String getName () {
            return name;
        }
        
        private String getSDKVersion() {
        	return SDKVersion;
        }
        
    }
    
    private static class CustomLabel extends JLabel
    {
        private static final Color selectionColor = new Color ( 82, 158, 202 );

        private boolean selected;
        private DeviceListData data;

        public CustomLabel ()
        {
            super ();
            setOpaque ( false );
            setBorder ( BorderFactory.createEmptyBorder ( 0, 60, 0, 40 ) );
            setFont(new Font(getFont().getName(), Font.BOLD, 9));
        }

        private void setSelected ( boolean selected )
        {
            this.selected = selected;
            setForeground ( selected ? Color.WHITE : Color.BLACK );
        }

        private void setData ( DeviceListData data )
        {
            this.data = data;
            setText ( data.getName () );
        }

        private void centerString(Graphics g, Rectangle r, String s, 
                Font font) {
            FontRenderContext frc = 
                    new FontRenderContext(null, true, true);

            if(s==null) {
            	s = "";
            }
            
            Rectangle2D r2D = font.getStringBounds(s, frc);
            int rWidth = (int) Math.round(r2D.getWidth());
            int rHeight = (int) Math.round(r2D.getHeight());
            int rX = (int) Math.round(r2D.getX());
            int rY = (int) Math.round(r2D.getY());

            int a = (r.width / 2) - (rWidth / 2) - rX;
            int b = (r.height / 2) - (rHeight / 2) - rY;

            g.setFont(font);
            g.drawString(s, r.x + a, r.y + b);
        }
        
        @Override
        protected void paintComponent ( Graphics g )
        {
            Graphics2D g2d = ( Graphics2D ) g;
            g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

            if ( selected )
            {
                Area area = new Area ( new Ellipse2D.Double ( 0, 0, 52, 52 ) );
                area.add ( new Area ( new RoundRectangle2D.Double ( 36, 6, getWidth () - 36, 40, 12, 12 ) ) );
                g2d.setPaint ( selectionColor );
                g2d.fill ( area );

                g2d.setPaint ( Color.WHITE );
                g2d.fill ( new Ellipse2D.Double ( 4, 4, 44, 44 ) );
            }
            
            g2d.setPaint ( data.getCircleColor () );
            g2d.fill ( new Ellipse2D.Double ( 6, 6, 40, 40 ) );
            
            g2d.setPaint ( Color.WHITE );
            //g2d.drawString("N", 22, 22);
            
            centerString(g2d,new Rectangle(4, 4, 44, 44), data.getSDKVersion(), new Font(getFont().getName(), Font.BOLD, 20));
        	
            if(data.status.indexOf("ONLINE") > -1) {
            	g2d.setPaint ( new Color(116, 211, 109) ); // online color            
            } else if(data.status.indexOf("OFFLINE") > -1) {
            	g2d.setPaint(Color.GRAY);
            } else {
            	g2d.setPaint(Color.ORANGE);            	
            }
            g2d.fill ( new Ellipse2D.Double ( getWidth () - 18 - 10, getHeight () / 2 - 9, 18, 18 ) );
            
            g2d.setPaint ( Color.LIGHT_GRAY );
            g.drawLine(2, getHeight()-2, getWidth()-2, getHeight()-2 );
            
            g.setFont(new Font(getFont().getName(), Font.BOLD, 15));
            super.paintComponent ( g );
        }

        @Override
        public Dimension getPreferredSize ()
        {
            final Dimension ps = super.getPreferredSize ();
            ps.height = 54;
            ps.width = 200;
            return ps;
        }
    }
	public void deviceChanged(IDevice arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.d("change device state : " + arg0.getSerialNumber() + " : " + arg0.getState());
		setModeldata(listmodel, arg0);
	}

	public void deviceConnected(IDevice arg0) {
		// TODO Auto-generated method stub
		Log.d("deviceConnected device state : " + arg0.getSerialNumber() + " : " + arg0.getState());
		setModeldata(listmodel, arg0);
	}

	public void deviceDisconnected(IDevice arg0) {
		// TODO Auto-generated method stub
		Log.d("deviceDisconnected device state : " + arg0.getSerialNumber() + " : " + arg0.getState());
    	for(int i=0; i < listmodel.size(); i++) {
    		DeviceListData temp = (DeviceListData) listmodel.getElementAt(i);
    		if(temp.serialnumber.equals(arg0.getSerialNumber())) {
    			listmodel.remove(i);    			
    			return;
    			//setDeviceProperty(device, temp, IDevice.PROP_DEVICE_MODEL);
    		}
    	}
		
	}
}
