package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class EasyButton extends JButton {
	private static final long serialVersionUID = -6927025737749969747L;
	boolean entered = false;
	private final Color btnhovercolor = new Color(140, 140, 140);

	public EasyButton(ImageIcon icon) {
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
	
	public void paint(Graphics g) {
		super.paint(g);
		if (entered) {
			g.setColor(btnhovercolor);
			g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		}
	}
}
