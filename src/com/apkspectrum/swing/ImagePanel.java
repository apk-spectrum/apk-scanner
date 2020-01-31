package com.apkspectrum.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	private static final long serialVersionUID = 2003631789889579741L;

	private Image img;
	
	public ImagePanel() { }
	
	public ImagePanel(Image imgtemp) {
		setImage(imgtemp);
	}
	
	public ImagePanel(ImageIcon imgtemp) {
		setImage(imgtemp.getImage());
	}
	
	public void setImage(Image imgtemp) {
		img = imgtemp;
		if(img != null) {
			Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
			setMinimumSize(size);
			setMaximumSize(size);
			setPreferredSize(size);
		}
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
	}
}
