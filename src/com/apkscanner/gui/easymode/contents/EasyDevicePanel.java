package com.apkscanner.gui.easymode.contents;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyDevicePanel extends RoundPanel implements MouseListener, ActionListener{
	 
	private static int DEVICE_TARGET = 0;
	private static int DEVICE_STATE_ONLINE = 1;
	private static int DEVICE_STATE_OFFLINE = 2;

	private static String CARD_LAYOUT_SPREAD= "card_spread";
	private static String CARD_LAYOUT_FOLD= "card_fold";
	
	private final Color linecolor = new Color(128, 100, 162);
	private final Color textcolor = new Color(127, 127, 127);
	
	private final Color[] Devicecolor = {new Color(80, 80, 80), new Color(156, 177, 117), new Color(247, 150, 70)}; 
	
	public ArrayList<sdkDrawObject> arraysdkObject = new ArrayList<sdkDrawObject>();
	private Boolean isspread = false;
	private GridBagConstraints cons;
	private int WIDTH = 30;
	private int SPREAD_WIDTH = 60;
	private int DEIVCE_HEIGHT = 50;
	private int DEIVCE_SPREAD_HEIGHT = 50;
	
	private class sdkDrawObject implements Comparable<sdkDrawObject> {
		public int sdkversion = 0;
		public JPanel panel;
		public String devicename;
		
		public sdkDrawObject(JPanel panel, int version) {
			// TODO Auto-generated constructor stub
			this.sdkversion = version;
			this.panel = panel;
		}

		public sdkDrawObject(JPanel panel, int version, String name) {
			// TODO Auto-generated constructor stub
			this(panel, version);
			this.devicename = name;
		}
		
		@Override
		public int compareTo(sdkDrawObject s) {
			// TODO Auto-generated method stub
            if (this.sdkversion < s.sdkversion) {
                return -1;
            } else if (this.sdkversion > s.sdkversion) {
                return 1;
            }
            return 0;			
		}
	}
	
	
	public EasyDevicePanel(int width) {
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		WIDTH = width;
		SPREAD_WIDTH = width *2;
		
		setLayout(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;		
		cons.weightx = 1;
		cons.gridx = 0;
		
//		setLayout(new FlowLayout(FlowLayout.CENTER));
//		add(makeTextPanel("min", 23));
//		add(makeTextPanel("max", 24));
//		add(makeDevicePanel(Devicecolor[DEVICE_TARGET], 23));
		//add(makeDevicePanel(Devicecolor[DEVICE_STATE_ONLINE], 26, "aaaa"), cons);
		//add(makeDevicePanel(Devicecolor[DEVICE_STATE_OFFLINE], 30, "bbbbbb"), cons);
		//add(Box.createVerticalStrut(10));
	}
	
	private JPanel makeDevicePanel(Color devicecolor, int sdkversion, String devicename) {
		//RoundPanel panel = new RoundPanel();
		
		final JPanel cardlayout = new JPanel();
		cardlayout.setLayout(new CardLayout());		
		cardlayout.setOpaque(false);
		cardlayout.setBorder(new EmptyBorder(3, 3, 3, 3));
		//cardlayout.setPreferredSize(new Dimension(SPREAD_WIDTH-4, DEIVCE_HEIGHT-4));
		cardlayout.setPreferredSize(new Dimension(WIDTH-4,DEIVCE_HEIGHT-4));
		
		final EasyRoundLabel panel = new EasyRoundLabel(" ", devicecolor, Color.BLACK );
		
		EasyRoundButton imagepanel = new EasyRoundButton(ImageUtils.setcolorImage(Resource.IMG_EASY_WINDOW_DEVICE.getImageIcon(35, 35), Color.BLACK));
		//EasyRoundButton imagepanel = new EasyRoundButton(Resource.IMG_EASY_WINDOW_DEVICE.getImageIcon(35, 35));
		
		imagepanel.setOpaque(false);
		imagepanel.setBackground(devicecolor);
		imagepanel.addActionListener(this);
		//imagepanel.setPreferredSize(new Dimension(WIDTH-4, DEIVCE_HEIGHT-4));
		//minlabel.setIcon(ImageUtils.setcolorImage(Resource.IMG_EASY_WINDOW_DEVICE.getImageIcon(25, 25), devicecolor));		
				
		//panel.setPreferredSize(new Dimension(SPREAD_WIDTH-4, DEIVCE_HEIGHT-4));
		panel.setMouseHoverEffect(true);
		panel.setOpaque(false);		
		panel.setMouseListener(this);
		panel.setshadowlen(1);
		panel.setText(devicename + " / " + sdkversion);
		panel.setTextFont(new Font(getFont().getName(), Font.BOLD, 17));
		
		
		cardlayout.add(CARD_LAYOUT_FOLD, imagepanel);
		cardlayout.add(CARD_LAYOUT_SPREAD, panel);
		
		((CardLayout)cardlayout.getLayout()).show(cardlayout, (isspread)?CARD_LAYOUT_SPREAD : CARD_LAYOUT_FOLD);
		
		
		return cardlayout;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2); // paint parent's background
		// setBackground(Color.BLACK); // set background color for this JPanel
		// setForeground(Color.BLACK);
		
//		if(arraysdkObject.size() == 0) return;
//		
//		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setRenderingHints(rh);
//		AffineTransform at = g2.getTransform();
//		g2.setColor(linecolor);
//		//drawArrow(g2, 50, 10, 50, 160);
//		g2.setTransform(at);

	}

	private void adddevices(IDevice[] devices) {
		if(devices.length ==0) {			
			//setPreferredSize(new Dimension(0,0));
			setVisible(false);
			return;
		}
		
		setVisible(true);
		if(!isspread) {
			setPreferredSize(new Dimension(WIDTH,0));
		} else {
			setPreferredSize(new Dimension(SPREAD_WIDTH,0));
		}
		
		for(IDevice device : devices) {
			String name = "";
			String sdkversion="";
			int nversion = 0;
			
			if(device.getState() == IDevice.DeviceState.ONLINE) {
				name = getDeviceName(device);
				sdkversion = device.getProperty(IDevice.PROP_BUILD_API_LEVEL);
				nversion = Integer.parseInt(sdkversion);
			} else {
				name = "OFFLINE";
			}
			
			arraysdkObject.add(new sdkDrawObject(
					makeDevicePanel(
					Devicecolor[(device.getState() == IDevice.DeviceState.ONLINE)?DEVICE_STATE_ONLINE : DEVICE_STATE_OFFLINE],
					nversion, name), nversion, name));
		}
	}
	
	public void setsdkpanel(ApkInfo apkInfo) {		
		// TODO Auto-generated method stub
		
//		if(AndroidDebugBridge.getBridge()!=null) {
//			IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
//			adddevices(devices);
//		}
		refreshpanel();
	}
	
	private String getDeviceName(IDevice device) {
		String deviceName;
			deviceName = device.getProperty(IDevice.PROP_DEVICE_MODEL);
			if(deviceName != null) deviceName = deviceName.trim();
			if(deviceName != null && deviceName.isEmpty()) {
				deviceName = "";
			}		
		return deviceName;
	}

	
	private void refreshpanel() {		
		Collections.sort(arraysdkObject);		
		for(sdkDrawObject obj: arraysdkObject) {			
			add(obj.panel, cons);			
		}
		//add(Box.createVerticalStrut(10));
		//validate();
		updateUI();
	}

	public void clear() {
		// TODO Auto-generated method stub
		removeAll();
		arraysdkObject.clear();
		//add(Box.createVerticalStrut(10));
		updateUI();
	}

	public void changeDevice(IDevice[] devices) {
		// TODO Auto-generated method stub
		removeAll();

		arraysdkObject.clear();

		adddevices(devices);
		refreshpanel();
		updateUI();
	}

	private void changespread() {		
		for(sdkDrawObject obj: arraysdkObject) {
			if(!isspread) {
				setPreferredSize(new Dimension(SPREAD_WIDTH,0));
				obj.panel.setPreferredSize(new Dimension(SPREAD_WIDTH-4,DEIVCE_SPREAD_HEIGHT-4));
				
			} else {
				setPreferredSize(new Dimension(WIDTH,0));
				obj.panel.setPreferredSize(new Dimension(WIDTH-4,DEIVCE_HEIGHT-4));				
			}
			((CardLayout)obj.panel.getLayout()).show(obj.panel, (!isspread)?CARD_LAYOUT_SPREAD : CARD_LAYOUT_FOLD);			
		}
		isspread = !isspread;
		
		updateUI();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		changespread();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		changespread();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}	
}
