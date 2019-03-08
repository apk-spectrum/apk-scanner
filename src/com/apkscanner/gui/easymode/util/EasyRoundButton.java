package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class EasyRoundButton extends JButton {
	private static final long serialVersionUID = -6927025737749969747L;
	boolean entered = false;
	private final Color btnhovercolor = new Color(100, 100, 100);
	private Color originalcolor;
	private int len = 0;
	
	public EasyRoundButton(ImageIcon icon) {
		// TODO Auto-generated constructor stub
		super(icon);
		// setlistener();
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setContentAreaFilled(false);
		setFocusable(false);
		setlistener();
	}
	
	void setlistener() {
		addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				// this.setBackground(new Color(255,255,255));
				// setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				entered = true;
				repaint();
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				// setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				entered = false;
				repaint();
			}
		});
	}
	
	public void setBackground(Color color) {
		super.setBackground(color);
		originalcolor = color;
	}
	
	public void setshadowlen(int setlen) {
		this.len = setlen;
		//setBorder(BorderFactory.createEmptyBorder(1, len/2, len, len/2));
		repaint();
	}
	
    @Override
	protected void paintComponent(Graphics gr)
    {   
        Graphics2D g = (Graphics2D)gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        //Rectangle2D r = new Rectangle2D.Double(len, len, getWidth() - len *2, getHeight() - len *2);
        //draw(g, r, len);

        //Shape rr = new Rectangle2D.Double(len / 2, 0, getWidth()-len, getHeight()-len);
        //g.setColor(new Color(49,56,71,200));
        if(entered) {
        	g.setColor(originalcolor.darker().darker());
        } else {
        	g.setColor(originalcolor);
        }
        //g.fill(rr);
        //g.fillRoundRect(0, 0, getWidth(), getHeight(), 13,13);
        
        g.fillRoundRect(0, 0, getWidth()-len, getHeight()-len, 13,13);
        
        super.paintComponent(gr);
    }
	
}
