package com.apkscanner.gui.install;

import java.awt.BorderLayout;
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
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import com.apkscanner.gui.install.DeviceCustomList.DeviceListData;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;


public class ToggleButtonBar extends JPanel{

	public static final String BUTTON_TYPE_PACAKGE_INFO = "pacakge_info";
	public static final String BUTTON_TYPE_INSTALL_INFO = "install_info";
	
	DeviceListData data;
		
	private static final Color[] ColorSet = { new Color(0x006400), new Color(0xFF7400), new Color(0x8b0000), new Color(0x555555)};
	
	private static String[] installtextSet = {"Installed", "Not installed", "", "waiting"};
	private final String[] installOptiontextSet = {"Install", "Push", "Impossible", "waiting"};
	
	
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
        ButtonGroup bg = new ButtonGroup();
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
        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));        
        Color color = new Color(ccl);
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
            if (Objects.isNull(parent)) {
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
    
    @Override
    protected void paintComponent ( Graphics g ) {
    	
    	
    	btninstalled.setBackground(ColorSet[data.isinstalled]);
    	btnoption.setBackground(ColorSet[data.possibleOption]);
    	
    	btninstalled.setText(installtextSet[data.isinstalled]);
    	
    	//Log.d("" +data.isinstalled);
    	
    	
    }
    
	public void setData(DeviceListData value) {
		// TODO Auto-generated method stub
		this.data = value;
	}
}
