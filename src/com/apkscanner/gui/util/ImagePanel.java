package com.apkscanner.gui.util;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	private static final long serialVersionUID = 2003631789889579741L;

	private Image img;
	public void SetImage(Image imgtemp) {
		this.img = imgtemp;
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
	}
}
