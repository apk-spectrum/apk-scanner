package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.ImageObserver;
import java.io.IOException;

import java.util.Objects;
import java.util.jar.JarFile;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.DefaultOptionsFactory;
import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.PackageInfoPanel;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.resource.Resource;

import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;


@SuppressWarnings("rawtypes")
public class DeviceCustomList extends JList implements ListSelectionListener{
	private static final long serialVersionUID = 4647130365982201484L;
	
	DefaultListModel<DeviceListData> listmodel;
	ButtonsRenderer<DeviceListData> listrenderer;
	ActionListener FindPackagelistener;
	private int status;
	int Installfinishcount = 0;
    @SuppressWarnings("unchecked")
	public DeviceCustomList(ActionListener listener) {
		// TODO Auto-generated constructor stub
    	setLayout(new BorderLayout());    	
        listmodel = new DefaultListModel<DeviceListData> ();
		//AndroidDebugBridge.init(true);
        //IDevice[] devices = AdbServerMonitor.getAndroidDebugBridge().getDevices();        
        //Log.d(devices.length + "         " + ApkInstallWizard.pakcageFilePath);        
        //AndroidDebugBridge.addDeviceChangeListener(this);
        
        setPreferredSize(new Dimension(200, 0));
        
        listrenderer = new ButtonsRenderer<DeviceListData>(listmodel);
        FindPackagelistener = listener;
        
        CellButtonsMouseListener cbml = new CellButtonsMouseListener(this);
        
        this.addMouseListener(cbml);
        this.addMouseMotionListener(cbml);
        
        this.setModel(listmodel);
        this.setCellRenderer ( listrenderer);
        
        
        this.setBorder ( BorderFactory.createEmptyBorder ( 5, 5, 5, 5 ) );
        this.addListSelectionListener(this);
        
	}

    private int hashCode(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
        	//hash = str.charAt(i)^10 + ((hash << 4) - hash);
        	hash = str.charAt(i) + ((hash << 10) * hash);
        }
        return hash;
    }
    
	public void setStatus(int status) {
		this.status = status;
		((ButtonsRenderer<DeviceListData>)this.getCellRenderer()).setStatus(status);
		switch(status) {
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:
			
			break;
		case ApkInstallWizard.STATUS_INSTALLING:			
			deviceListinstall();
			break;
		case ApkInstallWizard.STATUS_COMPLETED:
			
			break;
		}
	}
    
	private void deviceListinstall() {
		final ListModel<DeviceListData> listmodel  = this.getModel();
		
		ThreadGroup installpool = new ThreadGroup("install");
		
		Installfinishcount =0;
		
		for(int i=0; i< listmodel.getSize(); i++) {
			final int j = i;
			
			//Log.d(""+ listmodel.getElementAt(j).isNoinstall());
			
			if(listmodel.getElementAt(j).isNoinstall()) {				
				if(isfinishall(listmodel.getSize())) {
					Log.d("insall finish");
					FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_FINISHED_INSTALL));					
				}
				continue;
			}
			
			new Thread(new Runnable() {
					@Override
					public void run() {
						DeviceListData data = listmodel.getElementAt(j);
						
						data.showstate = DeviceListData.SHOW_LOADING_INSTALL;
						
						String message = ApkInstaller.install(data.device, ApkInstallWizard.apkInfo, data.bundleoption);
					
					if( message == null) {
						Log.d("suc");
						data.installErrorCuase = message;
					} else {						
						Log.d("error = " + message);
						data.installErrorCuase = message;
					}
					data.showstate = DeviceListData.SHOW_COMPLETE_INSTALL;
					FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_REFRESH_DETAIL_PANEL));

					if(isfinishall(listmodel.getSize())) {
						Log.d("insall finish");
						FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_FINISHED_INSTALL));
						
					}
				}
			}).start();
		}		
	}
	
	synchronized boolean isfinishall(int size) {
		Installfinishcount++;
		if(size == Installfinishcount) {
			return true;			
		}		
		return false;
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
    
    @SuppressWarnings("unchecked")
	private void setModeldata(DefaultListModel listmodel, final IDevice device) {
    	Log.d("set model");
    	for(int i=0; i < listmodel.size(); i++) {
    		DeviceListData temp = (DeviceListData) listmodel.getElementAt(i);
    		if(temp.serialnumber.equals(device.getSerialNumber())) {
    			
    			refteshdefaultListData(temp, device);
    			
    			this.repaint();
    			return;
    		}
    	}
    	
		final DeviceListData data = new DeviceListData();
		data.serialnumber = device.getSerialNumber();
		
				
		refteshdefaultListData(data, device);
		
		listmodel.addElement (data);
		
		if(listmodel.size() ==1) {
			setSelectedIndex(0);
			fireSelectionValueChanged(0, 0, true);
		}
		
		this.repaint();
    }
    
    private void refteshdefaultListData(final DeviceListData data, final IDevice device) {
		setDeviceProperty(device,data,IDevice.PROP_DEVICE_MODEL);
		setDeviceProperty(device,data,IDevice.PROP_BUILD_VERSION);
		
		data.circleColor = Color.decode("#"+intToARGB(hashCode(data.name)));
		
		data.status = data.SDKVersion == null ? "OFFLINE" : device.getState().toString();
		
		data.showstate = DeviceListData.SHOW_INSTALL_OPTION;
		data.pacakgeLoadingstatus =DeviceListData.WAITING; 
		data.AppDetailpanel = new JLabel(Resource.IMG_LOADING.getImageIcon());
		data.installoptionpanel = new JLabel(Resource.IMG_LOADING.getImageIcon());
		data.device = device;

		final JList list = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				data.AppDetailpanel = getPackageInfopanel(device);
				//data.AppDetailpanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
				
				if(data.AppDetailpanel instanceof PackageInfoPanel) {
					data.isinstalled = DeviceListData.INSTALLED;
				} else {
					data.isinstalled = DeviceListData.NOT_INSTALLED;
				}
				FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_REFRESH_DETAIL_PANEL));
				
				setInstalloptionListener(list, data, device);
								
		        fireSelectionValueChanged(0, 0, true);
			}
		}).start();
		
    }
    
    private synchronized void setInstalloptionListener(final JList list, final DeviceListData data ,final IDevice device) {
		new Thread(new Runnable() {
			@Override
			public void run() {
	    	if(ApkInstallWizard.optFactory==null) {
	    		if(ApkInstallWizard.apkInfo == null) {
			        ApkScanner scanner = ApkScanner.getInstance("AAPTLIGHT");
			        scanner.openApk(ApkInstallWizard.pakcageFilePath);
			        
			        ApkInstallWizard.apkInfo = new CompactApkInfo(scanner.getApkInfo());
	    		}
	    		if(ApkInstallWizard.signatureReport == null) {
	    			try {
						ApkInstallWizard.signatureReport = new SignatureReport(new JarFile(ApkInstallWizard.pakcageFilePath, true));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    		ApkInstallWizard.optFactory = new DefaultOptionsFactory(ApkInstallWizard.apkInfo, ApkInstallWizard.signatureReport);
	    	}
	    	
	    	InstallOptionPanel installtemppanel = new InstallOptionPanel();
	    	
	        OptionsBundle bundle = ApkInstallWizard.optFactory.createOptions(device);
	        bundle.addOptionsChangedListener(new OptionsBundle.IOptionsChangedListener() {
	        	//DeviceListData Listdata;
	            @Override
	            public void changeOptions(int changedFlag, String... extraData) {
	            	Log.d("change Install Option : " + changedFlag);
	                switch(changedFlag) {
	                case OptionsBundle.FLAG_OPT_INSTALL:
	                    // 인스톨로 바뀜
	                	data.selectedinstalloption =  DeviceListData.OPTION_INSTALL;
	                    break;
	                case OptionsBundle.FLAG_OPT_PUSH:
	                    // 푸쉬로 바뀜
	                	data.selectedinstalloption =  DeviceListData.OPTION_PUSH;
	                    break;
	                case OptionsBundle.FLAG_OPT_NO_INSTALL:
	                    // 푸쉬로 바뀜
	                	data.selectedinstalloption =  DeviceListData.OPTION_NO_INSTALL;
	                    break;
	                case OptionsBundle.FLAG_OPT_DISSEMINATE:
	                	
	                	break;
	                }
	                list.repaint();
	            }
	        });
	        
	        //((InstallOptionPanel)data.installoptionpanel).selectedinstalloption;
	        		
	        if(bundle.isDontInstallOptions()) {
	        	data.selectedinstalloption =  DeviceListData.OPTION_IMPOSSIBLE_INSTALL;
	        } else if(bundle.isPushOptions()) {
	        	data.selectedinstalloption =  DeviceListData.OPTION_PUSH;
	        } else if(bundle.isInstallOptions()) {	        	
	        	data.selectedinstalloption =  DeviceListData.OPTION_INSTALL;
	        } else if(bundle.isNoInstallOptions()) {
	        	data.selectedinstalloption =  DeviceListData.OPTION_NO_INSTALL;
	        }
	        
	        installtemppanel.setApkInfo(ApkInstallWizard.apkInfo);
	        installtemppanel.setOptions(bundle);
	        data.pacakgeLoadingstatus =DeviceListData.DONE;
	        data.installoptionpanel = installtemppanel;
	        data.bundleoption = bundle;
	        list.repaint();
	        FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_REFRESH_DETAIL_PANEL));
			}
			
		}).start();
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
                
		return new JLabel("Not installed");
        //return null;
	}
    
	public void setDeviceProperty(IDevice device, final DeviceListData DO, final String propertyname) {
		try {
		if("ONLINE".equals(device.getState().toString())) {
				device.executeShellCommand("getprop "+propertyname, new MultiLineReceiver() {
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
    
	class CellButtonsMouseListener extends MouseAdapter {
	    private int prevIndex = -1;
	    private JButton prevButton;
	    private final JList<String> list;
	    protected CellButtonsMouseListener(JList<String> list) {
	        super();
	        this.list = list;
	    }
	    @SuppressWarnings("unchecked")
		@Override public void mouseMoved(MouseEvent e) {
	        //JList list = (JList) e.getComponent();
	        Point pt = e.getPoint();
	        int index = list.locationToIndex(pt);
	        if (!list.getCellBounds(index, index).contains(pt)) {
	            if (prevIndex >= 0) {
	                listRepaint(list, list.getCellBounds(prevIndex, prevIndex));
	            }
	            index = -1;
	            prevButton = null;
	            return;
	        }
	        if (index >= 0) {
	            JButton button = getButton(list, pt, index);
	            ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
	            renderer.button = button;
	            if (Objects.nonNull(button) && button.isEnabled()) {
	                button.getModel().setRollover(true);
	                renderer.rolloverIndex = index;
	                if (!button.equals(prevButton)) {
	                    listRepaint(list, list.getCellBounds(prevIndex, index));
	                }
	            } else {
	                renderer.rolloverIndex = -1;
	                Rectangle r = null;
	                if (prevIndex == index) {
	                    if (prevIndex >= 0 && Objects.nonNull(prevButton)) {
	                        r = list.getCellBounds(prevIndex, prevIndex);
	                    }
	                } else {
	                    r = list.getCellBounds(index, index);
	                }
	                listRepaint(list, r);
	                prevIndex = -1;
	            }
	            prevButton = button;
	        }
	        prevIndex = index;
	    }
	    @Override public void mousePressed(MouseEvent e) {
	    	clickevent(e);
	    }
	    @Override public void mouseReleased(MouseEvent e) {
	    	clickevent(e);
	    }
	    
	    @SuppressWarnings("unchecked")
		private void clickevent(MouseEvent e) {
	        Point pt = e.getPoint();
	        int index = list.locationToIndex(pt);
	        if (index >= 0) {
	            JButton button = getButton(list, pt, index);
	            if (Objects.nonNull(button) && button.isEnabled()) {
	            	ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
	                renderer.pressedIndex = -1;
	                renderer.button = null;
	                button.doClick();
	                listRepaint(list, list.getCellBounds(index, index));
	                
                	DeviceListData temp = (DeviceListData) listmodel.get(list.getSelectedIndex());
                	
                	if(status != ApkInstallWizard.STATUS_CHECK_PACKAGES) return;
                	
                	if(temp.isinstalled == DeviceListData.NOT_INSTALLED) { 
                		temp.showstate = DeviceListData.SHOW_INSTALL_OPTION;
                		return;
                	} else if(button.getActionCommand().equals(ToggleButtonBar.BUTTON_TYPE_INSTALL_INFO)) {
                		temp.showstate = DeviceListData.SHOW_INSTALL_OPTION;
                	} else if(button.getActionCommand().equals(ToggleButtonBar.BUTTON_TYPE_PACAKGE_INFO)){                		
                		temp.showstate = DeviceListData.SHOW_INSTALL_DETAL;
                	}
                	//list.repaint();
                	listRepaint(list, list.getCellBounds(index, index));
	                FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_REFRESH_DETAIL_PANEL));
	                
	            }
	        }
	    }
	    
	    
	    private void listRepaint(JList list, Rectangle rect) {
	        if (Objects.nonNull(rect)) {
	            list.repaint(rect);
	        }
	    }
	    private JButton getButton(JList<String> list, Point pt, int index) {
	        Component c = list.getCellRenderer().getListCellRendererComponent(list, "", index, false, false);
	        Rectangle r = list.getCellBounds(index, index);
	        c.setBounds(r);
	        //c.doLayout(); //may be needed for mone LayoutManager
	        pt.translate(-r.x, -r.y);
	        Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
	        if (b instanceof JButton) {
	            return (JButton) b;
	        } else {
	            return null;
	        }
	    }
	}
    
	class ButtonsRenderer<E> extends JPanel implements ListCellRenderer<E> {	    
		/**
		 * 
		 */
		private static final long serialVersionUID = 927417951602577901L;	
				
	    public int pressedIndex  = -1;
	    public int rolloverIndex = -1;
	    public JButton button;
	    ToggleButtonBar Tagpanel;
	    CustomLabel customlabel = new CustomLabel();
	    JLabel isinstallIcon;
	    ImageIcon ii;
	    protected ButtonsRenderer(DefaultListModel<E> model) {
	        super(new BorderLayout());
	        
			 ii = new ImageIcon(Resource.IMG_INSTALL_LOADING.getImageIcon().getImage());
	        	 
	        
	        setBorder ( BorderFactory.createEmptyBorder ( 5, 5 , 5, 5 ) );
	        
	        setOpaque(true);
	        Tagpanel = new ToggleButtonBar(DeviceListData.WAITING);
	        
	        JPanel Iconpanel = new JPanel(new BorderLayout());
	        isinstallIcon = new JLabel(Resource.IMG_INSTALL_BLOCK.getImageIcon());
	        
	        Iconpanel.setBackground(Color.WHITE);
	        
	        Iconpanel.add(Tagpanel, BorderLayout.CENTER);
	        Iconpanel.add(isinstallIcon, BorderLayout.WEST);
	        
	        //add(textArea);	        
	        add(customlabel, BorderLayout.CENTER);
	        add(Iconpanel, BorderLayout.SOUTH);
	    }
	    @Override public Dimension getPreferredSize() {
	        Dimension d = super.getPreferredSize();
	        d.width = 0; // VerticalScrollBar as needed
	        return d;
	    }
	    
        @Override
        protected void paintComponent ( Graphics g ) {
        	Graphics2D g2d = ( Graphics2D ) g;
        	g2d.setPaint ( Color.LIGHT_GRAY );
            g.drawLine(2, getHeight()-2, getWidth()-2, getHeight()-2 );                    
        }
        public void setStatus(int status) {
        	Tagpanel.setStatus(status);
        }
	    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
	        //textArea.setText(Objects.toString(value, ""));
	    	
	    	if(value instanceof DeviceListData) {
	    		customlabel.setData((DeviceListData)value);
	    		Tagpanel.setData((DeviceListData)value);	    		
	    		resetButtonStatus((DeviceListData) value);
	    		
	    		//Log.d("aa" + ((DeviceListData) value).selectedinstalloption);
	    		
	    		if(((DeviceListData)value).showstate == DeviceListData.SHOW_LOADING_INSTALL) {
	    			 ii.setImageObserver(new AnimatedObserver(list, index));

	    			isinstallIcon.setIcon(ii);
	    		}else {	    		
	    			if(((DeviceListData)value).status.equals("OFFLINE") || ((DeviceListData)value).isNoinstall()) {    				
	    				isinstallIcon.setIcon(Resource.IMG_INSTALL_BLOCK.getImageIcon());
	    			} else if(((DeviceListData)value).selectedinstalloption != DeviceListData.OPTION_NO_INSTALL) {
	    				isinstallIcon.setIcon(Resource.IMG_INSTALL_CHECK.getImageIcon());	    					    				
	    			}
	    		}
    		}
	    		        
	        if (isSelected) {
	            setBackground(list.getSelectionBackground());
	            customlabel.setSelected(isSelected);
	            
	        } else {
	            setBackground(list.getBackground());
	            customlabel.setSelected(isSelected);
	        }
	        
	        if (Objects.nonNull(button)) {
	            if (index == pressedIndex) {
	                button.getModel().setSelected(true);
	                button.getModel().setArmed(true);
	                button.getModel().setPressed(true);
	            } else if (index == rolloverIndex) {
	                button.getModel().setRollover(true);
	            }
	        }
	        return this;
	    }
	    private void resetButtonStatus(DeviceListData value) {	    	
	    	for( Component b: Tagpanel.getComponents() ) {
	    		
	    		if(b instanceof JButton || b instanceof JLabel) {
	    			
	    			if((value).status.equals("OFFLINE") || (value).pacakgeLoadingstatus ==DeviceListData.WAITING) {
	    				b.setEnabled(false);
	    				continue;
	    			} else if((value).status.equals("ONLINE")) {
	    				b.setEnabled(true);
	    			}
	    			
	    			ButtonModel m = ((JButton)b).getModel();
	    			m.setRollover(false);
		            m.setArmed(false);
		            m.setPressed(false);
		            m.setSelected(false);
	    		}
	    	}	    	
	    }
	}
    
    public class DeviceListData
    {
        public Color circleColor;
        public String status;
        public int pacakgeLoadingstatus;
        
        public String name;
        public String serialnumber;
        public String SDKVersion;
        
        public int selectedinstalloption = WAITING;
        
        public JComponent AppDetailpanel;
        public JComponent installoptionpanel;
        
        public int isinstalled = WAITING;
        //public int possibleOption = WAITING;
        
        public int showstate;
        
        public IDevice device;
        public OptionsBundle bundleoption = null;
        public String installErrorCuase;
        public static final int INSTALLED = 0;
        public static final int NOT_INSTALLED = 1;
        
        public static final int OPTION_INSTALL = 0;
        public static final int OPTION_NO_INSTALL = 1;
        public static final int OPTION_PUSH = 2;
        public static final int OPTION_IMPOSSIBLE_INSTALL = 3;
                
        public static final int WAITING = 4;
        public static final int DONE = 1;
        
        public static final int SHOW_INSTALL_DETAL = 0;
        public static final int SHOW_INSTALL_OPTION = 1;
        
        public static final int SHOW_LOADING_INSTALL = 2;
        public static final int SHOW_COMPLETE_INSTALL = 3;
        
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

        private String getName () {
            return name;
        }
        
        private String getSDKVersion() {
        	return SDKVersion;
        }
        
        public Boolean isNoinstall() {
        	return (selectedinstalloption == OPTION_NO_INSTALL || selectedinstalloption == OPTION_IMPOSSIBLE_INSTALL) ;
        }
    }
    
    
	private class CustomLabel extends JLabel
    {
		private static final long serialVersionUID = -9172448518025388689L;

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
    			listmodel.removeElementAt(i);
    			
    			if(listmodel.size() >=1) {
    				this.setSelectedIndex(0);
    			}
    			return;
    			//setDeviceProperty(device, temp, IDevice.PROP_DEVICE_MODEL);
    		}
    	}		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
        //boolean adjust = e.getValueIsAdjusting();
        //if (!adjust) {
		
		if(status != ApkInstallWizard.STATUS_CHECK_PACKAGES) return;
		
          JList list = (JList) e.getSource();
          int selections[] = list.getSelectedIndices();
          @SuppressWarnings("deprecation")
		Object selectionValues[] = list.getSelectedValues();
          for (int i = 0, n = selections.length; i < n; i++) {            
            //System.out.print(selections[i] + "/" + selectionValues[i] + " ");
        	  if(status == ApkInstallWizard.STATUS_CHECK_PACKAGES)((DeviceListData)selectionValues[i]).showstate  = DeviceListData.SHOW_INSTALL_OPTION;
        	  FindPackagelistener.actionPerformed(new ActionEvent(this, 0, FindPackagePanel.REQ_REFRESH_DETAIL_PANEL));
          }
        //}
		 
	}
	
	class AnimatedObserver implements ImageObserver
	{
	   JList list;
	   int index;
	  
	   public AnimatedObserver(JList list, int index) {
	      this.list = list;
	      this.index = index;
	   } 
	  
	   public boolean imageUpdate (Image img, int infoflags, int x, int y, int width, int height) {
	      if ((infoflags & (FRAMEBITS|ALLBITS)) != 0) {
	         Rectangle rect = list.getCellBounds(index, index);
	         list.repaint(rect);
	      }
	  
	      return (infoflags & (ALLBITS|ABORT)) == 0;
	   }
	} 
}
