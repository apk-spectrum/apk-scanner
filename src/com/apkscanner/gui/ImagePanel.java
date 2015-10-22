package com.apkscanner.gui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel{
	private Image img;
	public void SetImage(Image imgtemp) {
		this.img = imgtemp;
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
		
		
	}
}
