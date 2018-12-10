package com.apkscanner.gui.easymode.contents;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasysdkNotDrawPanel extends FlatPanel {
	private final int ARR_SIZE = 6;	
	 
	private static int DEVICE_TARGET = 0;
	private static int DEVICE_STATE_ONLINE = 1;
	private static int DEVICE_STATE_OFFLINE = 2;

	private final Color linecolor = new Color(128, 100, 162);
	private final Color textcolor = new Color(127, 127, 127);
	
	private final Color[] Devicecolor = {new Color(80, 80, 80), new Color(156, 177, 117), new Color(247, 150, 70)}; 
	
	public ArrayList<sdkDrawObject> arraysdkObject = new ArrayList<sdkDrawObject>();
	
	private class sdkDrawObject implements Comparable<sdkDrawObject> {
		public int sdkversion = 0;
		public JPanel panel;
		public boolean isDevice = false;
		
		public sdkDrawObject(JPanel panel, int version) {
			// TODO Auto-generated constructor stub
			this.sdkversion = version;
			this.panel = panel;
		}

		public sdkDrawObject(JPanel panel, int version, boolean isdevice) {
			// TODO Auto-generated constructor stub
			this(panel, version);
			this.isDevice = isdevice;
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
	
	
	public EasysdkNotDrawPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

//		add(makeTextPanel("min", 23));
//		add(makeTextPanel("max", 24));
//		add(makeDevicePanel(Devicecolor[DEVICE_TARGET], 23));
//		add(makeDevicePanel(Devicecolor[DEVICE_STATE_ONLINE], 26));
//		add(makeDevicePanel(Devicecolor[DEVICE_STATE_OFFLINE], 30));
				
		add(Box.createVerticalStrut(10));
	}
	
	private JPanel makeDevicePanel(Color devicecolor, int sdkversion) {
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 7));
		topPanel.setOpaque(false);
		// topPanel.setPreferredSize(new Dimension(0, 0));
		// topPanel.setBorder(BorderFactory.createEtchedBorder());
		topPanel.setPreferredSize(new Dimension(0, 10));

		JPanel temp = new JPanel(new BorderLayout());
		temp.setOpaque(false);
		JLabel minlabel = new JLabel();
		
		minlabel.setIcon(ImageUtils.setcolorImage(Resource.IMG_EASY_WINDOW_DEVICE.getImageIcon(25, 25), devicecolor));
		minlabel.setOpaque(false);

		JLabel allowlabel = new JLabel();
		allowlabel.setIcon(Resource.IMG_EASY_WINDOW_ALLOW.getImageIcon(15, 15));
		allowlabel.setOpaque(false);

		temp.add(minlabel, BorderLayout.CENTER);
		temp.add(allowlabel, BorderLayout.EAST);
		topPanel.add(temp, BorderLayout.WEST);

		JLabel sdkverionlabel = new JLabel(sdkversion + "");
		sdkverionlabel.setForeground(textcolor);
		sdkverionlabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		topPanel.add(sdkverionlabel, BorderLayout.EAST);

		return topPanel;
	}

	private JPanel makeTextPanel(String text, int sdkversion) {
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 7));
		topPanel.setOpaque(false);
		// topPanel.setPreferredSize(new Dimension(0, 0));
		// topPanel.setBorder(BorderFactory.createEtchedBorder());
		topPanel.setPreferredSize(new Dimension(0, 10));
		JLabel minlabel = new JLabel(text);
		minlabel.setForeground(textcolor);
		minlabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		minlabel.setIcon(Resource.IMG_EASY_WINDOW_ALLOW.getImageIcon(15, 15));
		minlabel.setHorizontalTextPosition(JLabel.LEFT);
		minlabel.setIconTextGap(-2);
		minlabel.setOpaque(false);
		topPanel.add(minlabel, BorderLayout.WEST);
		JLabel sdkverionlabel = new JLabel(sdkversion + "");
		sdkverionlabel.setForeground(textcolor);
		sdkverionlabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		topPanel.add(sdkverionlabel, BorderLayout.EAST);

		return topPanel;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2); // paint parent's background
		// setBackground(Color.BLACK); // set background color for this JPanel
		// setForeground(Color.BLACK);
		
		if(arraysdkObject.size() == 0) return;
		
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHints(rh);
		AffineTransform at = g2.getTransform();
		g2.setColor(linecolor);
		drawArrow(g2, 50, 10, 50, 160);
		g2.setTransform(at);

	}

	void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		// Draw horizontal arrow starting in (0, 0)
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, 0));
		g.drawLine(0, 0, len - 4, 0);

		g.drawLine(len, 0, len - ARR_SIZE, -ARR_SIZE);

		g.drawLine(len - ARR_SIZE, ARR_SIZE, len, 0);
		// g.drawLine(0, 0, len-4, 0);

		// g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
		// new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
	}

	public void setsdkpanel(ApkInfo apkInfo) {
		// TODO Auto-generated method stub
		if(apkInfo.manifest.usesSdk.minSdkVersion!=null) {
			int minsdk = apkInfo.manifest.usesSdk.minSdkVersion;			
			arraysdkObject.add(new sdkDrawObject(makeTextPanel("min", minsdk), minsdk));
		}
		
		if(apkInfo.manifest.usesSdk.maxSdkVersion!=null) {
			int maxsdk = apkInfo.manifest.usesSdk.maxSdkVersion;
			arraysdkObject.add(new sdkDrawObject(makeTextPanel("max", maxsdk), maxsdk));
		}
		
		if(apkInfo.manifest.usesSdk.targetSdkVersion!=null) {
			int targetsdk = apkInfo.manifest.usesSdk.targetSdkVersion;
			//arraysdkObject.add(new sdkDrawObject(makeDevicePanel(Devicecolor[DEVICE_TARGET], targetsdk), targetsdk));
			arraysdkObject.add(new sdkDrawObject(makeTextPanel("tar", targetsdk), targetsdk));
		}
		
		if(AndroidDebugBridge.getBridge()!=null) {
			if(AndroidDebugBridge.getBridge().getDevices().length > 0) {
				IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
				for(IDevice device : devices) {
					if(device.getState() != IDevice.DeviceState.ONLINE) continue;
					String name = getDeviceName(device);
					String sdkversion = device.getProperty(IDevice.PROP_BUILD_API_LEVEL);
					int nversion = Integer.parseInt(sdkversion);
					
					arraysdkObject.add(new sdkDrawObject(
							makeDevicePanel(
							Devicecolor[(device.getState() == IDevice.DeviceState.ONLINE)?DEVICE_STATE_ONLINE : DEVICE_STATE_OFFLINE],
							nversion), nversion, true));
				}
			}			
		}
				
		refreshpanel();
	}
	
	private String getDeviceName(IDevice device) {
		String deviceName;
			deviceName = device.getProperty(IDevice.PROP_DEVICE_MODEL);
			if(deviceName != null) deviceName = deviceName.trim();
			if(deviceName != null && deviceName.isEmpty()) {
				deviceName = null;
			}		
		return deviceName;
	}

	
	private void refreshpanel() {		
		Collections.sort(arraysdkObject);
		for(sdkDrawObject obj: arraysdkObject) {			
			add(obj.panel);
		}
		add(Box.createVerticalStrut(10));
		validate();
	}

	public void clear() {
		// TODO Auto-generated method stub
		removeAll();
		arraysdkObject.clear();
		add(Box.createVerticalStrut(10));
		validate();
	}

	public void changeDevice(IDevice[] devices) {
		// TODO Auto-generated method stub		 
		 removeAll();
		 for(int i = arraysdkObject.size()-1 ; i >=0 ; i--) {
				if(arraysdkObject.get(i).isDevice) {
					arraysdkObject.remove(i);
				}
		 }
		
		for(IDevice device : devices) {
			String name = getDeviceName(device);
			String sdkversion = device.getProperty(IDevice.PROP_BUILD_API_LEVEL);
			int nversion = Integer.parseInt(sdkversion);
			
			arraysdkObject.add(new sdkDrawObject(
					makeDevicePanel(
					Devicecolor[(device.getState() == IDevice.DeviceState.ONLINE)?DEVICE_STATE_ONLINE : DEVICE_STATE_OFFLINE],
					nversion), nversion, true));
		}
		
		refreshpanel();
		updateUI();
	}	
}
