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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.IDevice.DeviceState;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyDevicePanel extends RoundPanel implements MouseListener, ActionListener, ComponentListener{
	 
	private static int DEVICE_TARGET = 0;
	private static int DEVICE_STATE_ONLINE = 1;
	private static int DEVICE_STATE_OFFLINE = 2;

	private static String CARD_LAYOUT_SPREAD= "card_spread";
	private static String CARD_LAYOUT_FOLD= "card_fold";
	
	private static String CMD_MODEL_NAME = "ro.product.model";
	private static String CMD_MODEL_API = "ro.build.version.release";
	
	private final Color linecolor = new Color(128, 100, 162);
	private final Color textcolor = new Color(127, 127, 127);
	
	private final Color[] Devicecolor = {new Color(80, 80, 80), new Color(156, 177, 117), new Color(247, 150, 70)}; 
	
	public ArrayList<sdkDrawObject> arraysdkObject = new ArrayList<sdkDrawObject>();
	private Boolean isspread = false;
	private GridBagConstraints cons;
	EasyGuiDeviceToolPanel devicetoolbar;
	JComponent clickedpanel;
	
	private int WIDTH = 30;
	private int SPREAD_WIDTH = 60;
	private int DEIVCE_HEIGHT = 50;
	private int DEIVCE_SPREAD_HEIGHT = 50;
	
	private class sdkDrawObject {
		public String sdkversion;
		public JPanel panel;
		public String devicename;
		public IDevice devicestate;
		public sdkDrawObject(JPanel panel, String version) {
			// TODO Auto-generated constructor stub
			this.sdkversion = version;
			this.panel = panel;
		}

		public sdkDrawObject(JPanel panel, String version, String name) {
			// TODO Auto-generated constructor stub
			this(panel, version);
			this.devicename = name;
		}
		public sdkDrawObject(JPanel panel, String version, String name, IDevice devicestate) {
			// TODO Auto-generated constructor stub
			this(panel, version);
			this.devicestate = devicestate;
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
		
		addComponentListener(this);
//		setLayout(new FlowLayout(FlowLayout.CENTER));
//		add(makeTextPanel("min", 23));
//		add(makeTextPanel("max", 24));
//		add(makeDevicePanel(Devicecolor[DEVICE_TARGET], 23));
		//add(makeDevicePanel(Devicecolor[DEVICE_STATE_ONLINE], 26, "aaaa"), cons);
		//add(makeDevicePanel(Devicecolor[DEVICE_STATE_OFFLINE], 30, "bbbbbb"), cons);
		//add(Box.createVerticalStrut(10));
	}
	
	private JPanel makeDevicePanel(Color devicecolor, String sdkversion, String devicename) {
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
		panel.setTextFont(new Font(getFont().getName(), Font.PLAIN, 20));
				
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
			
			if(device.getState() == IDevice.DeviceState.ONLINE) {
				name = getDeviceName(IDevice.PROP_DEVICE_MODEL, device);
				
				sdkversion = getDeviceName(IDevice.PROP_BUILD_API_LEVEL, device);
				
			} else {
				name = "OFFLINE";
			}
			
			arraysdkObject.add(new sdkDrawObject(
					makeDevicePanel(
					Devicecolor[(device.getState() == IDevice.DeviceState.ONLINE)?DEVICE_STATE_ONLINE : DEVICE_STATE_OFFLINE],
					sdkversion, name), sdkversion, name, device));

		}
	}
	
	private String getDeviceName(String prop, IDevice device) {
		String response = "";
		if(device != null) {
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			try {
				device.executeShellCommand("getprop " + prop, outputReceiver);
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response = outputReceiver.getOutput();
		}
		return response;
	}

	private void refreshpanel() {		
		//Collections.sort(arraysdkObject);		
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
				if(	obj.devicestate.getState() == IDevice.DeviceState.ONLINE) {
					updatetoolbarPosition();
					devicetoolbar.setVisible(true);
				}

			} else {
				setPreferredSize(new Dimension(WIDTH,0));
				obj.panel.setPreferredSize(new Dimension(WIDTH-4,DEIVCE_HEIGHT-4));
				devicetoolbar.setVisible(false);
			}
			((CardLayout)obj.panel.getLayout()).show(obj.panel, (!isspread)?CARD_LAYOUT_SPREAD : CARD_LAYOUT_FOLD);			
		}
		isspread = !isspread;
		
		updateUI();
	}
	
	public void updatetoolbarPosition() {
		if(devicetoolbar != null && clickedpanel != null) {
			devicetoolbar.setBounds(this.getX() - 150,this.getY()+ clickedpanel.getParent().getY()-5,200,150);
			devicetoolbar.updateUI();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		clickedpanel =  (EasyRoundButton)e.getSource();
		changespread();

	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

//		clickedpanel =  (EasyRoundButton)e.getSource();
		changespread();

	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {
		clickedpanel =  (JComponent) ((JComponent)e.getSource()).getParent();
		
		for(sdkDrawObject object : arraysdkObject) {
			//Log.d(object.devicestate.getState() + "");
			if(object.panel.equals(clickedpanel.getParent())) {
				devicetoolbar.setSelecteddevice(object.devicestate);
			}
		}
//devicetoolbar.setSelecteddevice(device);
//		Log.d(""+ clickedpanel.getParent());
		
		updatetoolbarPosition();
	}
	@Override
	public void mouseExited(MouseEvent e) {}

	public void setdevicetoolbar(EasyGuiDeviceToolPanel iconhoverpanel) {
		// TODO Auto-generated method stub
		devicetoolbar = iconhoverpanel;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
//		devicetoolbar.setBounds(this.getX() - 250,this.getY()+ clickedpanel.getY(),200,50);
		
		updatetoolbarPosition();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}	
}
