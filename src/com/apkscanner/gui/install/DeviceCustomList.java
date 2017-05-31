package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApktoolScanner;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.PackageInfoDlg;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.install.DeviceTablePanel.DeviceDO;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class DeviceCustomList extends JList{

	DefaultListModel listmodel;
	private XmlPath sdkXmlPath;
	Listrenderer listrenderer;
	ActionListener FindPackagelistener;
    public DeviceCustomList(ActionListener listener) {
		// TODO Auto-generated constructor stub
    	setLayout(new BorderLayout());    	
        listmodel = new DefaultListModel ();
		//AndroidDebugBridge.init(true);
        //IDevice[] devices = AdbServerMonitor.getAndroidDebugBridge().getDevices();        
        //Log.d(devices.length + "         " + ApkInstallWizard.pakcageFilePath);        
        //AndroidDebugBridge.addDeviceChangeListener(this);
        
        setPreferredSize(new Dimension(200, 0));
        
        listrenderer = new Listrenderer(this);
        FindPackagelistener = listener;
        
        this.setModel(listmodel);
        this.setCellRenderer ( listrenderer);
        
        
        this.setBorder ( BorderFactory.createEmptyBorder ( 5, 5, 5, 5 ) );
        
        
	}

    private int hashCode(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            //hash = str.charAt(i)^10 + ((hash << 4) - hash);
        	hash = str.charAt(i) + ((hash << 10) * hash);
        }
        return hash;
    }
    
    private String intToARGB(int i) {
        String hex = ""+ Integer.toHexString((i>>24)&0xFF) + Integer.toHexString((i>>16)&0xFF) +
        		Integer.toHexString((i>>8)&0xFF) + Integer.toHexString(i&0xFF);
        // Sometimes the string returned will be too short so we 
        // add zeros to pad it out, which later get removed if
        // the length is greater than six.
        //hex += "000000";
        return hex.substring(0, 6);
    }
    
    private void setModeldata(DefaultListModel listmodel, final IDevice device) {
    	for(int i=0; i < listmodel.size(); i++) {
    		DeviceListData temp = (DeviceListData) listmodel.getElementAt(i);
    		if(temp.serialnumber.equals(device.getSerialNumber())) {
    			setDeviceProperty(device, temp, IDevice.PROP_DEVICE_MODEL);
    			setDeviceProperty(device,temp,IDevice.PROP_BUILD_VERSION);    			
    			temp.status = temp.SDKVersion == null ? "OFFLINE" : device.getState().toString();    			
    			temp.AppDetailpanel = getPackageInfopanel(device);
    			
    			Log.d(temp.name + "#"+intToARGB(hashCode(temp.name)));
    			temp.circleColor = Color.decode("#"+intToARGB(hashCode(temp.name)));
    			
    			
    			this.repaint();
    			return;
    		}
    	}
    	
		final DeviceListData data = new DeviceListData();
		//data.circleColor = new Color( 209, 52, 23 );
		data.serialnumber = device.getSerialNumber();
		//data.SDKVersion = device.getProperty(IDevice.PROP_BUILD_VERSION_NUMBER);
		//data.name = device.getName();
		setDeviceProperty(device,data,IDevice.PROP_DEVICE_MODEL);
		setDeviceProperty(device,data,IDevice.PROP_BUILD_VERSION);
		
		//Log.d(data.name + "#"+intToARGB(hashCode(data.name)));
		data.circleColor = Color.decode("#"+intToARGB(hashCode(data.name)));
		
		
		//data.circleColor = ;
		
		data.status = data.SDKVersion == null ? "OFFLINE" : device.getState().toString();
		data.installoptionpanel = new InstallOptionPanel();
		data.showstate = DeviceListData.SHOW_INSTALL_DETAL;
		
		data.AppDetailpanel = new JLabel(Resource.IMG_LOADING.getImageIcon());
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				data.AppDetailpanel = getPackageInfopanel(device);
				//data.AppDetailpanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
				
				fireSelectionValueChanged(0, 0, true);
			}			
		}).start();
		
		listmodel.addElement (data);
		
		if(listmodel.size() ==1) {
			setSelectedIndex(0);
			fireSelectionValueChanged(0, 0, true);
		}
		this.repaint();
    }
    
    private JComponent getPackageInfopanel(IDevice dev)
	{
    	
        String packageName = ApkScanner.getPackageName(ApkInstallWizard.pakcageFilePath);
        PackageInfo info = PackageManager.getPackageInfo(dev, packageName);
                
        if(info != null) {
        	PackageInfoPanel packageInfoPanel = new PackageInfoPanel();
        	packageInfoPanel.setPackageInfo(info);
			//packageInfoDlg.setVisible(true);
			return packageInfoPanel;
        }
        
		return new JLabel("not installed");
        //return null;
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
					        		} else if(propertyname.indexOf(IDevice.PROP_BUILD_VERSION) > -1) {
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
    	private CustomListPanel renderer;
    	private JList list;
    	private MouseAdapter adapter;
    	public JRadioButton button;
        public int pressedIndex  = -1;
        public int rolloverIndex = -1;
        
        @Override
        public Component getListCellRendererComponent ( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            renderer.setSelected ( isSelected );
            renderer.setData ( ( DeviceListData ) value );

            if (Objects.nonNull(button)) {
                if (index == pressedIndex) {
                    button.getModel().setSelected(true);
                    button.getModel().setArmed(true);
                    button.getModel().setPressed(true);
                } else if (index == rolloverIndex) {
                    button.getModel().setRollover(true);
                }
            }
            
            return renderer;
        }
        
        private Listrenderer (final JList list) {
            super ();
            this.list = list;
            renderer = new CustomListPanel ();
            
            list.addMouseListener ( adapter = new MouseAdapter () {
                @Override
                public void mouseReleased ( MouseEvent e )
                {
                	
                    if ( SwingUtilities.isLeftMouseButton ( e ) )
                    {
                    	Component child = getComponentinList(e);
                        
                        if(child instanceof JRadioButton) { //&& ((JLabel)child).getText().equals("")) {
                        	Log.d(((JRadioButton)child).getText());
                        	DeviceListData temp = (DeviceListData) listmodel.get(list.getSelectedIndex());
                        	
                        	if(temp.showstate == DeviceListData.SHOW_INSTALL_OPTION) {
                        		temp.showstate = DeviceListData.SHOW_INSTALL_DETAL;
//                        		((JLabel)child).setIcon(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_PREV.getImageIcon());
                        		list.repaint();
                        		
                        	} else {
                        		temp.showstate = DeviceListData.SHOW_INSTALL_OPTION;
//                        		((JLabel)child).setIcon(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_NEXT.getImageIcon());
                        		list.repaint();
                        	}
                        	
                        	
                        	//((JLabel)child).setIcon(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_PREV.getImageIcon());
                        	
                        	FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_REFRESH_DETAIL_PANEL));
                        }                        
                    }
                }
                
                @Override
                public void mouseMoved ( MouseEvent e ) {
                	Component child = getComponentinList(e);
                	Log.d("aa"+child);
                	if(!(child instanceof JRadioButton)) {
                		return ;
                	}
                	
                	button = (JRadioButton)child;
                	
                	
                	//((JRadioButton) child).getModel().setRollover(true);
                	list.repaint();
                }
            } );
            
            list.addMouseMotionListener(adapter);
        }
	    private Component getComponentinList(MouseEvent e) {
	        int index = list.locationToIndex ( e.getPoint () );
	        if ( index != -1 && list.isSelectedIndex ( index ) ) {
	            Rectangle rect = list.getCellBounds ( index, index );
	            Point pointWithinCell = new Point ( e.getX () - rect.x, e.getY () - rect.y );
	            
	            //Log.d("x = "+pointWithinCell.getX() + " y = " + pointWithinCell.getY());
	            
	//                Rectangle crossRect = new Rectangle ( rect.width - 9 - 5 - crossIcon.getIconWidth () / 2,
	//                        rect.height / 2 - crossIcon.getIconHeight () / 2, crossIcon.getIconWidth (), crossIcon.getIconHeight () );
	//                if ( crossRect.contains ( pointWithinCell ) )
	            
	            Object value = list.getModel().getElementAt(index);
	            Component comp = listrenderer.getListCellRendererComponent(list, value, index, true, true);
	            comp.setBounds(list.getCellBounds(index, index));
	            
	            //Component child = comp.getComponentAt(pointWithinCell);
	                    	
	            return SwingUtilities.getDeepestComponentAt(comp, pointWithinCell.x, pointWithinCell.y); 
	        }
	        
	        return null;
	    }
	    
	}
    
    
    public class DeviceListData
    {
        public Color circleColor;
        public String status;
        public String name;
        public String serialnumber;
        public String SDKVersion;
                
        public JComponent AppDetailpanel;
        public JComponent installoptionpanel;
        
        public int showstate;
        
        public static final String INSTALLED = "Installed";
        public static final String NOT_INSTALLED = "Not installed";
        public static final String CAN_NOT_INSTALL = "Can't installed";
        public static final String PUSH = "Push";
        
        public static final String WAITING = "walting";

        public static final int SHOW_INSTALL_DETAL = 0;
        public static final int SHOW_INSTALL_OPTION = 1;
        
        
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
    private class CustomListPanel extends JPanel {
    	CustomLabel label;
    	JPanel Tagpanel;
    	JLabel TagLabel;
    	DeviceListData data;
    	
    	JLabel IconLabel;
    	
    	public CustomListPanel() {
    		label = new CustomLabel();
    		setLayout(new BorderLayout());
    		add(label, BorderLayout.CENTER);
    		
    		//Tagpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    		
    		Tagpanel = ToggleButtonBar.makeToggleButtonBar(0x888888);
    		
    		//Tagpanel.setBackground(Color.white);
    		
    		TagLabel = new JLabel("Install");
    		TagLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
    		
    		IconLabel = new JLabel("");
    		    		
    		//Tagpanel.add(TagLabel);
    		//Tagpanel.add(IconLabel);
    		
    		//Tagpanel.setBackground(Color.GRAY);
    		
    		add( Tagpanel , BorderLayout.SOUTH);
    		
    		
    		
    		setBackground(Color.white);
    		setBorder ( BorderFactory.createEmptyBorder ( 5, 5 , 5, 5 ) );
            
    	}
    	
        @Override
        protected void paintComponent ( Graphics g ) {
        	Graphics2D g2d = ( Graphics2D ) g;
        	g2d.setPaint ( Color.LIGHT_GRAY );
            g.drawLine(2, getHeight()-2, getWidth()-2, getHeight()-2 );
                        
            
        	if(data.showstate != DeviceListData.SHOW_INSTALL_OPTION) {
        		//data.showstate = DeviceListData.SHOW_INSTALL_DETAL;
        		IconLabel.setIcon(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_PREV.getImageIcon());
        	} else {
        		//data.showstate = DeviceListData.SHOW_INSTALL_OPTION;
        		IconLabel.setIcon(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_NEXT.getImageIcon());        		
        	}

            
        }
    	
    	
		public void setData(DeviceListData value) {
			// TODO Auto-generated method stub
			this.data = value;
			label.setData(value);
		}


		public void setSelected(boolean isSelected) {
			// TODO Auto-generated method stub
			label.setSelected(isSelected);
		}
    	
    }
    
    private class CustomLabel extends JLabel
    {
        private final Color selectionColor = new Color ( 82, 158, 202 );

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
            
            g2d.setPaint ( data.status.indexOf("ONLINE") > -1? data.getCircleColor():Color.GRAY );
            g2d.fill ( new Ellipse2D.Double ( 6, 6, 40, 40 ) );
            
            g2d.setPaint ( Color.WHITE );
            //g2d.drawString("N", 22, 22);
            
            if(data.getSDKVersion()!=null) {
                if(data.getSDKVersion().length() < 4) {
	            	centerString(g2d,new Rectangle(4, 4, 44, 44), data.getSDKVersion(), new Font(getFont().getName(), Font.BOLD, 20));
	            } else {
	            	centerString(g2d,new Rectangle(4, 4, 44, 44), data.getSDKVersion(), new Font(getFont().getName(), Font.BOLD, 15));
	            }
            }
            if(data.status.indexOf("ONLINE") > -1) {
            	g2d.setPaint ( new Color(116, 211, 109) ); // online color            
            } else if(data.status.indexOf("OFFLINE") > -1) {            	
            	g2d.setPaint(Color.GRAY);
            } else {
            	g2d.setPaint(Color.ORANGE);            	
            }
            g2d.fill ( new Ellipse2D.Double ( getWidth () - 18 - 10, getHeight () / 2 - 9, 18, 18 ) );
            
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
    			if(listmodel.size() >=1) {
    				this.setSelectedIndex(0);
    			}
    			return;
    			//setDeviceProperty(device, temp, IDevice.PROP_DEVICE_MODEL);
    		}
    	}		
	}
}
