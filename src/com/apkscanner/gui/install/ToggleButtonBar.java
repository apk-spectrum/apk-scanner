package com.apkscanner.gui.install;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.resource.Resource;

public class ToggleButtonBar extends JPanel{
	
	private static final long serialVersionUID = -8497802681135584399L;
	public static final String BUTTON_TYPE_PACAKGE_INFO = "pacakge_info";
	public static final String BUTTON_TYPE_INSTALL_INFO = "install_info";
	
	DeviceListData data;
	int status;	
	private static final Color[] ColorSet = { new Color(0x50AF49), new Color(0xFF7400), new Color(0x5CD1E5), new Color(0x8b0000), new Color(0x555555)};
	
	private String[] installtextSet = {Resource.STR_BTN_INSTALLED.getString(), Resource.STR_BTN_NOT_INSTALLED.getString(), "", "", Resource.STR_BTN_WAITING.getString()};
	private String[] installOptiontextSet = {"Install", Resource.STR_BTN_NO_INSTALL.getString(),
			"Push", Resource.STR_BTN_IMPOSSIBLE_INSTALL.getString(), Resource.STR_BTN_WAITING.getString()};
	
	//private static final Color[] InstallingColorSet = {null, null, new Color(0x555555), new Color(0x50AF49), new Color(0x8b0000)};
	private static final String[] installingtextSet = {Resource.STR_LABEL_INSTALLING.getString(), Resource.STR_BTN_SUCCESS.getString(), Resource.STR_BTN_FAIL.getString()};
	
    //public static final int OPTION_INSTALL = 0;
    //public static final int OPTION_NO_INSTALL = 1;
    //public static final int OPTION_PUSH = 2;
    //public static final int OPTION_IMPOSSIBLE_INSTALL = 3;
    
    //public static final int SHOW_LOADING_INSTALL = 2;
    //public static final int SHOW_COMPLETE_INSTALL = 3;
	
	AbstractButton btninstalled;
	AbstractButton btnoption;
	
	public ToggleButtonBar(int state) {
        setLayout(new GridLayout(1, 0, 0, 0));
        
//        for (AbstractButton b: Arrays.asList(makeButton("install",BUTTON_TYPE_INSTALL_INFO, state), 
//        		makeButton("installed", BUTTON_TYPE_PACAKGE_INFO,state))) {            
//            b.setIcon(new ToggleButtonBarCellIcon());
//            //bg.add(b);
//            this.add(b);
//        }
//        
        btninstalled = makeButton("installed",BUTTON_TYPE_PACAKGE_INFO, state);
        btnoption = makeButton("install",BUTTON_TYPE_INSTALL_INFO, state);
        
        btninstalled.setIcon(new ToggleButtonBarCellIcon());
        btnoption.setIcon(new ToggleButtonBarCellIcon());
        
        
        this.add(btnoption);        
        this.add(btninstalled);
	}
	
    private static AbstractButton makeButton(String title, String ActionString, int level) {
        AbstractButton b = new JButton(title);
        //b.setVerticalAlignment(SwingConstants.CENTER);
        //b.setVerticalTextPosition(SwingConstants.CENTER);
        //b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setActionCommand(ActionString);
        //b.setBackground(new Color(cc));
        b.setForeground(Color.WHITE);
        
        b.setBackground(ColorSet[level]);
        
        return b;
    }
	
    // - 20:02   설치됨,안됨 / 설치,푸쉬,불가
   
    
    public JPanel makeToggleButtonBar(int install_level, int package_level) {
        JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));        
        //Color color = new Color(cc);
        for (AbstractButton b: Arrays.asList(makeButton("install",BUTTON_TYPE_INSTALL_INFO, install_level), 
        		makeButton("installed", BUTTON_TYPE_PACAKGE_INFO,package_level))) {            
            b.setIcon(new ToggleButtonBarCellIcon());
            //bg.add(b);
            p.add(b);
        }
        return p;
    }
    
    public JPanel makeToggleButtonBar(int ccl) {
        JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));        
        //Color color = new Color(ccl);
        for (AbstractButton b: Arrays.asList(makeButton("install",BUTTON_TYPE_INSTALL_INFO, ccl), 
        		makeButton("installed", BUTTON_TYPE_PACAKGE_INFO,ccl))) {            
            b.setIcon(new ToggleButtonBarCellIcon());
            //bg.add(b);
            p.add(b);
        }
        return p;
    }
    
    static class ToggleButtonBarCellIcon implements Icon {
        private static final Color TL = new Color(1f, 1f, 1f, .2f);
        private static final Color BR = new Color(0f, 0f, 0f, .2f);
        private static final Color ST = new Color(1f, 1f, 1f, .4f);
        private static final Color SB = new Color(1f, 1f, 1f, .1f);

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Container parent = c.getParent();
            if (parent == null) {
                return;
            }
            int r = 8;
            int w = c.getWidth();
            int h = c.getHeight() - 1;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Path2D p = new Path2D.Double();
            if (c == parent.getComponent(0)) {
                //:first-child
                p.moveTo(x, y + r);
                p.quadTo(x, y, x + r, y);
                p.lineTo(x + w, y);
                p.lineTo(x + w, y + h);
                p.lineTo(x + r, y + h);
                p.quadTo(x, y + h, x, y + h - r);
            } else if (c == parent.getComponent(parent.getComponentCount() - 1)) {
                //:last-child
                w--;
                p.moveTo(x, y);
                p.lineTo(x + w - r, y);
                p.quadTo(x + w, y, x + w, y + r);
                p.lineTo(x + w, y + h - r);
                p.quadTo(x + w, y + h, x + w - r, y + h);
                p.lineTo(x, y + h);
            } else {
                p.moveTo(x, y);
                p.lineTo(x + w, y);
                p.lineTo(x + w, y + h);
                p.lineTo(x, y + h);
            }
            p.closePath();

            Color ssc = TL;
            Color bgc = BR;
            if (c instanceof AbstractButton) {
                ButtonModel m = ((AbstractButton) c).getModel();
                if (m.isSelected() || m.isRollover()) {
                    ssc = ST;
                    bgc = SB;
                }
            }

            Area area = new Area(p);
            g2.setPaint(c.getBackground());
            g2.fill(area);
            g2.setPaint(new GradientPaint(x, y, ssc, x, y + h, bgc, true));
            g2.fill(area);
            g2.setPaint(BR);
            g2.draw(area);
            g2.dispose();
        }
        @Override public int getIconWidth() {
            return 80;
        }
        @Override public int getIconHeight() {
            return 20;
        }
    }
    
    
    //public static final int OPTION_INSTALL = 0;
    //public static final int OPTION_NO_INSTALL = 1;
    //public static final int OPTION_PUSH = 2;
    //public static final int OPTION_IMPOSSIBLE_INSTALL = 3;
    
    //public static final int SHOW_LOADING_INSTALL = 2;
    //public static final int SHOW_COMPLETE_INSTALL = 3;
    
    @Override
    synchronized protected void paintComponent ( Graphics g ) {    	
    	btnoption.setBackground(ColorSet[data.selectedinstalloption]);
    	btnoption.setText(installOptiontextSet[data.selectedinstalloption]);
    	switch(status) {
		case ApkInstallWizard.STATUS_SET_OPTIONS:
	    	btninstalled.setBackground(ColorSet[data.isinstalled]);
	    	btninstalled.setText(installtextSet[data.isinstalled]);
	    	break;
		case ApkInstallWizard.STATUS_INSTALLING:
			if(!data.isNoinstall()) {
				if(data.showstate == DeviceListData.SHOW_COMPLETE_INSTALL) {					
					if(data.installErrorCuase == null) {
						btninstalled.setBackground(ColorSet[0]);
						btninstalled.setText(installingtextSet[1]);
					} else {
						btninstalled.setBackground(ColorSet[3]);
						btninstalled.setText(installingtextSet[2]);
					}
				} else if(data.showstate == DeviceListData.SHOW_LOADING_INSTALL){
					btninstalled.setBackground(ColorSet[4]);
					btninstalled.setText(installingtextSet[0]);
				}
			} else {
				btninstalled.setBackground(ColorSet[data.isinstalled]);
		    	btninstalled.setText(installtextSet[data.isinstalled]);
			}
			break;
		case ApkInstallWizard.STATUS_COMPLETED:
			if(!data.isNoinstall()) {
				if(data.installErrorCuase == null) {
					btninstalled.setBackground(ColorSet[0]);
					btninstalled.setText(installingtextSet[1]);
				} else {
					btninstalled.setBackground(ColorSet[3]);
					btninstalled.setText(installingtextSet[2]);
				}
			} else {
				btninstalled.setBackground(ColorSet[data.isinstalled]);
		    	btninstalled.setText(installtextSet[data.isinstalled]);
			}
			break;
		}
    	//Log.d("" +data.isinstalled);
    }
    
	public void setData(DeviceListData value) {
		// TODO Auto-generated method stub
		this.data = value;
	}
	public void setStatus(int status) {
		this.status = status;
		
	}
}
